package io.redspace.ironsspellbooks.item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
public abstract class ExtendedArmorItem extends ArmorItem implements GeoItem {
    private static final UUID[] ARMOR_MODIFIER_UUID_PER_SLOT = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private final software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    public ExtendedArmorItem(IronsExtendedArmorMaterial material, Type type, Properties properties, AttributeContainer... attributes) {
        super(material, type, properties);
        this.defaultModifiers = platformHandleDefaultModifiers(material, type);
    }

    private static Multimap<Attribute, AttributeModifier> platformHandleDefaultModifiers(IronsExtendedArmorMaterial material, Type type) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        float defense = material.getDefenseForType(type);
        float toughness = material.getToughness();
        float knockbackResistance = material.getKnockbackResistance();
        UUID uuid = ARMOR_MODIFIER_UUID_PER_SLOT[type.getSlot().getIndex()];
        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", defense, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", toughness, AttributeModifier.Operation.ADDITION));
        if (knockbackResistance > 0) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", knockbackResistance, AttributeModifier.Operation.ADDITION));
        }
        for (Map.Entry<Attribute, AttributeModifier> modifierEntry : material.getAdditionalAttributes().entrySet()) {
            AttributeModifier atr = modifierEntry.getValue();
            atr = new AttributeModifier(uuid, atr.getName(), atr.getAmount(), atr.getOperation());
            builder.put(modifierEntry.getKey(), atr);
        }
        //testing for upgrade system
        //builder.put(AttributeRegistry.FIRE_SPELL_POWER.get(), new AttributeModifier("Armor knockback resistance", .1, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    public static AttributeContainer[] schoolAttributes(Holder<Attribute> school) {
        return new AttributeContainer[]{new AttributeContainer(AttributeRegistry.MAX_MANA, 125, AttributeModifier.Operation.ADDITION), new AttributeContainer(school, 0.10, AttributeModifier.Operation.MULTIPLY_BASE), new AttributeContainer(AttributeRegistry.SPELL_POWER, 0.05, AttributeModifier.Operation.MULTIPLY_BASE)};
    }

    public static AttributeContainer[] withManaAttribute(int mana) {
        return new AttributeContainer[]{new AttributeContainer(AttributeRegistry.MAX_MANA, mana, AttributeModifier.Operation.ADDITION)};
    }

    public static AttributeContainer[] withManaAndSpellPowerAttribute(int mana, double spellPower) {
        return new AttributeContainer[]{new AttributeContainer(AttributeRegistry.MAX_MANA, mana, AttributeModifier.Operation.ADDITION), new AttributeContainer(AttributeRegistry.SPELL_POWER, spellPower, AttributeModifier.Operation.MULTIPLY_BASE)};
    }


    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
        if (pEquipmentSlot == this.type.getSlot()) {
            return defaultModifiers;
        } else {
            return ImmutableMultimap.of();
        }
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<ExtendedArmorItem>(this, "controller", 20, this::predicate));
    }

    private PlayState predicate(AnimationState<ExtendedArmorItem> extendedArmorItemAnimationState) {
        //TODO: (1.19.4 port) look at WolfArmorItem. their predicate is way more advanced. Are we missing stuff?
        extendedArmorItemAnimationState.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null)
                    this.renderer = supplyRenderer();

                // This prepares our GeoArmorRenderer for the current render frame.
                // These parameters may be null however, so we don't do anything further with them
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public abstract GeoArmorRenderer<?> supplyRenderer();
}