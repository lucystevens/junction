package uk.co.lucystevens.junction.utils

import org.shredzone.acme4j.util.KeyPairUtils
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.security.KeyPair

fun writeToString(fn: (Writer) -> Unit): String =
    StringWriter().use { fn(it) }.toString()

fun <T> String.read(fn: (Reader) -> T): T =
    reader().use { fn(it) }

fun String.readKeyPair() =
    read { KeyPairUtils.readKeyPair(it) }

fun KeyPair.writeToString() =
    writeToString { KeyPairUtils.writeKeyPair(this, it) }