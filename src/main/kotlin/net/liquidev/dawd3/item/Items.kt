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
    val patchCable =
        addItem("patch_cable", PatchCable(FabricItemSettings().group(ItemGroup.REDSTONE)))

    fun addItem(name: Identifier, item: Item): D3Registry.Registered<RegisteredItem> =
        registry.add(name, RegisteredItem(item))

    fun addItem(name: String, item: Item) =
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

