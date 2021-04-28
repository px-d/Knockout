package de.soho5k.utils

import org.bukkit.entity.Player
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*

object Localization {
    private val bundles = hashMapOf<Locale, ResourceBundle>()
    private fun getOrLoadBundle(locale: Locale): ResourceBundle {
        return bundles[locale] ?: InputStreamReader(
            javaClass
                .classLoader
                .getResourceAsStream("values_$locale.properties") ?: throw Exception("No Bundle"),
            StandardCharsets.UTF_8
        ).use {
            val bundle = PropertyResourceBundle(it)
            bundles[locale] = bundle
            bundle
        }
    }

    fun Player.ll(key: String): String {
        return if (locale.contains("de", true)) {
            getOrLoadBundle(Locale("de_de")).getString(key) ?: throw Exception("No Such Key / Resource Bundle")
        } else {
            getOrLoadBundle(Locale("en_us")).getString(key) ?: throw Exception("No Such Key / Resource Bundle")
        }
    }
}