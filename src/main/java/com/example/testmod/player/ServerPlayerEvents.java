package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import com.example.testmod.network.PacketCancelCast;
import com.example.testmod.registries.ItemRegistry;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.trading.Merchant;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.world.item.Items.GLASS_BOTTLE;

@Mod.EventBusSubscriber()
public class ServerPlayerEvents {

    @SubscribeEvent()
    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()
                    && (event.getSlot().getIndex() == 0 || event.getSlot().getIndex() == 1)
                    && (event.getFrom().getItem() instanceof SpellBook || event.getFrom().getItem() instanceof Scroll)) {
                serverSideCancelCast(serverPlayer, playerMagicData);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()) {
                serverSideCancelCast(serverPlayer, playerMagicData);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTakeDamage(LivingDamageEvent event) {
        //THIS EVENT IS SERVER SIDE ONLY
        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting() &&
                    SpellType.values()[playerMagicData.getCastingSpellId()].getCastType() == CastType.LONG &&
                    event.getSource() != DamageSource.ON_FIRE &&
                    event.getSource() != DamageSource.WITHER) {
                serverSideCancelCast(serverPlayer, playerMagicData);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClickEntity(PlayerInteractEvent.EntityInteract event) {

        TestMod.LOGGER.debug("onPlayerRightClickEntity {} {}", event.getEntity().getName().getString(), event.getTarget().getName().getString());

        if (event.getEntity().level.isClientSide) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);

            if (serverPlayer.getItemInHand(serverPlayer.getUsedItemHand()).is(GLASS_BOTTLE)) {
                //TestMod.LOGGER.debug("onPlayerRightClickEntity: Glass Bottle");
                if (event.getTarget() instanceof Creeper creeper) {
                    //TestMod.LOGGER.debug("onPlayerRightClickEntity: Creeper");
                    if (creeper.isPowered()) {
                        //TestMod.LOGGER.debug("onPlayerRightClickEntity: Charged");
                        //((ClientLevel) player.level).playSound(player, new BlockPos(player.getX(), player.getY(), player.getZ()), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        ItemStack bottleStack = serverPlayer.getItemInHand(serverPlayer.getUsedItemHand());
                        ItemUtils.createFilledResult(bottleStack, serverPlayer, new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get()));
                        creeper.ignite();
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                    }
                }
            } else if (event.getTarget() instanceof Merchant && playerMagicData.isCasting()) {
                serverSideCancelCast(serverPlayer, playerMagicData);
            }
        }
    }

    private static void serverSideCancelCast(ServerPlayer serverPlayer, PlayerMagicData playerMagicData) {
        PacketCancelCast.cancelCast(serverPlayer, SpellType.values()[playerMagicData.getCastingSpellId()].getCastType() == CastType.CONTINUOUS);
    }
}
