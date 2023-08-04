package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.registry.IronsSpellRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ExtendedLightningBolt extends LightningBolt {
    Entity owner;
    float damage;

    public ExtendedLightningBolt(Level pLevel, Entity owner, float damage) {
        //None of us is serialized but that's okay we're just lightning
        super(EntityType.LIGHTNING_BOLT, pLevel);
        this.owner = owner;
        this.damage = damage;
    }

    @Override
    public float getDamage() {
        //So that thunderHit doesn't deal extra damage; we handle custom damage logic instead
        return 0;
    }

    public void tick() {
        super.tick();
        //Copied from base
        if (tickCount == 1) {
            if (!level.isClientSide) {
                List<Entity> list1 = this.level.getEntities(this, new AABB(this.getX() - 3.0D, this.getY() - 3.0D, this.getZ() - 3.0D, this.getX() + 3.0D, this.getY() + 6.0D + 3.0D, this.getZ() + 3.0D), Entity::isAlive);

                for (Entity entity : list1) {
                    DamageSources.applyDamage(entity, damage, IronsSpellRegistry.LIGHTNING_BOLT_SPELL.get().getDamageSource(owner), SchoolType.LIGHTNING);
                }
            }
        }
    }
}
