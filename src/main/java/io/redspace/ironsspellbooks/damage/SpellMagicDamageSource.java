package io.redspace.ironsspellbooks.damage;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpellMagicDamageSource extends DamageSource {
    AbstractSpell spell;

    protected SpellMagicDamageSource(@NotNull Entity directEntity, @NotNull Entity causingEntity, @Nullable Vec3 damageSourcePosition, AbstractSpell spell) {
        super(getHolderFromResource(directEntity, spell.getSchoolType().getDamageType()), directEntity, causingEntity, damageSourcePosition);
        this.spell = spell;
    }

    @Override
    public @NotNull Component getLocalizedDeathMessage(@NotNull LivingEntity pLivingEntity) {
//        if (this.causingEntity == null && this.directEntity == null) {
//            LivingEntity livingentity1 = pLivingEntity.getKillCredit();
//            String s1 = s + ".player";
//            return livingentity1 != null ? Component.translatable(s1, pLivingEntity.getDisplayName(), livingentity1.getDisplayName()) : Component.translatable(s, pLivingEntity.getDisplayName());
//        }

        String s = "death.attack." + spell.getDeathMessageId();
        Component component = this.causingEntity == null ? this.directEntity.getDisplayName() : this.causingEntity.getDisplayName();
        return Component.translatable(s, pLivingEntity.getDisplayName(), component);
    }

    //TODO: need a better way to get the registry access without going through the level each time
    private static Holder<DamageType> getHolderFromResource(Entity entity, ResourceKey<DamageType> damageTypeResourceKey) {
        var option = entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(damageTypeResourceKey);
        if (option.isPresent()) {
            return option.get();
        } else {
            return entity.level().damageSources().genericKill().typeHolder();
        }
    }

    public static DamageSource source(@NotNull Entity entity, @NotNull AbstractSpell spell) {
        return new SpellMagicDamageSource(entity, entity, null, spell);
    }

    public static DamageSource source(@NotNull Entity directEntity, @NotNull Entity causingEntity, @NotNull AbstractSpell spell) {
        return new SpellMagicDamageSource(directEntity, causingEntity, null, spell);
    }
}
