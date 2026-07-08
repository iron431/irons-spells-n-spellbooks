package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.data.SkillContainer;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
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
        this(properties.component(SkillcastingDataComponents.SKILL_CONTAINER, SkillContainer.create(true, maxSpellSlots)));
    }

    public SpellBook(Item.Properties properties){
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
        // fixme: tooltips
//        if (this.isUnique()) {
//            lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_rarity", Component.translatable("tooltip.irons_spellbooks.spellbook_unique").withStyle(TooltipsUtils.UNIQUE_STYLE)).withStyle(ChatFormatting.GRAY));
//        }
//        var player = MinecraftInstanceHelper.getPlayer();
//        if (player != null && ISpellContainer.isSpellContainer(itemStack)) {
//            var spellList = ISpellContainer.get(itemStack);
//            lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_spell_count", spellList.getMaxSpellCount()).withStyle(ChatFormatting.GRAY));
//            var activeSpellSlots = spellList.getActiveSpells();
//            if (!activeSpellSlots.isEmpty()) {
//                lines.add(Component.empty());
//                lines.add(Component.translatable("tooltip.irons_spellbooks.press_to_cast", Component.keybind("key.irons_spellbooks.spellbook_cast")).withStyle(ChatFormatting.GOLD));
//                lines.add(Component.empty());
//                lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_tooltip").withStyle(ChatFormatting.GRAY));
//                SkillSelectionManager spellSelectionManager = SkillcastingData.get(player).selectionManager();
//                for (int i = 0; i < activeSpellSlots.size(); i++) {
//                    var spellText = TooltipsUtils.getTitleComponent(activeSpellSlots.get(i).spellData(), (LocalPlayer) player).setStyle(Style.EMPTY);
//                    var option = spellSelectionManager.getOptionAt(spellSelectionManager.getSelectionIndex());
//                    if ((MinecraftInstanceHelper.getPlayer() != null &&
//                            Utils.getPlayerSpellbookStack(MinecraftInstanceHelper.getPlayer()) == itemStack) &&
//                            option != null &&
//                            option.equipmentSlot.equals(Curios.SPELLBOOK_SLOT) &&
//                            option.localIndex == i) {
//                        // fixme: ensure skillcasting stuff works
//                        var shiftMessage = TooltipsUtils.formatActiveSpellTooltip(itemStack, spellSelectionManager.getSelectedSkillData(), CastSource.SPELLBOOK, (LocalPlayer) player);
//                        shiftMessage.remove(0); // remove buffering empty line
//                        TooltipsUtils.addShiftTooltip(
//                                lines,
//                                Component.literal("> ").append(spellText).withStyle(ChatFormatting.YELLOW),
//                                shiftMessage.stream().map(component -> Component.literal(" ").append(component)).collect(Collectors.toList())
//                        );
//                    } else {
//                        lines.add(Component.literal(" ").append(spellText.withStyle(Style.EMPTY.withColor(0x8888fe))));
//                    }
//                }
//            }
//        }
        super.appendHoverText(itemStack, context, lines, flag);
    }

    @NotNull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundRegistry.EQUIP_SPELL_BOOK.get(), 1.0f, 1.0f);
    }

//    @Override
//    public void initializeSpellContainer(ItemStack itemStack) {
//        if (itemStack == null) {
//            return;
//        }
//
//        if (!ISpellContainer.isSpellContainer(itemStack)) {
//            ISpellContainer.set(itemStack, ISpellContainer.create(getMaxSpellSlots(), true, true));
//        }
//    }

    @Override
    public List<Component> getPages(ItemStack stack) {
        // fixme: bleh
//        var spellbookData = ISpellContainer.get(stack);
//        if (spellbookData != null && !spellbookData.isEmpty()) {
//            var player = MinecraftInstanceHelper.getPlayer();
//            return spellbookData.getActiveSpells().stream().map(slot -> {
//                var color = slot.getSpell().getSchoolType().getDisplayName().getStyle().getColor().getValue();
//                color = RenderHelper.colorLerp(.6f, color, 0);
//                var titleStyle = Style.EMPTY.withColor(color).withUnderlined(true).withBold(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/iron431"));
//                boolean hideStats = false;
//                if (player != null) {
//                    var scrollTooltip = TooltipsUtils.formatActiveSpellTooltip(null, slot.spellData(), CastSource.SPELLBOOK, (LocalPlayer) player);
//                    scrollTooltip.remove(0); // this is a space for tooltip, which we don't want
//                    titleStyle = titleStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, scrollTooltip.stream().reduce((a, b) -> a.append("\n").append(b)).get()));
//                    if (slot.getSpell().obfuscateStats(player)) {
//                        hideStats = true;
//                    }
//                }
//                var title = Component.translatable(slot.getSpell().getComponentId()).withStyle(titleStyle);
//                var desc = Component.translatable(slot.getSpell().getComponentId() + ".guide").withStyle(ChatFormatting.BLACK);
//                var page = Component.literal("").append(title).append("\n\n").append(desc);
//                if (hideStats) {
//                    page = page.withStyle(page.getStyle().applyTo(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("alt"))));
//                }
//                return (Component) page;
//            }).toList();
//        }
        return List.of(Component.translatable("ui.irons_spellbooks.empty_spellbook_lectern").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}
