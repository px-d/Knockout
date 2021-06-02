package de.soho5k.game.games

import de.soho5k.game.GameManager
import de.soho5k.game.GameSchematic
import de.soho5k.game.events.CommonEvents
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.Localization.getLocalized
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.clearInv
import de.soho5k.utils.extensions.mode
import de.soho5k.utils.extensions.tc
import de.soho5k.utils.extensions.timeBombSound
import de.soho5k.utils.hpEquip
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.actionBar
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.runnables.task
import net.axay.kspigot.utils.addEffect
import net.axay.kspigot.utils.fireworkItemStack
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.meta.FireworkMeta
import kotlin.math.ceil
import kotlin.random.Random

class HotPotato :
    GameSchematic("Hot Potato", "hotpotato", Location(Bukkit.getWorld("hotpotato"), 0.5, 70.0, 0.5), 240, 10) {

    val players = hashMapOf<Player, Boolean>()

    val points = PointDistributor()

    var Player.isPotato: Boolean
        get() = players[this] ?: false
        set(value) {
            players[this] = value
            if (value) {
                walkSpeed = 0.25F
                hpEquip()
                for (i in 0..8) {
                    inventory.setItem(i, itemStack(Material.POTATO) {})
                }
            } else {
                clearInv()
                walkSpeed = 0.2F
            }
        }

    init {
        spawn.world?.worldBorder?.size = 100.0
    }

    private fun potatoAmount(): Int {
        return ceil(onlinePlayers.filter { it.mode == PlayerMode.PLAY }.size * 1.0 / 4.0).toInt()
    }

    override fun onStart() {

        //All Events
        addEvent(CommonEvents().allEvents())
        addEvent(listen<EntityDamageEvent> {
            if (it.cause == EntityDamageEvent.DamageCause.FALL) it.isCancelled = true
            if ((it.entity as? Player)?.mode != PlayerMode.PLAY) {
                it.isCancelled = true
            }
            if (it.cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
                it.isCancelled = true
            }
        })
        addEvent(listen<EntityDamageByEntityEvent> {
            if (it.entity is Player && it.damager !is Player) it.isCancelled = true
            if (it.entity is Player && it.damager is Player) {
                if ((it.entity as Player).mode == PlayerMode.SPECTATE || (it.damager as Player).mode == PlayerMode.SPECTATE) {
                    it.isCancelled = true
                    return@listen
                }
                it.damage = 0.0
                val victim = it.entity as Player
                val damager = it.damager as Player

                if (damager.isPotato && !(it.entity as Player).isPotato) {
                    damager.isPotato = false
                    victim.isPotato = true
                }
            }
        })
        addEvent(listen<PlayerSwapHandItemsEvent> {
            it.isCancelled = true
        })

        addEvent(listen<InventoryClickEvent> {
            it.isCancelled = it.whoClicked.gameMode != GameMode.CREATIVE
        })

        val allPlayers = Bukkit.getOnlinePlayers().filter { it.mode == PlayerMode.PLAY }
        for (i in 0 until potatoAmount()) {
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
                        player.actionBar("&c${player.getLocalized("TEXT.explodesoon")}".tc())
                        val firework = fireworkItemStack {
                            addEffect {
                                repeat((1..3).random()) {
                                    withColor(Color.fromRGB(255, 50, 50))
                                }
                                repeat((1..3).random()) {
                                    withColor(Color.fromRGB(213, 19, 177))
                                }
                                with(FireworkEffect.Type.BALL)
                            }
                            power = 1

                        }
                        val fr = player.world.spawn(player.location.clone().add(0.0, 0.3, 0.0), Firework::class.java)
                        fr.fireworkMeta = firework.itemMeta as FireworkMeta
                        fr.detonate()

                    }
                } else {
                    players.keys.filter { p -> p.isPotato }.forEach { player ->
                        val firework = fireworkItemStack {
                            addEffect {
                                repeat(1) {
                                    withColor(Color.fromRGB(104, 255, 83))
                                }
                                with(FireworkEffect.Type.BALL)
                            }
                            power = 1

                        }
                        val fr = player.world.spawn(player.location.clone().add(0.0, 0.3, 0.0), Firework::class.java)
                        fr.fireworkMeta = firework.itemMeta as FireworkMeta
                        fr.detonate()
                    }
                }
            }
        }
    }

    private fun explode() {
        for (i in onlinePlayers.filter { it.isPotato }) {
            points.addPlayer(i)
            PlayerManager.setToSpectator(i)
            i.world.spawnParticle(Particle.EXPLOSION_HUGE, i.location, 10)
            i.isPotato = false
            i.isFlying = true
        }
        val allPlayers = Bukkit.getOnlinePlayers().filter { it.mode == PlayerMode.PLAY && !it.isPotato }
        if (allPlayers.size == 1) {
            stop()
            points.giveRewards(RewardMode.FIRST, listOf(10, 9, 8, 7, 6, 5))
        } else {
            for (i in 0 until potatoAmount()) {
                allPlayers.filter { !it.isPotato }.random().isPotato = true
            }
        }
    }
}