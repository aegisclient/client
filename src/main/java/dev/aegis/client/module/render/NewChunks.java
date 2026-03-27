package dev.aegis.client.module.render;

import dev.aegis.client.module.Category;
import dev.aegis.client.module.Module;
import net.minecraft.util.math.ChunkPos;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class NewChunks extends Module {

    private final Set<ChunkPos> newChunks = new HashSet<>();
    private final Set<ChunkPos> oldChunks = new HashSet<>();

    public NewChunks() {
        super("NewChunks", "Highlights newly generated chunks (useful for finding bases)", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        // new chunk detection is done via packet events
        // chunks with liquid flow updates in them are flagged as new
    }

    public void markChunkNew(ChunkPos pos) {
        if (!oldChunks.contains(pos)) {
            newChunks.add(pos);
        }
    }

    public void markChunkOld(ChunkPos pos) {
        oldChunks.add(pos);
    }

    public Set<ChunkPos> getNewChunks() {
        return newChunks;
    }

    @Override
    protected void onDisable() {
        newChunks.clear();
        oldChunks.clear();
    }
}
