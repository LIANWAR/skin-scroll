/*
 * Copyright (c) 2022 AlphaGot
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package com.lianserver.skinscroll

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.lianserver.skinscroll.interfaces.PrefixedTextInterface
import io.github.monun.kommand.Kommand.Companion.register
import io.github.monun.kommand.getValue
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/***
 * @author AlphaGot
 */

class Main : JavaPlugin(), Listener, PrefixedTextInterface {

    companion object {
        lateinit var instance: Main
            private set
    }

    private lateinit var skinItems: YamlConfiguration

    override fun onEnable() {
        instance = this

        skinItems = YamlConfiguration.loadConfiguration(File(dataFolder, "db.yml"))

        server.pluginManager.registerEvents(this, this)

        register(this, "getskinitem"){
            requires { player.isOp }
            executes { player.sendMessage(adminText("스킨 아이디를 입력해주세요.")) }
            then("id" to int()){
                executes {
                    val id: Int by it

                    player.inventory.addItem(skinItems.getItemStack(id.toString()) ?: ItemStack(Material.BROWN_WOOL))
                    player.sendMessage(adminText("아이템을 지급했습니다."))
                }
            }
        }
        register(this, "regskinitem"){
            requires { player.isOp }
            executes { player.sendMessage(adminText("스킨 아이디를 입력해주세요.")) }
            then("id" to int()){
                executes {
                    val id: Int by it

                    if(player.inventory.itemInMainHand.type == Material.KNOWLEDGE_BOOK && player.inventory.itemInMainHand.hasItemMeta()){
                        skinItems[id.toString()] = player.inventory.itemInMainHand
                        player.sendMessage(adminText("아이템을 등록했습니다."))
                    }
                    else {
                        player.sendMessage(adminText("스킨 아이템이 아닙니다."))
                    }
                }
            }
        }
    }

    override fun onDisable() {
        logger.info(adminTextS("아새기들 일 안하네"))
        skinItems.save(File(dataFolder, "db.yml"))
    }

    @EventHandler
    fun onUseEffectBook(e: PlayerInteractEvent){
        if(e.hasItem()){
            if(e.item!!.type == Material.KNOWLEDGE_BOOK){
                if(e.item!!.hasItemMeta()){
                    if(e.item!!.itemMeta.hasLore()){
                        val c = PlainTextComponentSerializer.plainText().serialize(e.item!!.itemMeta.lore()!![0]!!)

                        if(c.contains("(id=")){
                            val id = c.split("=")[1].replace(")", "")
                            val type = when(PlainTextComponentSerializer.plainText().serialize(e.item!!.itemMeta.lore()!![1]!!).split(": ")[1]){
                                "검" -> "sword"
                                "도끼" -> "_axe"
                                "곡괭이" -> "pickaxe"
                                "삽" -> "shovel"
                                "괭이" -> "hoe"
                                "활" -> "bow"
                                "쇠뇌" -> "crossbow"
                                else -> "푸틴 개쉑"
                            }

                            println(type)

                            val invSelItem = ChestGui(4, "스킨을 적용할 아이템 선택")

                            val p = StaticPane(0, 0, 9, 4)

                            val xy = Pair(0, 0)

                            e.player.inventory.filterNotNull().filter {
                                println(it.type.name.lowercase())
                                when(it.type.name.lowercase()){
                                    in arrayOf("bow", "crossbow") -> {
                                        it.type.name.lowercase() == type
                                    }
                                    else -> {
                                        it.type.name.lowercase().contains(type)
                                    }
                                }
                            }.forEach {
                                p.addItem(
                                    GuiItem(
                                        it
                                    ){ ev: InventoryClickEvent ->
                                        ev.isCancelled = true

                                        val m = it.itemMeta

                                        m.setCustomModelData(id.toIntOrNull())
                                        it.itemMeta = m

                                        e.player.sendMessage(userText("").append(it.displayName()).append(text("아이템에 스킨을 적용했습니다.")))
                                        e.item!!.subtract(1)
                                    },
                                    xy.first,
                                    xy.second
                                )
                            }

                            invSelItem.addPane(p)
                            invSelItem.update()

                            invSelItem.show(e.player)
                        }
                    }
                }
            }
        }
    }
}