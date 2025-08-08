package io.redspace.ironsspellbooks.registries;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.attachment.AttachmentType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.NeoForgeRegistries;


public class DataAttachmentRegistry {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static final RegistryObject<AttachmentType<?>> MAGIC_DATA = ATTACHMENT_TYPES.register("magic_data",
            () -> AttachmentType.builder((holder) -> holder instanceof ServerPlayer serverPlayer ? new MagicData(serverPlayer) : new MagicData()).serialize(new PlayerMagicProvider()).build());
}
