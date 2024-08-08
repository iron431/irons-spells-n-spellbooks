package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber
public class LurkerRing extends PassiveAbilityCurio {
    public static final int COOLDOWN_IN_TICKS = 15 * 20;
    public static final float MULTIPLIER = 1.5f;

    public LurkerRing() {
        super(new Properties().stacksTo(1), Curios.RING_SLOT);
    }

    @Override
    public Component getDescription(ItemStack stack) {
        return Component.literal(" ").append(Component.translatable(this.getDescriptionId() + ".desc",
                (int) ((MULTIPLIER - 1) * 100)
        )).withStyle(descriptionStyle);
    }

    @Override
    protected int getCooldownTicks() {
        return COOLDOWN_IN_TICKS;
    }

    @SubscribeEvent
    public static void handleAbility(LivingIncomingDamageEvent event) {
        var RING = ((LurkerRing) ItemRegistry.LURKER_RING.get());
        if (event.getSource().getEntity() instanceof ServerPlayer attackingPlayer) {
            if (attackingPlayer.isInvisible() && RING.isEquippedBy(attackingPlayer) && RING.tryProcCooldown(attackingPlayer)) {
                event.setAmount(event.getAmount() * MULTIPLIER);
            }
        }
    }
}
