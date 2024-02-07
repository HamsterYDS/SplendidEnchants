package world.icebear03.splendidenchants.ui

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.serverct.parrot.parrotx.function.variables
import org.serverct.parrot.parrotx.mechanism.Reloadable
import org.serverct.parrot.parrotx.ui.MenuComponent
import org.serverct.parrot.parrotx.ui.config.MenuConfiguration
import org.serverct.parrot.parrotx.ui.feature.util.MenuFunctionBuilder
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.PageableChest
import world.icebear03.splendidenchants.api.internal.colorify
import world.icebear03.splendidenchants.api.load
import world.icebear03.splendidenchants.api.pages
import world.icebear03.splendidenchants.api.skull
import world.icebear03.splendidenchants.enchant.EnchantFilter
import world.icebear03.splendidenchants.enchant.EnchantLoader
import world.icebear03.splendidenchants.enchant.data.Target
import world.icebear03.splendidenchants.enchant.data.targets
import world.icebear03.splendidenchants.ui.internal.UIType
import world.icebear03.splendidenchants.ui.internal.record
import kotlin.collections.set


@MenuComponent("FilterTarget")
object FilterTargetUI {

    @Config("gui/filter_target.yml")
    private lateinit var source: Configuration
    private lateinit var config: MenuConfiguration

    @Reloadable
    fun reload() {
        source.reload()
        config = MenuConfiguration(source)
    }

    fun open(player: Player) {
        player.record(UIType.FILTER_TARGET)
        player.openMenu<PageableChest<Target>>(config.title().colorify()) {
            val (shape, templates) = config
            rows(shape.rows)
            val slots = shape["FilterTarget:filter"].toList()
            slots(slots)
            elements { targets.values.toList() }

            load(shape, templates, player, "FilterTarget:filter", "Previous", "Next")
            pages(shape, templates)

            val template = templates.require("FilterTarget:filter")
            onGenerate { _, element, index, slot ->
                template(slot, index) {
                    this["target"] = element
                    this["player"] = player
                }
            }
            onClick { event, element -> templates[event.rawSlot]?.handle(this, event, "target" to element) }
        }
    }

    @MenuComponent
    private val filter = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val target = args["target"] as Target
            val player = args["player"] as Player

            when (EnchantFilter.getStatement(player, EnchantFilter.FilterType.TARGET, target.id)) {
                EnchantFilter.FilterStatement.ON -> icon.type = Material.LIME_STAINED_GLASS_PANE
                EnchantFilter.FilterStatement.OFF -> icon.type = Material.RED_STAINED_GLASS_PANE
                else -> {}
            }

            icon.variables {
                when (it) {
                    "name" -> listOf(target.name)
                    "amount" -> listOf((EnchantLoader.BY_TARGET[target]?.size ?: 0).toString())
                    else -> emptyList()
                }
            }.skull(target.skull)
        }

        onClick { (_, _, _, event, args) ->
            val clickType = event.clickEvent().click
            val player = event.clicker
            val target = args["target"] as Target

            when (clickType) {
                ClickType.LEFT, ClickType.RIGHT -> {
                    EnchantFilter.clearFilter(player, EnchantFilter.FilterType.TARGET, target)
                    EnchantFilter.addFilter(
                        player, EnchantFilter.FilterType.TARGET, target.id,
                        when (clickType) {
                            ClickType.RIGHT -> EnchantFilter.FilterStatement.OFF
                            else -> EnchantFilter.FilterStatement.ON
                        }
                    )
                    open(player)
                }

                ClickType.MIDDLE -> {
                    EnchantFilter.clearFilter(player, EnchantFilter.FilterType.TARGET, target)
                    open(player)
                }

                else -> {}
            }
        }
    }

    @MenuComponent
    private val reset = MenuFunctionBuilder {
        onClick { (_, _, _, event, _) ->
            val player = event.clicker
            EnchantFilter.clearFilter(player, EnchantFilter.FilterType.TARGET)
            open(player)
        }
    }
}