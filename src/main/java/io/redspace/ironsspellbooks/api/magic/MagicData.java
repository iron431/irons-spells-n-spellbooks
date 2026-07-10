package io.redspace.ironsspellbooks.api.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public class MagicData implements IHolderSensitiveData {

    public static final Codec<MagicData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            LearnedSpellData.CODEC.fieldOf("learned_spells").forGetter(MagicData::getLearnedSpellData),
            Codec.FLOAT.fieldOf("mana").forGetter(MagicData::getMana),
            Codec.FLOAT.optionalFieldOf("heartstop_damage", 0f).forGetter(MagicData::getHeartStopAccumulatedDamage)
    ).apply(builder, MagicData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MagicData> STREAM_CODEC = StreamCodec.composite(
            LearnedSpellData.STREAM_CODEC, MagicData::getLearnedSpellData,
            ByteBufCodecs.FLOAT, MagicData::getMana,
            ByteBufCodecs.FLOAT, MagicData::getHeartStopAccumulatedDamage,
            MagicData::new
    );

    public MagicData persistentCopy(IAttachmentHolder holder, HolderLookup.Provider provider) {
        MagicData persisted = new MagicData(this.learnedSpellData, 0, 0);
        persisted.setHolder(holder);
        return persisted;
    }

    private MagicData(LearnedSpellData data, float mana, float heartStopAccumulatedDamage) {
        this.learnedSpellData = data;
        this.mana = mana;
        this.heartStopAccumulatedDamage = heartStopAccumulatedDamage;
    }

    public static MagicData factory(IAttachmentHolder attachmentHolder) {
        MagicData data = new MagicData(new LearnedSpellData(), 0, 0);
        data.setHolder(attachmentHolder);
        return data;
    }

    private @Nullable IAttachmentHolder holder;
    private SpinAttackType spinAttackType = SpinAttackType.RIPTIDE;
    private final LearnedSpellData learnedSpellData;
    private float heartStopAccumulatedDamage;
    private float mana;
    private String cachedCastingEquipmentSlot;

    public String getCachedCastingEquipmentSlot() {
        return cachedCastingEquipmentSlot;
    }

    public void setCachedCastingEquipmentSlot(String cachedCastingEquipmentSlot) {
        this.cachedCastingEquipmentSlot = cachedCastingEquipmentSlot;
    }

    public float getHeartStopAccumulatedDamage() {
        return heartStopAccumulatedDamage;
    }

    public void setHeartStopAccumulatedDamage(float heartStopAccumulatedDamage) {
        this.heartStopAccumulatedDamage = heartStopAccumulatedDamage;
    }

    public SpinAttackType getSpinAttackType() {
        return spinAttackType;
    }

    public void setSpinAttackType(SpinAttackType spinAttackType) {
        this.spinAttackType = spinAttackType;
    }

    public LearnedSpellData getLearnedSpellData() {
        return learnedSpellData;
    }

    public float getMana() {
        return mana;
    }

    public void setMana(float mana) {
        if (!(this.holder instanceof LivingEntity livingEntity)) {
            this.mana = mana;
            return;
        }
        //fixme: mana event for any entity? just players?
//        ChangeManaEvent e = new ChangeManaEvent(this.serverPlayer, this, this.mana, mana);
//        if (this.serverPlayer == null || !NeoForge.EVENT_BUS.post(e).isCanceled()) {
//            this.mana = e.getNewMana();
//        }
//        if (this.serverPlayer != null) {
//            float maxMana = (float) serverPlayer.getAttributeValue(AttributeRegistry.MAX_MANA);
//            if (this.mana > maxMana) {
//                this.mana = maxMana;
//            }
//        }
        float maxMana = (float) livingEntity.getAttributeValue(AttributeRegistry.MAX_MANA);
        this.mana = Math.min(maxMana, mana);

    }

    public void addMana(float mana) {
        setMana(this.mana + mana);
    }

    public static MagicData get(IAttachmentHolder holder) {
        return holder.getData(DataAttachmentRegistry.MAGIC_DATA);
    }

    public static boolean has(IAttachmentHolder holder) {
        return holder.hasData(DataAttachmentRegistry.MAGIC_DATA);
    }

    @Override
    public void setHolder(IAttachmentHolder holder) {
        this.holder = holder;
    }
}
