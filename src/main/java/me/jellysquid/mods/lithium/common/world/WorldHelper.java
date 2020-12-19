package me.jellysquid.mods.lithium.common.world;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;

public class WorldHelper {
    public interface MixinLoadTest {
    }

    /**
     * [VanillaCopy] Method for getting entities by class but also exclude one entity
     */
    public static List<Entity> getEntitiesOfClass(World world, Entity except, Class<? extends Entity> entityClass, Box box) {
        world.getProfiler().visit("getEntities");

        int minChunkX = MathHelper.floor((box.minX - 2.0D) / 16.0D);
        int maxChunkX = MathHelper.ceil((box.maxX + 2.0D) / 16.0D);
        int minChunkZ = MathHelper.floor((box.minZ - 2.0D) / 16.0D);
        int maxChunkZ = MathHelper.ceil((box.maxZ + 2.0D) / 16.0D);

        List<Entity> entities = Lists.newArrayList();
        ChunkManager chunkManager = world.getChunkManager();

        for (int chunkX = minChunkX; chunkX < maxChunkX; ++chunkX) {
            for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; ++chunkZ) {
                WorldChunk chunk = chunkManager.getWorldChunk(chunkX, chunkZ, false);

                if (chunk != null) {
                    WorldHelper.getEntitiesOfClass(chunk, except, entityClass, box, entities);
                }
            }
        }

        return entities;
    }

    /**
     * [VanillaCopy] Method for getting entities by class but also exclude one entity
     */
    private static void getEntitiesOfClass(WorldChunk worldChunk, Entity excluded, Class<? extends Entity> entityClass, Box box, List<Entity> out) {
        TypeFilterableList<Entity>[] entitySections = worldChunk.getEntitySectionArray();
        int minChunkY = MathHelper.floor((box.minY - 2.0D) / 16.0D);
        int maxChunkY = MathHelper.floor((box.maxY + 2.0D) / 16.0D);
        minChunkY = MathHelper.clamp(minChunkY, 0, entitySections.length - 1);
        maxChunkY = MathHelper.clamp(maxChunkY, 0, entitySections.length - 1);

        for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
            for (Entity entity : entitySections[chunkY].getAllOfType(entityClass)) {
                if (entity != excluded && entity.getBoundingBox().intersects(box)) {
                    out.add(entity);
                }
            }
        }
    }

    public static boolean areNeighborsWithinSameChunk(BlockPos pos) {
        int localX = pos.getX() & 15;
        int localY = pos.getY() & 15;
        int localZ = pos.getZ() & 15;

        return localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15;
    }

    public static boolean areAllNeighborsOutOfBounds(BlockPos pos) {
        return pos.getY() < -1 || pos.getY() > 256;
    }
}
