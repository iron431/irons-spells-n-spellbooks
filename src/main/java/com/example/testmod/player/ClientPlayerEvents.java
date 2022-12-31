package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.data.PlayerMagicData;
import com.example.testmod.capabilities.magic.data.PlayerMagicProvider;
import com.example.testmod.capabilities.magic.network.PacketCancelCast;
import com.example.testmod.item.SpellBook;
import com.example.testmod.setup.Messages;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;


public class ClientPlayerEvents {

    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {
        //THIS EVENT IS SERVER SIDE ONLY
        if (event.getEntityLiving() == null) {
            return;
        }

//        TestMod.LOGGER.info("onLivingEquipmentChangeEvent: " + event.getEntityLiving().getName().getString());
//        TestMod.LOGGER.info("onLivingEquipmentChangeEvent: " + event.getEntity().getName().getString());
//        TestMod.LOGGER.info("onLivingEquipmentChangeEvent: " + event.getResult().name());
//        TestMod.LOGGER.info("onLivingEquipmentChangeEvent: " + event.getSlot().getName());
//        TestMod.LOGGER.info("onLivingEquipmentChangeEvent: " + event.getFrom().getItem().getDescription().getString());
//        TestMod.LOGGER.info("onLivingEquipmentChangeEvent: " + event.getTo().getItem().getDescription().getString());

        var cap = event.getEntityLiving().getCapability(PlayerMagicProvider.PLAYER_MAGIC);
        if (cap.isPresent()) {
            var playerMagicData = cap.resolve().get();

            if (playerMagicData.isCasting()) {
                TestMod.LOGGER.info("onLivingEquipmentChangeEvent: Cancel Cast");
                Messages.sendToServer(new PacketCancelCast());
            }
        }
    }

    public static void onLivingEntityUseItemEventStart(LivingEntityUseItemEvent.Start event) {
        TestMod.LOGGER.info("onLivingEntityUseItemEventStart.1");
        if (event.getItem().getItem() instanceof SpellBook) {
            TestMod.LOGGER.info("onLivingEntityUseItemEventStart");
        }
    }

    public static void onLivingEntityUseItemEventFinish(LivingEntityUseItemEvent.Finish event) {
        TestMod.LOGGER.info("onLivingEntityUseItemEventFinish.1");
        if (event.getItem().getItem() instanceof SpellBook) {
            TestMod.LOGGER.info("onLivingEntityUseItemEventFinish");
        }
    }

    public static void onLivingEntityUseItemEventTick(LivingEntityUseItemEvent.Tick event) {
        TestMod.LOGGER.info("onLivingEntityUseItemEventTick.1");
        if (event.getItem().getItem() instanceof SpellBook) {
            TestMod.LOGGER.info("onLivingEntityUseItemEventTick");
        }
    }


    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END) {
            ClientMagicData.getCooldowns().tick(1);
            if (ClientMagicData.castDurationRemaining > 0)
                ClientMagicData.castDurationRemaining--;
        }
    }

    //https://github.com/TeamPneumatic/pnc-repressurized/commit/f901dae5180f378548c288da8f74373e1db631eb
    //https://www.tabnine.com/code/java/classes/net.minecraftforge.client.event.RenderPlayerEvent$Post
    //https://www.programcreek.com/java-api-examples/?code=TeamWizardry%2FWizardry%2FWizardry-master%2Fsrc%2Fmain%2Fjava%2Fcom%2Fteamwizardry%2Fwizardry%2Fclient%2Fcosmetics%2FCapeHandler.java
    //https://forums.minecraftforge.net/topic/87214-1152-rotate-player-arms-and-editremove-animation-when-holding-an-item/

    //This one is 1.18 specific..
    //https://forums.minecraftforge.net/topic/111556-version-1182-solved-change-rendered-mobs-model-under-certain-conditions/

    public static void onPlayerRenderPost(RenderPlayerEvent.Pre event) {
        //TestMod.LOGGER.info("RenderPlayerEvent.Post");

//        if(ClientMagicData.isCasting){
//            TestMod.LOGGER.info("onPlayerRenderPost: isUsingItem:" + event.getPlayer().isUsingItem());
//                event.getPlayer().startUsingItem(InteractionHand.MAIN_HAND);
//        }

//        if (ClientMagicData.getCooldowns().isOnCooldown(SpellType.FIREBALL_SPELL)) {
//
//            var player = event.getPlayer();
//            var render = event.getRenderer();
//            var model = render.getModel();
//            event.getPoseStack().clear();
//            var skinLocation = ((AbstractClientPlayer) player).getSkinTextureLocation();
//
//            //event.getPoseStack().scale(8,2,8);
//
//            model.leftArm.z = 0.0F;
//            model.leftArm.x = 5.0F;
//            model.leftArm.xRot = Mth.cos(model.leftArm.xRot * 0.6662F) * 0.25F;
//            model.leftArm.zRot = -2.3561945F;
//            model.leftArm.yRot = 0.0F;
//
//            model.rightArm.x = -5.0F;
//            model.rightArm.xRot = Mth.cos(model.rightArm.xRot * 0.6662F) * 0.25F;
//            model.rightArm.zRot = 2.3561945F;
//            model.rightArm.yRot = 0.0F;
//
//
//            model.leftArm.render(event.getPoseStack(), event.getMultiBufferSource().getBuffer(model.renderType(skinLocation)), event.getPackedLight(), OverlayTexture.NO_OVERLAY);
//            model.rightArm.render(event.getPoseStack(), event.getMultiBufferSource().getBuffer(model.renderType(skinLocation)), event.getPackedLight(), OverlayTexture.NO_OVERLAY);
//
//        }
    }

    public static void onLivingEntityUseItemEvent(LivingEntityUseItemEvent event) {
        TestMod.LOGGER.info("onLivingEntityUseItemEvent");
        if (event.getItem().getItem() instanceof SpellBook) {
            if (ClientMagicData.isCasting || ClientMagicData.getCooldowns().hasCooldownsActive()) {
                event.setCanceled(true);
            }
        }


    }


}