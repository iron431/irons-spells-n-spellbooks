package io.redspace.ironsspellbooks.network.casting;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.player.ClientSpellCastHelper;
import io.redspace.ironsspellbooks.setup.IronsAdjustmentModifier;
import io.redspace.skillcastingapi.data.caster_id.CasterId;
import io.redspace.skillcastingapi.data.caster_id.CasterIdStreamCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CastingAnimationPacket(CasterId casterId,
                                     ResourceLocation animationId
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CastingAnimationPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "cast_animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastingAnimationPacket> STREAM_CODEC =
            StreamCodec.composite(
                    CasterIdStreamCodec.instance(), CastingAnimationPacket::casterId,
                    ResourceLocation.STREAM_CODEC, CastingAnimationPacket::animationId,
                    CastingAnimationPacket::new
            );

    public static void handle(CastingAnimationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            IAttachmentHolder holder = packet.casterId.getCaster(Minecraft.getInstance().level);
            if(packet.animationId.getPath().equals("none")){
                if (holder instanceof Player player) {
                    var animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(SpellAnimations.ANIMATION_RESOURCE);
                    if (animation != null) {
                        animation.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(4, Ease.INOUTSINE), null, false);
                        IronsAdjustmentModifier.INSTANCE.fadeOut(5);
                    }
                } else if (holder instanceof AbstractSpellCastingMob mob) {

                }
            }else{

                if (holder instanceof Player player) {
                    //todo: create resourcelocation lookup or something
                    ClientSpellCastHelper.animatePlayerStart(player, packet.animationId);
                } else if (holder instanceof AbstractSpellCastingMob mob) {

                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
