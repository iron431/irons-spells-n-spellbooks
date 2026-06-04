package io.redspace.ironsspellbooks.entity.spells.ender_chain;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.scores.Team;
import net.minecraftforge.entity.PartEntity;

public class EnderChainPart extends PartEntity<EnderChain> implements AntiMagicSusceptible {
    public final EnderChain parentChain;
    private final EntityDimensions size;

    public EnderChainPart(EnderChain parent, float width, float height) {
        super(parent);
        this.parentChain = parent;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return parentChain.hurt(source, amount);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return parentChain.isAlliedTo(entity);
    }

    @Override
    public boolean isAlliedTo(Team team) {
        return parentChain.isAlliedTo(team);
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        parentChain.onAntiMagic(playerMagicData);
    }
}
