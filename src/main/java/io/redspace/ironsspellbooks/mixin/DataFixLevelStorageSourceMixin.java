package io.redspace.ironsspellbooks.mixin;

import com.mojang.datafixers.DataFixer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.datafix.IronsTagTraverser;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

@Mixin(LevelStorageSource.class)
public abstract class DataFixLevelStorageSourceMixin {
    @Unique
    private static final Object iron_sSpells_nSpellbooks$sync = new Object();

    @Inject(method = "readLevelData", at = @At("HEAD"))
    private void readLevelData(LevelStorageSource.LevelDirectory pLevelDirectory, BiFunction<Path, DataFixer, Object> pLevelDatReader, CallbackInfoReturnable<Object> cir) {
        if (Files.exists(pLevelDirectory.path())) {
            Path path = pLevelDirectory.dataFile();
            try {
                synchronized (iron_sSpells_nSpellbooks$sync) {
                    var compoundTag1 = NbtIo.readCompressed(path.toFile());
                    var compoundTag2 = compoundTag1.getCompound("Data");
                    var compoundTag3 = compoundTag2.getCompound("Player");

                    var ironsTraverser = new IronsTagTraverser();
                    ironsTraverser.visit(compoundTag3);

                    if (ironsTraverser.changesMade()) {
                        NbtIo.writeCompressed(compoundTag1, path.toFile());
                        IronsSpellbooks.LOGGER.debug("DataFixLevelStorageSourceMixin: Single player inventory updated: {} updates", ironsTraverser.totalChanges());
                    }
                }
            } catch (Exception exception) {
                IronsSpellbooks.LOGGER.warn("DataFixLevelStorageSourceMixin failed to load {}, {}", path, exception.getMessage());
            }
        }
    }
}