package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageESP extends Module {

    private final List<BlockPos> storagePositions = new ArrayList<>();
    private int scanDelay = 0;

    public StorageESP() {
        super("StorageESP", "Highlights storage containers like chests, ender chests, and shulkers", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        if (scanDelay > 0) {
            scanDelay--;
            return;
        }
        scanDelay = 20;

        storagePositions.clear();

        int chunkRange = 4;
        ChunkPos playerChunk = mc.player.getChunkPos();

        for (int cx = -chunkRange; cx <= chunkRange; cx++) {
            for (int cz = -chunkRange; cz <= chunkRange; cz++) {
                WorldChunk chunk = mc.world.getChunk(playerChunk.x + cx, playerChunk.z + cz);
                if (chunk == null) continue;

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockEntity be = entry.getValue();
                    if (isStorageBlock(be)) {
                        storagePositions.add(entry.getKey());
                    }
                }
            }
        }
    }

    public List<BlockPos> getStoragePositions() {
        return storagePositions;
    }

    public static boolean isStorageBlock(BlockEntity be) {
        return be instanceof ChestBlockEntity
                || be instanceof EnderChestBlockEntity
                || be instanceof ShulkerBoxBlockEntity
                || be instanceof BarrelBlockEntity
                || be instanceof HopperBlockEntity;
    }
}
