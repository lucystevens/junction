package uk.co.lucystevens.junction.utils

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import kotlin.reflect.KProperty

data class CacheExpiry(
    val cacheLength: Long,
    val cacheUnit: TemporalUnit
)

class CacheDelegate<T>(
    private val expiry: CacheExpiry,
    private val clock: Clock,
    private val fetchFn: () -> T
) {

    var cachedValue: Pair<T?, Instant> = null to Instant.now(clock)

    private fun fetch(): Pair<T, Instant> =
        fetchFn() to nextExpiry()

    private fun nextExpiry(): Instant =
        Instant.now(clock)
            .plus(expiry.cacheLength, expiry.cacheUnit)

    private fun isExpired(): Boolean =
        cachedValue.second < Instant.now(clock)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if(isExpired() || cachedValue.first == null){
            cachedValue = fetch()
        }
        return cachedValue.first!!
    }

}


fun <T> cached(
    expiry: CacheExpiry,
    fetchFn: () -> T
) = CacheDelegate(expiry, Clock.systemUTC(), fetchFn)