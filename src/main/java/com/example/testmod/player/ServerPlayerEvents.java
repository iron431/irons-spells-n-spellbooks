package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.PlayerMagicProvider;
import com.example.testmod.capabilities.magic.network.PacketCancelCast;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.DEDICATED_SERVER)
public class ServerPlayerEvents {

    @SubscribeEvent()
    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer serverPlayer) {
            var cap = event.getEntityLiving().getCapability(PlayerMagicProvider.PLAYER_MAGIC);
            if (cap.isPresent()) {
                var playerMagicData = cap.resolve().get();

                if (playerMagicData.isCasting()
                        && (event.getSlot().getIndex() == 0 || event.getSlot().getIndex() == 1)
                        && (event.getFrom().getItem() instanceof SpellBook || event.getFrom().getItem() instanceof Scroll)) {

                    PacketCancelCast.cancelCast(serverPlayer, SpellType.values()[playerMagicData.getCastingSpellId()].getCastType() == CastType.CONTINUOUS);
                }
            }
        }
    }
}
