package io.redspace.ironsspellbooks.registries;


import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicProvider;
import io.redspace.ironsspellbooks.effect.EchoingStrikesData;
import io.redspace.ironsspellbooks.item.armor.IArmorCapeProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;


public class DataAttachmentRegistry {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MagicData>> MAGIC_DATA = ATTACHMENT_TYPES.register("magic_data",
            () -> AttachmentType.builder((holder) -> holder instanceof ServerPlayer serverPlayer ? new MagicData(serverPlayer) : new MagicData()).serialize(new PlayerMagicProvider()).sync(MagicData.STREAM_CODEC).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IArmorCapeProvider.CapeData>> CAPE_DATA = ATTACHMENT_TYPES.register("cape_data",
            () -> AttachmentType.builder((holder) -> new IArmorCapeProvider.CapeData()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EchoingStrikesData>> ECHOING_STRIKES_DATA = ATTACHMENT_TYPES.register("echoing_strikes_data",
            () -> AttachmentType.builder(EchoingStrikesData::new).serialize(Codec.INT.xmap(EchoingStrikesData::new, EchoingStrikesData::getHitCount), EchoingStrikesData::hasHitsRemaining).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Unit>> OAKSKIN_FROM_ELIXIR = ATTACHMENT_TYPES.register("oakskin_data",
            () -> AttachmentType.builder(holder -> Unit.INSTANCE).serialize(Unit.CODEC).build());
}
