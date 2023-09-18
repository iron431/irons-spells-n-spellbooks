package io.redspace.ironsspellbooks.entity.mobs.debug_wizard;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.DebugTargetClosestEntityGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.DebugWizardAttackGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

public class DebugWizard extends AbstractSpellCastingMob implements Enemy {
    private AbstractSpell spell;
    private int spellLevel;
    private boolean targetsPlayer;
    private String spellInfo = "No Spell Found";
    private int cancelCastAfterTicks;
    private static final EntityDataAccessor<String> DEBUG_SPELL_INFO = SynchedEntityData.defineId(DebugWizard.class, EntityDataSerializers.STRING);

    public DebugWizard(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public DebugWizard(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel, AbstractSpell spell, int spellLevel, boolean targetsPlayer, int cancelCastAfterTicks) {
        super(pEntityType, pLevel);

        this.targetsPlayer = targetsPlayer;
        this.spellLevel = spellLevel;
        this.spell = spell;
        this.cancelCastAfterTicks = cancelCastAfterTicks;
        initGoals();
    }

    public String getSpellInfo() {
        return spellInfo;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DEBUG_SPELL_INFO, "DEFAULT");
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);

        if (!level().isClientSide) {
            return;
        }

        if (pKey.getId() == DEBUG_SPELL_INFO.getId()) {
            spellInfo = entityData.get(DEBUG_SPELL_INFO);
        }
    }

    private void initGoals() {
        this.goalSelector.addGoal(1, new DebugWizardAttackGoal(this, spell, spellLevel, cancelCastAfterTicks));

        if (this.targetsPlayer) {
            IronsSpellbooks.LOGGER.debug("DebugWizard: Adding DebugTargetClosestEntityGoal");
            this.targetSelector.addGoal(1, new DebugTargetClosestEntityGoal(this));
        }
        entityData.set(DEBUG_SPELL_INFO, String.format("%s (L%s)", spell.getSpellName(), spellLevel));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putString("spellId", spell.getSpellId());
        pCompound.putInt("spellLevel", spellLevel);
        pCompound.putBoolean("targetsPlayer", targetsPlayer);
        pCompound.putInt("cancelCastAfterTicks", cancelCastAfterTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        spell = SpellRegistry.getSpell(pCompound.getString("spellId"));
        spellLevel = pCompound.getInt("spellLevel");
        targetsPlayer = pCompound.getBoolean("targetsPlayer");
        cancelCastAfterTicks = pCompound.getInt("cancelCastAfterTicks");
        initGoals();
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, .4);
    }
}
