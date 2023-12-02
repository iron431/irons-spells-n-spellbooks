package io.redspace.ironsspellbooks.item.weapons;

import io.redspace.ironsspellbooks.api.item.weapons.ExtendedSwordItem;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;


public class FirebrandItem extends ExtendedSwordItem {
    public FirebrandItem() {
        super(ExtendedWeaponTiers.KEEPER_FLAMBERGE, 10, -1.8, Map.of(AttributeRegistry.FIRE_SPELL_POWER.get(), new AttributeModifier(UUID.fromString("c552273e-6669-4cd2-80b3-a703b7616336"), "weapon mod", .10, AttributeModifier.Operation.MULTIPLY_BASE)), ItemPropertiesHelper.equipment().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        Vec3 center = pAttacker.getEyePosition().add(pAttacker.getForward().scale(2.5f));
        for (int i = 0; i < 25; i++) {
            MagicManager.spawnParticles(pAttacker.level, ParticleHelper.EMBERS,
                    center.x + (i - 12.5f) * .1f * Mth.cos(pAttacker.getYRot() * Mth.DEG_TO_RAD),
                    center.y + Mth.sin(pAttacker.getXRot() * Mth.DEG_TO_RAD),
                    center.z + (i - 12.5f) * .1f * Mth.sin(pAttacker.getYRot() * Mth.DEG_TO_RAD) + Mth.sin(Mth.lerp(i / 25f, 0, Mth.PI)) * -.85f, 1, 0, 0, 0, 0.08, false);
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }
}
