package io.redspace.ironsspellbooks.capabilities.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.magic.LearnedSpellData;
import io.redspace.ironsspellbooks.player.SpinAttackType;
import io.redspace.ironsspellbooks.registries.DataAttachmentRegistry;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public class ActualMagicData {
    public static final Codec<ActualMagicData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.DOUBLE.fieldOf("mana").forGetter(ActualMagicData::getMana),
            Codec.FLOAT.optionalFieldOf("heartstopDamage", 0f).forGetter(ActualMagicData::getHeartstopAccumulatedDamage),
            LearnedSpellData.CODEC.optionalFieldOf("learnedSpells", new LearnedSpellData()).forGetter(ActualMagicData::getLearnedSpellData)
            //todo:
            // spin attack type
    ).apply(builder, ActualMagicData::temp));

    private static ActualMagicData temp(double mana, float heartstop, LearnedSpellData learnedSpellData) {
        var d = new ActualMagicData();
        d.mana = mana;
        d.heartStopAccumulatedDamage = heartstop;
        d.learnedSpellData = learnedSpellData;
        return d;
    }

    public double getMana() {
        return mana;
    }

    public float getHeartstopAccumulatedDamage() {
        return heartStopAccumulatedDamage;
    }

    public void setHeartstopAccumulatedDamage(float heartStopAccumulatedDamage) {
        this.heartStopAccumulatedDamage = heartStopAccumulatedDamage;
    }


    private double mana;
    private float heartStopAccumulatedDamage;
    private LearnedSpellData learnedSpellData;
    private SpinAttackType spinAttackType;

    public static ActualMagicData get(IAttachmentHolder holder) {
        return holder.getData(DataAttachmentRegistry.MAGIC_DATA);
    }

    public LearnedSpellData getLearnedSpellData() {
        return learnedSpellData;
    }

    public void setMana(double newMana) {
        //todo: events
        this.mana = newMana;
    }

    public void addMana(double addition) {
        setMana(this.mana + addition);
    }

    public SpinAttackType getSpinAttackType() {
        return spinAttackType;
    }

    public void setSpinAttackType(SpinAttackType spinAttackType) {
        this.spinAttackType = spinAttackType;
    }
}
