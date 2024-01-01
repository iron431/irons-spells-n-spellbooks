package io.redspace.ironsspellbooks.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.item.weapons.IMultihandWeapon;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "updateInvisibilityStatus", at = @At(value = "TAIL"))
    public void updateInvisibilityStatus(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.hasEffect(MobEffectRegistry.TRUE_INVISIBILITY.get()))
            self.setInvisible(true);
    }

    /**
     * Vanilla still hardcodes the minimum sight range to 2 blocks, but this at least removes the effect of wearing armor
     */
    @Inject(method = "getArmorCoverPercentage", at = @At(value = "HEAD"), cancellable = true)
    public void getArmorCoverPercentage(CallbackInfoReturnable<Float> cir) {
        if (((LivingEntity) (Object) this).hasEffect(MobEffectRegistry.TRUE_INVISIBILITY.get())) {
            cir.setReturnValue(0f);
        }
    }

    @Inject(method = "isCurrentlyGlowing", at = @At(value = "HEAD"), cancellable = true)
    public void isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level.isClientSide() && self.hasEffect(MobEffectRegistry.GUIDING_BOLT.get())) {
            cir.setReturnValue(true);
        }
    }

    @Shadow
    abstract ItemStack getLastHandItem(EquipmentSlot pSlot);

    @Unique
    private static final List<EquipmentSlot> slots = List.of(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND);

    // The equipment change event fires 5 lines too early for this to have been able to be done via events
    @Inject(method = "collectEquipmentChanges", at = @At(value = "TAIL"))
    public void handleEquipmentChanges(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        //Last hand items are accurate at this point
        //Mainhand assigning/removing is handled by minecraft. All we are doing is fudging offhand handling
        LivingEntity self = (LivingEntity) (Object) this;
        for (EquipmentSlot slot : slots) {
            if (slot.getType() == EquipmentSlot.Type.HAND) {
                ItemStack currentStack = self.getItemBySlot(slot);
                ItemStack oldStack = getLastHandItem(slot);
                if (!ItemStack.isSameItemSameTags(oldStack, currentStack)) {
                    //IronsSpellbooks.LOGGER.debug("LivingMixin.handleEquipmentChanges - Hands: {}| {} -> {}", slot, oldStack, currentStack);
                    boolean selected = currentStack.getItem() instanceof IMultihandWeapon;
                    boolean deselected = oldStack.getItem() instanceof IMultihandWeapon;
                    if (selected || deselected) {
                        if (slot == EquipmentSlot.MAINHAND) {
                            ItemStack offhandStack = self.getOffhandItem();
                            if (offhandStack.getItem() instanceof IMultihandWeapon && !ItemStack.isSameItemSameTags(offhandStack, currentStack)) {
                                //If we select a mainhand item, revoke offhand attributes
                                //If we deselect a mainhand item, reinstate offhand attributes
                                if (selected) {
                                    self.getAttributes().removeAttributeModifiers(offhandStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
                                } else if (deselected) {
                                    self.getAttributes().addTransientAttributeModifiers(offhandStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
                                }
                            }
                        } else if (slot == EquipmentSlot.OFFHAND) {
                            //If we select an offhand item, grant attributes, unless we already hold a mainhand item
                            //If we deselect an offhand item, revoke attributes
                            ItemStack mainhandStack = self.getMainHandItem();
                            if (selected) {
                                if (!(mainhandStack.getItem() instanceof IMultihandWeapon)) {
                                    self.getAttributes().addTransientAttributeModifiers(currentStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
                                }
                            } else if (deselected && !ItemStack.isSameItemSameTags(mainhandStack, oldStack)) {
                                self.getAttributes().removeAttributeModifiers(oldStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
                            }
                        }
                    }
                }
            }
        }
    }
//
//    @Unique
//    private static Multimap<Attribute, AttributeModifier> filterApplicableAttributes(Multimap<Attribute, AttributeModifier> attributeModifierMap){
//        Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
//        for (Map.Entry<Attribute, AttributeModifier> entry : attributeModifierMap.entries()){
//            Predicate<Attribute>
//            if()
//        }
//    }
}