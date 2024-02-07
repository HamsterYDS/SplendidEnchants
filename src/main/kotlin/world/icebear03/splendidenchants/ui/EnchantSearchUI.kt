@file:Suppress("UNCHECKED_CAST")

package world.icebear03.splendidenchants.ui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.serverct.parrot.parrotx.function.variable
import org.serverct.parrot.parrotx.function.variables
import org.serverct.parrot.parrotx.mechanism.Reloadable
import org.serverct.parrot.parrotx.ui.MenuComponent
import org.serverct.parrot.parrotx.ui.config.MenuConfiguration
import org.serverct.parrot.parrotx.ui.feature.util.MenuFunctionBuilder
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.ui.openMenu
import taboolib.platform.util.nextChat
import com.mcstarrysky.starrysky.i18n.sendLang
import taboolib.module.ui.type.PageableChest
import world.icebear03.splendidenchants.api.internal.colorify
import world.icebear03.splendidenchants.api.load
import world.icebear03.splendidenchants.api.pages
import world.icebear03.splendidenchants.api.setSlots
import world.icebear03.splendidenchants.api.skull
import world.icebear03.splendidenchants.enchant.EnchantFilter
import world.icebear03.splendidenchants.enchant.SplendidEnchant
import world.icebear03.splendidenchants.player.filters
import world.icebear03.splendidenchants.ui.internal.UIType
import world.icebear03.splendidenchants.ui.internal.record
import kotlin.collections.set

@MenuComponent("EnchantSearch")
object EnchantSearchUI {

    @Config("gui/enchant_search.yml")
    private lateinit var source: Configuration
    private lateinit var config: MenuConfiguration

    @Reloadable
    fun reload() {
        source.reload()
        config = MenuConfiguration(source)
    }

    fun open(player: Player) {
        player.record(UIType.ENCHANT_SEARCH)
        player.openMenu<PageableChest<SplendidEnchant>>(config.title().colorify()) {
            val (shape, templates) = config
            rows(shape.rows)
            val slots = shape["EnchantSearch:enchant"].toList()
            slots(slots)
            elements { EnchantFilter.filter(player.filters) }

            load(
                shape, templates, player,
                "EnchantSearch:enchant", "EnchantSearch:filter_rarity", "EnchantSearch:filter_target",
                "EnchantSearch:filter_group", "EnchantSearch:filter_string", "Previous", "Next"
            )
            pages(shape, templates)

            val template = templates.require("EnchantSearch:enchant")
            onGenerate { _, element, index, slot -> template(slot, index) { this["enchant"] = element } }
            onClick { event, element -> templates[event.rawSlot]?.handle(this, event, "element" to element) }

            EnchantFilter.FilterType.values().forEach {
                setSlots(
                    shape, templates, "EnchantSearch:filter_${it.toString().lowercase()}", listOf(),
                    "filters" to EnchantFilter.generateLore(it, player)
                )
            }
        }
    }

    @MenuComponent
    private val enchant = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) ->
            val enchant = args["enchant"] as SplendidEnchant
            val holders = enchant.displayer.holders(enchant.maxLevel)
            icon.variables { variable -> listOf(holders[variable] ?: "") }
                .skull(enchant.rarity.skull)
        }
        onClick { (_, _, _, event, args) ->
            EnchantInfoUI.open(event.clicker, args["element"] as SplendidEnchant)
        }
    }

    @MenuComponent
    private val filter_rarity = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) -> icon.variable("rarities", args["filters"] as List<String>) }
        onClick { (_, _, _, event, _) ->
            val clickType = event.clickEvent().click
            val player = event.clicker
            if (clickType == ClickType.MIDDLE) {
                EnchantFilter.clearFilter(player, EnchantFilter.FilterType.RARITY)
                open(player)
            } else FilterRarityUI.open(event.clicker)
        }
    }

    @MenuComponent
    private val filter_target = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) -> icon.variable("targets", args["filters"] as List<String>) }
        onClick { (_, _, _, event, _) ->
            val clickType = event.clickEvent().click
            val player = event.clicker
            if (clickType == ClickType.MIDDLE) {
                EnchantFilter.clearFilter(player, EnchantFilter.FilterType.TARGET)
                open(player)
            } else FilterTargetUI.open(event.clicker)
        }
    }

    @MenuComponent
    private val filter_group = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) -> icon.variable("groups", args["filters"] as List<String>) }
        onClick { (_, _, _, event, _) ->
            val clickType = event.clickEvent().click
            val player = event.clicker
            if (clickType == ClickType.MIDDLE) {
                EnchantFilter.clearFilter(player, EnchantFilter.FilterType.GROUP)
                open(player)
            } else FilterGroupUI.open(event.clicker)
        }
    }

    @MenuComponent
    private val filter_string = MenuFunctionBuilder {
        onBuild { (_, _, _, _, icon, args) -> icon.variable("strings", args["filters"] as List<String>) }
        onClick { (_, _, _, event, _) ->
            val clickType = event.clickEvent().click
            val player = event.clicker
            when (clickType) {
                ClickType.LEFT, ClickType.RIGHT -> {
                    player.closeInventory()
                    player.sendLang("messages.menu.search.input")
                    player.nextChat {
                        player.sendLang("messages.menu.search.input_finish")
                        EnchantFilter.addFilter(
                            player, EnchantFilter.FilterType.STRING, it,
                            when (clickType) {
                                ClickType.RIGHT -> EnchantFilter.FilterStatement.OFF
                                else -> EnchantFilter.FilterStatement.ON
                            }
                        )
                        submit { open(player) }
                    }
                }

                ClickType.MIDDLE -> {
                    EnchantFilter.clearFilter(player, EnchantFilter.FilterType.STRING)
                    open(player)
                }

                else -> {}
            }
        }
    }
}