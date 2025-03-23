package io.redspace.ironsspellbooks.fluids;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;

public class SimpleClientFluidType implements IClientFluidTypeExtensions {

    private final ResourceLocation texture;

    public SimpleClientFluidType(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    public @NotNull ResourceLocation getStillTexture() {
        return texture;
    }

    @Override
    public @NotNull ResourceLocation getFlowingTexture() {
        return texture;
    }

}
