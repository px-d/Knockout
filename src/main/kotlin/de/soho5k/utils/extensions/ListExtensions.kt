package de.soho5k.utils.extensions

fun List<Any>.second(): Any? {
    if (size >= 2) {
        return this[1]
    }
    return null
}

fun List<Any>.third(): Any? {
    if (size >= 3) {
        return this[2]
    }
    return null
}
