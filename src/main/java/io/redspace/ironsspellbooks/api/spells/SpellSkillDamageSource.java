package io.redspace.ironsspellbooks.api.spells;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpellSkillDamageSource extends DamageSource {
    protected AbstractSpell spell;
    protected @Nullable Vec3 sourcePosition;
    protected float lifesteal;
    protected int freezeTicks;
    protected int fireTime;
    protected int iFrames = -1;

    protected SpellSkillDamageSource(Holder<DamageType> damageType, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition, AbstractSpell spell) {
        super(damageType, directEntity, causingEntity, damageSourcePosition);
        this.spell = spell;
    }

    public static SpellSkillDamageSource source(AbstractSpell spell, Level level, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition) {
        return new SpellSkillDamageSource(
                getHolderFromResource(level, spell.getSchoolType().getDamageType()),
                directEntity,
                causingEntity,
                damageSourcePosition,
                spell
        );
    }
//
//    public static SpellSkillDamageSource source(CastContext castContext, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
//        if (!(castContext.skill().value() instanceof AbstractSpell spell)) {
//            throw new IllegalArgumentException("SpellDamageSource can only be created for spells");
//        }
//        return source(spell, castContext.level(), directEntity, causingEntity, castContext.position(PositionAnchor.BOTTOM_CENTER));
//    }
//
//    public static SpellSkillDamageSource source(CastContext castContext) {
//        return source(castContext, castContext.asEntityCaster(), castContext.asEntityCaster());
//    }
//
//    public static SpellSkillDamageSource projectile(AbstractSpell spell, @NotNull Projectile projectile) {
//        return new SpellSkillDamageSource(
//                getHolderFromResource(projectile.level(), spell.getSchoolType().getDamageType()),
//                projectile,
//                projectile.getOwner(),
//                null,
//                spell
//        );
//    }
//
//    //    public static SpellSkillDamageSource direct(AbstractSpell spell, Level level, @Nullable  Entity causingEntity){

    /// /        return
    /// /    }
//    public static SpellSkillDamageSource direct(CastContext castContext) {
//        if (!(castContext.skill().value() instanceof AbstractSpell spell)) {
//            throw new IllegalArgumentException("SpellDamageSource can only be created for spells");
//        }
//        return new SpellSkillDamageSource(
//                getHolderFromResource(castContext.level(), spell.getSchoolType().getDamageType()),
//                castContext.asEntityCaster(),
//                castContext.asEntityCaster(),
//                castContext.position(PositionAnchor.BOTTOM_CENTER),
//                spell
//        );
//    }
//
//    public static SpellSkillDamageSource indirect(CastContext castContext) {
//        if (!(castContext.skill().value() instanceof AbstractSpell spell)) {
//            throw new IllegalArgumentException("SpellDamageSource can only be created for spells");
//        }
//        return new SpellSkillDamageSource(
//                getHolderFromResource(castContext.level(), spell.getSchoolType().getDamageType()),
//                null,
//                castContext.asEntityCaster(),
//                castContext.position(PositionAnchor.BOTTOM_CENTER),
//                spell
//        );
//    }
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
        if (directEntity != null) {
            return directEntity.position();
        }
        if (sourcePosition != null) {
            return sourcePosition;
        }
        if (causingEntity != null) {
            return causingEntity.position();
        }
        return null;
    }

    public static Holder<DamageType> getHolderFromResource(Level level, ResourceKey<DamageType> damageTypeResourceKey) {
        var option = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(damageTypeResourceKey);
        if (option.isPresent()) {
            return option.get();
        } else {
            return level.damageSources().generic().typeHolder();
        }
    }

//    public static SpellSkillDamageSource source(Level level, @Nullable Entity entity, @NotNull AbstractSpell spell) {
//        return source(level, entity, entity, spell);
//    }
//
//    public static SpellSkillDamageSource source(Level level, @Nullable Entity directEntity, @Nullable Entity causingEntity, @NotNull AbstractSpell spell) {
//        return new SpellSkillDamageSource(getHolderFromResource(level, spell.getSchoolType().getDamageType()), directEntity, causingEntity, null, spell);
//    }

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

    public AbstractSpell spell() {
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
