package me.icebear03.splendidenchants.enchant

import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import taboolib.common.io.newFolder
import taboolib.common.platform.function.getDataFolder
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.setProperty
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object EnchantLoader {

    val enchantById = ConcurrentHashMap<String, SplendidEnchant>()
    val enchantByName = ConcurrentHashMap<String, SplendidEnchant>()

    //TODO 分类！ 按品质/targets/packet，仅仅中文 便于做Filter
    val enchantsByPacket = ConcurrentHashMap<String, Set<SplendidEnchant>>()
    val enchantsByRarity = ConcurrentHashMap<String, Set<SplendidEnchant>>()
    val enchantsByTarget = ConcurrentHashMap<String, Set<SplendidEnchant>>()

    //enchants文件夹应该为若干文件夹，每个文件夹内为各个附魔配置
    fun initialize() {
        Enchantment::class.java.setProperty("acceptingNew", value = true, isStatic = true)
        val directory = File(getDataFolder(), "enchants/")
        if (!directory.exists()) directory.mkdirs()
        newFolder(getDataFolder(), "enchants").listFiles { dir, _ -> dir.isDirectory }?.forEach { folder ->
            folder.listFiles()?.forEach { file ->
                val id = file.nameWithoutExtension
                val key = NamespacedKey.fromString(id)!!
                val enchant = SplendidEnchant(file, key)

                // 注册附魔
                if (!folder.name.equals("原版附魔"))
                    Enchantment.registerEnchantment(enchant)
                enchantById[id] = enchant
                enchantByName[enchant.basicData.name] = enchant
            }
        }
        Enchantment::class.java.setProperty("acceptingNew", value = false, isStatic = true)
    }

    fun unregister() {
        enchantById.values.forEach {
            if (!EnchantGroup.isIn(it, "原版附魔")) {
                val keyMap = Enchantment::class.java.getProperty<HashMap<*, *>>("byKey", true)
                val nameMap = Enchantment::class.java.getProperty<HashMap<*, *>>("byName", true)
                keyMap?.remove(it.key)
                nameMap?.remove(it.name)
            }
        }
    }
}