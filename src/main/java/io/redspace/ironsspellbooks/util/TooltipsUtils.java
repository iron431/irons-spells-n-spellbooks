package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.player.ClientInputEvents;
import io.redspace.ironsspellbooks.spells.eldritch.AbstractEldritchSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TooltipsUtils {


    public static List<Component> formatActiveSpellTooltip(ItemStack stack, SpellData spellData, CastSource castSource, @Nonnull LocalPlayer player) {
        var spell = spellData.getSpell();
        var title = getTitleComponent(spellData, player);
        var uniqueInfo = spell.getUniqueInfo(spellData.getLevel(), player);
        var manaCost = getManaCostComponent(spell.getCastType(), spell.getManaCost(spellData.getLevel(), player)).withStyle(ChatFormatting.BLUE);
        var cooldownTime = Component.translatable("tooltip.irons_spellbooks.cooldown_length_seconds", Utils.timeFromTicks(MagicManager.getEffectiveSpellCooldown(spell, player, castSource), 2)).withStyle(ChatFormatting.BLUE);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.empty());
        lines.add(title);
        uniqueInfo.forEach((line) -> lines.add(Component.literal(" ").append(line.withStyle(getStyleFor(player, spell)))));
        if (spell.getCastType() != CastType.INSTANT) {
            lines.add(Component.literal(" ").append(getCastTimeComponent(spell.getCastType(), Utils.timeFromTicks(spell.getEffectiveCastTime(spellData.getLevel(), player), 2)).withStyle(ChatFormatting.BLUE)));
        }
        if (castSource != CastSource.SWORD || ServerConfigs.SWORDS_CONSUME_MANA.get())
            lines.add(manaCost);
        if (castSource != CastSource.SWORD || ServerConfigs.SWORDS_CD_MULTIPLIER.get().floatValue() > 0)
            lines.add(cooldownTime);
        return lines;
    }



    public static List<Component> formatScrollTooltip(ItemStack stack, @Nonnull LocalPlayer player) {
        var spellData = SpellData.getSpellData(stack);

        if (spellData.equals(SpellData.EMPTY)) {
            return List.of();
        }

        var spell = spellData.getSpell();

        var levelText = getLevelComponenet(spellData, player);
        var title = Component.translatable("tooltip.irons_spellbooks.level", levelText).append(" ").append(Component.translatable("tooltip.irons_spellbooks.rarity", spell.getRarity(spellData.getLevel()).getDisplayName()).withStyle(spell.getRarity(spellData.getLevel()).getDisplayName().getStyle())).withStyle(ChatFormatting.GRAY);
        var uniqueInfo = spell.getUniqueInfo(spellData.getLevel(), player);
        var whenInSpellBook = Component.translatable("tooltip.irons_spellbooks.scroll_tooltip").withStyle(ChatFormatting.GRAY);
        var manaCost = getManaCostComponent(spell.getCastType(), spell.getManaCost(spellData.getLevel(), player)).withStyle(ChatFormatting.BLUE);
        var cooldownTime = Component.translatable("tooltip.irons_spellbooks.cooldown_length_seconds", Utils.timeFromTicks(MagicManager.getEffectiveSpellCooldown(spell, player, CastSource.SCROLL), 2)).withStyle(ChatFormatting.BLUE);
        MutableComponent castType = null;
        if (spell.getCastType() != CastType.INSTANT) {
            castType = (Component.literal(" ").append(getCastTimeComponent(spell.getCastType(), Utils.timeFromTicks(spell.getEffectiveCastTime(spellData.getLevel(), player), 2)).withStyle(ChatFormatting.BLUE)));
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(" ").append(title));
        uniqueInfo.forEach((line) -> lines.add(Component.literal(" ").append(line.withStyle(getStyleFor(player, spell)))));
        if (castType != null) {
            lines.add(castType);
        }
        lines.add(Component.empty());
        lines.add(whenInSpellBook);
        lines.add(manaCost);
        lines.add(cooldownTime);
        lines.add(spell.getSchoolType().getDisplayName().copy());

        return lines;
    }

    public static void addShiftTooltip(List<Component> currentTooltip, List<Component> tooltipToAdd) {
        if (ClientInputEvents.isShiftKeyDown) {
            currentTooltip.addAll(tooltipToAdd);
        } else {
            currentTooltip.add(Component.translatable("tooltip.irons_spellbooks.shift_tooltip").withStyle(ChatFormatting.GRAY));
        }
    }


    private static final Style INFO_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
    private static final Style OBFUSCATED_STYLE = AbstractEldritchSpell.ELDRITCH_OBFUSCATED_STYLE.applyTo(INFO_STYLE);

    public static MutableComponent getLevelComponenet(SpellData spellData, LivingEntity caster) {
        int levelTotal = spellData.getSpell().getLevel(spellData.getLevel(), caster);
        int diff = levelTotal - spellData.getLevel();
        if (diff > 0) {
            return Component.translatable("tooltip.irons_spellbooks.level_plus", levelTotal, diff);
        } else {
            return Component.literal(String.valueOf(levelTotal));
        }
    }

    public static MutableComponent getCastTimeComponent(CastType type, String castTime) {
        return switch (type) {
            case CONTINUOUS -> Component.translatable("tooltip.irons_spellbooks.cast_continuous", castTime);
            case LONG -> Component.translatable("tooltip.irons_spellbooks.cast_long", castTime);
            default -> Component.translatable("ui.irons_spellbooks.cast_instant");
        };
    }

//    public static MutableComponent getSpellNameComponent(AbstractSpell spell, Player caster) {
//        return spell.obfuscateStats(caster) ? Component.translatable("ui.irons_spellbooks.unlearned_spell") : spell.getDisplayName();
//    }

    public static MutableComponent getManaCostComponent(CastType castType, int manaCost) {
        if (castType == CastType.CONTINUOUS) {
            return Component.translatable("tooltip.irons_spellbooks.mana_cost_per_second", manaCost * (20 / MagicManager.CONTINUOUS_CAST_TICK_INTERVAL));
        } else {
            return Component.translatable("tooltip.irons_spellbooks.mana_cost", manaCost);
        }
    }

    public static MutableComponent getTitleComponent(SpellData spellData, @NotNull LocalPlayer player) {
        var levelText = getLevelComponenet(spellData, player);
        var spell = spellData.getSpell();
        return Component.translatable("tooltip.irons_spellbooks.selected_spell",
                spell.getDisplayName(player),
                levelText).withStyle(spell.getSchoolType().getDisplayName().getStyle());
    }

    public static List<FormattedCharSequence> createSpellDescriptionTooltip(AbstractSpell spell, Font font) {
        Player player = MinecraftInstanceHelper.instance.player();
        var name = spell.getDisplayName(player);
        var description = font.split(Component.translatable(String.format("%s.guide", spell.getComponentId())).withStyle(ChatFormatting.GRAY), 180);
        var hoverText = new ArrayList<FormattedCharSequence>();
        hoverText.add(FormattedCharSequence.forward(name.getString(), name.getStyle().withUnderlined(true)));
        if (!spell.obfuscateStats(player)) {
            hoverText.addAll(description);
        }
        return hoverText;
    }

    public static Style getStyleFor(Player player, AbstractSpell spell) {
        return spell.obfuscateStats(player) ? OBFUSCATED_STYLE : INFO_STYLE;
    }

//    private static void obfuscateStat(MutableComponent component) {
//        var style = Style.EMPTY.withObfuscated(true).withFont(RUNIC_FONT);
//        component.setStyle(style);
//    }
}
