package io.redspace.ironsspellbooks.entity.spells.visual_spell_rendering;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.OwnerHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class VisualSpellEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_SPELL_TYPE = SynchedEntityData.defineId(VisualSpellEntity.class, EntityDataSerializers.INT);

    public VisualSpellEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public VisualSpellEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.VISUAL_SPELLCASTING_ENTITY.get(), level);
        setOwner(owner);
    }

    protected LivingEntity cachedOwner;
    protected UUID ownerUUID;

    public LivingEntity getOwner() {
        return OwnerHelper.getAndCacheOwner(level, cachedOwner, ownerUUID);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Override
    public void tick() {


        if (level.isClientSide)
            return;
        if (getOwner() == null)
            discard();
        else {
            moveTo(getOwner().getEyePosition());
            IronsSpellbooks.LOGGER.debug("VisualSpellEntityTickPre: yrot: {} yrotold: {}", getYRot(), yRotO);
            setRot(getOwner().getYRot(), getOwner().getXRot());
            IronsSpellbooks.LOGGER.debug("VisualSpellEntityTickPost: yrot: {} yrotold: {}", getYRot(), yRotO);

            //IronsSpellbooks.LOGGER.debug("VisualSpellEntityTick: ownerYrot: {} ownerYrotOld: {}", getOwner().getYRot(), getOwner().yRotO);
            //this.yRotO = getOwner().yRotO;
            //this.xRotO = getOwner().xRotO;
        }
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;//return super.shouldRender(pX, pY, pZ);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_SPELL_TYPE, 0);
    }

    public void setSpellType(int spellType) {
        if (!level.isClientSide)
            this.entityData.set(DATA_SPELL_TYPE, spellType);
    }

    public int getSpellType() {
        return this.entityData.get(DATA_SPELL_TYPE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setSpellType(tag.getInt("SpellType"));
        if (tag.contains("Owner"))
            this.ownerUUID = tag.getUUID("Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("SpellType", getSpellType());
        if (ownerUUID != null)
            tag.putUUID("Owner", ownerUUID);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
