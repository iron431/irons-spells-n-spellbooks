package io.redspace.ironsspellbooks.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.core.object.Color;

import javax.annotation.Nullable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    //TODO: 1.19.4 port
//    @Inject(method = "renderGuiItemDecorations", at = @At(value = "TAIL"))
//    public void renderSpellbookCooldown(PoseStack p_275269_, Font font, ItemStack stack, int one, int two, String p_275302_, CallbackInfo ci) {
//        Item item = stack.getItem();
//        if (item instanceof SpellBook) {
//            AbstractSpell spell = SpellBookData.getSpellBookData(stack).getActiveSpell();
//            renderSpellCooldown(p_275269_, one, two, spell);
//        } else if (SpellData.hasSpellData(stack) && !stack.getItem().equals(ItemRegistry.SCROLL.get())) {
//            AbstractSpell spell = SpellData.getSpellData(stack).getSpell();
//            renderSpellCooldown(p_275269_, one, two, spell);
//        }
//    }

    //TODO: 1.20 port :(
//    private void renderSpellCooldown(PoseStack poseStack, int one, int two, AbstractSpell spell) {
//        if (spell.getSpellType().getValue() > 0) {
//            float f = ClientMagicData.getCooldownPercent(spell.getSpellType());
//
//            if (f > 0.0F) {
//                RenderSystem.disableDepthTest();
//                //RenderSystem.disableTexture();
//                RenderSystem.enableBlend();
//                RenderSystem.defaultBlendFunc();
//                Tesselator tesselator = Tesselator.getInstance();
//                BufferBuilder bufferbuilder = tesselator.getBuilder();
//                GuiComponent.fill(poseStack, one, two + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), Color.ofRGBA(1f, 1f, 1f, .5f).getColor());
//                //RenderSystem.enableTexture();
//                RenderSystem.enableDepthTest();
//            }
//        }
//    }
}
