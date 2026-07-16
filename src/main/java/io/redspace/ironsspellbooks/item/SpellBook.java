package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellCastSources;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import io.redspace.ironsspellbooks.item.spell_containers.SpellbookContainer;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.render.RenderHelper;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.skillcasting.data.cast.CastSource;
import io.redspace.skillcasting.data.skill.ISkillContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;

public class SpellBook extends CurioBaseItem implements /*IPresetSpellContainer,*/ ILecternPlaceable {

    public SpellBook(int maxSpellSlots, Item.Properties properties) {
        this(properties.component(ComponentRegistry.SPELLBOOK_CONTAINER, ISkillContainer.create(true, maxSpellSlots)));
    }

    public SpellBook(Item.Properties properties) {
        super(properties);
    }

    public SpellBook withAttribute(Holder<Attribute> attribute, double value) {
        return (SpellBook) withAttributes(Curios.SPELLBOOK_SLOT, new AttributeContainer(attribute, value, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    public boolean isUnique() {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, Item.TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        if (this.isUnique()) {
            lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_rarity", Component.translatable("tooltip.irons_spellbooks.spellbook_unique").withStyle(TooltipsUtils.UNIQUE_STYLE)).withStyle(ChatFormatting.GRAY));
        }
        var player = MinecraftInstanceHelper.getPlayer();
        var spellList = SpellbookContainer.get(itemStack);
        if (player != null && spellList != null) {
            lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_spell_count", spellList.getMaxSkillCount()).withStyle(ChatFormatting.GRAY));
            var activeSpellSlots = spellList.getActiveSkills();
            if (!activeSpellSlots.isEmpty()) {
                lines.add(Component.empty());
                lines.add(Component.translatable("tooltip.irons_spellbooks.press_to_cast", Component.keybind("key.irons_spellbooks.spellbook_cast")).withStyle(ChatFormatting.GOLD));
                lines.add(Component.empty());
                lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_tooltip").withStyle(ChatFormatting.GRAY));
                var source = CastSource.of(SpellCastSources.SPELLBOOK);
                lines.addAll(TooltipsUtils.createSpellAccordion(itemStack, source, player, activeSpellSlots));
            }
        }
        super.appendHoverText(itemStack, context, lines, flag);
    }

    @NotNull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundRegistry.EQUIP_SPELL_BOOK.get(), 1.0f, 1.0f);
    }

    @Override
    public List<Component> getPages(ItemStack stack) {
        var spellbookData = SpellbookContainer.get(stack);
        if (spellbookData != null && !spellbookData.isEmpty()) {
            var player = MinecraftInstanceHelper.getPlayer();
            return spellbookData.getActiveSkills().stream()
                    .filter(slot -> slot.getSkill() instanceof AbstractSpell)
                    .map(slot -> {
                        var spell = (AbstractSpell) slot.getSkill();
                        var color = spell.getSchoolType().getDisplayName().getStyle().getColor().getValue();
                        color = RenderHelper.colorLerp(.6f, color, 0);
                        var titleStyle = Style.EMPTY.withColor(color).withUnderlined(true).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/iron431"));
                        boolean hideStats = false;
                        if (player != null) {
                            var scrollTooltip = TooltipsUtils.formatActiveSpellTooltip(null, slot.skillData(), CastSource.of(SpellCastSources.SPELLBOOK), (LocalPlayer) player);
                            scrollTooltip.remove(0); // this is a space for tooltip, which we don't want
                            titleStyle = titleStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, scrollTooltip.stream().reduce((a, b) -> a.append("\n").append(b)).get()));
                            if (spell.obfuscateStats(player)) {
                                hideStats = true;
                            }
                        }
                        var title = Component.translatable(spell.getDescriptionId()).withStyle(titleStyle);
                        var desc = Component.translatable(spell.getDescriptionId() + ".guide").withStyle(ChatFormatting.BLACK);
                        var page = Component.literal("").append(title).append("\n\n").append(desc);
                        if (hideStats) {
                            page = page.withStyle(page.getStyle().applyTo(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"))));
                        }
                        return (Component) page;
                    }).toList();
        }
        return List.of(Component.translatable("ui.irons_spellbooks.empty_spellbook_lectern").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
