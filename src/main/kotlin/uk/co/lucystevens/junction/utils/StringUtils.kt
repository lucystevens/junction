package uk.co.lucystevens.junction.utils

import org.shredzone.acme4j.util.KeyPairUtils
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.security.KeyPair
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

fun writeToString(fn: (Writer) -> Unit): String =
    StringWriter().use {
        fn(it)
        it.toString()
    }

fun streamToString(fn: (OutputStream) -> Unit): String =
    ByteArrayOutputStream().use {
        fn(it)
        String(it.toByteArray())
    }

fun <T> String.read(fn: (Reader) -> T): T =
    reader().use { fn(it) }

fun String.readKeyPair() =
    read { KeyPairUtils.readKeyPair(it) }

fun KeyPair.writeToString() =
    writeToString { KeyPairUtils.writeKeyPair(this, it) }

fun String.ifExists(fn: (String) -> String) =
    if(isNotEmpty()) fn(this) else this

fun String.readCert(): List<X509Certificate> {
    val factory = CertificateFactory.getInstance("X509")
    return byteInputStream().use { stream ->
        factory.generateCertificates(stream)
            .map { it as X509Certificate }
    }
}