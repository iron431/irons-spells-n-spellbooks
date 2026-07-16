package io.redspace.ironsspellbooks.api.magic;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.data.AbstractSkill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LearnedSpellData {
    public static final Codec<LearnedSpellData> CODEC = Codec.list(ResourceLocation.CODEC).xmap(LearnedSpellData::new, data -> new ArrayList<>(data.learnedSpells));
    public static final StreamCodec<RegistryFriendlyByteBuf, LearnedSpellData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.learnedSpells.size());
                for (ResourceLocation spell : data.learnedSpells) {
                    buf.writeResourceLocation(spell);
                }
            },
            buf -> {
                int count = buf.readVarInt();
                LearnedSpellData data = new LearnedSpellData();
                for (int i = 0; i < count; i++) {
                    data.learnedSpells.add(buf.readResourceLocation());
                }
                return data;
            });

    private LearnedSpellData(Collection<ResourceLocation> spells) {
        learnedSpells.addAll(spells);
    }

    public LearnedSpellData() {
    }

    public final Set<ResourceLocation> learnedSpells = new HashSet<>();

    public boolean isLearned(AbstractSkill skill) {
        return learnedSpells.contains(skill.getSkillId());
    }

    /**
     * @return true if the skill was added (not already learned)
     */
    public boolean add(AbstractSkill skill) {
        return learnedSpells.add(skill.getSkillId());
    }

    /**
     * @return true if skill was removed, false if skill was not present
     */
    public boolean remove(AbstractSkill skill) {
        return learnedSpells.remove(skill.getSkillId());
    }

    public void clear() {
        this.learnedSpells.clear();
    }

}
