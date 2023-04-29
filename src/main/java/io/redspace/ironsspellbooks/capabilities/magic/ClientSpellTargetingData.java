package io.redspace.ironsspellbooks.capabilities.magic;

import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class ClientSpellTargetingData {
    public UUID targetUUID;
    public int spellId;

    public ClientSpellTargetingData() {
        targetUUID = null;
    }

    public ClientSpellTargetingData(UUID targetUUID, int spellId) {
        this.targetUUID = targetUUID;
        this.spellId = spellId;
    }

    public boolean isTargeted(LivingEntity livingEntity) {
        return livingEntity.getUUID().equals(targetUUID);
    }

    public boolean isTargeted(UUID uuid) {
        return uuid.equals(targetUUID);
    }
}
