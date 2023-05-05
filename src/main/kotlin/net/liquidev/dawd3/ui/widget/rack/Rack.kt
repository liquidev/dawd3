package net.liquidev.dawd3.ui.widget.rack

import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.liquidev.dawd3.render.NinePatch
import net.liquidev.dawd3.render.Render
import net.liquidev.dawd3.render.TextureStrip
import net.liquidev.dawd3.ui.*
import net.liquidev.dawd3.ui.widget.DeviceWidget
import net.liquidev.dawd3.ui.widget.Widget
import net.liquidev.dawd3.ui.widget.Window
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW

class Rack(
    override var width: Float,
    override var height: Float,
    private val world: ClientWorld,
    shownDevices: Iterable<BlockPos>,
) : Widget<Unit, Message>(0f, 0f) {

    private val sidebar = Sidebar(0f, 0f, 160f, 0f)
    private val blockPositionsByWidget = hashMapOf<DeviceWidget, BlockPos>()

    private val shelves = MutableList(10) { Shelf(0f, 0f, width) }

    enum class DragPlace {
        Sidebar,
        Shelf,
    }

    data class DraggedWindow(
        val window: Window,
        val source: DragPlace,
        val byX: Float,
        val byY: Float,

        val sourceShelfIndex: Int = 0, // Only relevant when source == DragPlace.Shelf
    )

    private var draggedWindow: DraggedWindow? = null

    init {
        for (blockPosition in shownDevices) {
            val blockEntity = world.getBlockEntity(blockPosition) as? DeviceBlockEntity ?: continue
            val widget = blockEntity.descriptor.ui?.open(blockEntity.controls, x = 0f, y = 0f)
            if (widget != null) {
                sidebar.windows.add(widget)
                blockPositionsByWidget[widget] = blockPosition
            }
        }
    }

    private fun <W : DeviceWidget> removeStaleWidgetsFromContainer(container: MutableList<W>) {
        container.removeAll { world.getBlockEntity(blockPositionsByWidget[it]) !is DeviceBlockEntity }
    }

    override fun drawContent(
        matrices: MatrixStack,
        mouseX: Float,
        mouseY: Float,
        deltaTime: Float,
    ) {
        removeStaleWidgetsFromContainer(sidebar.windows)

        for (shelf in shelves) {
            shelf.draw(matrices, mouseX, mouseY, deltaTime)
        }

        sidebar.draw(matrices, mouseX, mouseY, deltaTime)

        val draggedWindow = draggedWindow
        if (draggedWindow != null) {
            Render.ninePatch(
                matrices,
                mouseX - draggedWindow.byX,
                mouseY - draggedWindow.byY,
                draggedWindow.window.width,
                draggedWindow.window.height,
                RackScreen.atlas,
                windowDraggingPreview,
            )

            val destination = findDragDestination(mouseX, mouseY)
            println(destination)
            when (destination?.place) {
                DragPlace.Sidebar -> {
                    sidebar.drawInside(matrices) {
                        val y = if (sidebar.windows.size == 0) {
                            Sidebar.padding
                        } else if (destination.indexInPlace == sidebar.windows.size) {
                            val window = sidebar.windows.last()
                            window.y + window.height - Sidebar.spacingBetweenWindows / 2f
                        } else {
                            val window = sidebar.windows[destination.indexInPlace]
                            window.y - Sidebar.spacingBetweenWindows / 2f
                        }
                        Render.line(
                            matrices,
                            Sidebar.padding,
                            y,
                            sidebar.width - Sidebar.padding,
                            y,
                            thickness = 0.5f,
                            RackScreen.atlas,
                            windowDestinationPreview
                        )
                    }
                }
                DragPlace.Shelf -> {
                    val shelf = shelves[destination.shelfIndex]
                    shelf.drawInside(matrices) {
                        val x = if (shelf.windows.size == 0) {
                            Shelf.padding
                        } else if (destination.indexInPlace == shelf.windows.size) {
                            val window = shelf.windows.last()
                            window.x + window.width + Shelf.spacingBetweenWindows / 2f
                        } else {
                            val window = shelf.windows[destination.indexInPlace]
                            window.x - Shelf.spacingBetweenWindows / 2f
                        }
                        Render.line(
                            matrices,
                            x,
                            Shelf.padding,
                            x,
                            shelf.height - Shelf.padding,
                            thickness = 0.5f,
                            RackScreen.atlas,
                            windowDestinationPreview
                        )
                    }
                }
                null -> {}
            }
        }
    }

    private fun sendEventToWindows(
        windows: List<Window>,
        event: Event,
        source: DragPlace,
        sourceShelfIndex: Int = 0,
    ): Message {
        for (window in windows) {
            val blockPosition = blockPositionsByWidget[window]!!
            val windowRelativeEvent = event.relativeTo(window.x, window.y)
            val windowMessage =
                window.event(DeviceEventContext(world, blockPosition), windowRelativeEvent)
            if (windowMessage is Window.BeginDrag) {
                draggedWindow =
                    DraggedWindow(
                        window,
                        source,
                        windowMessage.x,
                        windowMessage.y,
                        sourceShelfIndex
                    )
            }
            if (windowMessage.eventConsumed) return Message.eventUsed
        }
        return Message.eventIgnored
    }

    override fun event(context: Unit, event: Event): Message {
        val sidebarWindowMessage =
            sendEventToWindows(
                sidebar.windows,
                event.relativeTo(sidebar.x, sidebar.y),
                DragPlace.Sidebar
            )
        if (sidebarWindowMessage.eventConsumed) {
            return sidebarWindowMessage
        }

        shelves.forEachIndexed { index, shelf ->
            val shelfWindowMessage =
                sendEventToWindows(
                    shelf.windows,
                    event.relativeTo(shelf.x, shelf.y),
                    DragPlace.Shelf,
                    index,
                )
            if (shelfWindowMessage.eventConsumed) {
                return shelfWindowMessage
            }
        }

        if (event is MouseButton && event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.action == Action.Up) {
            val draggedWindow = draggedWindow
            if (draggedWindow != null) {
                val destination = findDragDestination(event.mouseX, event.mouseY)
                if (destination != null) {
                    removeWindowFromSource(
                        draggedWindow.source,
                        draggedWindow.sourceShelfIndex,
                        draggedWindow.window,
                    )
                    when (destination.place) {
                        DragPlace.Sidebar -> sidebar.windows.add(
                            destination.indexInPlace,
                            draggedWindow.window
                        )
                        DragPlace.Shelf -> shelves[destination.shelfIndex].windows.add(
                            destination.indexInPlace,
                            draggedWindow.window
                        )
                    }
                    reflow()
                }
            }
            this.draggedWindow = null
        }

        return Message.eventIgnored
    }

    internal data class DragDestination(
        val place: DragPlace,
        val indexInPlace: Int,
        val shelfIndex: Int = 0,
    )

    private fun findDragDestination(mouseX: Float, mouseY: Float): DragDestination? {
        val indexInSidebar = sidebar.findDragDestination(mouseX - sidebar.x, mouseY - sidebar.y)
        if (indexInSidebar != null) {
            return DragDestination(DragPlace.Sidebar, indexInSidebar)
        }

        shelves.forEachIndexed { shelfIndex, shelf ->
            val indexInShelf = shelf.findDragDestination(mouseX - shelf.x, mouseY - shelf.y)
            if (indexInShelf != null) {
                return DragDestination(DragPlace.Shelf, indexInShelf, shelfIndex)
            }
        }

        return null
    }

    private fun removeWindowFromSource(
        source: DragPlace,
        indexOfSource: Int,
        window: Window,
    ) {
        when (source) {
            DragPlace.Sidebar -> {
                sidebar.windows.remove(window)
            }
            DragPlace.Shelf -> {
                shelves[indexOfSource].windows.remove(window)
            }
        }
    }

    override fun reflow() {
        sidebar.x = width - sidebar.width
        sidebar.height = height
        sidebar.reflow()

        var dy = 0f
        for (shelf in shelves) {
            shelf.reflow()
            shelf.y = dy
            shelf.width = width
            dy += shelf.height
        }
    }

    companion object {
        private val windowDraggingPreview =
            NinePatch(u = 48f, v = 0f, width = 8f, height = 8f, border = 2f)
        private val windowDestinationPreview = TextureStrip(25f, 16f, 25f, 32f)
    }
}