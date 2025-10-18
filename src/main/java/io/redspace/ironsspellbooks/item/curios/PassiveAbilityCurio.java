package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PassiveAbilityCurio extends SimpleDescriptiveCurio {
    public PassiveAbilityCurio(Properties properties, String slotIdentifier) {
        super(properties, slotIdentifier);
        descriptionStyle = Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
    }

    protected abstract int getCooldownTicks();

    /**
     * If the curio is off cooldown, return true and trigger cooldown. Otherwise, return false
     */
    public boolean tryProcCooldown(Player player) {
        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        } else {
            player.getCooldowns().addCooldown(this, getCooldownTicks(player));
            return true;
        }
    }

    public int getCooldownTicks(@Nullable LivingEntity livingEntity) {
        return Utils.applyCooldownReduction(this.getCooldownTicks(), livingEntity);
    }

    @Override
    public List<Component> getDescriptionLines(ItemStack stack) {

        return List.of(
                Component.literal(" ").append(Component.translatable(
                        "tooltip.irons_spellbooks.passive_ability",
                        Component.literal(Utils.timeFromTicks(getCooldownTicks(MinecraftInstanceHelper.getPlayer()), 1)).withStyle(ChatFormatting.LIGHT_PURPLE)
                ).withStyle(ChatFormatting.DARK_PURPLE)),
                getDescription(stack)
        );
    }
}
