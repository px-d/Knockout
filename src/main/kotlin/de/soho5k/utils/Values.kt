package de.soho5k.utils

import de.soho5k.utils.extensions.tc
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

const val PREFIX = "§bKnock§fOut"
const val QUOTATION = "\u00BB"

fun getPatchBook(lang: String): ItemStack {
    val book = ItemStack(Material.WRITTEN_BOOK)
    val meta = book.itemMeta as BookMeta
    meta.title = "&a&lPatchnotes v1.0.0".tc()
    if (lang.contains("de", true)) {
        meta.addPage("&a&lKnockout Patch-Notes:\n\n&r- Content Creator können jetzt rejoinen, wenn sie während des Spiels die Verbindung getrennt haben.\n\n- InGame Patchnotes hinzugefügt :)".tc())
        meta.addPage("- Beschreibungen zu &a/game&r hinzugefügt")
        meta.addPage("&cSurvival Games:\n\n&r&a+&r Chance für Stöcke um 10% erhöht.\n\n&a+&r Das Feuerzeug wurde durch Feuerkugeln ersetzt.\n\n&c!&r TNT Bug wurde behoben".tc())
        meta.addPage("\nSolltest du Bugs finden bitte &lumgehend&r und umfangreich auf Discord berichten!".tc())
    } else {
        meta.addPage("&a&lKnockout Patch-Notes:\n\n&r- Content Creators can now rejoin if they disconnected during the match.\n\n- Added InGame Patchnotes :)".tc())
        meta.addPage("- Added descriptions to &a/game&r")
        meta.addPage("&cSurvival Games:\n\n&a+&r Increased Chance for Sticks by 10%\n\n&a+&r Flint and Steel got replaced by Fire Charges.\n\n&c!&r Fixed TNT Bug".tc())
        meta.addPage("\nIf you should come across a &lbug&r please report it instantly and well documented in our Discord!".tc())
    }
    meta.author = "HGLabor"
    book.itemMeta = meta
    return book
}