package io.redspace.ironsspellbooks.compat.tetra.effects;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchStatsGui;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.LabelGetterBasic;
import se.mickelus.tetra.gui.stats.getter.StatGetterEffectLevel;
import se.mickelus.tetra.gui.stats.getter.TooltipGetterDecimal;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloStatsGui;

public class FreezeTetraEffect {
    public static final ItemEffect freezeOnHit = ItemEffect.get(IronsSpellbooks.MODID + ":freeze");
    public static final String  freezeName = IronsSpellbooks.MODID + ".tetra_effect.freeze";
    public static final String  freezeTooltip = IronsSpellbooks.MODID + ".tetra_effect.freeze.tooltip";

    @OnlyIn(Dist.CLIENT)
    public static void addGuiBars() {
        final IStatGetter effectStatGetter = new StatGetterEffectLevel(freezeOnHit, 1);
        final GuiStatBar effectBar = new GuiStatBar(0, 0, StatsHelper.barLength, freezeName, 0, 30, false, effectStatGetter, LabelGetterBasic.decimalLabel,
                new TooltipGetterDecimal(freezeTooltip, effectStatGetter));
        WorkbenchStatsGui.addBar(effectBar);
        HoloStatsGui.addBar(effectBar);
    }

    public static void handleLivingAttackEvent(LivingAttackEvent event) {
        LivingEntity attackedEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack heldStack = livingAttacker.getMainHandItem();
            if (heldStack.getItem() instanceof ModularItem item) {

                int level = item.getEffectLevel(heldStack, freezeOnHit);
                if (level > 0) {
                    if (attackedEntity.canFreeze()) {
                        attackedEntity.setTicksFrozen(attackedEntity.getTicksFrozen() + level * 20);

                    }
                    MagicManager.spawnParticles(attackedEntity.level, ParticleHelper.SNOWFLAKE, attackedEntity.getX(), attackedEntity.getY() + attackedEntity.getBbHeight() * .5f, attackedEntity.getZ(), 10, attackedEntity.getBbWidth() * .5f, attackedEntity.getBbHeight() * .5f, attackedEntity.getBbWidth() * .5f, .03, false);

                    //IronsSpellbooks.LOGGER.debug("FreezeTetraEffect.level: {}", level);
                }
            }
        }
    }
}
