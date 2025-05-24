package io.redspace.ironsspellbooks.spells.eldritch;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.entity.spells.pocket_dimension_portal.PocketDimensionManager;
import io.redspace.ironsspellbooks.entity.spells.pocket_dimension_portal.PocketDimensionPortalEntity;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@AutoSpellConfig
public class PocketDimensionSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "pocket_dimension");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(/*Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(spellLevel, caster) * 20, 1))*/);
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(200)
            .build();

    public PocketDimensionSpell() {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 6;
        this.castTime = 0;
        this.baseManaCost = 300;
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
        if (entity instanceof ServerPlayer serverPlayer) {
            var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, 32);
            Vec3 hitResultPos = blockHitResult.getLocation().subtract(entity.getForward().normalize().multiply(.25, 0, .25));
            Vec3 portalLocation = level.clip(new ClipContext(hitResultPos, hitResultPos.add(0, -entity.getBbHeight() - 1, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getLocation().add(0, 0.076, 0);
            float portalRotation = 90 + Utils.getAngle(portalLocation.x, portalLocation.z, entity.getX(), entity.getZ()) * Mth.RAD_TO_DEG;

            PortalData portalData = new PortalData();
            portalData.setPortalDuration(20 * 60);
            PocketDimensionPortalEntity portalEntity = new PocketDimensionPortalEntity(level, portalData);
            portalData.firstPortal(portalEntity.getUUID(), PortalPos.of(serverPlayer.level.dimension(), portalLocation, portalRotation));

            //todo: use entity id for now as portal exit id?
            portalData.secondPortal(entity.getUUID(), PortalPos.of(PocketDimensionManager.POCKET_DIMENSION, PocketDimensionManager.INSTANCE.originForPlayer(serverPlayer).above().getBottomCenter(), 0));
            PocketDimensionManager.INSTANCE.maybeGeneratePocketRoom(serverPlayer);
            PortalManager.INSTANCE.addPortalData(entity.getUUID(), portalData);

            portalEntity.moveTo(portalLocation);
            portalEntity.setOwnerUUID(entity.getUUID());
            portalEntity.setYRot(portalRotation);
            level.addFreshEntity(portalEntity);
        }
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
