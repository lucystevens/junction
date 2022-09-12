package uk.co.lucystevens.junction.utils

import org.shredzone.acme4j.Status
import org.shredzone.acme4j.exception.AcmeRetryAfterException
import java.time.Clock

class PollingHandler(
    private val retryIntervalMs: Long = 3000,
    private val timeoutMs: Long = 60000,
    private val waitTimeMs: Long = 500,
    private val clock: Clock,
    ) {

    fun waitForComplete(getStatus: () -> Status, update: () -> Unit): Long{
        return waitFor(listOf(Status.VALID, Status.INVALID), getStatus, update)
    }

    fun waitForReady(getStatus: () -> Status, update: () -> Unit): Long{
        return waitFor(listOf(Status.READY), getStatus, update)
    }

    fun waitFor(expectedStatuses: List<Status>, getStatus: () -> Status, update: () -> Unit): Long{
        val startTime = clock.millis()
        var retryAfter = startTime + retryIntervalMs
        val timeoutTime = startTime + timeoutMs
        while (!expectedStatuses.contains(getStatus()) && clock.millis() < timeoutTime) {
            Thread.sleep(waitTimeMs)

            if(clock.millis() > retryAfter)
                retryAfter = doUpdate(update)
        }

        // TODO throw exception if not valid
        return clock.millis() - startTime
    }

    private fun doUpdate(update: () -> Unit): Long =
        try {
            update()
            clock.millis() + retryIntervalMs
        } catch(e: AcmeRetryAfterException) {
            e.retryAfter.toEpochMilli()
        }
}