package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EldritchManuscript extends Item {
    public EldritchManuscript(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            MagicData.getPlayerMagicData(serverPlayer).getSyncedData().learnSpell(SpellRegistry.SONIC_BOOM_SPELL.get());
            pPlayer.getItemInHand(pUsedHand).shrink(1);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
