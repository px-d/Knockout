package de.soho5k.game.games

import de.soho5k.game.GameManager
import de.soho5k.game.GameSchematic
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.SGLoot
import de.soho5k.utils.extensions.*
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.onlinePlayers
import net.axay.kspigot.runnables.task
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import util.ItemBuilder
import javax.swing.text.rtf.RTFEditorKit
import kotlin.math.floor
import kotlin.math.round
import kotlin.random.Random


class SurvivalGames :
    GameSchematic(
        "Survival Games",
        "sg",
        Location(Bukkit.getWorld("sg"), -94.50, 110.00, 39.50),
        600,
        20,
        broadcast = 30
    ) {

    private val spawns = ConfigManager.getSpawns("sg")
    private val pointDistributor = PointDistributor()
    private val randomTP = ItemBuilder(Material.COMPASS).name("&fRandom TP".tc()).build()

    var isGrace = true

    private val center = Location(
        spawn.world,
        -2.5,
        100.0,
        1.5
    )


    override fun preGrace() {
        addEvent(listen<PlayerMoveEvent> {
            if (!running) {
                val from: Location = it.from
                val to: Location = it.to ?: return@listen
                val x = floor(from.x)
                val z = floor(from.z)
                if (floor(to.x) != x || floor(to.z) != z) {
                    //player just moved a block
                    it.player.teleport(from)
                }
            }
        })
        val players = Bukkit.getOnlinePlayers().toList()
        if (players.size <= spawns.size) {
            for (i in players) {
                val loc = spawns[players.indexOf(i)]
                i.teleport(loc.clone().add(.5, .0, .5))
            }
        }
    }

    override fun tick() {
        if (time % 60 == 0 && time < 480) {
            val border = spawn.world!!.worldBorder
            if (border.size >= 999.0) {
                border.setSize(400.0, 40L)
                onlinePlayers.forEach { it.sendLocalizedMessage("MESSAGES.sg.shrink", replacements = arrayOf("400")) }
            } else {
                border.setSize(border.size - 50.0, 50L)
                onlinePlayers.forEach {
                    it.sendLocalizedMessage(
                        "MESSAGES.sg.shrink",
                        replacements = arrayOf("${border.size - 50}")
                    )
                }
            }

        }
    }

    override fun onStart() {
        addEvent(listen<PlayerInteractEvent> {
            if (it.action == Action.RIGHT_CLICK_BLOCK) {
                if (it.clickedBlock != null) {
                    if (it.clickedBlock!!.type == Material.CHEST) {
                        SGLoot.setLoot(it.clickedBlock!!)
                    }
                }
            }
        })
        addEvent(listen<EntityDamageByEntityEvent> {
            if (isGrace) it.isCancelled = true
            if (it.damager is Player) {
                if ((it.damager as Player).mode != PlayerMode.PLAY) it.isCancelled = true
            }
        })
        addEvent(listen<EntityDamageEvent> {
            if (isGrace) it.isCancelled = true
            if (it.entity is Player && it.cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
                it.isCancelled = true
                it.entity.teleport(spawn)
            }
        })
        addEvent(listen<PlayerInteractEvent> {
            if (!GameManager.activeGame.running) {
                if (it.clickedBlock?.type == Material.CHEST) it.isCancelled = true
                return@listen
            }

            if (it.player.mode == PlayerMode.SPECTATE) {
                it.isCancelled = true
                if (it.item?.type == Material.COMPASS) {
                    it.player.teleport(onlinePlayers.filter { p -> p.mode == PlayerMode.PLAY }.random().location)
                }
                return@listen
            }

            if (it.player.mode == PlayerMode.PLAY && it.item?.type == Material.COMPASS && it.hand == EquipmentSlot.HAND) {
                val clicker = it.player
                val players = onlinePlayers.filter { p -> p != clicker && p.mode == PlayerMode.PLAY }
                var nearest = players.first()

                for (i in players) {
                    if (i.location.distance(clicker.location) < nearest.location.distance(clicker.location)) {
                        nearest = i
                    }
                }

                clicker.compassTarget = nearest.location
                clicker.sendLocalizedMessage(
                    "MESSAGES.tracker.nearby",
                    false,
                    ChatColor.AQUA,
                    ChatColor.YELLOW,
                    replacements = arrayOf(
                        nearest.name,
                        "${round(clicker.location.distance(nearest.location) * 10) / 10}"
                    )
                )

            }

        })
        addEvent(listen<BlockBreakEvent> {
            if (it.player.gameMode != GameMode.CREATIVE) {
                if (it.block.type != Material.COBWEB && it.block.type != Material.FIRE) {
                    it.isCancelled = true
                }
            }
        })
        addEvent(listen<BlockPlaceEvent> {
            if (it.block.type == Material.TNT) {
                it.blockPlaced.type = Material.AIR
                val a = it.blockPlaced.world.spawnEntity(it.blockPlaced.location, EntityType.PRIMED_TNT) as TNTPrimed
                a.source = it.player
                a.fuseTicks = 40
            }
        })
        addEvent(listen<EntityPickupItemEvent> {
            if (it.entity is Player) {
                if ((it.entity as Player).mode != PlayerMode.PLAY) it.isCancelled = true
            }
        })
        addEvent(listen<PlayerDropItemEvent> {
            if (it.player.mode == PlayerMode.SPECTATE) it.isCancelled = true
        })
        addEvent(listen<EntityExplodeEvent> {
            if (it.entity is TNTPrimed) {
                val tnt = it.entity as TNTPrimed
                val source = tnt.source
                it.isCancelled = true

                it.entity.world.spawnParticle(Particle.EXPLOSION_LARGE, it.entity.location, 1)
                for (ent in it.entity.getNearbyEntities(4.0, 4.0, 4.0)) {
                    if (ent is LivingEntity) {
                        ent.damage(4.0, source)
                    }
                }
            }
        })

        addEvent(listen<PlayerDeathEvent> {
            for (i in it.entity.inventory.contents) {
                if (i != null) {
                    it.entity.world.dropItemNaturally(it.entity.location, i)
                }
            }
            it.entity.mode = PlayerMode.SPECTATE
            task(delay = 5L) { _ ->
                it.entity.spigot().respawn()
                task(delay = 5L) { _ ->
                    it.entity.inventory.addItem(randomTP)
                }
                PlayerManager.setToSpectator(it.entity)
                PlayerManager.checkForAlive()
            }
            it.deathMessage = ""
            pointDistributor.addPlayer(it.entity, it.entity.killer, 1)
        })
        val world = spawn.world ?: throw Exception("World Not Existing")
        world.worldBorder.center = center

        onlinePlayers.forEach {
            it.sendLocalizedMessage(
                "MESSAGES.sg.grace",
                mainColor = ChatColor.RED,
                accentColor = ChatColor.YELLOW,
                replacements = arrayOf(30.toTime())
            )
        }

        task(delay = 20 * 30, howOften = 1L) { _ ->
            isGrace = false
            onlinePlayers.forEach { it.sendLocalizedMessage("MESSAGES.sg.graceended", false, ChatColor.RED) }
        }
    }

    override fun onStop() {
        pointDistributor.giveRewards(RewardMode.FIRST, listOf(6, 4, 2))
    }
}
