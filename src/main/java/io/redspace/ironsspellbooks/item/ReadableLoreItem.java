package io.redspace.ironsspellbooks.item;

import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;

public class ReadableLoreItem extends Item {
    public ReadableLoreItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        var itemstack = pPlayer.getItemInHand(pHand);
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            if (WrittenBookItem.resolveBookComponents(itemstack, serverPlayer.createCommandSourceStack(), serverPlayer)) {
                // not sure why vanilla broadcasts book resolution changes, but i will too
                serverPlayer.containerMenu.broadcastChanges();
            }

            serverPlayer.connection.send(new ClientboundOpenBookPacket(pHand));
        }
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

}
