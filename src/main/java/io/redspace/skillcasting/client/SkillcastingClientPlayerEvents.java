package io.redspace.skillcasting.client;

import io.redspace.skillcasting.client.render.SkillcastClientTickManager;
import io.redspace.skillcasting.client.render.SkillcastLevelRenderableManager;
import io.redspace.skillcasting.data.cast.ActiveCast;
import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.network.ServerboundCancelSkillCastPacket;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public final class SkillcastingClientPlayerEvents {

    @SubscribeEvent
    public static void onPlayerOpenScreen(ScreenEvent.Opening event) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        SkillcastingData data = SkillcastingData.get(player);
        if (!data.isCasting()) {
            return;
        }
        PacketDistributor.sendToServer(new ServerboundCancelSkillCastPacket());
    }

    @SubscribeEvent
    public static void onCalculatePlayerSpeed(MovementInputUpdateEvent event) {
        if (SkillcastingData.get(event.getEntity()).isCasting()) {
            ActiveCast cast = SkillcastingData.get(event.getEntity()).getActiveCast();
            float speed = Mth.clamp(cast.context().getOrDefault(SkillcastingComponentTypes.CASTING_MOVESPEED_MULTIPLIER, 1f),
                    0, 1);
            event.getInput().forwardImpulse *= speed;
            event.getInput().leftImpulse *= speed;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SkillcastClientTickManager.clear();
        SkillcastLevelRenderableManager.clear();
    }

    @SubscribeEvent
    public static void handleTooltips(ItemTooltipEvent event) {
        // Active Spell Tooltip
        ItemStack stack = event.getItemStack();
        if (stack.has(SkillcastingDataComponents.CASTING_IMPLEMENT)) {
            handleCastingImplementTooltip(stack, null, event.getToolTip(), event.getFlags().isAdvanced());
        }
    }

    private static void handleCastingImplementTooltip(ItemStack stack, LocalPlayer player, List<Component> lines, boolean advanced) {
        lines.add(1, Component.literal(" ").append(Component.translatable("tooltip.irons_spellbooks.press_to_cast_active", Component.keybind("key.use")).withStyle(ChatFormatting.GOLD)));
        // fixme skillcasting: rewrite ig
//
//        var spellSlot = ClientMagicData.getSpellSelectionManager().getSelection();
//        if (spellSlot != null && spellSlot.spellData != SpellData.EMPTY) {
//            var additionalLines = TooltipsUtils.formatActiveSpellTooltip(stack, spellSlot.spellData, spellSlot.getCastSource(), player);
//            //Add header
//            additionalLines.add(1, Component.translatable("tooltip.irons_spellbooks.casting_implement_tooltip").withStyle(ChatFormatting.GRAY));
//            //Indent the title because we have an additional header
//            additionalLines.set(2, Component.literal(" ").append(additionalLines.get(2)));
//            //Keybind notification
//            additionalLines.add(Component.literal(" ").append(Component.translatable("tooltip.irons_spellbooks.press_to_cast_active", Component.keybind("key.use")).withStyle(ChatFormatting.GOLD)));
//            int i = advanced ? TooltipsUtils.indexOfAdvancedText(lines, stack) : lines.size();
//            lines.addAll(i < 0 ? lines.size() : i, additionalLines);
//        }
    }
}
