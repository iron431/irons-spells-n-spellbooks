package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.util.SpellbookModCreativeTabs;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellBook extends Item implements ISpellbook {
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
        SpellData spellData = spellBookData.getActiveSpell();

        if (spellData.equals(SpellData.EMPTY)) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (level.isClientSide()) {
            if (ClientMagicData.isCasting()) {
                return InteractionResultHolder.fail(itemStack);
            } else if (ClientMagicData.getPlayerMana() < spellData.getSpell().getManaCost(spellData.getLevel(), player)
                    || ClientMagicData.getCooldowns().isOnCooldown(spellData.getSpell())) {
                return InteractionResultHolder.pass(itemStack);
            } else {
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
        }

        if (spellData.getSpell().attemptInitiateCast(itemStack, spellData.getLevel(), level, player, CastSource.SPELLBOOK, true)) {
            if (spellData.getSpell().getCastType().holdToCast()) {
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
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level p_41413_, LivingEntity entity, int p_41415_) {
        IronsSpellbooks.LOGGER.debug("Spellbook Release Using ticks used: {}", p_41415_);
        entity.stopUsingItem();
        Utils.releaseUsingHelper(entity, itemStack, p_41415_);
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
        lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_spell_count", this.spellSlots).withStyle(ChatFormatting.GRAY));

        var player = Minecraft.getInstance().player;
        if (player != null && !SpellBookData.getSpellBookData(itemStack).getActiveSpell().equals(SpellData.EMPTY)) {
            lines.addAll(TooltipsUtils.formatActiveSpellTooltip(itemStack, CastSource.SPELLBOOK, player));
        }

        super.appendHoverText(itemStack, level, lines, flag);
    }
}
