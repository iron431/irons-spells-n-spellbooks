package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import top.theillusivec4.curios.api.CuriosApi;

public class WickedBoneRingItem extends CurioBaseItem {
    public WickedBoneRingItem(Properties properties) {
        super(properties);
        withAttributes(Curios.RING_SLOT, new AttributeContainer(AttributeRegistry.SPELL_RICOCHET, 1, AttributeModifier.Operation.ADD_VALUE));
    }
}
