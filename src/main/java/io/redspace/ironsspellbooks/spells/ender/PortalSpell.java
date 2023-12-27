package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import io.redspace.ironsspellbooks.util.Log;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class PortalSpell extends AbstractSpell {
    public static final int PORTAL_RECAST_COUNT = 2;
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "portal");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(100)
            .build();

    public PortalSpell() {
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 10;
        this.baseManaCost = 50;
        this.manaCostPerLevel = 10;
        this.castTime = 0;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.PORTAL_TRIGGER);
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new PortalData();
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return PORTAL_RECAST_COUNT;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("PortalSpell.onCast isClient:{}, entity:{}, pmd:{}", level.isClientSide, entity, playerMagicData);
        }

        if (entity instanceof Player player && level instanceof ServerLevel serverLevel) {

            if (playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId())) {
                var portalData = (PortalData) playerMagicData.getPlayerRecasts().getRecastInstance(getSpellId()).getCastData();

                if (portalData.globalPos1 != null & portalData.portalEntityId1 != null) {
                    var blockHitResult = Utils.getTargetBlock(level, player, ClipContext.Fluid.NONE, getCastDistance(spellLevel, player));
                    var pos = blockHitResult.getBlockPos();
                    portalData.globalPos2 = GlobalPos.of(player.level.dimension(), pos.above(2));
                    portalData.setPortalDuration(getPortalDuration(spellLevel, player));
                    var secondPortalEntity = new PortalEntity(level, portalData);
                    secondPortalEntity.setOwnerUUID(player.getUUID());
                    portalData.portalEntityId2 = secondPortalEntity.getUUID();

                    PortalManager.INSTANCE.addPortalData(portalData.portalEntityId1, portalData);
                    PortalManager.INSTANCE.addPortalData(portalData.portalEntityId2, portalData);

                    secondPortalEntity.moveTo(new Vec3(portalData.globalPos2.pos().getX(), portalData.globalPos2.pos().getY(), portalData.globalPos2.pos().getZ()));
                    level.addFreshEntity(secondPortalEntity);
                }
            } else {
                var blockHitResult = Utils.getTargetBlock(level, player, ClipContext.Fluid.NONE, getCastDistance(spellLevel, player));
                var pos = blockHitResult.getBlockPos();
                var portalData = new PortalData();
                portalData.setPortalDuration(getRecastDuration(spellLevel, player) + 10);
                var portalEntity = new PortalEntity(level, portalData);
                portalEntity.setOwnerUUID(player.getUUID());
                portalData.globalPos1 = GlobalPos.of(player.level.dimension(), pos.above(2));
                portalData.portalEntityId1 = portalEntity.getUUID();
                portalEntity.moveTo(new Vec3(portalData.globalPos1.pos().getX(), portalData.globalPos1.pos().getY(), portalData.globalPos1.pos().getZ()));
                level.addFreshEntity(portalEntity);
                playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, 1, getRecastDuration(spellLevel, player), portalData));
            }
        }

        super.onCast(level, spellLevel, entity, playerMagicData);
    }

    public int getRecastDuration(int spellLevel, LivingEntity caster) {
        //TODO: revisit this
        return 20 * 120;
    }

    public int getPortalDuration(int spellLevel, LivingEntity caster) {
        //TODO: revisit this
        return (int) (getSpellPower(spellLevel, caster) * 200);
    }

    private float getPortalMaxDistanceApart(int spellLevel, LivingEntity sourceEntity) {
        //TODO: revisit this
        return 200;
    }

    private float getCastDistance(int spellLevel, LivingEntity sourceEntity) {
        //TODO: revisit this
        return 200;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getCastDistance(spellLevel, caster), 1)));
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return AnimationHolder.none();
    }

}
