package io.redspace.ironsspellbooks.capabilities.magic;

import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClientSpellTargetingData {
    public ArrayList<UUID> targetUUIDs;
    public String spellId;

    public ClientSpellTargetingData() {
        targetUUIDs = new ArrayList<>();
    }

    public ClientSpellTargetingData(String spellId, UUID... targetUUID) {
        this();
        targetUUIDs.addAll(Arrays.asList(targetUUID));
        this.spellId = spellId;
    }

    public ClientSpellTargetingData(String spellId, List<UUID> uuids) {
        this();
        targetUUIDs.addAll(uuids);
        this.spellId = spellId;
    }
    public boolean isTargeted(LivingEntity livingEntity) {
        return targetUUIDs.contains(livingEntity.getUUID());
    }

    public boolean isTargeted(UUID uuid) {
        return targetUUIDs.contains(uuid);
    }

    @Override
    public String toString() {
        return targetUUIDs.toString();
    }
}
