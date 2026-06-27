package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.thrown_item.ThrownItemProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.selection.SkillSelectionManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ThrowSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        float weaponDamage = castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f);
        String plus = weaponDamage > 0 ? String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1)) : "";
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(damage + weaponDamage, 1) + plus));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(8)
            .build();

    public ThrowSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 5;
        this.baseManaCost = 10;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.THROW_DAGGER).toOpt();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.THROW_SINGLE_ITEM;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_TIME, castTime);
        float weaponDamage = 0;
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            weaponDamage = Utils.getWeaponDamage(entity);
        }
        castContext.set(SkillcastingComponentTypes.WEAPON_DAMAGE, weaponDamage);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        ItemStack stack = ItemStack.EMPTY;
        if (caster instanceof LivingEntity living) {
            boolean offhand = SkillSelectionManager.OFFHAND.equals(castContext.getOrNull(SkillcastingComponentTypes.CAST_SOURCE));
            stack = offhand ? living.getOffhandItem() : living.getMainHandItem();
        }

        ThrownItemProjectile thrownItem = new ThrownItemProjectile(level, stack);
        thrownItem.setOwner(caster);
        Vec3 spawn = caster != null
                ? caster.position().add(0, caster.getEyeHeight() - thrownItem.getBoundingBox().getYsize() * 0.5f, 0)
                : castContext.position(PositionAnchor.CASTING_POSITION);
        thrownItem.setPos(spawn);
        thrownItem.shoot(castContext.direction());
        thrownItem.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f)
                + castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f));
        thrownItem.setScale(caster instanceof LivingEntity living ? living.getScale() : 1f);
        level.addFreshEntity(thrownItem);
    }
}
