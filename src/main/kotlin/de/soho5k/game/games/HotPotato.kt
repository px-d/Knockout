package de.soho5k.game.games

import de.soho5k.game.events.CommonEvents
import de.soho5k.game.GameManager
import de.soho5k.game.GameSchematic
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.*
import de.soho5k.utils.Localization.ll
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.extensions.tc
import de.soho5k.utils.extensions.timeBombSound
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.actionBar
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.ceil
import kotlin.random.Random

class HotPotato :
    GameSchematic("Hot Potato", "hotpotato", Location(Bukkit.getWorld("hotpotato"), 0.5, 70.0, 0.5), 600, 10) {

    val players = hashMapOf<Player, Boolean>()

    val points = PointDistributor()

    var Player.isPotato: Boolean
        get() = players[this] ?: false
        set(value) {
            players[this] = value
            if (value) {
                walkSpeed = 0.23F
                hpEquip()
            } else {
                clearInv()
                walkSpeed = 0.2F
            }
        }

    override fun onStart() {
        //All Events
        addEvent(CommonEvents().allEvents())
        addEvent(listen<EntityDamageByEntityEvent> {
            if (it.entity is Player && it.damager is Player) {
                val victim = it.entity as Player
                val damager = it.damager as Player

                if (damager.isPotato) {
                    damager.isPotato = false

                    victim.isPotato = true
                }
            }
        })

        addEvent(listen<InventoryClickEvent> {
            it.isCancelled = it.whoClicked.gameMode != GameMode.CREATIVE
        })


        val allPlayers = Bukkit.getOnlinePlayers().filter { it.mode == PlayerMode.PLAY }
        allPlayers.forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false, false))
        }
        val potatoAmount = ceil(allPlayers.size / 7.0).toInt()
        for (i in 0 until potatoAmount) {
            allPlayers.filter { !it.isPotato }.random().isPotato = true
        }

        activateBoom()
    }

    private fun activateBoom() {
        var crash = Random.nextInt(15) + 15
        task(period = 20) {
            if (GameManager.inLobby) it.cancel()
            if (crash == 0) {
                crash = Random.nextInt(15) + 15
                explode()
            } else {
                crash--
                if (crash < 10) {
                    players.keys.filter { player -> player.isPotato }.forEach { player ->
                        player.timeBombSound()
                        player.actionBar(player.ll("TEXT.explodesoon").tc())
                    }
                }
            }
        }
    }

    private fun explode() {
        for (i in Bukkit.getOnlinePlayers().filter { it.isPotato }) {
            points.addPlayer(i)
            PlayerManager.setToSpectator(i)
            i.isFlying = true
        }
        val allPlayers = Bukkit.getOnlinePlayers().filter { it.mode == PlayerMode.PLAY }
        if (allPlayers.size == 1) {
            stop()
            points.giveRewards(RewardMode.FIRST, listOf(10, 9, 8, 7, 6, 5))
        } else {
            val potatoAmount = ceil(allPlayers.size / 7.0).toInt()
            for (i in 0 until potatoAmount) {
                allPlayers.filter { !it.isPotato }.random().isPotato = true
            }
        }
    }
}