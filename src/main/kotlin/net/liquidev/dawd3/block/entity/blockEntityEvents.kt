package net.liquidev.dawd3.block.entity

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents

fun registerBlockEntityEvents() {
    ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register { blockEntity, world ->
        if (blockEntity is D3BlockEntity) {
            blockEntity.onClientLoad(world)
        }
    }

    ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register { blockEntity, world ->
        if (blockEntity is D3BlockEntity) {
            blockEntity.onClientUnload(world)
        }
    }
}
