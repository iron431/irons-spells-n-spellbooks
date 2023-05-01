package io.redspace.ironsspellbooks.tetra;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.AttributeRegistry;
import io.redspace.ironsspellbooks.tetra.effects.FreezeTetraEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.*;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;

public class TetraActualImpl implements ITetraProxy {

    @Override
    public void initClient() {
        FreezeTetraEffect.addGuiBars();

        /*
        Cooldown Attribute (From magic cloth) display
         */
        IStatGetter cooldownStat = new StatGetterPercentAttribute(AttributeRegistry.COOLDOWN_REDUCTION.get());
        GuiStatBar cooldownBar = new GuiStatBar(0, 0, StatsHelper.barLength, "attribute.irons_spellbooks.cooldown_reduction", 0, 100, false,
                cooldownStat,
                LabelGetterBasic.percentageLabel,
                new TooltipGetterPercentage(IronsSpellbooks.MODID + ".tetra_bar.cooldown_reduction.tooltip", cooldownStat));
        WorkbenchStatsGui.addBar(cooldownBar);
        HoloStatsGui.addBar(cooldownBar);

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
        LivingEntity attackedEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        /*
        Attacker Effects
         */
        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack heldStack = livingAttacker.getMainHandItem();
            if (heldStack.getItem() instanceof ModularItem item) {

                FreezeTetraEffect.handleFreezeEffect(livingAttacker, attackedEntity, heldStack);
            }
        }
    }
}
