package world.icebear03.splendidenchants.api.nms

import org.bukkit.boss.BarColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.nms.nmsProxy
import taboolib.module.nms.sendPacket
import world.icebear03.splendidenchants.`object`.Overlay

abstract class NMS {

    abstract fun sendBossBar(
        player: Player,
        message: String,
        progress: Float,
        time: Int,
        overlay: Overlay,
        color: BarColor
    )

    abstract fun toBukkitItemStack(item: Any): ItemStack

    abstract fun toNMSItemStack(item: ItemStack): Any

    abstract fun adaptMerchantRecipe(merchantRecipeList: Any, player: Player): Any

    fun sendPacket(player: Player, packet: Any, vararg fields: Pair<Any, Any>) {
        fields.forEach { packet.setProperty(it.first.toString(), it.second) }
        player.sendPacket(packet)
    }

    companion object {

        val INSTANCE by lazy {
            nmsProxy<NMS>()
        }
    }
}