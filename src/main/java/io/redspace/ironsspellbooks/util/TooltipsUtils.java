package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TooltipsUtils {


    public static List<Component> formatActiveSpellTooltip(ItemStack stack, CastSource castSource, @Nonnull LocalPlayer player) {
        //var player = Minecraft.getInstance().player;
        AbstractSpell spell = stack.getItem() instanceof SpellBook ? SpellBookData.getSpellBookData(stack).getActiveSpell() : SpellData.getSpellData(stack).getSpell(); //Put me in utils?
        SpellType spellType = spell.getSpellType();
//        var title = Component.translatable("tooltip.irons_spellbooks.selected_spell",
//                spellType.getDisplayName().withStyle(spellType.getSchoolType().getDisplayName().getStyle()),
//                Component.literal("" + spell.getLevel()).withStyle(spellType.getRarity(spell.getLevel()).getDisplayName().getStyle()));
//        var title = Component.translatable("tooltip.irons_spellbooks.selected_spell",
//                spellType.getDisplayName().withStyle(spellType.getSchoolType().getDisplayName().getStyle()),
//                Component.literal("" + spell.getLevel())).withStyle(spellType.getRarity(spell.getLevel()).getDisplayName().getStyle());
        var levelText = getLevelComponenet(spell, player);

        var title = Component.translatable("tooltip.irons_spellbooks.selected_spell",
                spellType.getDisplayName(),
                levelText).withStyle(spellType.getSchoolType().getDisplayName().getStyle());
        var uniqueInfo = spell.getUniqueInfo(player);
        var manaCost = getManaCostComponent(spell.getCastType(), spell.getManaCost()).withStyle(ChatFormatting.BLUE);
        var cooldownTime = Component.translatable("tooltip.irons_spellbooks.cooldown_length_seconds", Utils.timeFromTicks(MagicManager.getEffectiveSpellCooldown(spellType, player, castSource), 1)).withStyle(ChatFormatting.BLUE);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.empty());
        lines.add(title);
        uniqueInfo.forEach((line) -> lines.add(Component.literal(" ").append(line.withStyle(ChatFormatting.DARK_GREEN))));
        if (spell.getCastType() != CastType.INSTANT) {
            lines.add(Component.literal(" ").append(getCastTimeComponent(spell.getCastType(), Utils.timeFromTicks(spell.getEffectiveCastTime(player), 1)).withStyle(ChatFormatting.BLUE)));
        }
        if (castSource != CastSource.SWORD || ServerConfigs.SWORDS_CONSUME_MANA.get())
            lines.add(manaCost);
        lines.add(cooldownTime);
        return lines;
    }

    public static List<Component> formatScrollTooltip(ItemStack stack, @Nonnull LocalPlayer player) {
        AbstractSpell spell = SpellData.getSpellData(stack).getSpell();
        SpellType spellType = spell.getSpellType();
        if (spellType == SpellType.NONE_SPELL)
            return List.of();
        var levelText = getLevelComponenet(spell, player);
        var title = Component.translatable("tooltip.irons_spellbooks.level", levelText).append(" ").append(Component.translatable("tooltip.irons_spellbooks.rarity", spell.getRarity().getDisplayName()).withStyle(spell.getRarity().getDisplayName().getStyle())).withStyle(ChatFormatting.GRAY);
        var uniqueInfo = spell.getUniqueInfo(player);
        var whenInSpellBook = Component.translatable("tooltip.irons_spellbooks.scroll_tooltip").withStyle(ChatFormatting.GRAY);
        var manaCost = getManaCostComponent(spell.getCastType(), spell.getManaCost()).withStyle(ChatFormatting.BLUE);
        var cooldownTime = Component.translatable("tooltip.irons_spellbooks.cooldown_length_seconds", Utils.timeFromTicks(MagicManager.getEffectiveSpellCooldown(spellType, player, CastSource.SCROLL), 1)).withStyle(ChatFormatting.BLUE);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(" ").append(title));
        uniqueInfo.forEach((line) -> lines.add(Component.literal(" ").append(line.withStyle(ChatFormatting.DARK_GREEN))));
        if (spell.getCastType() != CastType.INSTANT) {
            lines.add(Component.literal(" ").append(getCastTimeComponent(spell.getCastType(), Utils.timeFromTicks(spell.getEffectiveCastTime(player), 1)).withStyle(ChatFormatting.BLUE)));
        }
        lines.add(Component.empty());
        lines.add(whenInSpellBook);
        lines.add(manaCost);
        lines.add(cooldownTime);
        lines.add(spell.getSchoolType().getDisplayName().copy());

        return lines;
    }

    public static MutableComponent getLevelComponenet(AbstractSpell spell, LivingEntity caster) {
        int levelTotal = spell.getLevel(caster);
        int rawLevel = spell.getRawLevel();
        int diff = levelTotal - rawLevel;
        if (diff > 0)
            return Component.translatable("tooltip.irons_spellbooks.level_plus", levelTotal, diff);
        else
            return Component.literal("" + levelTotal);
    }

    public static MutableComponent getCastTimeComponent(CastType type, String castTime) {
        return switch (type) {
            case CONTINUOUS -> Component.translatable("tooltip.irons_spellbooks.cast_continuous", castTime);
            case LONG -> Component.translatable("tooltip.irons_spellbooks.cast_long", castTime);
            case CHARGE -> Component.translatable("tooltip.irons_spellbooks.cast_charge", castTime);
            default -> Component.translatable("ui.irons_spellbooks.cast_instant");
        };
    }

    public static MutableComponent getManaCostComponent(CastType castType, int manaCost) {
        if (castType == CastType.CONTINUOUS) {
            return Component.translatable("tooltip.irons_spellbooks.mana_cost_per_second", manaCost * (20 / MagicManager.CONTINUOUS_CAST_TICK_INTERVAL));
        } else {
            return Component.translatable("tooltip.irons_spellbooks.mana_cost", manaCost);
        }
    }
}
