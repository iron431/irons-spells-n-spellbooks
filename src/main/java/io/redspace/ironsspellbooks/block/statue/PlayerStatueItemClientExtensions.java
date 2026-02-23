package io.redspace.ironsspellbooks.block.statue;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.util.MemoizedSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class PlayerStatueItemClientExtensions implements IClientItemExtensions {
    MemoizedSupplier<BlockEntityWithoutLevelRenderer> renderer = new MemoizedSupplier<>(() ->
            new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {
                @Override
                public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
                    Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(
                            PlayerStatueBlockEntity.renderable(stack.get(ComponentRegistry.STATUE_ITEM_DATA)),
                            poseStack, buffer, packedLight, packedOverlay
                    );
                }
            }
    );

    @Override
    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer.get();
    }
}
