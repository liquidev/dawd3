package net.liquidev.dawd3.ui

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.liquidev.dawd3.render.Atlas
import net.liquidev.dawd3.render.Icon
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.ui.widget.Widget
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

@Environment(EnvType.CLIENT)
class Rack(
    val world: ClientWorld,
    shownDevices: Iterable<BlockPos>,
) : Screen(Text.translatable("screen.dawd3.rack.title")) {
    private data class OpenWidget(
        val blockPosition: BlockPos,
        val widget: Widget,
    )

    private var openWidgets = run {
        var windowX = 32
        ArrayList(shownDevices.mapNotNull { blockPosition ->
            val blockEntity = world.getBlockEntity(blockPosition) as DeviceBlockEntity
            blockEntity.descriptor.openUI(blockEntity.controls, windowX, 32)
                ?.let { widget ->
                    windowX += widget.width + 8
                    OpenWidget(blockPosition, widget)
                }
        })
    }

    fun hasOpenWindows(): Boolean = openWidgets.isNotEmpty()

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)

        // Handle blocks being destroyed behind our back.
        openWidgets.removeIf { world.getBlockEntity(it.blockPosition) !is DeviceBlockEntity }
        if (openWidgets.isEmpty()) {
            close()
        }

        for (openWidget in openWidgets) {
            openWidget.widget.draw(matrices, mouseX, mouseY, delta)
        }

        Render.icon(matrices, 8, 8, badge.width * 2, badge.height * 2, atlas, badge)
    }

    private fun propagateEvent(event: Event): Boolean {
        for (openWidget in openWidgets) {
            if (openWidget.widget.event(
                    EventContext(world, openWidget.blockPosition),
                    event.relativeTo(
                        openWidget.widget.x.toDouble(),
                        openWidget.widget.y.toDouble(),
                    )
                ) == null
            ) return true
        }
        return false
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        propagateEvent(MouseMove(mouseX, mouseY))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
        propagateEvent(MouseButton(Action.Down, mouseX, mouseY, button))

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
        propagateEvent(MouseButton(Action.Up, mouseX, mouseY, button))

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return if (super.keyPressed(keyCode, scanCode, modifiers)) {
            true
        } else if (client?.options?.inventoryKey?.matchesKey(keyCode, scanCode) == true) {
            // Handle the player pressing 'E' to close the rack, since even though it is not an
            // inventory, it's just more intuitive that way.
            close()
            true
        } else {
            false
        }
    }

    override fun shouldPause(): Boolean = false

    companion object {
        val atlas = Atlas(asset = Identifier(Mod.id, "textures/ui/rack.png"), size = 64)
        val badge = Icon(u = 0, v = 16, width = 6, height = 3)

        // TODO: This "adjacent device" system is kind of janky but it allows for pretty nice
        //  organization of your devices into multiple racks of sorts. Maybe in the future we can
        //  think of collecting non-adjacent devices as well.

        fun collectAdjacentDevices(world: World, position: BlockPos): HashSet<BlockPos> {
            val result = hashSetOf<BlockPos>()
            collectAdjacentDevicesRec(result, world, position)
            return result
        }

        private fun collectAdjacentDevicesRec(
            outBlockPositions: HashSet<BlockPos>,
            world: World,
            position: BlockPos,
        ) {
            if (position !in outBlockPositions && world.getBlockEntity(position) is DeviceBlockEntity) {
                outBlockPositions.add(position)
                collectAdjacentDevicesRec(outBlockPositions, world, position.up())
                collectAdjacentDevicesRec(outBlockPositions, world, position.down())
                collectAdjacentDevicesRec(outBlockPositions, world, position.north())
                collectAdjacentDevicesRec(outBlockPositions, world, position.south())
                collectAdjacentDevicesRec(outBlockPositions, world, position.east())
                collectAdjacentDevicesRec(outBlockPositions, world, position.west())
            }
        }
    }
}