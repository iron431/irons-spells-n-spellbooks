package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber
public class WickedBoneRingItem extends SimpleDescriptiveCurio {
    public WickedBoneRingItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON).fireResistant(), Curios.RING_SLOT);
    }

    @SubscribeEvent
    public static void handleAbility(EntityJoinLevelEvent event) {
        if (!event.loadedFromDisk() && event.getEntity() instanceof AbstractMagicProjectile magicProjectile) {
            var owner = magicProjectile.getOwner();
            if (owner instanceof LivingEntity livingEntity) {
                int ringCount = CuriosApi.getCuriosInventory(livingEntity).map(inventory -> inventory.findCurios(ItemRegistry.WICKED_BONE_RING.get()).size()).orElse(0);
                int r = magicProjectile.getRicochetLevel();
                //fixme: manual circumvention of infinite terminator as -1 is unsustainable
                if (ringCount > 0 && r >= 0) {
                    magicProjectile.setRicochetLevel(r + ringCount);
                }
            }
        }
    }
}
