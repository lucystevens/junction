package uk.co.lucystevens.junction.utils

import java.sql.ResultSet

fun <T> ResultSet.map(mapFn: (ResultSet) -> T): List<T> =
    mutableListOf<T>().also {
        while(next()){
            it.add(mapFn(this))
        }
    }