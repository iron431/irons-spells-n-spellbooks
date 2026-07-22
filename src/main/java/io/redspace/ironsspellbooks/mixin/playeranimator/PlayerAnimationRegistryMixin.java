package io.redspace.ironsspellbooks.mixin.playeranimator;

import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import dev.kosmx.playerAnim.minecraftApi.codec.AnimationCodecs;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

@Mixin(PlayerAnimationRegistry.class)
public class PlayerAnimationRegistryMixin {
    @Shadow
    private static final HashMap<ResourceLocation, IPlayable> animations = new HashMap<>();

    @Inject(method = "resourceLoaderCallback", at = @At("TAIL"))
    private static void readGeckoFile(ResourceManager manager, CallbackInfo ci) {
        // todo: exposing this as api could be nice
        ResourceLocation geoAnimationLocation = SpellAnimations.MOB_ANIMATION_RESOURCE;
        Optional<Resource> resourceOpt = manager.getResource(geoAnimationLocation);
        if (resourceOpt.isEmpty()) {
            return;
        }
        Resource resource = resourceOpt.get();
        var extension = AnimationCodecs.getExtension(geoAnimationLocation.getPath());
        if (extension == null) {
            return;
        }
        var a = AnimationCodecs.deserialize(extension, () -> {
            try {
                return resource.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        for (var animation : a) {
            animations.put(ResourceLocation.fromNamespaceAndPath(geoAnimationLocation.getNamespace(), PlayerAnimationRegistry.serializeTextToString(animation.getName())), animation);
        }
    }
}
