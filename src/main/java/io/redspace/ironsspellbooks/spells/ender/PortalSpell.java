package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import io.redspace.ironsspellbooks.util.Log;
import io.redspace.ironsspellbooks.util.NBT;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import java.util.UUID;

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

                    var firstPortalLevel = serverLevel.getServer().getLevel(portalData.globalPos1.dimension());

                    if (firstPortalLevel == null) {
                        return;
                    }

                    firstPortalLevel.setChunkForced(portalData.globalPos1.pos().getX(), portalData.globalPos1.pos().getZ(),false);

                    var serverEntity = firstPortalLevel.getEntity(portalData.portalEntityId1);
                    if (serverEntity instanceof PortalEntity portalEntity) {

                        var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, getCastDistance(spellLevel, player));
                        var pos = blockHitResult.getBlockPos();
                        portalData.globalPos2 = GlobalPos.of(entity.level.dimension(), pos.above(2));

                        var secondPortalEntity = new PortalEntity(level, entity, portalEntity);
                        portalData.portalEntityId2 = secondPortalEntity.getUUID();

                        portalEntity.setDurationTicks(getPortalDuration(spellLevel, player));
                        secondPortalEntity.setDurationTicks(getPortalDuration(spellLevel, entity));

                        secondPortalEntity.moveTo(new Vec3(portalData.globalPos2.pos().getX(), portalData.globalPos2.pos().getY(), portalData.globalPos2.pos().getZ()));
                        secondPortalEntity.setDurationTicks(getPortalDuration(spellLevel, player));
                        level.addFreshEntity(secondPortalEntity);
                    } else {
                        IronsSpellbooks.LOGGER.debug("Could not find portal with globalpos:{} and id:{}", portalData.globalPos1, portalData.portalEntityId1);
                    }
                }
            } else {
                var blockHitResult = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, getCastDistance(spellLevel, player));
                var pos = blockHitResult.getBlockPos();
                var portalData = new PortalData();
                var portalEntity = new PortalEntity(level, entity);

                portalData.globalPos1 = GlobalPos.of(entity.level.dimension(), pos.above(2));
                portalData.portalEntityId1 = portalEntity.getUUID();

                portalEntity.setDurationTicks(getRecastDuration(spellLevel, player) + 10);
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

    private float getPortalMaxDistance(int spellLevel, LivingEntity sourceEntity) {
        //TODO: revisit this
        return 200;
    }

    private float getCastDistance(int spellLevel, LivingEntity sourceEntity) {
        //TODO: revisit this
        return 200;
    }

    public static class PortalData implements ICastDataSerializable {
        public GlobalPos globalPos1;
        public UUID portalEntityId1;
        public GlobalPos globalPos2;
        public UUID portalEntityId2;

        public PortalData() {
        }

        @Override
        public void writeToBuffer(FriendlyByteBuf buffer) {
            if (globalPos1 != null && portalEntityId1 != null) {
                buffer.writeBoolean(true);
                buffer.writeGlobalPos(globalPos1);
                buffer.writeUUID(portalEntityId1);

                if (globalPos2 != null && portalEntityId2 != null) {
                    buffer.writeBoolean(true);
                    buffer.writeGlobalPos(globalPos2);
                    buffer.writeUUID(portalEntityId2);
                } else {
                    buffer.writeBoolean(false);
                }
            } else {
                buffer.writeBoolean(false);
            }
        }

        @Override
        public void readFromBuffer(FriendlyByteBuf buffer) {
            if (buffer.readBoolean()) {
                globalPos1 = buffer.readGlobalPos();
                portalEntityId1 = buffer.readUUID();

                if (buffer.readBoolean()) {
                    globalPos2 = buffer.readGlobalPos();
                    portalEntityId2 = buffer.readUUID();
                }
            }
        }

        @Override
        public void reset() {
            //nothing to clean up for Portal
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();

            if (globalPos1 != null) {
                tag.put("gp1", NBT.writeGlobalPos(globalPos1));
                tag.putUUID("pe1", portalEntityId1);

                if (globalPos2 != null) {
                    tag.put("gp2", NBT.writeGlobalPos(globalPos2));
                    tag.putUUID("pe2", portalEntityId2);
                }
            }

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag compoundTag) {
            if (compoundTag.contains("gp1") && compoundTag.contains("pe1")) {
                this.globalPos1 = NBT.readGlobalPos(compoundTag.getCompound("gp1"));
                this.portalEntityId1 = compoundTag.getUUID("pe1");

                if (compoundTag.contains("gp2") && compoundTag.contains("pe2")) {
                    this.globalPos2 = NBT.readGlobalPos(compoundTag.getCompound("gp2"));
                    this.portalEntityId2 = compoundTag.getUUID("pe2");
                }
            }
        }
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
