package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.scapegoat.ScapegoatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ScapegoatSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "scapegoat");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.taunt_range", Utils.stringTruncation(tauntRange(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.hp", Utils.stringTruncation(getGoatHealth(spellLevel, caster), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(45)
            .build();

    public ScapegoatSpell() {
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 10;
        this.castTime = 0;
        this.baseManaCost = 30;
        this.manaCostPerLevel = 15;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
        Vec3 targetPos = Utils.moveToRelativeGroundLevel(world, Utils.raycastForBlock(world, entity.getEyePosition(), entity.getEyePosition().add(entity.getForward().scale(getRange(spellLevel, entity))), ClipContext.Fluid.NONE).getLocation(), 4, 24);
        ScapegoatEntity goat = new ScapegoatEntity(world);
        goat.setOwner(entity);
        goat.setTargetPos(BlockPos.containing(targetPos));
        float y = entity.getYRot();
        goat.setYBodyRot(y);
        goat.yBodyRotO = y;
        goat.setYRot(y);
        goat.yRotO = y;
        goat.yHeadRot = y;
        goat.yHeadRotO = y;
        //todo: clip?
        goat.moveTo(entity.getEyePosition().add(entity.getForward().scale(2)));
        goat.getAttributes().getInstance(Attributes.MAX_HEALTH).setBaseValue(getGoatHealth(spellLevel, entity));
        goat.setHealth(goat.getMaxHealth());
        goat.poofParticles(20, 1f);
        // todo: expose to tootip/power scaling?
        goat.setDurationRemaining(20 * 15);
        goat.invulnerableTime = 60;
        world.addFreshEntity(goat);
        Utils.performTaunt(goat, 12f, Utils.tauntPredicate(entity));
    }

    private int getRange(int spellLevel, LivingEntity entity) {
        return 18;
    }

    private float tauntRange(int spellLevel, LivingEntity entity) {
        return 12f;
    }

    private float getGoatHealth(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity);
    }
}
