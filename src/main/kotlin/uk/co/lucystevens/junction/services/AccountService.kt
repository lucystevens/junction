package uk.co.lucystevens.junction.services

import org.shredzone.acme4j.util.KeyPairUtils
import uk.co.lucystevens.junction.config.Config
import uk.co.lucystevens.junction.db.dao.ConfigDao
import uk.co.lucystevens.junction.utils.readKeyPair
import uk.co.lucystevens.junction.utils.writeToString
import java.security.KeyPair

class AccountService(
    private val config: Config,
    private val configDao: ConfigDao
) {


    fun getAccountEmail(): String {
        return configDao.getValue("account.email") ?: run {
            config.getEmailAddress().also {
                setAccountEmail(it)
            }
        }
    }

    fun setAccountEmail(email: String) =
        configDao.setValue("account.email", email)

    fun getAccountLocator() =
        configDao.getValue("account.locator")

    fun setAccountLocator(locator: String) =
        configDao.setValue("account.locator", locator)

    fun getAccountKeyPair(): KeyPair =
        configDao.getValue("account.keypair")?.readKeyPair() ?: run {
            KeyPairUtils.createKeyPair(config.getRSAKeySize()).also {
                setAccountKeyPair(it)
            }
        }

    fun setAccountKeyPair(keyPair: KeyPair) =
        configDao.setValue("account.keypair", keyPair.writeToString())
}