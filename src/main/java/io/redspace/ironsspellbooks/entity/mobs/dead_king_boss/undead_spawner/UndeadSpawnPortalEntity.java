package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.undead_spawner;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton;
import io.redspace.ironsspellbooks.entity.mobs.SummonedZombie;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class UndeadSpawnPortalEntity extends Entity implements IMagicSummon {
    public UndeadSpawnPortalEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public UndeadSpawnPortalEntity(Level level) {
        super(EntityRegistry.UNDEAD_PORTAL_WIP.get(), level);
    }

    int summonedCount;
    int summonsToSpawn;
    int delay;

    public void setForcedTarget(LivingEntity forcedTarget) {
        this.forcedTarget = forcedTarget;
    }

    @Nullable LivingEntity forcedTarget;

    @Override
    public boolean isAlliedTo(Entity entity) {
        return super.isAlliedTo(entity) || isAlliedHelper(entity);
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        onRemovedHelper(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            if (delay != 0 && tickCount >= delay * 2 && tickCount % delay == 0) {
                if (summonedCount >= summonsToSpawn) {
                    vanish();
                } else {
                    doSummon();
                    summonedCount++;
                }
            }
        } else {
            Vec3 center = this.getBoundingBox().getCenter();
            for (int i = 0; i < 2; i++) {
                // todo: color variants
                level.addParticle(ParticleHelper.PORTAL_FRAME, center.x, center.y, center.z, 1f, 2.1f, this.getYRot());
            }
        }
    }

    private void vanish() {
        discard();
        //todo: vfxs
    }

    private void doSummon() {
        boolean isSkeleton = random.nextDouble() < .3;
        Monster undead = isSkeleton ?
                new SummonedSkeleton(level, false) :
                new SummonedZombie(level, false);
        equip(undead, generateEquipment());
        undead.moveTo(this.position());
        undead.setYRot(this.getYRot());
        undead.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(undead.getOnPos()), MobSpawnType.MOB_SUMMONED, null);
        SummonManager.setDuration(undead, 5 * 60 * 20);
        var owner = getSummoner();
        if (owner != null) {
            SummonManager.setOwner(undead, owner);
            if (forcedTarget != null) {
                undead.setTarget(forcedTarget);
            } else if (owner instanceof Mob mob) {
                undead.setTarget(mob.getTarget());
            }
        }
        undead.setDeltaMovement(this.getForward().add(0, 0.35, 0).scale(0.25));
        level.addFreshEntity(undead);
        // todo: vfx

    }

    private void equip(Mob mob, ItemStack[] equipment) {
        mob.setItemSlot(EquipmentSlot.FEET, equipment[0]);
        mob.setItemSlot(EquipmentSlot.LEGS, equipment[1]);
        mob.setItemSlot(EquipmentSlot.CHEST, equipment[2]);
        mob.setItemSlot(EquipmentSlot.HEAD, equipment[3]);
        mob.setDropChance(EquipmentSlot.FEET, 0.0F);
        mob.setDropChance(EquipmentSlot.LEGS, 0.0F);
        mob.setDropChance(EquipmentSlot.CHEST, 0.0F);
        mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
    }

    private ItemStack[] generateEquipment() {
        Item[] leather = {Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET};
        Item[] chain = {Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET};
        Item[] iron = {Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET};
        ItemStack[] armor = new ItemStack[4];
        /*
        Generate random number "power" [0,3) to select armor options
        [0,1) is leather, [1,2) is chain, [2,3) is iron
        each piece has additional +/- 0.5 for variation
        */
        float power = random.nextFloat() * 3;
        for (int i = 0; i < 4; i++) {
            float roll = power + random.nextFloat() - 0.5f;
            if (roll >= 2) {
                armor[i] = new ItemStack(iron[i]);
            } else if (roll >= 1) {
                armor[i] = new ItemStack(chain[i]);
            } else if (roll >= 0) {
                armor[i] = new ItemStack(leather[i]);
            } else {
                armor[i] = ItemStack.EMPTY;
            }
        }
        return armor;
    }

    public int getSummonedCount() {
        return summonedCount;
    }

    public int getSummonsToSpawn() {
        return summonsToSpawn;
    }

    public void setSummonsToSpawn(int value) {
        this.summonsToSpawn = value;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int value) {
        this.delay = value;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.summonedCount = compound.getInt("SummonedCount");
        this.summonsToSpawn = compound.getInt("SummonsToSpawn");
        this.delay = compound.getInt("Delay");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("SummonedCount", this.summonedCount);
        compound.putInt("SummonsToSpawn", this.summonsToSpawn);
        compound.putInt("Delay", this.delay);
    }

    @Override
    public void onUnSummon() {
        //todo: implement
    }
}
