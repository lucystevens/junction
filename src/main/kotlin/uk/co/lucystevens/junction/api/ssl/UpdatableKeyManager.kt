package uk.co.lucystevens.junction.api.ssl

import java.net.Socket
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedKeyManager

// Updatable wrapper around a key manager to allow 'hot-reloading' of SSL certs
class UpdatableKeyManager(var keyManager: X509ExtendedKeyManager) : X509ExtendedKeyManager() {
    override fun getClientAliases(p0: String?, p1: Array<out Principal>?): Array<String> = keyManager.getClientAliases(p0,p1)

    override fun chooseClientAlias(p0: Array<out String>?, p1: Array<out Principal>?, p2: Socket?): String = keyManager.chooseClientAlias(p0,p1,p2)

    override fun getServerAliases(p0: String?, p1: Array<out Principal>?): Array<String> = keyManager.getServerAliases(p0,p1)

    override fun chooseServerAlias(p0: String?, p1: Array<out Principal>?, p2: Socket?): String = keyManager.chooseServerAlias(p0,p1,p2)

    override fun getCertificateChain(p0: String?): Array<X509Certificate> = keyManager.getCertificateChain(p0)

    override fun getPrivateKey(p0: String?): PrivateKey = keyManager.getPrivateKey(p0)

    override fun chooseEngineClientAlias(
        keyType: Array<out String>?,
        issuers: Array<out Principal>?,
        engine: SSLEngine?
    ): String? = keyManager.chooseEngineClientAlias(keyType, issuers, engine)

    override fun chooseEngineServerAlias(keyType: String?, issuers: Array<out Principal>?, engine: SSLEngine?): String? =
        keyManager.chooseEngineServerAlias(keyType, issuers, engine)

}