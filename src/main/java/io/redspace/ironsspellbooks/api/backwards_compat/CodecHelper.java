package io.redspace.ironsspellbooks.api.backwards_compat;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class CodecHelper {

    public static <T> Codec<T> withAlternative(final Codec<T> primary, final Codec<? extends T> alternative) {
        return Codec.either(
                primary,
                alternative
        ).xmap(
                either -> either.map(Function.identity(), Function.identity()),
                Either::left
        );
    }

    public static <T> T get(Codec<T> codec, Tag rawDataTag) {
        return codec.decode(NbtOps.INSTANCE, rawDataTag).getOrThrow(false, IronsSpellbooks.LOGGER::error).getFirst();
    }

    public static <T> T getOrElse(Codec<T> codec, Tag rawDataTag, T backup) {
        return codec.decode(NbtOps.INSTANCE, rawDataTag).get().map(Pair::getFirst, (pair) -> backup);
    }

    public static <T> T get(Codec<T> codec, ItemStack stack, String nbt) {
        return get(codec, stack.getOrCreateTag().get(nbt));
    }

    public static <T> T getOrElse(ItemStack stack, String nbt, Codec<T> codec, T empty) {
        return stack.hasTag() && stack.getOrCreateTag().contains(nbt) ?
                get(codec, stack.getOrCreateTag().get(nbt))
                : empty;
    }

    public static <T> T getWithLegacy(Codec<T> codec, ItemStack stack, String nbt, String legacyNbt, Codec<T> legacyCodec) {
        var tag = stack.getOrCreateTag();
        if (tag.contains(nbt)) {
            return get(codec, tag.get(nbt));
        } else {
            var data = get(legacyCodec, tag.get(legacyNbt));
            tag.remove(legacyNbt);
            tag.put(nbt, codec.encode(data, NbtOps.INSTANCE, NbtOps.INSTANCE.empty()).getOrThrow(false, IronsSpellbooks.LOGGER::error));
            return data;
        }
    }

    public static <T> T getOrElseWithLegacy(ItemStack stack, String nbt, Codec<T> codec, T empty, String legacyNbt, Codec<T> legacyCodec) {
        if (hasWithLegacy(stack, nbt, legacyNbt)) {
            return getWithLegacy(codec, stack, nbt, legacyNbt, legacyCodec);
        }
        return empty;
    }

    public static boolean hasWithLegacy(ItemStack stack, String nbt, String legacyNbt) {
        return stack.hasTag() && (stack.getOrCreateTag().contains(nbt) || stack.getOrCreateTag().contains(legacyNbt));
    }

    public static <T> void set(ItemStack stack, String nbt, Codec<T> codec, T data) {
        stack.getOrCreateTag().put(nbt, codec.encode(data, NbtOps.INSTANCE, NbtOps.INSTANCE.empty()).getOrThrow(false, IronsSpellbooks.LOGGER::error));
    }

    public static boolean has(ItemStack stack, String nbt) {
        return stack.hasTag() && stack.getOrCreateTag().contains(nbt);
    }

    public static <E> Codec<E> createLegacyCodec(Function<Tag, E> decoder) {
        return Codec.of(
                Encoder.error("Legacy codec should never write!"),
                new Decoder<>() {
                    @Override
                    public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
                        try {
                            var data = decoder.apply((Tag) input);
                            return DataResult.success(com.mojang.datafixers.util.Pair.of(data, input));
                        } catch (Exception e) {
                            return DataResult.error(e::getMessage);
                        }
                    }
                }
        );
    }
}
