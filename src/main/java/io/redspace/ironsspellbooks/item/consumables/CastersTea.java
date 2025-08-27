package io.redspace.ironsspellbooks.item.consumables;

import io.redspace.skillcastingapi.data.SkillcastingData;
import io.redspace.skillcastingapi.data.caster_id.EntityCasterId;
import io.redspace.skillcastingapi.network.packets.SyncSkillcastingDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class CastersTea extends DrinkableItem {
    public CastersTea(Properties pProperties) {
        super(pProperties, CastersTea::onConsume, null, true);
    }

    private static void onConsume(ItemStack itemStack, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            var cooldowns = SkillcastingData.get(livingEntity).getCooldowns();
            cooldowns.getCooldowns().forEach((key, value) -> cooldowns.decrementCooldown(value, (int) (value.getCooldownLength() * .15f)));
//            cooldowns.syncToPlayer(serverPlayer);
            PacketDistributor.sendToPlayer(serverPlayer, SyncSkillcastingDataPacket.builder().cooldowns(SkillcastingData.get(serverPlayer).getCooldowns()).build(EntityCasterId.of(serverPlayer)));
        }
    }
}
