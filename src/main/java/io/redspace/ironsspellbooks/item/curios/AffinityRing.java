package io.redspace.ironsspellbooks.item.curios;

import io.redspace.ironsspellbooks.api.item.curios.AffinityData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.render.AffinityRingRenderer;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class AffinityRing extends SimpleDescriptiveCurio {

    public AffinityRing(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        var spell = AffinityData.getAffinityData(pStack).getSpell();
        if (!spell.equals(SpellRegistry.none())) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("curios.modifiers.ring").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal(" ").append(Component.translatable("tooltip.irons_spellbooks.enhance_spell_level", spell.getDisplayName(MinecraftInstanceHelper.instance.player()).withStyle(spell.getSchoolType().getDisplayName().getStyle())).withStyle(ChatFormatting.YELLOW)));
        } else {
            tooltip.add(Component.translatable("tooltip.irons_spellbooks.empty_affinity_ring").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatable(this.getDescriptionId(pStack), AffinityData.getAffinityData(pStack).getNameForItem());
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new AffinityRingRenderer(Minecraft.getInstance().getItemRenderer(),
                        Minecraft.getInstance().getEntityModels());
            }
        });
    }
}
