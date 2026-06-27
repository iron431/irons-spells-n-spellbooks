package io.redspace.skillcasting.irons_spellbooks;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpellSkillDamageSource extends DamageSource {
    AbstractSpellSkill spell;
    float lifesteal;
    int freezeTicks;
    int fireTime;
    int iFrames = -1;
    boolean indirectOverride;

    protected SpellSkillDamageSource(Holder<DamageType> damageType, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition, AbstractSpellSkill spell) {
        super(damageType, directEntity, causingEntity, damageSourcePosition);
        this.spell = spell;
    }

    @Override
    public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity pLivingEntity) {
        String s = "death.attack." + spell.getDeathMessageId();
        Component component;
        if (this.causingEntity == null && this.directEntity == null) {
            component = spell.getDisplayName(null);
        } else if (this.causingEntity != null) {
            component = this.causingEntity.getDisplayName();
            //fixme: the spell item death messages have never worked :skull:
//            ItemStack itemstack = this.causingEntity.getWeaponItem();
        } else {
            component = this.directEntity.getDisplayName();
        }
        return Component.translatable(s, pLivingEntity.getDisplayName(), component);
    }

    @Override
    @Nullable
    public Vec3 getSourcePosition() {
        Vec3 pos = super.getSourcePosition();
        // vanilla only defers position to direct entity, but that may be null in the cast of hitscan damage sources
        // in such a case, defer to causing entity, if present
        return pos != null ? pos :
                (causingEntity == null ? null : causingEntity.position());
    }

    public static Holder<DamageType> getHolderFromResource(Level level, ResourceKey<DamageType> damageTypeResourceKey) {
        var option = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(damageTypeResourceKey);
        if (option.isPresent()) {
            return option.get();
        } else {
            return level.damageSources().generic().typeHolder();
        }
    }

    public static SpellSkillDamageSource source(Level level, @Nullable Entity entity, @NotNull AbstractSpellSkill spell) {
        return source(level, entity, entity, spell);
    }

    public static SpellSkillDamageSource source(Level level, @Nullable Entity directEntity, @Nullable Entity causingEntity, @NotNull AbstractSpellSkill spell) {
        return new SpellSkillDamageSource(getHolderFromResource(level, spell.getSchoolType().getDamageType()), directEntity, causingEntity, null, spell);
    }

    public SpellSkillDamageSource setLifestealPercent(float lifesteal) {
        this.lifesteal = lifesteal;
        return this;
    }

    public SpellSkillDamageSource setFireTicks(int fireTicks) {
        this.fireTime = fireTicks;
        return this;
    }

    public SpellSkillDamageSource setFreezeTicks(int freezeTicks) {
        this.freezeTicks = freezeTicks;
        return this;
    }

    public SpellSkillDamageSource setIFrames(int iFrames) {
        this.iFrames = iFrames;
        return this;
    }

    public DamageSource get() {
        return this;
    }

    public SpellSkillDamageSource indirect() {
        this.indirectOverride = true;
        return this;
    }

    @Override
    public boolean isDirect() {
        return !indirectOverride && super.isDirect();
    }

    public AbstractSpellSkill spell() {
        return this.spell;
    }

    public float getLifestealPercent() {
        return this.lifesteal;
    }

    public int getFireTime() {
        return this.fireTime;
    }

    public int getFreezeTicks() {
        return this.freezeTicks;
    }

    public int getIFrames() {
        return this.iFrames;
    }

    public boolean hasPostHitEffects() {
        return getLifestealPercent() > 0 || getFireTime() > 0 || getFreezeTicks() > 0;
    }
}
