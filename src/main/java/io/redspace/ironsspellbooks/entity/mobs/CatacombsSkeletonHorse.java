package io.redspace.ironsspellbooks.entity.mobs;

import io.redspace.ironsspellbooks.entity.mobs.necromancer.NecromancerEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Due to structure block's inability to load entities with Passengers, we create a wrapper class that dynamically does it on entity spawn.
 */
public class CatacombsSkeletonHorse extends SkeletonHorse {
    public CatacombsSkeletonHorse(EntityType<? extends SkeletonHorse> pEntityType, Level level) {
        super(EntityType.SKELETON_HORSE, level);
        this.setTamed(true);
        NecromancerEntity necromancer = EntityRegistry.NECROMANCER.get().create(level);
        if (necromancer != null) {
            necromancer.setPersistenceRequired();
            necromancer.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            necromancer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemRegistry.TARNISHED_CROWN.get()));
            necromancer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
            necromancer.startRiding(this);
        }
    }
}
