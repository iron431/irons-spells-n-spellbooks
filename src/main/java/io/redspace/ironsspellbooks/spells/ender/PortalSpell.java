package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.Log;
import io.redspace.ironsspellbooks.util.NBT;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class PortalSpell extends AbstractSpell {
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
        return Optional.of(SoundEvents.END_PORTAL_SPAWN);
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new PortalData();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (Log.SPELL_DEBUG) {
            IronsSpellbooks.LOGGER.debug("PortalSpell.onCast isClient:{}, entity:{}, pmd:{}", level.isClientSide, entity, playerMagicData);
        }

        if (entity instanceof Player player) {
            var portalData = new PortalData();
            portalData.castCount = 5;
            portalData.globalPos1 = GlobalPos.of(entity.level.dimension(), entity.getOnPos());
            playerMagicData.getPlayerRecasts().addRecast(getSpellId(), spellLevel, player, portalData,5);
            playerMagicData.getPlayerRecasts().addRecast("irons_spellbooks:firebolt", spellLevel, player, portalData,2);
            playerMagicData.getPlayerRecasts().addRecast("irons_spellbooks:magic_missile", spellLevel, player, portalData,9);
        }

        super.onCast(level, spellLevel, entity, playerMagicData);
    }

    public static void particleCloud(Level level, Vec3 pos) {
        if (level.isClientSide) {
            double width = 0.5;
            float height = 1;
            for (int i = 0; i < 55; i++) {
                double x = pos.x + Utils.random.nextDouble() * width * 2 - width;
                double y = pos.y + height + Utils.random.nextDouble() * height * 1.2 * 2 - height * 1.2;
                double z = pos.z + Utils.random.nextDouble() * width * 2 - width;
                double dx = Utils.random.nextDouble() * .1 * (Utils.random.nextBoolean() ? 1 : -1);
                double dy = Utils.random.nextDouble() * .1 * (Utils.random.nextBoolean() ? 1 : -1);
                double dz = Utils.random.nextDouble() * .1 * (Utils.random.nextBoolean() ? 1 : -1);
                level.addParticle(ParticleTypes.PORTAL, true, x, y, z, dx, dy, dz);
            }
        }
    }

    private float getDistance(int spellLevel, LivingEntity sourceEntity) {
        return 200;
    }

    public static class PortalData implements ICastDataSerializable {
        public int castCount;
        public GlobalPos globalPos1;
        public GlobalPos globalPos2;

        public PortalData() {
            castCount = 0;
        }

        @Override
        public void writeToBuffer(FriendlyByteBuf buffer) {
            buffer.writeInt(castCount);

            if (globalPos1 != null) {
                buffer.writeBoolean(true);
                buffer.writeGlobalPos(globalPos1);

                if (globalPos2 != null) {
                    buffer.writeBoolean(true);
                    buffer.writeGlobalPos(globalPos2);
                } else {
                    buffer.writeBoolean(false);
                }
            } else {
                buffer.writeBoolean(false);
            }
        }

        @Override
        public void readFromBuffer(FriendlyByteBuf buffer) {
            castCount = buffer.readInt();
            if (buffer.readBoolean()) {
                globalPos1 = buffer.readGlobalPos();

                if (buffer.readBoolean()) {
                    globalPos2 = buffer.readGlobalPos();
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
            tag.putInt("cnt", castCount);

            if (globalPos1 != null) {
                tag.put("gp1", NBT.writeGlobalPos(globalPos1));

                if (globalPos2 != null) {
                    tag.put("gp2", NBT.writeGlobalPos(globalPos2));
                }
            }

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag compoundTag) {
            this.castCount = compoundTag.getInt("cnt");

            if (compoundTag.contains("gp1")) {
                this.globalPos1 = NBT.readGlobalPos(compoundTag.getCompound("gp1"));

                if (compoundTag.contains("gp2")) {
                    this.globalPos2 = NBT.readGlobalPos(compoundTag.getCompound("gp2"));
                }
            }
        }
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getDistance(spellLevel, caster), 1)));
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return AnimationHolder.none();
    }

}
