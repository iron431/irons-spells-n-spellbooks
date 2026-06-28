package io.redspace.ironsspellbooks.api.magic;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.api.network.ISerializable;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//TODO: refactor learned spell data to use INBTSerializable instead of this custom deal
public class LearnedSpellData implements ISerializable {
    public static final Codec<LearnedSpellData> CODEC = Codec.list(ResourceLocation.CODEC).xmap(LearnedSpellData::new, data -> new ArrayList<>(data.learnedSpells));
    public static final StreamCodec<RegistryFriendlyByteBuf, LearnedSpellData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> data.writeToBuffer(buf),
            (buf) -> {
                var data = new LearnedSpellData();
                data.readFromBuffer(buf);
                return data;
            }
    );

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
    public boolean add(AbstractSkill skill){
        return learnedSpells.add(skill.getSkillId());
    }

    /**
     * @return true if skill was removed, false if skill was not present
     */
    public boolean remove(AbstractSkill skill){
        return learnedSpells.remove(skill.getSkillId());
    }
    /// ///////////////////////////////////
    /// ///////////////////////////////////
    public static final String LEARNED_SPELLS = "learnedSpells";

    @Deprecated(forRemoval = true)
    public void saveToNBT(CompoundTag compound) {
        if (!learnedSpells.isEmpty()) {
            ListTag listTag = new ListTag();
            for (ResourceLocation resourceLocation : learnedSpells) {
                listTag.add(StringTag.valueOf(resourceLocation.toString()));
            }
            compound.put(LEARNED_SPELLS, listTag);
        }
    }
    @Deprecated(forRemoval = true)

    public void loadFromNBT(CompoundTag compound) {
        ListTag learnedTag = (ListTag) compound.get(LEARNED_SPELLS);
        if (learnedTag != null && !learnedTag.isEmpty()) {
            for (Tag tag : learnedTag) {
                if (tag instanceof StringTag stringTag) {
                    ResourceLocation resourceLocation = ResourceLocation.parse(stringTag.getAsString());
                    if (SpellRegistry.getSpell(resourceLocation) != null) {
                        learnedSpells.add(resourceLocation);
                    }
                }
            }
        }
    }
    @Deprecated(forRemoval = true)

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeInt(learnedSpells.size());
        for (ResourceLocation resourceLocation : learnedSpells) {
            buf.writeResourceLocation(resourceLocation);
        }
    }
    @Deprecated(forRemoval = true)

    @Override
    public void readFromBuffer(FriendlyByteBuf buf) {
        int i = buf.readInt();
        if (i > 0) {
            for (int j = 0; j < i; j++) {
                ResourceLocation resourceLocation = buf.readResourceLocation();
                if (SpellRegistry.REGISTRY.get(resourceLocation) != null) {
                    learnedSpells.add(resourceLocation);
                }
            }
        }
    }
}
