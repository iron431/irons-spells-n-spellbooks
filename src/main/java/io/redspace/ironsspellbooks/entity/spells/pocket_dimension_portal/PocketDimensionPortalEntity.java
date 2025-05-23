package io.redspace.ironsspellbooks.entity.spells.pocket_dimension_portal;

import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PocketDimensionPortalEntity extends PortalEntity {
    public PocketDimensionPortalEntity(Level level, PortalData portalData) {
        this(EntityRegistry.POCKET_DIMENSION_PORTAL.get(), level);
        PortalManager.INSTANCE.addPortalData(uuid, portalData);
        setTicksToLive(portalData.ticksToLive);
    }

    public PocketDimensionPortalEntity(EntityType<? extends PortalEntity> portalEntityEntityType, Level level) {
        super(portalEntityEntityType, level);
    }
}
