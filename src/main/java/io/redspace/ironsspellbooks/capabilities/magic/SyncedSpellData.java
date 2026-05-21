package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.gui.overlays.SpellSelection;
import io.redspace.ironsspellbooks.network.casting.SyncMagicDataPacket;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

@Deprecated(forRemoval = true)
public class SyncedSpellData {
    private final int serverPlayerId;
    private final MagicData magicData;

    public SyncedSpellData(int serverPlayerId) {
        this.serverPlayerId = serverPlayerId;
        this.magicData = new MagicData((LivingEntity) null);
    }

    public SyncedSpellData(LivingEntity livingEntity) {
        this.serverPlayerId = livingEntity == null ? -1 : livingEntity.getId();
        this.magicData = livingEntity == null ? new MagicData((LivingEntity) null) : MagicData.getPlayerMagicData(livingEntity);
    }

    public SyncedSpellData(MagicData magicData) {
        this.serverPlayerId = -1;
        this.magicData = magicData;
    }

    public static void write(FriendlyByteBuf buffer, SyncedSpellData data) {
        buffer.writeInt(data.serverPlayerId);
        buffer.writeBoolean(data.isCasting());
        buffer.writeUtf(data.getCastingSpellId());
        buffer.writeInt(data.getCastingSpellLevel());
        buffer.writeFloat(data.getHeartstopAccumulatedDamage());
        buffer.writeInt(0);
        buffer.writeResourceLocation(data.getSpinAttackType().textureId());
        buffer.writeBoolean(data.getSpinAttackType().fullbright());
        buffer.writeUtf(data.getCastingEquipmentSlot());
        data.magicData.getLearnedSpelLData().writeToBuffer(buffer);
        data.magicData.getSpellSelection().writeToBuffer(buffer);
    }

    public static SyncedSpellData read(FriendlyByteBuf buffer) {
        var data = new SyncedSpellData(buffer.readInt());
        boolean isCasting = buffer.readBoolean();
        String castingSpellId = buffer.readUtf();
        int castingSpellLevel = buffer.readInt();
        float heartstop = buffer.readFloat();
        buffer.readInt();
        SpinAttackType spinAttackType = new SpinAttackType(buffer.readResourceLocation(), buffer.readBoolean());
        String castingSlot = buffer.readUtf();
        data.magicData.getLearnedSpelLData().readFromBuffer(buffer);
        data.magicData.getSpellSelection().readFromBuffer(buffer);
        data.setHeartstopAccumulatedDamage(heartstop);
        data.setSpinAttackType(spinAttackType);
        data.setIsCasting(isCasting, castingSpellId, castingSpellLevel, castingSlot);
        return data;
    }

    public int getServerPlayerId() {
        return serverPlayerId;
    }

    public String getCastingEquipmentSlot() {
        return magicData.getCastingEquipmentSlot();
    }

    public float getHeartstopAccumulatedDamage() {
        return magicData.getHeartstopAccumulatedDamage();
    }

    public void setHeartstopAccumulatedDamage(float damage) {
        magicData.setHeartstopAccumulatedDamage(damage);
    }

    public SpellSelection getSpellSelection() {
        return magicData.getSpellSelection();
    }

    public void setSpellSelection(SpellSelection spellSelection) {
        magicData.setSpellSelection(spellSelection);
    }

    public void learnSpell(AbstractSpell spell) {
        learnSpell(spell, true);
    }

    public void learnSpell(AbstractSpell spell, boolean sync) {
        magicData.getLearnedSpelLData().learnSpell(spell, false);
        if (sync) {
            doSync();
        }
    }

    public void forgetAllSpells() {
        magicData.getLearnedSpelLData().forgetAllSpells();
        doSync();
    }

    public boolean isSpellLearned(AbstractSpell spell) {
        return magicData.getLearnedSpelLData().isSpellLearned(spell);
    }

    public SpinAttackType getSpinAttackType() {
        return magicData.getSpinAttackType();
    }

    public void setSpinAttackType(SpinAttackType spinAttackType) {
        magicData.setSpinAttackType(spinAttackType);
    }

    public void addHeartstopDamage(float damage) {
        magicData.addHeartstopDamage(damage);
    }

    public void doSync() {
        magicData.doSync();
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        PacketDistributor.sendToPlayer(serverPlayer, new SyncMagicDataPacket(magicData, magicData.getOwner()));
    }

    public void setIsCasting(boolean isCasting, String castingSpellId, int castingSpellLevel, String castingEquipmentSlot) {
        if (!isCasting) {
            magicData.resetCastingState();
            return;
        }
        magicData.initiateCast(io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell(castingSpellId), castingSpellLevel, 0, CastSource.NONE, castingEquipmentSlot);
    }

    public boolean isCasting() {
        return magicData.isCasting();
    }

    public String getCastingSpellId() {
        return magicData.getCastingSpellId();
    }

    public int getCastingSpellLevel() {
        return magicData.getCastingSpellLevel();
    }

    public SyncedSpellData getPersistentData(ServerPlayer serverPlayer) {
        return new SyncedSpellData(magicData.getPersistentData(serverPlayer));
    }

    public MagicData asMagicData() {
        return magicData;
    }
}
