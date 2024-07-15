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
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class AffinityRing extends SimpleDescriptiveCurio {

    public AffinityRing(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        var affinity = AffinityData.getAffinityData(pStack);
        var spell = affinity.getSpell();
        if (!spell.equals(SpellRegistry.none())) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("curios.modifiers.ring").withStyle(ChatFormatting.GOLD));
            var name = spell.getDisplayName(MinecraftInstanceHelper.instance.player()).withStyle(spell.getSchoolType().getDisplayName().getStyle());
            tooltip.add(Component.literal(" ").append(
                    (affinity.bonus() == 1 ? Component.translatable("tooltip.irons_spellbooks.enhance_spell_level", name) : Component.translatable("tooltip.irons_spellbooks.enhance_spell_level_plural", affinity.bonus(), name))
                            .withStyle(ChatFormatting.YELLOW)));
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
