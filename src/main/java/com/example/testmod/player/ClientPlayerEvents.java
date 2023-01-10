package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.capabilities.magic.PlayerMagicProvider;
import com.example.testmod.network.PacketCancelCast;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientPlayerEvents {
    //
    //  Cancel Cast if we Open a Container
    //
    @SubscribeEvent
    public static void onPlayerOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getEntity().level.isClientSide)
            return;

        if (serverSideIsCasting(event.getEntity()))
            Messages.sendToServer(new PacketCancelCast(false));

    }

    //
    //  Cancel Cast if we Interact with an entity (horse, villager, minecart, etc)
    //
    @SubscribeEvent
    public static void onPlayerRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getEntity().level.isClientSide)
            return;

        if (serverSideIsCasting(event.getEntity()))
            Messages.sendToServer(new PacketCancelCast(false));
    }

    //
    //  Cancel Long Cast if we take non-dot damage
    //
    @SubscribeEvent
    public static void onPlayerTakeDamage(LivingDamageEvent event) {
        //THIS EVENT IS SERVER SIDE ONLY
        if (event.getEntity() == null) {
            return;
        }

        /*
        ON_FIRE is lingering fire ticks
        IN_FIRE will still cancel cast

        Poison (id 19) is magic damage
        Wither (id 20) is wither damage
        Poison is not the only source of magic damage (wither is)

        We shall make magic damage interrupt cast, so poison will cancel.
         */
        var cap = event.getEntity().getCapability(PlayerMagicProvider.PLAYER_MAGIC);
        if (cap.isPresent()) {
            var playerMagicData = cap.resolve().get();

            if (playerMagicData.isCasting() &&
                    SpellType.values()[playerMagicData.getCastingSpellId()].getCastType() == CastType.LONG &&
                    event.getSource() != DamageSource.ON_FIRE &&
                    event.getSource() != DamageSource.WITHER) {
                TestMod.LOGGER.debug("onPlayerTakeDamage: Cancel Cast");
                Messages.sendToServer(new PacketCancelCast(false));
            }
        }
    }

    //
    //  Handle (Client Side) cast duration
    //
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END) {
            ClientMagicData.getCooldowns().tick(1);
            if (ClientMagicData.castDurationRemaining > 0)
                ClientMagicData.castDurationRemaining--;
        }
    }

    //
    //  Helper Methods
    //
    private static boolean serverSideIsCasting(Player player) {
        var playerMagicData = getPlayerMagicData(player);
        if (playerMagicData != null)
            return playerMagicData.isCasting();
        else
            return false;
    }

    private static PlayerMagicData getPlayerMagicData(Player player) {
        var cap = player.getCapability(PlayerMagicProvider.PLAYER_MAGIC);
        if (cap.isPresent()) {
            var playerMagicData = cap.resolve().get();

            return playerMagicData;
        } else {
            return null;
        }
    }


    //https://github.com/TeamPneumatic/pnc-repressurized/commit/f901dae5180f378548c288da8f74373e1db631eb
    //https://www.tabnine.com/code/java/classes/net.minecraftforge.client.event.RenderPlayerEvent$Post
    //https://www.programcreek.com/java-api-examples/?code=TeamWizardry%2FWizardry%2FWizardry-master%2Fsrc%2Fmain%2Fjava%2Fcom%2Fteamwizardry%2Fwizardry%2Fclient%2Fcosmetics%2FCapeHandler.java
    //https://forums.minecraftforge.net/topic/87214-1152-rotate-player-arms-and-editremove-animation-when-holding-an-item/

    //This one is 1.18 specific..
    //https://forums.minecraftforge.net/topic/111556-version-1182-solved-change-rendered-mobs-model-under-certain-conditions/

    public static void onPlayerRenderPost(RenderPlayerEvent.Pre event) {
        //TestMod.LOGGER.debug("RenderPlayerEvent.Post");

//        if(ClientMagicData.isCasting){
//            TestMod.LOGGER.debug("onPlayerRenderPost: isUsingItem:" + event.getPlayer().isUsingItem());
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

}