package net.liquidev.dawd3.ui.widget.rack

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.liquidev.dawd3.block.device.DeviceBlockEntity
import net.liquidev.dawd3.common.moveElement
import net.liquidev.dawd3.net.EditRack
import net.liquidev.dawd3.net.ReorderRack
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
import java.util.*

class Rack(
    override var width: Float,
    override var height: Float,
    private val world: ClientWorld,
    shownDevices: Iterable<BlockPos>,
) : Widget<Unit, Message>(0f, 0f) {

    private val sidebar = Sidebar(0f, 0f, 0f, 0f)
    private val blockPositionsByWidget = hashMapOf<DeviceWidget, BlockPos>()

    private val shelves = mutableListOf<Shelf>()

    enum class DragPlace {
        Sidebar,
        Shelf,
    }

    data class DraggedWindow(
        val window: Window,
        val source: DragPlace,
        val indexInSource: Int,
        val byX: Float,
        val byY: Float,

        val sourceShelfIndex: Int = 0, // Only relevant when source == DragPlace.Shelf
    )

    private var draggedWindow: DraggedWindow? = null

    init {
        val shelvesByUuid = hashMapOf<UUID, Shelf>()

        for (blockPosition in shownDevices) {
            val blockEntity = world.getBlockEntity(blockPosition) as? DeviceBlockEntity ?: continue
            for (shelfUuid in blockEntity.shelfOrder) {
                if (shelfUuid !in shelvesByUuid) {
                    val shelf = Shelf(0f, 0f, width, shelfUuid)
                    shelves.add(shelf)
                    shelvesByUuid[shelfUuid] = shelf
                }
            }
        }

        for (blockPosition in shownDevices) {
            val blockEntity = world.getBlockEntity(blockPosition) as? DeviceBlockEntity ?: continue
            val window = blockEntity.descriptor.ui?.open(blockEntity.controls, x = 0f, y = 0f)
            if (window != null) {
                val shelfUuid = blockEntity.shelf
                if (shelfUuid != null) {
                    shelvesByUuid[shelfUuid]?.windows?.add(window)
                } else {
                    sidebar.windows.add(window)
                }
                blockPositionsByWidget[window] = blockPosition
            }
        }
        removeEmptyShelvesFromEnd()
        addEmptyShelfIfNotPresent()

        sortWindowsInContainer(sidebar.windows)
        shelves.forEach { sortWindowsInContainer(it.windows) }
    }

    private fun <W : DeviceWidget> sortWindowsInContainer(container: MutableList<W>) {
        container.sortBy { window ->
            val blockPosition = blockPositionsByWidget[window]
            val blockEntity = world.getBlockEntity(blockPosition) as? DeviceBlockEntity
            blockEntity?.sortPriority ?: Int.MAX_VALUE
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
        if (shelves.size == 1 && shelves.last().windows.isEmpty()) {
            shelves.last().drawUsageHint(matrices)
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
            when (destination?.place) {
                DragPlace.Sidebar -> {
                    sidebar.drawInside(matrices) {
                        val y = if (sidebar.windows.size == 0) {
                            Sidebar.padding
                        } else if (destination.indexInPlace == sidebar.windows.size) {
                            val window = sidebar.windows.last()
                            window.y + window.height + Sidebar.spacingBetweenWindows / 2f
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
        windows.forEachIndexed { index, window ->
            val blockPosition = blockPositionsByWidget[window]!!
            val windowRelativeEvent = event.relativeTo(window.x, window.y)
            val windowMessage =
                window.event(DeviceEventContext(world, blockPosition), windowRelativeEvent)
            if (windowMessage is Window.BeginDrag) {
                draggedWindow =
                    DraggedWindow(
                        window,
                        source,
                        index,
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
                    val fromList: MutableList<Window> = when (draggedWindow.source) {
                        DragPlace.Sidebar -> sidebar.windows
                        DragPlace.Shelf -> shelves[draggedWindow.sourceShelfIndex].windows
                    }
                    val toList: MutableList<Window> = when (destination.place) {
                        DragPlace.Sidebar -> sidebar.windows
                        DragPlace.Shelf -> shelves[destination.shelfIndex].windows
                    }
                    moveElement(
                        fromList,
                        fromIndex = draggedWindow.indexInSource,
                        toList,
                        toIndex = destination.indexInPlace
                    )

                    val blockPosition = blockPositionsByWidget[draggedWindow.window]
                    if (blockPosition != null) {
                        when (destination.place) {
                            DragPlace.Shelf -> {
                                val shelf = shelves[destination.shelfIndex]
                                updateBlockData(blockPosition, shelf.uuid)
                            }
                            DragPlace.Sidebar -> {
                                updateBlockData(blockPosition, null)
                            }
                        }
                    }

                    removeEmptyShelvesFromEnd()
                    addEmptyShelfIfNotPresent()
                    updateBlockPriorities(fromList)
                    updateBlockPriorities(toList)
                    reflow()
                }
            }
            this.draggedWindow = null
        }

        return Message.eventIgnored
    }

    private fun removeEmptyShelvesFromEnd() {
        while (shelves.size > 1 && shelves.last().windows.isEmpty()) {
            shelves.removeLast()
        }
    }

    private fun addEmptyShelfIfNotPresent() {
        if (shelves.size == 0 || shelves.last().windows.isNotEmpty()) {
            shelves.add(Shelf(0f, 0f, width, UUID.randomUUID()))
        }
    }

    private fun <W : DeviceWidget> updateBlockPriorities(windows: MutableList<W>) {
        val packetEntries = mutableListOf<ReorderRack.Entry>()
        windows.forEachIndexed { index, window ->
            val blockPosition = blockPositionsByWidget[window] ?: return@forEachIndexed
            packetEntries.add(ReorderRack.Entry(blockPosition, index))
        }
        ClientPlayNetworking.send(ReorderRack.id, ReorderRack(packetEntries).serialize())
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

    override fun reflow() {
        sidebar.width = if (sidebar.windows.isNotEmpty()) 160f else 16f
        sidebar.x = width - sidebar.width
        sidebar.height = height
        sidebar.reflow()

        var dy = 0f
        for (shelf in shelves) {
            shelf.reflow()
            shelf.y = dy
            shelf.width = width - sidebar.width
            dy += shelf.height
        }
    }

    private fun updateBlockData(editedDevicePosition: BlockPos, newShelfUuid: UUID?) {
        ClientPlayNetworking.send(
            EditRack.id, EditRack(
                editedDevicePosition,
                shelf = newShelfUuid,
                shelfOrder = shelves.map { it.uuid }.toTypedArray()
            ).serialize()
        )
    }

    companion object {
        private val windowDraggingPreview =
            NinePatch(u = 48f, v = 0f, width = 8f, height = 8f, border = 2f)
        private val windowDestinationPreview = TextureStrip(25f, 16f, 25f, 32f)
    }
}