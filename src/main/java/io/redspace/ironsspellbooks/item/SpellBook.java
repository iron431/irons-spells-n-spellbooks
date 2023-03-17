package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.player.ClientPlayerEvents;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.SpellRarity;
import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellBook extends Item {
    protected final SpellRarity rarity;
    protected final int spellSlots;

    public SpellBook() {
        this(1, SpellRarity.COMMON);
    }

    public SpellBook(int spellSlots, SpellRarity rarity) {
        this(spellSlots, rarity, new Item.Properties().stacksTo(1).tab(SpellbookModCreativeTabs.SPELL_EQUIPMENT_TAB).rarity(Rarity.UNCOMMON));
    }

    public SpellBook(int spellSlots, SpellRarity rarity, Item.Properties pProperties) {
        super(pProperties);
        this.spellSlots = spellSlots;
        this.rarity = rarity;
    }

    public SpellRarity getRarity() {
        return rarity;
    }

    public int getSpellSlots() {
        return spellSlots;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        var spellBookData = SpellBookData.getSpellBookData(itemStack);
        AbstractSpell spell = spellBookData.getActiveSpell();

        if (level.isClientSide()) {
            if (ClientMagicData.isCasting()) {
                return InteractionResultHolder.fail(itemStack);
            } else if (ClientMagicData.getPlayerMana() < spell.getManaCost()
                    || ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType())) {
                return InteractionResultHolder.pass(itemStack);
            } else {
                spell.onClientPreCast(level, player, hand, null);
                if (spell.getCastType().holdToCast()) {
                    player.startUsingItem(hand);
                }
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
        }

        if (spell.attemptInitiateCast(itemStack, level, player, CastSource.SPELLBOOK, true)) {
            if (spell.getCastType().holdToCast()) {
                player.startUsingItem(hand);
            }
            return InteractionResultHolder.success(itemStack);
        } else {
            return InteractionResultHolder.fail(itemStack);
        }
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 7200;//return getSpellBookData(itemStack).getActiveSpell().getCastTime();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level p_41413_, LivingEntity entity, int p_41415_) {
        entity.stopUsingItem();
        Utils.releaseUsingHelper(entity);
        super.releaseUsing(itemStack, p_41413_, entity, p_41415_);
    }

    public boolean isUnique() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        if (!this.isUnique()) {
            lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_rarity", this.rarity.getDisplayName()).withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_rarity", Component.translatable("tooltip.irons_spellbooks.spellbook_unique").withStyle(Style.EMPTY.withColor(0xe04324))).withStyle(ChatFormatting.GRAY));
        }

        if (SpellBookData.getSpellBookData(itemStack).getActiveSpell().getID() > 0) {
            lines.addAll(ClientPlayerEvents.formatActiveSpellTooltip(itemStack, Minecraft.getInstance().player, CastSource.SPELLBOOK));
        }

        super.appendHoverText(itemStack, level, lines, flag);
    }


}
