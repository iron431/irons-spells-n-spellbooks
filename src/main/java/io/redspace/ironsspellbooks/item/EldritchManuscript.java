package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.network.ClientboundOpenEldritchScreen;
import io.redspace.ironsspellbooks.setup.Messages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EldritchManuscript extends Item {
    public EldritchManuscript(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pUsedHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            Messages.sendToPlayer(new ClientboundOpenEldritchScreen(pUsedHand), serverPlayer);
        }
        return super.use(level, player, pUsedHand);
    }
}
