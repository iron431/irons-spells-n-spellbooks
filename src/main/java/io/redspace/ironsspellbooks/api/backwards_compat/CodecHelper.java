package io.redspace.ironsspellbooks.api.backwards_compat;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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

    public static <T> T get(Codec<T> codec, ItemStack stack, String nbt) {
        return get(codec, stack.getOrCreateTag().get(nbt));
    }

    public static <T> T getOrElse(ItemStack stack, String nbt, Codec<T> codec, T empty) {
        return stack.hasTag() && stack.getOrCreateTag().contains(nbt) ?
                get(codec, stack.getOrCreateTag().get(nbt))
                : empty;
    }

    public static <T> void set(ItemStack stack, String nbt, Codec<T> codec, T data) {
        stack.getOrCreateTag().put(nbt, codec.encode(data, NbtOps.INSTANCE, NbtOps.INSTANCE.empty()).getOrThrow(false, IronsSpellbooks.LOGGER::error));
    }

    public static boolean has(ItemStack stack, String nbt) {
        return stack.hasTag() && stack.getOrCreateTag().contains(nbt);
    }
}
