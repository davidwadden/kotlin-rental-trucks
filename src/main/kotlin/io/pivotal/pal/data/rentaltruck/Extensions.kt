package io.pivotal.pal.data.rentaltruck

import org.slf4j.LoggerFactory
import java.util.*
import kotlin.streams.asSequence

internal fun generateRandomString(outputLength: Long): String {
    val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return Random()
        .ints(outputLength, 0, source.length)
        .asSequence()
        .map(source::get)
        .joinToString("")
}

inline fun <reified T : Any> loggerFor(clazz: Class<T>) = LoggerFactory.getLogger(clazz)!!
