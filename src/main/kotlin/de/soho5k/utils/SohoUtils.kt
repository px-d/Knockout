package de.soho5k.utils

import com.mojang.util.UUIDTypeAdapter
import org.bukkit.entity.Player
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

const val NAME_LINK = "https://api.mojang.com/users/profiles/minecraft"

object SohoUtils {


    /**
     * Get a UUID from a specific Player Name
     * (keine Ahnung ob das effizient ist oder nicht)
     */
    private fun getUUIDbyName(name: String, callback: (UUID?) -> Unit) {
        val uuid = BufferedReader(InputStreamReader(URL("$NAME_LINK/$name").openStream())).readLine().replace("\"", "")
            .replace("{", "").replace("}", "").split(",")[1].split(":")[1]
        callback.invoke(UUIDTypeAdapter.fromString(uuid))
    }
}