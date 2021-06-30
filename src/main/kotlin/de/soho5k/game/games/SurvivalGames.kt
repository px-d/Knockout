package de.soho5k.game.games

import de.soho5k.game.GameManager
import de.soho5k.game.GameSchematic
import de.soho5k.knockout.managers.ConfigManager
import de.soho5k.player.PlayerManager
import de.soho5k.player.PlayerMode
import de.soho5k.utils.PointDistributor
import de.soho5k.utils.RewardMode
import de.soho5k.utils.SGLoot
import de.soho5k.utils.clearInv
import de.soho5k.utils.extensions.*
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import util.ItemBuilder
import kotlin.math.floor
import kotlin.math.round


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
    private val totalDuration = time
    var isGrace = true

    private val center = setConfig(
        "location.center", Location(
            spawn.world,
            -2.5,
            100.0,
            1.5
        )
    ) as Location

    override fun preGrace() {
        addEvent(listen<PlayerMoveEvent> {
            if (!isRunning) {
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
        if (time == totalDuration - 60) {
            val border = spawn.world!!.worldBorder
            border.setSize(50.0, (totalDuration - 120).toLong())
            onlinePlayers.forEach {
                it.sendLocalizedMessage(
                    "MESSAGES.sg.shrink",
                    replacements = arrayOf("50", (totalDuration - 60).toTime())
                )
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
            if (it.damager is TNTPrimed) {
                it.isCancelled = true
            }
        })
        addEvent(listen<EntityDamageEvent> {
            if (isGrace) it.isCancelled = true
            if ((it.entity as? Player)?.isSpectating == true) {
                it.isCancelled = true
            }
            if (it.entity is Player && it.cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
                if ((it.entity as Player).mode == PlayerMode.SPECTATE) {
                    it.isCancelled = true
                    it.entity.teleport(Location(spawn.world, 0.0, 100.0, 0.0))
                }
            }
        })
        addEvent(listen<PlayerPickupArrowEvent> {
            if (it.player.mode != PlayerMode.PLAY) {
                it.isCancelled = true
            }
        })
        addEvent(listen<PlayerInteractEvent> {
            if (!GameManager.activeGame.isRunning) {
                if (it.clickedBlock?.type == Material.CHEST) it.isCancelled = true
                return@listen
            }
            if (it.player.isSpectating) {
                it.isCancelled = true
                if (it.item?.type == Material.COMPASS) {
                    it.player.teleport(onlinePlayers.filter { p -> p.mode == PlayerMode.PLAY }.random().location)
                }
                return@listen
            }
            if (it.action == Action.RIGHT_CLICK_BLOCK || it.action == Action.RIGHT_CLICK_AIR) {
                val p = it.player
                if (p.mode == PlayerMode.PLAY) {
                    val interactItem = p.getHandItem(it.hand) ?: return@listen
                    if (interactItem.type.isStew) {
                        var ifConsume = false
                        if (p.health < p.healthScale) {
                            (p.health + 7).let { i ->
                                if (i >= p.healthScale) p.health = p.healthScale else p.health = i
                            }
                            ifConsume = true
                        } else if (p.foodLevel < 20) {
                            (p.foodLevel + interactItem.type.restoredFood).let { i ->
                                if (i >= 20) p.foodLevel = 20 else p.foodLevel = i
                            }
                            (p.saturation + interactItem.type.restoredSaturation).let { i ->
                                if (i >= p.foodLevel) p.saturation = p.foodLevel.toFloat() else p.saturation = i
                            }
                            p.playSound(p.location, Sound.ENTITY_PLAYER_BURP, 1f, 1f)
                            ifConsume = true
                        }

                        if (ifConsume) interactItem.type = Material.BOWL

                    } else if (interactItem.type == Material.COMPASS && it.hand == EquipmentSlot.HAND) {
                        val clicker = it.player
                        val players =
                            onlinePlayers.filter { player -> player != clicker && player.mode == PlayerMode.PLAY }
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
                }
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
            if (it.player.isSpectating) it.isCancelled = true
        })
        addEvent(listen<EntityExplodeEvent> {
            if (it.entity is TNTPrimed) {
                it.isCancelled = true
                val tnt = it.entity as TNTPrimed
                it.entity.world.spawnParticle(Particle.EXPLOSION_LARGE, it.entity.location, 1)
                for (entity in it.entity.getNearbyEntities(4.0, 4.0, 4.0)) {
                    if (entity is Player) {
                        if (entity.mode == PlayerMode.PLAY) {
                            entity.damage(4.0, tnt.source)
                        }
                    }
                }
            }
        })
        addEvent(listen<PlayerDeathEvent> {
            it.keepInventory = true
            for (i in it.entity.inventory.contents) {
                if (i != null) {
                    it.entity.world.dropItemNaturally(it.entity.location, i)
                }
            }
            it.entity.clearInv()
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
        world.worldBorder.size = 400.0

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
        pointDistributor.giveRewards(RewardMode.FIRST_TO_LAST, listOf(6, 4, 2))
    }

    override fun onRejoin(player: Player) {
        PlayerManager.setToSpectator(player, PlayerMode.LEFT)
        player.teleport(spawn.clone().add(0.0, 15.0, 0.0))
        player.inventory.clear()
        player.inventory.addItem(randomTP)
    }

    private val Material.isStew: Boolean
        get() = when (this) {
            Material.MUSHROOM_STEW -> true
            Material.BEETROOT_SOUP -> true
            Material.RABBIT_STEW -> true
            else -> false
        }

    private val Material.restoredFood: Int
        get() = when (this) {
            Material.MUSHROOM_STEW, Material.BEETROOT_SOUP -> 6
            Material.RABBIT_STEW -> 10
            else -> 0
        }

    private val Material.restoredSaturation: Float
        get() = when (this) {
            Material.MUSHROOM_STEW, Material.BEETROOT_SOUP -> 7.2f
            Material.RABBIT_STEW -> 12f
            else -> 0f
        }
}
