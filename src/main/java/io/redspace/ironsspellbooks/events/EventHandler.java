package io.redspace.ironsspellbooks.events;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.events.SpellCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid= IronsSpellbooks.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public static void playerHealedEvent(SpellHealEvent healEvent) {
        if (healEvent.isCanceled()) return;

        LivingEntity targetEntity = healEvent.getTargetEntity();
        if(targetEntity == null) return;

        if(targetEntity.getHealth() == targetEntity.getMaxHealth()) {
            healEvent.getEntity().sendSystemMessage(Component.literal(targetEntity.getName() + " is at max health. What a waste."));
        } else {
            healEvent.getEntity().sendSystemMessage(Component.literal("You healed " + targetEntity.getName() + " for " + healEvent.getHealAmount() + " with a level "));
        }
    }

    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent spellCastEvent) {
        if (spellCastEvent.isCanceled()) return;
        spellCastEvent.setCanceled(true); // cancels the spell
    }
}
