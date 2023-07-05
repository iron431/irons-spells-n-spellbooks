package io.redspace.ironsspellbooks.spells.ice;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleProjectile;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class IcicleSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "icicle");

    public IcicleSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchool(SchoolType.ICE)
            .setMaxLevel(10)
            .setCooldownSeconds(1)
            .build();

    public IcicleSpell(int level) {
        super(SpellType.ICICLE_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 10;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, MagicData playerMagicData) {
        IcicleProjectile icicle = new IcicleProjectile(world, entity);
        icicle.setPos(entity.position().add(0, entity.getEyeHeight() - icicle.getBoundingBox().getYsize() * .5f, 0));
        icicle.shoot(entity.getLookAngle());
        icicle.setDamage(getDamage(entity));
        icicle.setNoGravity(true);
        world.addFreshEntity(icicle);
        super.onCast(world, entity, playerMagicData);
    }
    private float getDamage(LivingEntity entity) {
        return getSpellPower(entity) * .5f;
    }

}
