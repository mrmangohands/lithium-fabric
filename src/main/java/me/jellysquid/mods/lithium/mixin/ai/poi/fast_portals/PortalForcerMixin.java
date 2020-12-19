package me.jellysquid.mods.lithium.mixin.ai.poi.fast_portals;

import me.jellysquid.mods.lithium.common.world.interests.PointOfInterestStorageExtended;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Shadow
    @Final
    private ServerWorld world;

    /**
     * @author JellySquid
     * @reason Use optimized search for nearby points, avoid slow filtering, check for valid locations first
     */
    @Nullable
    @Overwrite
    public BlockPattern.TeleportTarget getPortal(BlockPos centerPos, Vec3d vec3d, Direction direction, double x, double y, boolean canActivate) {
        int searchRadius = 128;

        PointOfInterestStorage poiStorage = this.world.getPointOfInterestStorage();
        poiStorage.preloadChunks(this.world, centerPos, searchRadius);

        Optional<BlockPos> ret = ((PointOfInterestStorageExtended) poiStorage).findNearestInSquare(centerPos, searchRadius,
                PointOfInterestType.NETHER_PORTAL, PointOfInterestStorage.OccupationStatus.ANY,
                (poi) -> this.world.getBlockState(poi.getPos()).contains(Properties.HORIZONTAL_AXIS)
        );

        return (BlockPattern.TeleportTarget)(ret.map((pos) -> {
            this.world.getChunkManager().addTicket(ChunkTicketType.field_19280, new ChunkPos(pos), 3, pos);

            BlockPattern.Result result = NetherPortalBlock.findPortal(this.world, pos);
            return result.getTeleportTarget(direction, pos, y, vec3d, x);
        }).orElse(null));
    }
}
