package de.soho5k.utils.extensions

import de.soho5k.knockout.Knockout
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

fun FileConfiguration.getOrSet(path: String, defaultValue: Any): Any? {
    return if (isSet(path)) {
        get(path)
    } else {
        set(path, defaultValue)
        Knockout.INSTANCE.saveConfig()
        defaultValue
    }
}

infix fun YamlConfiguration.safe(file: File) {
    save(file)
}

fun YamlConfiguration.setOrDefault(path: String, defaultValue: Any, file: File): Any {
    if (isSet(path)) return get(path)!!
    set(path, defaultValue)
    this safe file
    return defaultValue
}