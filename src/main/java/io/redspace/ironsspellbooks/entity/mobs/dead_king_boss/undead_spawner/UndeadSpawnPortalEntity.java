package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.undead_spawner;

import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class UndeadSpawnPortalEntity extends Entity implements IMagicSummon {
    public UndeadSpawnPortalEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    int summonedCount;
    int summonsToSpawn;
    int delay;

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
            if (tickCount >= delay && tickCount % delay == 0) {
                if (summonedCount >= summonsToSpawn) {
                    vanish();
                } else {
                    doSummon();
                    summonedCount++;
                }
            }
        }
    }

    private void doSummon() {
        Item[] leather = {Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET};
        Item[] chain = {Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET};
        Item[] iron = {Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET};
        float power = random.nextFloat() * 3; // 3 for three armorsets
        for (int i = 0; i < 4; i++) {
            float roll = power + random.nextFloat() - 0.5f;
            if(roll>)
        }
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

    private ItemStack[] getEquipment(float power, RandomSource random) {
        Item[] leather = {Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET};
        Item[] chain = {Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET};
        Item[] iron = {Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET};

        int minQuality = 12;
        int maxQuality = getMaxLevel() * spellPowerPerLevel + 15;

        ItemStack[] result = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            float quality = Mth.clamp((power + (random.nextIntBetweenInclusive(-3, 8)) - minQuality) / (maxQuality - minQuality), 0, .95f);
            if (random.nextDouble() < quality * quality) {
                if (quality > .85) {
                    result[i] = new ItemStack(iron[i]);
                } else if (quality > .65) {
                    result[i] = new ItemStack(chain[i]);
                } else if (quality > .15) {
                    result[i] = new ItemStack(leather[i]);
                } else {
                    result[i] = ItemStack.EMPTY;
                }
            } else {
                result[i] = ItemStack.EMPTY;
            }
        }
        return result;
    }

    public int getSummonedCount() {
        return summonedCount;
    }

    public void setSummonedCount(int value) {
        this.summonedCount = value;
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
