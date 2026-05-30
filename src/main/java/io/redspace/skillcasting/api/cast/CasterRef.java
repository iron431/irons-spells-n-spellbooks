package io.redspace.skillcasting.api.cast;

import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.NotNull;

public interface CasterRef {
    static EntityCasterRef entity(@NotNull Entity entity) {
        return new EntityCasterRef(entity);
    }

    static BlockCasterRef block(@NotNull BlockEntity blockEntity) {
        return new BlockCasterRef(blockEntity);
    }

    CasterId id();

    boolean isValid();

    Level level();

    Vec3 position(PositionAnchor anchor);

    Vec3 forward();

    default SkillcastingData skillcastingData() {
        return SkillcastingData.get(get());
    }

    void distributeToClients(CustomPacketPayload payload);

    IAttachmentHolder get();
}
