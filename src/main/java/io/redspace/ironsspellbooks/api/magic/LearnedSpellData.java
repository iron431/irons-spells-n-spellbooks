package io.redspace.ironsspellbooks.api.magic;

import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.skillcastingapi.data.AbstractSkill;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LearnedSpellData {
    public static final String LEARNED_SPELLS = "learnedSpells";

    private final Set<ResourceLocation> learnedSpells = new HashSet<>();

    public static final Codec<LearnedSpellData> CODEC = Codec.list(ResourceLocation.CODEC).xmap(list -> {
        var d = new LearnedSpellData();
        d.learnedSpells.addAll(list);
        return d;
    }, d -> new ArrayList<>(d.learnedSpells));
    public static final StreamCodec<RegistryFriendlyByteBuf, LearnedSpellData> STREAM_CODEC = StreamCodec.of(LearnedSpellData::writeToBuffer, LearnedSpellData::readFromBuffer);

    //    public void saveToNBT(CompoundTag compound) {
//        if (!learnedSpells.isEmpty()) {
//            ListTag listTag = new ListTag();
//            for (ResourceLocation resourceLocation : learnedSpells) {
//                listTag.add(StringTag.valueOf(resourceLocation.toString()));
//            }
//            compound.put(LEARNED_SPELLS, listTag);
//        }
//    }
//
//    public void loadFromNBT(CompoundTag compound) {
//        ListTag learnedTag = (ListTag) compound.get(LEARNED_SPELLS);
//        if (learnedTag != null && !learnedTag.isEmpty()) {
//            for (Tag tag : learnedTag) {
//                if (tag instanceof StringTag stringTag) {
//                    ResourceLocation resourceLocation = ResourceLocation.parse(stringTag.getAsString());
//                    if (SpellRegistry.getSpell(resourceLocation) != null) {
//                        learnedSpells.add(resourceLocation);
//                    }
//                }
//            }
//        }
//    }
    public void learnSpell(AbstractSkill spell) {
        //todo: validate spellskill?
        this.learnedSpells.add(spell.getId());
    }

    public void forgetAllSpells() {
        this.learnedSpells.clear();
    }

    public boolean isLearned(AbstractSkill spell) {
        return this.learnedSpells.contains(spell.getId());
    }

    public static void writeToBuffer(FriendlyByteBuf buf, LearnedSpellData data) {
        buf.writeInt(data.learnedSpells.size());
        for (ResourceLocation resourceLocation : data.learnedSpells) {
            buf.writeResourceLocation(resourceLocation);
        }
    }

    public static LearnedSpellData readFromBuffer(FriendlyByteBuf buf) {
        LearnedSpellData data = new LearnedSpellData();
        int i = buf.readInt();
        if (i > 0) {
            for (int j = 0; j < i; j++) {
                ResourceLocation resourceLocation = buf.readResourceLocation();
                if (SpellRegistry.REGISTRY.get(resourceLocation) != null) {
                    data.learnedSpells.add(resourceLocation);
                }
            }
        }
        return data;
    }
}
