package io.redspace.ironsspellbooks.item;

import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelectionManager;
import io.redspace.ironsspellbooks.item.curios.CurioBaseItem;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.List;
import java.util.stream.Collectors;

public class SpellBook extends CurioBaseItem implements ISpellbook, IContainSpells {
    protected final SpellRarity rarity;
    protected final int maxSpellSlots;

    public SpellBook() {
        this(1, SpellRarity.COMMON);
    }

    public SpellBook(int maxSpellSlots, SpellRarity rarity) {
        this(maxSpellSlots, rarity, ItemPropertiesHelper.equipment().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public SpellBook(int maxSpellSlots, SpellRarity rarity, Item.Properties pProperties) {
        super(pProperties);
        this.maxSpellSlots = maxSpellSlots;
        this.rarity = rarity;
    }

    public SpellRarity getRarity() {
        return rarity;
    }

    public int getMaxSpellSlots() {
        return maxSpellSlots;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pLevel.isClientSide()) {
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("ui.irons_spellbooks.spellbook_cast_error").withStyle(ChatFormatting.RED), false);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    //    @Override
//    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
//        ItemStack itemStack = player.getItemInHand(hand);
//        var spellBookData = SpellBookData.getSpellBookData(itemStack);
//        SpellData spellData = spellBookData.getActiveSpell();
//
//        if (spellData.equals(SpellData.EMPTY)) {
//            return InteractionResultHolder.pass(itemStack);
//        }
//
//        if (level.isClientSide()) {
//            if (ClientMagicData.isCasting()) {
//                return InteractionResultHolder.fail(itemStack);
//            } else if (ClientMagicData.getPlayerMana() < spellData.getSpell().getManaCost(spellData.getLevel(), player)
//                    || ClientMagicData.getCooldowns().isOnCooldown(spellData.getSpell())
//                    || !ClientMagicData.getSyncedSpellData(player).isSpellLearned(spellData.getSpell())) {
//                return InteractionResultHolder.pass(itemStack);
//            } else {
//                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
//            }
//        }
//
//        if (spellData.getSpell().attemptInitiateCast(itemStack, spellData.getLevel(), level, player, CastSource.SPELLBOOK, true)) {
//            if (spellData.getSpell().getCastType().holdToCast()) {
//                player.startUsingItem(hand);
//            }
//            return InteractionResultHolder.success(itemStack);
//        } else {
//            return InteractionResultHolder.fail(itemStack);
//        }
//    }
//
//    @Override
//    public int getUseDuration(ItemStack itemStack) {
//        return 7200;//return getSpellBookData(itemStack).getActiveSpell().getCastTime();
//    }
//
//    @Override
//    public UseAnim getUseAnimation(ItemStack pStack) {
//        return UseAnim.BOW;
//    }
//
//    @Override
//    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
//        return slotChanged;
//    }
//
//    @Override
//    public void releaseUsing(ItemStack itemStack, Level p_41413_, LivingEntity entity, int p_41415_) {
//        IronsSpellbooks.LOGGER.debug("Spellbook Release Using ticks used: {}", p_41415_);
//        entity.stopUsingItem();
//        Utils.releaseUsingHelper(entity, itemStack, p_41415_);
//        super.releaseUsing(itemStack, p_41413_, entity, p_41415_);
//    }

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
        var player = MinecraftInstanceHelper.getPlayer();
        if (player != null) {
            var ssc = getSpellSlotContainer(itemStack);
            lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_spell_count", ssc.getMaxSlotCount()).withStyle(ChatFormatting.GRAY));
            var activeSpellSlots = ssc.getActiveSpellSlots();
            if (!activeSpellSlots.isEmpty()) {
                lines.add(Component.empty());
                lines.add(Component.translatable("tooltip.irons_spellbooks.spellbook_tooltip").withStyle(ChatFormatting.GRAY));
                SpellSelectionManager spellSelectionManager = new SpellSelectionManager(player);
                for (int i = 0; i < activeSpellSlots.size(); i++) {
                    var spellText = TooltipsUtils.getTitleComponent(activeSpellSlots.get(i), (LocalPlayer) player).setStyle(Style.EMPTY);
                    if ((MinecraftInstanceHelper.getPlayer() != null && Utils.getPlayerSpellbookStack(MinecraftInstanceHelper.getPlayer()) == itemStack) && spellSelectionManager.getCurrentSelection().equipmentSlot.equals(Curios.SPELLBOOK_SLOT) && i == spellSelectionManager.getSelectionIndex()) {
                        var shiftMessage = TooltipsUtils.formatActiveSpellTooltip(itemStack, spellSelectionManager.getSelectedSpellData(), CastSource.SPELLBOOK, (LocalPlayer) player);
                        shiftMessage.remove(0);
                        TooltipsUtils.addShiftTooltip(
                                lines,
                                Component.literal("> ").append(spellText).withStyle(ChatFormatting.YELLOW),
                                shiftMessage.stream().map(component -> Component.literal(" ").append(component)).collect(Collectors.toList())
                        );
                    } else {
                        lines.add(Component.literal(" ").append(spellText.withStyle(Style.EMPTY.withColor(0x8888fe))));
                    }
                }
            }
        }
        super.appendHoverText(itemStack, level, lines, flag);
    }

    @NotNull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundRegistry.EQUIP_SPELL_BOOK.get(), 1.0f, 1.0f);
    }

    @Override
    public ISpellSlotContainer getSpellSlotContainer(ItemStack itemStack) {
        if (itemStack == null) {
            return new SpellSlotContainer();
        }

        if (SpellSlotContainer.isSpellContainer(itemStack)) {
            return new SpellSlotContainer(itemStack);
        } else {
            return new SpellSlotContainer(getMaxSpellSlots(), CastSource.SPELLBOOK);
        }
    }
}
