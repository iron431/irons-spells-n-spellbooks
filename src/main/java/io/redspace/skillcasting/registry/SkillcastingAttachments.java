package io.redspace.skillcasting.registry;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class SkillcastingAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Skillcasting.NAMESPACE);

    private SkillcastingAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SkillcastingData>> SKILLCASTING_DATA =
            ATTACHMENT_TYPES.register("skillcasting_data", () -> AttachmentType
                    .builder(holder -> new SkillcastingData())
                    .serialize(SkillcastingData.CODEC, SkillcastingData::isLive)
                    .copyOnDeath()
                    .build());
}
