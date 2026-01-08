package io.redspace.ironsspellbooks.patreon.transmog;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.redspace.ironsspellbooks.patreon.PatreonPermissions;
import io.redspace.ironsspellbooks.util.MemoizedSupplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public record TransmogHolder(ResourceLocation id, PatreonPermissions requiredPermission,
                             MemoizedSupplier<GeoArmorRenderer<?>> memoizedSupplier) {
    public static final Codec<TransmogHolder> CODEC = ResourceLocation.CODEC.xmap(TransmogManager::get, TransmogHolder::id);
    // todo: dedicated stream codec would be more efficient
    public static final StreamCodec<ByteBuf, TransmogHolder> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public @NotNull GeoArmorRenderer<?> getArmorRenderer() {
        return memoizedSupplier.get();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof TransmogHolder oth && this.id.equals(oth.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String descriptionId() {
        return String.format("transmog.%s.%s", id.getNamespace(), id.getPath());
    }
}
