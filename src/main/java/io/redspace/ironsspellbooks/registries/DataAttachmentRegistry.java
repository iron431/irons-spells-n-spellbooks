package io.redspace.ironsspellbooks.registries;


import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicProvider;
import io.redspace.ironsspellbooks.item.armor.IArmorCapeProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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

    // todo: just use codecs and stuff
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MagicData>> MAGIC_DATA = ATTACHMENT_TYPES.register("magic_data",
            () -> AttachmentType.builder((holder) ->
            {
                if (!(holder instanceof Entity entity)) {
                    throw new IllegalArgumentException("MagicData does not support non-entity attachment holders");
                }
                return new MagicData(entity);
            }).serialize(new PlayerMagicProvider()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IArmorCapeProvider.CapeData>> CAPE_DATA = ATTACHMENT_TYPES.register("cape_data",
            () -> AttachmentType.builder((holder) -> new IArmorCapeProvider.CapeData()).build());
}
