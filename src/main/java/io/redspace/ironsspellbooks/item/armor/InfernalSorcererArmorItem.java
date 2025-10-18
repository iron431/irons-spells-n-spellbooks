package io.redspace.ironsspellbooks.item.armor;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.ImmolateEffect;
import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import io.redspace.ironsspellbooks.entity.armor.InfernalSorcererArmorModel;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.List;

public class InfernalSorcererArmorItem extends ImbuableChestplateArmorItem {
    public static final int COOLDOWN_TICKS = 1 * 20;

    public InfernalSorcererArmorItem(Type type, Properties settings) {
        super(ExtendedArmorMaterials.INFERNAL_SORCERER, type, settings,
                new AttributeContainer(AttributeRegistry.MAX_MANA, 150, AttributeModifier.Operation.ADDITION),
                new AttributeContainer(AttributeRegistry.SPELL_POWER, 0.10, AttributeModifier.Operation.MULTIPLY_BASE)
        );
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, tooltipComponents, pIsAdvanced);
        tooltipComponents.add(
                Component.translatable(
                        "tooltip.irons_spellbooks.passive_ability_no_cooldown",
                        Component.literal(Utils.timeFromTicks(Utils.applyCooldownReduction(COOLDOWN_TICKS, MinecraftInstanceHelper.getPlayer()), 1)).withStyle(ChatFormatting.AQUA)
                ).withStyle(ChatFormatting.DARK_PURPLE)
        );
        tooltipComponents.add(Component.literal(" ").append(Component.translatable(this.getDescriptionId() + ".desc")).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.literal(" ").append(Component.translatable(this.getDescriptionId() + ".immolate.desc",
                Component.literal(Utils.stringTruncation(ImmolateEffect.damageFor(MinecraftInstanceHelper.getPlayer()), 1)).withStyle(ChatFormatting.RED))
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new GenericCustomArmorRenderer<>(new InfernalSorcererArmorModel());
    }

}
