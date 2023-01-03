package com.example.testmod.item;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.MagicManager;
import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.capabilities.magic.network.PacketCancelCast;
import com.example.testmod.capabilities.magic.network.PacketCastingState;
import com.example.testmod.capabilities.scroll.data.ScrollData;
import com.example.testmod.capabilities.scroll.data.ScrollDataProvider;
import com.example.testmod.player.ClientMagicData;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Scroll extends Item {

    protected SpellType spellType;
    protected int level = 0;

    public Scroll() {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
        setSpellType(SpellType.NONE_SPELL);
    }

    public Scroll(SpellType spellType, int level) {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT).rarity(Rarity.UNCOMMON));
        setSpellType(spellType);
        this.level = level;
    }


    public void setSpellType(SpellType spellType) {
        this.spellType = spellType;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    protected void removeScrollAfterCast(ServerPlayer player, ItemStack stack) {
        if (!player.isCreative())
            player.getInventory().removeItem(stack);
    }

    public static boolean attemptRemoveScrollAfterCast(ServerPlayer serverPlayer) {
        ItemStack potentialScroll = PlayerMagicData.getPlayerMagicData(serverPlayer).getPlayerCastingItem();
        if (potentialScroll.getItem() instanceof Scroll scroll) {
            scroll.removeScrollAfterCast(serverPlayer, potentialScroll);
            return true;
        } else
            return false;
    }

    public ScrollData getScrollData(ItemStack stack) {
        return stack.getCapability(ScrollDataProvider.SCROLL_DATA).resolve().get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        var scrollData = getScrollData(stack);
        var spell = scrollData.getSpell();

        if (level.isClientSide) {
            if (ClientMagicData.isCasting) {
                Messages.sendToServer(new PacketCancelCast(false));

            }
            if (spell.getCastType() == CastType.CONTINUOUS) {
                player.startUsingItem(hand);
            }
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        var serverPlayer = Utils.getServerPlayer(level, player.getUUID());
        if (serverPlayer != null) {
            var playerMagicData = MagicManager.get(level).getPlayerMagicData(serverPlayer);


            if (playerMagicData.isCasting()) {
                if (spell.getCastType() == CastType.CONTINUOUS)
                    removeScrollAfterCast(serverPlayer, stack);
                return InteractionResultHolder.success(stack);
            }

            if (spell.getCastType() == CastType.INSTANT) {
                spell.castSpell(level, serverPlayer, false, false);
                removeScrollAfterCast(serverPlayer, stack);
            } else if (spell.getCastTime() > 0) {
                playerMagicData.initiateCast(spell.getID(), spell.getLevel(), spell.getCastTime());
                Messages.sendToPlayer(new PacketCastingState(spell.getID(), spell.getCastTime(), spell.getCastType(), false), serverPlayer);

            }


            return InteractionResultHolder.success(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }

    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 7200;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_41452_) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level p_41413_, LivingEntity entity, int ticksUsed) {
        entity.stopUsingItem();
        if (getUseDuration(itemStack) - ticksUsed >= 10)
            Messages.sendToServer(new PacketCancelCast(true));
        super.releaseUsing(itemStack, p_41413_, entity, ticksUsed);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        var scrollData = getScrollData(itemStack);
        return scrollData.getDisplayName();

    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.addAll(getScrollData(itemStack).getHoverText());
        super.appendHoverText(itemStack, level, lines, flag);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null) {
            var scrollDataProvider = new ScrollDataProvider(nbt);
            this.spellType = scrollDataProvider.getSpellType();
            this.level = scrollDataProvider.getLevel();
            return scrollDataProvider;
        } else {
            return new ScrollDataProvider(this.spellType, this.level);
        }
    }
}
