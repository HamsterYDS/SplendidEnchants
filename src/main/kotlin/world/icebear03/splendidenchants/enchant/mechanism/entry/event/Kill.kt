package world.icebear03.splendidenchants.enchant.mechanism.entry.event

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import world.icebear03.splendidenchants.api.displayName
import world.icebear03.splendidenchants.api.replace
import world.icebear03.splendidenchants.api.subList
import world.icebear03.splendidenchants.enchant.mechanism.entry.`object`.ObjectLivingEntity
import world.icebear03.splendidenchants.enchant.mechanism.entry.`object`.ObjectPlayer

object Kill {
    fun modify(e: Event, entity: LivingEntity, params: List<String>, holders: MutableMap<String, Any>) {
        val event = e as EntityDamageByEntityEvent

        val killed = event.entity as? LivingEntity ?: return
        holders["击杀者"] = entity
        holders["死者"] = killed
        holders["击杀者名称"] = entity.displayName
        holders["死者名称"] = killed.displayName

        val variabled = params.map { it.replace(holders) }
        val type = variabled[0]
        val after = variabled.subList(1)

        when (type) {
            "击杀者" ->
                if (entity is Player) ObjectPlayer.modify(entity, after, holders)
                else ObjectLivingEntity.modify(entity, after, holders)

            "死者" ->
                if (killed is Player) ObjectPlayer.modify(killed, after, holders)
                else ObjectLivingEntity.modify(killed, after, holders)

            else -> {}
        }
    }
}