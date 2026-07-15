package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class WickedBoneRingItem extends CurioBaseItem {
    public WickedBoneRingItem(Properties properties) {
        super(properties);
        withAttributes(Curios.RING_SLOT, new AttributeContainer(AttributeRegistry.SPELL_RICOCHET, 1, AttributeModifier.Operation.ADD_VALUE));
    }
}
