package de.soho5k.utils.extensions

fun Int.toTime(): String {
    val seconds = this % 60
    val minutes = (this * 1000 / (1000 * 60)) % 60
    val min = if (minutes > 9) "$minutes" else "0$minutes"
    val sec = if (seconds > 9) "$seconds" else "0$seconds"
    return "$min:$sec"
}

fun Int.shouldBroadcast(modulo: Int = 20): Boolean {
    return this <= 5 || this % modulo == 0
}