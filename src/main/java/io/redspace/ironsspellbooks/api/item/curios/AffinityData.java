package io.redspace.ironsspellbooks.api.item.curios;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class AffinityData {
    public static final Codec<AffinityData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf(SpellData.SPELL_ID).forGetter(data -> data.spellId),
            Codec.INT.optionalFieldOf("bonus", 1).forGetter(data -> data.bonus)
    ).apply(builder, AffinityData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AffinityData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeUtf(data.spellId);
                buf.writeInt(data.bonus);
            },
            (buf) -> new AffinityData(buf.readUtf(), buf.readInt()));


    //public static final String ISB_ENHANCE = "ISBEnhance";
    String spellId;
    int bonus;
    public static final AffinityData NONE = new AffinityData(SpellRegistry.none().getSpellId());

    private AffinityData(String id) {
        this(id, 1);
    }

    private AffinityData(AbstractSpell spell) {
        this(spell.getSpellId());
    }

    private AffinityData(String id, int bonus) {
        this.spellId = id;
        this.bonus = bonus;
    }

    public static AffinityData getAffinityData(ItemStack stack) {
        return stack.has(ComponentRegistry.AFFINITY_COMPONENT) ? stack.get(ComponentRegistry.AFFINITY_COMPONENT) : AffinityData.NONE;
    }

    public static void setAffinityData(ItemStack stack, AbstractSpell spell) {
        stack.set(ComponentRegistry.AFFINITY_COMPONENT.value(), new AffinityData(spell));
    }

    public static boolean hasAffinityData(ItemStack itemStack) {
        return itemStack.has(ComponentRegistry.AFFINITY_COMPONENT);
    }

    public AbstractSpell getSpell() {
        return SpellRegistry.getSpell(spellId);
    }

    public String getNameForItem() {
        return getSpell() == SpellRegistry.none() ? Component.translatable("tooltip.irons_spellbooks.no_affinity").getString() : getSpell().getSchoolType().getDisplayName().getString();
    }
}
