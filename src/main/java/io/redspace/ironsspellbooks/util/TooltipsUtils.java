package io.redspace.ironsspellbooks.util;

import io.redspace.ironslib.internal.client.ClientInputEvents;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.events.CustomizeScrollModNameEvent;
import io.redspace.ironsspellbooks.api.spells.SpellCastSources;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.CastSource;
import io.redspace.skillcasting.data.SkillData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TooltipsUtils {

    public static final Style UNIQUE_STYLE = Style.EMPTY.withColor(0xe04324);

    public static int indexOfComponent(List<Component> lines, String key) {
        return indexOfInternal(lines, key::equals);
    }

    public static int indexOfComponentRegex(List<Component> lines, String regex) {
        //IronsSpellbooks.LOGGER.debug("TooltipsUtils.indexOfComponentRegex: {}", regex);
        return indexOfInternal(lines, (string -> string.matches(regex)));
    }

    public static int indexOfAdvancedText(List<Component> lines, ItemStack itemStack) {
        return indexOfComponentRegex(lines, "item.durability|item.components|" + BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
    }

    private static int indexOfInternal(List<Component> lines, Predicate<String> comparator) {
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            var component = lines.get(i);
            if (component.getContents() instanceof TranslatableContents translatableContents) {
                //IronsSpellbooks.LOGGER.debug("TooltipsUtils.indexOfInternal {}: {}: {}", i, translatableContents.getKey(), comparator.test(translatableContents.getKey()));
                if (comparator.test(translatableContents.getKey())) {
                    return i;
                }
            } else if (component.getContents() instanceof PlainTextContents.LiteralContents literalContents) {
                //IronsSpellbooks.LOGGER.debug("TooltipsUtils.indexOfInternal {}: {}: {}", i, literalContents.text(), comparator.test(literalContents.text()));
                if (comparator.test(literalContents.text())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static List<MutableComponent> formatActiveSpellTooltip(ItemStack stack, SkillData spellData, CastSource castSource, @Nonnull LocalPlayer player) {
        if (!(spellData.getSkill() instanceof AbstractSpell spell)) {
            return new ArrayList<>();
        }
        // todo: move tooltip generation logic and helpers to expose as skillcasting helpers
        var context = SkillcastingManager.buildCastContext(CasterRef.entity(player), spell.holder(), spellData.getLevel(), castSource);
        var title = getTitleComponent(spellData, player, context);
        var uniqueInfo = spell.getUniqueInfo(context);
        int manaCost = context.getOrDefault(SpellcastingComponentTypes.MANA_COST, 0);
        int cooldownTicks = context.getOrDefault(SkillcastingComponentTypes.COOLDOWN_TICKS, 0);
        int castTimeTicks = context.getOrDefault(SkillcastingComponentTypes.CAST_TIME, spell.getCastTimeTicks());
        var manaCostComponent = getManaCostComponent(spell.getCastType(), manaCost).withStyle(ChatFormatting.BLUE);
        var cooldownTime = Component.translatable("tooltip.irons_spellbooks.cooldown_length_seconds", Utils.timeFromTicks(cooldownTicks, 2)).withStyle(ChatFormatting.BLUE);

        List<MutableComponent> lines = new ArrayList<>();
        lines.add(Component.empty());
        lines.add(title);
        uniqueInfo.forEach((line) -> lines.add(Component.literal(" ").append(line.withStyle(getStyleFor(player, spell)))));
        if (spell.getCastType() != CastType.INSTANT) {
            lines.add(Component.literal(" ").append(getCastTimeComponent(spell.getCastType(), Utils.timeFromTicks(castTimeTicks, 2)).withStyle(ChatFormatting.BLUE)));
        }
        if (manaCost > 0) {
            lines.add(manaCostComponent);
        }
        if (cooldownTicks > 0) {
            lines.add(cooldownTime);
        }
        return lines;
    }

    public static List<Component> formatScrollTooltip(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof Scroll)) {
            return List.of();
        }
        SkillData spellData = Scroll.getSpellSlotFromStack(stack);
        if (spellData == null || !(spellData.getSkill() instanceof AbstractSpell spell)) {
            return List.of();
        }
        CastContext context = SkillcastingManager.buildCastContext(CasterRef.entity(player), spell.holder(), spellData.getLevel(), CastSource.of(SpellCastSources.SCROLL, ""));
        int manaCost = context.getOrDefault(SpellcastingComponentTypes.MANA_COST, 0);
        int cooldownTicks = context.getOrDefault(SkillcastingComponentTypes.COOLDOWN_TICKS, 0);
        int castTimeTicks = context.getOrDefault(SkillcastingComponentTypes.CAST_TIME, spell.getCastTimeTicks());

        MutableComponent levelText = getLevelComponenet(spellData, player, context);
        MutableComponent title = Component.translatable("tooltip.irons_spellbooks.level", levelText)
                .append(" ")
                .append(Component.translatable("tooltip.irons_spellbooks.rarity", spell.getRarity(spellData.getLevel()).getDisplayName()).withStyle(spell.getRarity(spellData.getLevel()).getDisplayName().getStyle()))
                .withStyle(ChatFormatting.GRAY);
        List<MutableComponent> uniqueInfo = spell.getUniqueInfo(context);
        MutableComponent whenInSpellBook = Component.translatable("tooltip.irons_spellbooks.scroll_tooltip").withStyle(ChatFormatting.GRAY);
        MutableComponent manaCostComponent = getManaCostComponent(spell.getCastType(), manaCost).withStyle(ChatFormatting.BLUE);
        MutableComponent cooldownTime = Component.translatable("tooltip.irons_spellbooks.cooldown_length_seconds", Utils.timeFromTicks(context.getOrDefault(SkillcastingComponentTypes.COOLDOWN_TICKS, 0), 2)).withStyle(ChatFormatting.BLUE);
        MutableComponent castType = null;
        if (spell.getCastType() != CastType.INSTANT) {
            castType = (Component.literal(" ").append(getCastTimeComponent(spell.getCastType(), Utils.timeFromTicks(castTimeTicks, 2)).withStyle(ChatFormatting.BLUE)));
        }
        List<Component> lines = new ArrayList<>();
        String parentModId = spell.getSkillId().getNamespace();
        if (!parentModId.equals(IronsSpellbooks.MODID)) {
            CustomizeScrollModNameEvent.resolveModLabel(parentModId).ifPresent(lines::add);
        }
        lines.add(Component.literal(" ").append(title));
        uniqueInfo.forEach((line) -> lines.add(Component.literal(" ").append(line.withStyle(line.getStyle().applyTo(getStyleFor(player, spell))))));
        if (castType != null) {
            lines.add(castType);
        }

        lines.add(Component.empty());
        lines.add(whenInSpellBook);
        if (manaCost > 0) {
            lines.add(manaCostComponent);
        }
        if (spell.getCooldownTicks() > 0) {
            lines.add(cooldownTime);
        }
        lines.add(spell.getSchoolType().getDisplayName().copy());

        return lines;
    }

    public static void addShiftTooltip(List<Component> currentTooltip, List<Component> tooltipToAdd) {
        addShiftTooltip(currentTooltip, Component.translatable("tooltip.irons_spellbooks.shift_tooltip").withStyle(ChatFormatting.GRAY), tooltipToAdd);
    }

    public static void addShiftTooltip(List<Component> currentTooltip, Component shiftHeader, List<Component> tooltipToAdd) {
        if (ClientInputEvents.isShowExpandedTooltip()) {
            currentTooltip.addAll(tooltipToAdd);
        } else {
            currentTooltip.add(shiftHeader);
        }
    }

    private static final Style INFO_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
    private static final Style OBFUSCATED_STYLE = AbstractSpell.ELDRITCH_OBFUSCATED_STYLE.applyTo(INFO_STYLE);

    public static MutableComponent getLevelComponenet(SkillData spellData, LivingEntity caster, CastContext context) {
        int baseLevel = spellData.getLevel();
        int levelTotal = context.getSkillLevel();
        int diff = levelTotal - baseLevel;
        if (diff > 0) {
            return Component.translatable("tooltip.irons_spellbooks.level_plus", levelTotal, diff);
        } else if (diff < 0) {
            return Component.translatable("tooltip.irons_spellbooks.level_minus", levelTotal, diff);
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

    @Deprecated
    public static MutableComponent getManaCostComponent(CastType castType, int manaCost) {
        if (castType == CastType.CONTINUOUS) {
            return Component.translatable("tooltip.irons_spellbooks.mana_cost_per_second", manaCost * (20 / MagicManager.CONTINUOUS_CAST_TICK_INTERVAL));
        } else {
            return Component.translatable("tooltip.irons_spellbooks.mana_cost", manaCost);
        }
    }

    public static MutableComponent getTitleComponent(SkillData spellData, @NotNull LocalPlayer player, CastContext context) {
        var levelText = getLevelComponenet(spellData, player, context);
        var skill = spellData.getSkill();
        Style schoolStyle = skill instanceof AbstractSpell spell ? spell.getSchoolType().getDisplayName().getStyle() : Style.EMPTY;
        return Component.translatable("tooltip.irons_spellbooks.selected_spell",
                skill.getDisplayName(player),
                levelText).withStyle(schoolStyle);
    }

    public static List<FormattedCharSequence> createSpellDescriptionTooltip(AbstractSpell spell, Font font) {
        Player player = MinecraftInstanceHelper.instance.player();
        var name = spell.getDisplayName(player);
        var description = font.split(Component.translatable(String.format("%s.guide", spell.getDescriptionId())).withStyle(ChatFormatting.GRAY), 180);
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
