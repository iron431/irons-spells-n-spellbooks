package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.network.OpenHeldBookPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReadableLoreItem extends Item implements ILecternPlaceable {
    private final ResourceLocation lecternLocation;

    public ReadableLoreItem(ResourceLocation lecternLocation, Properties pProperties) {
        super(pProperties);
        this.lecternLocation = lecternLocation;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        var itemstack = pPlayer.getItemInHand(pHand);
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            if (WrittenBookItem.resolveBookComponents(itemstack, serverPlayer.createCommandSourceStack(), serverPlayer)) {
                // not sure why vanilla broadcasts book resolution changes, but i will too
                serverPlayer.containerMenu.broadcastChanges();
            }
            PacketDistributor.sendToPlayer(serverPlayer, new OpenHeldBookPacket(pHand));
        }
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(pContext.getPlayer(), level, blockpos, blockstate, pContext.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public List<Component> getPages(ItemStack stack) {
        if (!stack.hasTag()) {
            return List.of();
        }
        var copy = stack.copy();
        WrittenBookItem.resolveBookComponents(copy, null, MinecraftInstanceHelper.getPlayer());
        List<Component> resolvedPages = new ArrayList<>();
        ListTag listtag = copy.getOrCreateTag().getList("pages", 8);
        for (int i = 0; i < listtag.size(); ++i) {
            resolvedPages.add(Component.Serializer.fromJson(listtag.getString(i)));
        }
        return resolvedPages;
    }

    @Override
    public Optional<ResourceLocation> simpleTextureOverride(ItemStack stack) {
        return Optional.of(lecternLocation);
    }
}
