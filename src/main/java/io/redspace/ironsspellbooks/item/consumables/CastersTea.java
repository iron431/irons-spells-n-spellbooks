package io.redspace.ironsspellbooks.item.consumables;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CastersTea extends DrinkableItem {
    public CastersTea(Properties pProperties) {
        super(pProperties, CastersTea::onConsume, null, true);
    }

    private static void onConsume(ItemStack itemStack, LivingEntity livingEntity) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            var cooldowns = MagicData.getPlayerMagicData(livingEntity).getPlayerCooldowns();
            cooldowns.getSpellCooldowns().forEach((key, value) -> cooldowns.decrementCooldown(value, (int) (value.getSpellCooldown() * .15f)));
            cooldowns.syncToPlayer(serverPlayer);
        }
    }
}
