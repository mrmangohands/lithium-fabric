package me.jellysquid.mods.lithium.mixin.gen.cached_generator_settings;

import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.chunk.SurfaceChunkGenerator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(SurfaceChunkGenerator.class)
public class SurfaceChunkGeneratorMixin {
    private int cachedSeaLevel;

    /**
     * Use cached sea level instead of retrieving from the registry every time.
     * This method is called for every block in the chunk so this will save a lot of registry lookups.
     *
     * @author SuperCoder79
     */
    @Overwrite
    public int getSeaLevel() {
        return this.cachedSeaLevel;
    }

    /**
     * Initialize the cache early in the ctor to avoid potential future problems with uninitialized usages
     */
    @Inject(
            method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;JLnet/minecraft/world/gen/chunk/ChunkGeneratorType;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/chunk/ChunkGeneratorType;method_28559()Lnet/minecraft/world/gen/chunk/NoiseConfig;",
                    shift = At.Shift.BEFORE
            )
    )
    private void hookConstructor(BiomeSource populationSource, BiomeSource biomeSource, long seed, ChunkGeneratorType settings, CallbackInfo ci) {
        this.cachedSeaLevel = settings.method_28561();
    }
}
