package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.gui.EldritchResearchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EldritchManuscript extends Item {
    public EldritchManuscript(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pUsedHand) {
        if (level.isClientSide && player == Minecraft.getInstance().player) {
            Minecraft.getInstance().setScreen(new EldritchResearchScreen(Component.empty()));
        }
        //if (player instanceof ServerPlayer serverPlayer) {
        //    MagicData.getPlayerMagicData(serverPlayer).getSyncedData().learnSpell(SpellRegistry.SONIC_BOOM_SPELL.get());
        //    player.getItemInHand(pUsedHand).shrink(1);
        //}
        return super.use(level, player, pUsedHand);
    }
}
