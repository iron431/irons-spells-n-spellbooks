package io.redspace.ironsspellbooks.player;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.FogManager;
import io.redspace.ironsspellbooks.api.util.MusicManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.CustomDescriptionMobEffect;
import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import io.redspace.ironsspellbooks.entity.mobs.wizards.cursed_armor_stand.CursedArmorStandModel;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import io.redspace.skillcasting.data.CastSource;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.data.SkillSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class ClientPlayerEvents {

    @SubscribeEvent
    public static void onPlayerLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        MusicManager.clear();
        GuidingBoltManager.handleClientLogout();
        FogManager.clear();
    }

    @SubscribeEvent
    public static void onClientEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            for (MobEffectInstance inst : livingEntity.getActiveEffects()) {
                if (inst.getEffect().value() instanceof ISyncedMobEffect effect) {
                    effect.clientTick(livingEntity, inst);
                }
            }
        }
    }

    @SubscribeEvent
    public static void beforeLivingRender(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;

        var livingEntity = event.getEntity();
        if (livingEntity.hasEffect(MobEffectRegistry.TRUE_INVISIBILITY) && livingEntity.isInvisibleTo(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void imbuedWeaponTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.getItem() instanceof Scroll) return;
        MinecraftInstanceHelper.ifPlayerPresent((player1) -> {
            var player = (LocalPlayer) player1;
            var lines = event.getToolTip();
            boolean advanced = event.getFlags().isAdvanced();
            // Upgrade Orb tooltip
            if (stack.has(ComponentRegistry.UPGRADE_ORB_TYPE)) {
                handleUpgradeOrbTooltip(stack, player, lines, advanced);
            }
            // Imbued Spell Tooltip
            if (ISkillContainer.isSkillContainer(stack) && !(stack.getItem() instanceof SpellBook)) {
                handleImbuedSpellTooltip(stack, player, lines, advanced);
            }
            // "Can be Imbued" tooltip
            if (ISkillContainer.isSkillContainer(stack) && Utils.canImbue(stack)) {
                var spellContainer = ISkillContainer.get(stack);
                if (spellContainer != null) {
                    lines.add(1, Component.translatable("tooltip.irons_spellbooks.can_be_imbued_frame", Component.translatable("tooltip.irons_spellbooks.can_be_imbued_number", spellContainer.getActiveSkillCount(), spellContainer.getMaxSkillCount()).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD));
                }
            }
        });
    }

    private static void handleImbuedSpellTooltip(ItemStack stack, LocalPlayer player, List<Component> lines, boolean advanced) {
        var spellContainer = ISkillContainer.get(stack);
        int tooltipInjectIndex = advanced ? TooltipsUtils.indexOfAdvancedText(lines, stack) : lines.size();
        // fixme: not respecting "imbued" source and therefore buffs
        CastSource castSource = CastSource.EMPTY;
        if (stack == player.getMainHandItem()) {
            castSource = CastSource.of(EquipmentSlot.MAINHAND);
        } else if (stack == player.getOffhandItem()) {
            castSource = CastSource.of(EquipmentSlot.OFFHAND);
        }
        if (!spellContainer.isEmpty()) {
            var additionalLines = new ArrayList<Component>();
            List<SkillSlot> spellSlots = spellContainer.getActiveSkills().stream().filter(slot -> slot.skillData().getSkill() instanceof AbstractSpell).toList();
            int spellCount = spellSlots.size();
            var header = Component.translatable(spellCount > 1 ? "tooltip.irons_spellbooks.imbued_tooltip_plural" : "tooltip.irons_spellbooks.imbued_tooltip").withStyle(ChatFormatting.GRAY);
            if (spellCount >= 3) {
                additionalLines.add(Component.empty());
                additionalLines.addAll(TooltipsUtils.createSpellAccordion(stack, castSource, player, spellSlots));
            } else {
                // simple imbue display (fully expanded)
                for (var spellSlot : spellSlots) {
                    var spellTooltip = TooltipsUtils.formatActiveSpellTooltip(stack, spellSlot.skillData(), castSource, player);
                    //Indent the title because we'll have an additional header
                    spellTooltip.set(1, Component.literal(" ").append(spellTooltip.get(1)));
                    additionalLines.addAll(spellTooltip);
                }
            }

            //Add header to sword tooltip
            additionalLines.add(1, header);
            lines.addAll(tooltipInjectIndex < 0 ? lines.size() : tooltipInjectIndex, additionalLines);
        }
    }



    private static void handleUpgradeOrbTooltip(ItemStack stack, LocalPlayer player, List<Component> lines, boolean advanced) {
        var upgradeKey = stack.get(ComponentRegistry.UPGRADE_ORB_TYPE);
        if (upgradeKey != null) {
            var upgrade = UpgradeOrbTypeRegistry.upgradeTypeRegistry(player.registryAccess()).get(upgradeKey.location());
            if (upgrade == null) {
                return;
            }
            var newlines = new ArrayList<Component>();
            newlines.add(Component.empty());
            newlines.add(UpgradeOrbItem.TOOLTIP_HEADER);
            var text =
                    Component.literal(" ").append(Component.translatable("attribute.modifier.plus." + upgrade.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(upgrade.amount() * (upgrade.operation() == AttributeModifier.Operation.ADD_VALUE ? 1 : 100)),
                            Component.translatable(upgrade.attribute().value().getDescriptionId())).withStyle(ChatFormatting.BLUE));
            newlines.add(text);
            int i = advanced ? TooltipsUtils.indexOfAdvancedText(lines, stack) : lines.size();
            lines.addAll(i < 0 ? lines.size() : i, newlines);
        }
    }

    private static Attribute getAttributeForDescriptionId(String descriptionId) {
        return BuiltInRegistries.ATTRIBUTE.stream().filter(attribute -> attribute.getDescriptionId().equals(descriptionId)).findFirst().orElse(null);
    }

    @SubscribeEvent
    public static void customPotionTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        var potionData = stack.get(DataComponents.POTION_CONTENTS);
        if (potionData != null) {
            potionData.getAllEffects().forEach(mobEffectInstance -> {
                if (mobEffectInstance.getEffect().value() instanceof CustomDescriptionMobEffect customDescriptionMobEffect) {
                    CustomDescriptionMobEffect.handleCustomPotionTooltip(stack, event.getToolTip(), event.getFlags().isAdvanced(), mobEffectInstance, customDescriptionMobEffect);
                }
            });
        }
    }

    @SubscribeEvent
    public static void changeFogColor(ViewportEvent.ComputeFogColor event) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.hasEffect(MobEffectRegistry.PLANAR_SIGHT)) {
            var color = MobEffectRegistry.PLANAR_SIGHT.get().getColor();
            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = 0.0F;

            f += (float) ((color >> 16 & 255)) / 255.0F;
            f1 += (float) ((color >> 8 & 255)) / 255.0F;
            f2 += (float) ((color >> 0 & 255)) / 255.0F;
            event.setRed(f * .15f);
            event.setGreen(f1 * .15f);
            event.setBlue(f2 * .15f);
        }
    }

    @SubscribeEvent
    public static void onChatReceived(ClientChatReceivedEvent event) {
        //Test if it is a player (main or other) and the message
        if (!FMLLoader.isProduction()) {
            var str = event.getMessage().getString();
            if (str.contains("armorstand")) {
                int id = 0;
                int i = str.indexOf('[');
                double[] ad = new double[3];
                for (int c = 0; c < 100; c++) {
                    int j = str.indexOf(',', i + 1);
                    if (j >= 0) {
                        ad[id++] = Double.parseDouble(str.substring(i + 1, j));
                    } else {
                        ad[id] = Double.parseDouble(str.substring(i + 1, str.indexOf(']')));
                        break;
                    }
                    i = j;
                }
                CursedArmorStandModel.rightArmPos = ad;
            }

        }
    }
}