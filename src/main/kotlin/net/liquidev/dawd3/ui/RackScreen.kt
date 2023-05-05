package net.liquidev.dawd3.ui

import com.mojang.blaze3d.systems.RenderSystem
import net.liquidev.dawd3.Mod
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.liquidev.dawd3.render.Atlas
import net.liquidev.dawd3.render.Sprite
import net.liquidev.dawd3.ui.widget.rack.Rack
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class RackScreen(
    val world: ClientWorld,
    shownDevices: Iterable<BlockPos>,
) : Screen(Text.translatable("screen.dawd3.rack.title")) {

    private val rootWidget = Rack(width.toFloat(), height.toFloat(), world, shownDevices)

    override fun init() {
        super.init()
        rootWidget.width = width.toFloat()
        rootWidget.height = height.toFloat()
        rootWidget.reflow()
    }

    override fun resize(client: MinecraftClient?, width: Int, height: Int) {
        super.resize(client, width, height)
        rootWidget.width = width.toFloat()
        rootWidget.height = height.toFloat()
        rootWidget.reflow()
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        // For some silly reason blending is disabled by default, so let's enable it for rendering
        // widgets.
        RenderSystem.enableBlend()
        rootWidget.draw(matrices, mouseX.toFloat(), mouseY.toFloat(), delta)
        RenderSystem.disableBlend()
    }

    private fun propagateEvent(event: Event): Boolean {
        return rootWidget.event(Unit, event).eventConsumed
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        propagateEvent(MouseMove(mouseX.toFloat(), mouseY.toFloat()))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
        propagateEvent(MouseButton(Action.Down, mouseX.toFloat(), mouseY.toFloat(), button))

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean =
        propagateEvent(MouseButton(Action.Up, mouseX.toFloat(), mouseY.toFloat(), button))

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
        val atlas = Atlas(asset = Identifier(Mod.id, "textures/ui/rack.png"), size = 64f)
        val badge = Sprite(u = 0f, v = 16f, width = 6f, height = 3f)

        val smallFont = Identifier(Mod.id, "altopixel")
        val smallText = Style.EMPTY.withFont(smallFont)!!

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