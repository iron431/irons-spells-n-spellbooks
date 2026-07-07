package io.redspace.ironsspellbooks.api.magic;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface IHolderSensitiveData {

    void setHolder(IAttachmentHolder holder);

    static <T extends IHolderSensitiveData> IAttachmentSerializer<?, T> serializerFromCodec(Codec<T> codec) {
        return serializerFromCodec(codec, Predicates.alwaysTrue());
    }

    static <T extends IHolderSensitiveData> IAttachmentSerializer<?, T> serializerFromCodec(Codec<T> codec, Predicate<? super T> shouldSerialize) {
        return new IAttachmentSerializer<>() {
            @Override
            public @NotNull T read(@NotNull IAttachmentHolder holder, @NotNull Tag tag, HolderLookup.@NotNull Provider provider) {
                final DataResult<T> parsingResult = codec.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag);
                T data = parsingResult.getOrThrow(msg -> buildException("read", msg));
                data.setHolder(holder);
                return data;
            }

            @Nullable
            @Override
            public Tag write(@NotNull T attachment, HolderLookup.@NotNull Provider provider) {
                if (!shouldSerialize.test(attachment)) {
                    return null;
                }
                final DataResult<Tag> encodingResult = codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), attachment);
                return encodingResult.getOrThrow(msg -> buildException("write", msg));
            }

            private RuntimeException buildException(final String operation, final String error) {
                return new IllegalStateException("Unable to " + operation + " attachment due to an internal codec error: " + error);
            }
        };
    }
}
