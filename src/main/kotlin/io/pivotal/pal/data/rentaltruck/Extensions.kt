package io.pivotal.pal.data.rentaltruck

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
