package io.redspace.ironsspellbooks.compat.tetra.effects;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ModularItem;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA;

public class ManaSiphonTetraEffect {
    public static final ItemEffect manaSiphon = ItemEffect.get(IronsSpellbooks.MODID + ":mana_siphon");
    public static final String siphonName = IronsSpellbooks.MODID + ".tetra_effect.mana_siphon";
    public static final String siphonTooltip = IronsSpellbooks.MODID + ".tetra_effect.mana_siphon.tooltip";

    @OnlyIn(Dist.CLIENT)
    public static void addGuiBars() {
        //FIXME: 1.21: compiler complaining
//        final IStatGetter effectStatGetter = new StatGetterEffectLevel(manaSiphon, 1);
//        final GuiStatBar effectBar = new GuiStatBar(0, 0, StatsHelper.barLength, siphonName, 0, 30, false, effectStatGetter, LabelGetterBasic.percentageLabel,
//                new TooltipGetterPercentage(siphonTooltip, effectStatGetter));
//        WorkbenchStatsGui.addBar(effectBar);
//        HoloStatsGui.addBar(effectBar);
    }

    public static void handleLivingAttackEvent(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        Entity victim = event.getEntity();
        if (attacker instanceof ServerPlayer player) {
            ItemStack heldStack = player.getMainHandItem();
            if (heldStack.getItem() instanceof ModularItem item) {
                int level = item.getEffectLevel(heldStack, manaSiphon);
                if (level > 0) {
                    level *= .01f;
                    int increment = (int) Math.min(level * event.getAmount(), 50);
                    int maxMana = (int) player.getAttributeValue(MAX_MANA);
                    var playerMagicData = MagicData.getPlayerMagicData(player);
                    var newMana = Math.min(increment + playerMagicData.getMana(), maxMana);
                    playerMagicData.setMana(newMana);
                    PacketDistributor.sendToPlayer(player, new SyncManaPacket(playerMagicData));
                    MagicManager.spawnParticles(victim.level, ParticleTypes.GLOW, victim.getX(), victim.getY() + victim.getBbHeight() * .5f, victim.getZ(), 10, victim.getBbWidth() * .5f, victim.getBbHeight() * .5f, victim.getBbWidth() * .5f, victim.level.getRandom().nextDouble() * .005, false);
                }
            }
        }
    }
}
