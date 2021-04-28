package de.soho5k.utils.extensions

import org.bukkit.Location

fun Location.formatted(): String {
    return "World: ${world?.name}, x: $x, y: $y, z: $z"
}