package io.redspace.ironsspellbooks.block.statue;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsspellbooks.network.gui.OpenStatuePoseScreenPacket;
import io.redspace.ironsspellbooks.patreon.PatreonHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerStatueBlock extends AbstractStatueBlock {

    public PlayerStatueBlock() {
        super(1, 2, 1);
    }

    /* ----------------------------------- *
     * Codec
     * -----------------------------------*/
    public static final MapCodec<PlayerStatueBlock> CODEC = simpleCodec((t) -> new PlayerStatueBlock());

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /* ----------------------------------- *
     * Block Entity Handling
     * -----------------------------------*/
    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PlayerStatueBlockEntity(pos, state);
    }

    /* ----------------------------------- *
     * Gameplay
     * -----------------------------------*/
    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (stack.is(Items.NAME_TAG) && stack.has(DataComponents.CUSTOM_NAME)) {
            String username = stack.get(DataComponents.CUSTOM_NAME).getString();
            if (level.getBlockEntity(pos) instanceof PlayerStatueBlockEntity statueBlockEntity &&
                    PatreonHandler.getPatreonPermissionsByUsername(username).supportsStatues()) {
                UUID uuid = PatreonHandler.profileFromUsername(username);
                // todo: ensure we cant set statue name to what it already is
                if (uuid != null) {
                    statueBlockEntity.setPlayerUuid(uuid);
                    statueBlockEntity.setChanged();
                    if (!player.hasInfiniteMaterials()) {
                        stack.shrink(1);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (player.isCrouching()) {
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new OpenStatuePoseScreenPacket(pos));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
