package io.redspace.ironsspellbooks.compat.tetra;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.compat.tetra.effects.FreezeTetraEffect;
import io.redspace.ironsspellbooks.compat.tetra.effects.ManaSiphonTetraEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.*;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;

public class TetraActualImpl implements ITetraProxy {

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {

        FreezeTetraEffect.addGuiBars();
        ManaSiphonTetraEffect.addGuiBars();

        /*
        Cooldown Attribute (From magic cloth) display
         */
        createPercentAttributeBar(AttributeRegistry.COOLDOWN_REDUCTION.get(), "cooldown_reduction");
        createPercentAttributeBar(AttributeRegistry.FIRE_SPELL_POWER.get(), "fire_spell_power");
        createPercentAttributeBar(AttributeRegistry.ICE_SPELL_POWER.get(), "ice_spell_power");
        createPercentAttributeBar(AttributeRegistry.LIGHTNING_SPELL_POWER.get(), "lightning_spell_power");
        createPercentAttributeBar(AttributeRegistry.HOLY_SPELL_POWER.get(), "holy_spell_power");
        createPercentAttributeBar(AttributeRegistry.ENDER_SPELL_POWER.get(), "ender_spell_power");
        createPercentAttributeBar(AttributeRegistry.BLOOD_SPELL_POWER.get(), "blood_spell_power");
        createPercentAttributeBar(AttributeRegistry.EVOCATION_SPELL_POWER.get(), "evocation_spell_power");
        createPercentAttributeBar(AttributeRegistry.NATURE_SPELL_POWER.get(), "poison_spell_power");
        createPercentAttributeBar(AttributeRegistry.SPELL_RESIST.get(), "spell_resist");
        /*
        Mana Attribute (From arcane ingot) display
         */
        IStatGetter manaStat = new StatGetterAttribute(AttributeRegistry.MAX_MANA.get(), true);
        GuiStatBar manaStatBar = new GuiStatBar(0, 0, StatsHelper.barLength, "attribute.irons_spellbooks.max_mana", 0, 500, false,
                manaStat,
                LabelGetterBasic.integerLabel,
                new TooltipGetterInteger(IronsSpellbooks.MODID + ".tetra_bar.max_mana.tooltip", manaStat));
        WorkbenchStatsGui.addBar(manaStatBar);
        HoloStatsGui.addBar(manaStatBar);


    }

    @Override
    public boolean canImbue(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ModularBladedItem) {
            return true;
        }
        return false;
    }

    @Override
    public void handleLivingAttackEvent(LivingAttackEvent event) {
        if (!event.getEntity().level.isClientSide) {
            FreezeTetraEffect.handleLivingAttackEvent(event);
            ManaSiphonTetraEffect.handleLivingAttackEvent(event);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void createPercentAttributeBar(Attribute attribute, String languageKey) {
        IStatGetter statGetter = new StatGetterPercentAttribute(attribute);
        GuiStatBar statBar = new GuiStatBar(0, 0, StatsHelper.barLength, attribute.getDescriptionId(), 0, 100, false,
                statGetter,
                LabelGetterBasic.percentageLabel,
                new TooltipGetterPercentage(IronsSpellbooks.MODID + ".tetra_bar." + languageKey + ".tooltip", statGetter));
        //TODO: tetra reimplementation

        WorkbenchStatsGui.addBar(statBar);
        HoloStatsGui.addBar(statBar);
    }
}
