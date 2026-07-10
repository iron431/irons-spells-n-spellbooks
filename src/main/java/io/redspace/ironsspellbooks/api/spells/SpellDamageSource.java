package io.redspace.ironsspellbooks.api.spells;

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

public class SpellDamageSource extends DamageSource {
    protected AbstractSpell spell;
    protected @Nullable Vec3 sourcePosition;
    protected float lifesteal;
    protected int freezeTicks;
    protected int fireTime;
    protected int iFrames = -1;

    protected SpellDamageSource(Holder<DamageType> damageType, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition, AbstractSpell spell) {
        super(damageType, directEntity, causingEntity, damageSourcePosition);
        this.spell = spell;
    }

    public static SpellDamageSource source(AbstractSpell spell, Level level, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition) {
        return new SpellDamageSource(
                getHolderFromResource(level, spell.getSchoolType().getDamageType()),
                directEntity,
                causingEntity,
                damageSourcePosition,
                spell
        );
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

    public SpellDamageSource setLifestealPercent(float lifesteal) {
        this.lifesteal = lifesteal;
        return this;
    }

    public SpellDamageSource setFireTicks(int fireTicks) {
        this.fireTime = fireTicks;
        return this;
    }

    public SpellDamageSource setFreezeTicks(int freezeTicks) {
        this.freezeTicks = freezeTicks;
        return this;
    }

    public SpellDamageSource setIFrames(int iFrames) {
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
