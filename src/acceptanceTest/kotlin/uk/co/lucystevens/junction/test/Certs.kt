package uk.co.lucystevens.junction.test

import okhttp3.OkHttpClient
import uk.co.lucystevens.junction.utils.readCert
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.readText

object Certs {

    val pebble by lazy {
        val client = OkHttpClient()
        client.doRequest("https://localhost:15000/roots/0"){
            it.get()
        }.use {
            it.bodyAsString()!!.readCert().first()
        }
    }

    // local root certificate stored at pebble.pem
    // this is for a previous pebble instance that generated
    // the certificate in createDomain.sql
    val otherRoot by lazy {
        Paths.get("src/acceptanceTest/resources/pebble.pem")
            .readText()
            .readCert()
            .first()
    }
}