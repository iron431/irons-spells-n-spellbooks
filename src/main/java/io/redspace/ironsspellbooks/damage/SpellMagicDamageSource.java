package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpellMagicDamageSource extends DamageSource {
    SpellType spellType;

    public SpellMagicDamageSource(Holder<DamageType> damageTypeHolder, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition, SpellType spellType) {
        super(damageTypeHolder, directEntity, causingEntity, damageSourcePosition);
        this.spellType = spellType;
    }

    public SpellMagicDamageSource(Holder<DamageType> damageTypeHolder, @Nullable Entity directEntity, @Nullable Entity causingEntity, SpellType spellType) {
        this(damageTypeHolder, directEntity, causingEntity, (Vec3) null, spellType);
    }

    public SpellMagicDamageSource(Holder<DamageType> damageTypeHolder, Vec3 damageSourcePosition, SpellType spellType) {
        this(damageTypeHolder, (Entity) null, (Entity) null, damageSourcePosition, spellType);
    }

    public SpellMagicDamageSource(Holder<DamageType> damageTypeHolder, @Nullable Entity causingEntity, SpellType spellType) {
        this(damageTypeHolder, causingEntity, causingEntity, spellType);
    }

    public SpellMagicDamageSource(Holder<DamageType> damageTypeHolder, SpellType spellType) {
        this(damageTypeHolder, (Entity) null, (Entity) null, (Vec3) null, spellType);
    }

    @Override
    public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity pLivingEntity) {
        String s = "death.attack." + spellType.getFullId();
        if (this.causingEntity == null && this.directEntity == null) {
            LivingEntity livingentity1 = pLivingEntity.getKillCredit();
            String s1 = s + ".player";
            return livingentity1 != null ? Component.translatable(s1, pLivingEntity.getDisplayName(), livingentity1.getDisplayName()) : Component.translatable(s, pLivingEntity.getDisplayName());
        } else {
            Component component = this.causingEntity == null ? this.directEntity.getDisplayName() : this.causingEntity.getDisplayName();
            return Component.translatable(s, pLivingEntity.getDisplayName(), component);
        }
    }

    //TODO: need a better way to get the registry access without going through the level each time
    private static Holder<DamageType> getHolderFromResource(Level level, ResourceKey<DamageType> damageTypeResourceKey) {
        var option = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(damageTypeResourceKey);
        if (option.isPresent()) {
            return option.get();
        } else {
            return level.damageSources().genericKill().typeHolder();
        }
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> damageTypeResourceKey, SpellType spellType) {
        return new SpellMagicDamageSource(getHolderFromResource(level, damageTypeResourceKey), spellType);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> damageTypeResourceKey, @Nullable Entity entity, SpellType spellType) {
        return new SpellMagicDamageSource(getHolderFromResource(level, damageTypeResourceKey), entity, spellType);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> damageTypeResourceKey, @Nullable Entity directEntity, @Nullable Entity causingEntity, SpellType spellType) {
        return new SpellMagicDamageSource(getHolderFromResource(level, damageTypeResourceKey), directEntity, causingEntity, spellType);
    }
}
