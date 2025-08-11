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
            Codec.FLOAT.optionalFieldOf("heartstopDamage", 0f).forGetter(ActualMagicData::getHeartstopAccumulatedDamage)
            //todo:
            // spin attack type
            // learned spell data
    ).apply(builder, ActualMagicData::temp));

    private static ActualMagicData temp(double mana, float hearstop) {
        var d = new ActualMagicData();
        d.mana = mana;
        d.heartStopAccumulatedDamage = hearstop;
        return d;
    }

    public double getMana() {
        return mana;
    }

    private float getHeartstopAccumulatedDamage() {
        return heartStopAccumulatedDamage;
    }

    private double mana;
    private float heartStopAccumulatedDamage;
    private SpinAttackType spinAttackType;
    private LearnedSpellData learnedSpellData;

    public static ActualMagicData get(IAttachmentHolder holder) {
        return holder.getData(DataAttachmentRegistry.MAGIC_DATA);
    }

    public void setMana(double newMana) {
        //todo: events
        this.mana = newMana;
    }
}
