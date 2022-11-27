package net.liquidev.dawd3.item

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.liquidev.dawd3.D3Registry
import net.liquidev.dawd3.Mod
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object Items {
    var registry = object : D3Registry<RegisteredItem>() {
        override fun doRegister(identifier: Identifier, item: RegisteredItem) {
            Registry.register(Registry.ITEM, identifier, item.item)
        }
    }

    // Creative tab
    val creativeTab: ItemGroup = FabricItemGroupBuilder.create(Identifier(Mod.id, "main"))
        .icon { dawd3.item.item.defaultStack }
        .appendItems { list ->
            registry.registered.forEach { reg ->
                val item = reg.item
                if (item.showInCreativeTab) {
                    list.add(reg.item.item.defaultStack)
                }
            }
        }
        .build()

    // Icon for creative tab
    val dawd3 = registry.add(
        Identifier(Mod.id, "dawd3"),
        RegisteredItem(Item(FabricItemSettings())).hiddenFromCreativeTab()
    )

    // Tools
    private fun coloredPatchCable(name: String, color: Byte) =
        addItem(name, PatchCableItem(FabricItemSettings().group(ItemGroup.REDSTONE), color))

    val patchCables = arrayOf(
        coloredPatchCable("white_patch_cable", color = 0),
        coloredPatchCable("orange_patch_cable", color = 1),
        coloredPatchCable("magenta_patch_cable", color = 2),
        coloredPatchCable("light_blue_patch_cable", color = 3),
        coloredPatchCable("yellow_patch_cable", color = 4),
        coloredPatchCable("lime_patch_cable", color = 5),
        coloredPatchCable("pink_patch_cable", color = 6),
        coloredPatchCable("gray_patch_cable", color = 7),
        coloredPatchCable("light_gray_patch_cable", color = 8),
        coloredPatchCable("cyan_patch_cable", color = 9),
        coloredPatchCable("purple_patch_cable", color = 10),
        coloredPatchCable("blue_patch_cable", color = 11),
        coloredPatchCable("brown_patch_cable", color = 12),
        coloredPatchCable("green_patch_cable", color = 13),
        coloredPatchCable("red_patch_cable", color = 14),
        coloredPatchCable("black_patch_cable", color = 15),
    )

    fun addItem(name: Identifier, item: Item): D3Registry.Registered<RegisteredItem> =
        registry.add(name, RegisteredItem(item))

    private fun addItem(name: String, item: Item) =
        addItem(Identifier(Mod.id, name), item)

    data class RegisteredItem(
        val item: Item,
        var showInCreativeTab: Boolean = true,
    ) {
        fun hiddenFromCreativeTab(): RegisteredItem {
            this.showInCreativeTab = false
            return this
        }
    }
}

