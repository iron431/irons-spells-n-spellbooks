package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.fluids.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidRegistry {
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, IronsSpellbooks.MODID);
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }

    public static final RegistryObject<FluidType> BLOOD_TYPE = FLUID_TYPES.register("blood", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleClientFluidType(IronsSpellbooks.id("block/blood")));
        }
    });

    public static final RegistryObject<FluidType> TIMELESS_SLURRY_TYPE = FLUID_TYPES.register("timeless_slurry", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleClientFluidType(IronsSpellbooks.id("block/timeless_slurry")));
        }
    });

    public static final RegistryObject<FluidType> COMMON_INK_TYPE = FLUID_TYPES.register("common_ink", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.fromNamespaceAndPath("forge", "block/milk_still"), 0xFF222222));
        }
    });

    public static final RegistryObject<FluidType> UNCOMMON_INK_TYPE = FLUID_TYPES.register("uncommon_ink", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.fromNamespaceAndPath("forge", "block/milk_still"), 0xFF124300));
        }
    });

    public static final RegistryObject<FluidType> RARE_INK_TYPE = FLUID_TYPES.register("rare_ink", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.fromNamespaceAndPath("forge", "block/milk_still"), 0xFF0f3844));
        }
    });

    public static final RegistryObject<FluidType> EPIC_INK_TYPE = FLUID_TYPES.register("epic_ink", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.fromNamespaceAndPath("forge", "block/milk_still"), 0xFFa52ea0));
        }
    });

    public static final RegistryObject<FluidType> LEGENDARY_INK_TYPE = FLUID_TYPES.register("legendary_ink", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.fromNamespaceAndPath("forge", "block/milk_still"), 0xFFfcaf1c));
        }
    });

    public static final RegistryObject<FluidType> POTION_FLUID_TYPE = FLUID_TYPES.register("potion", () -> new PotionFluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new PotionClientFluidType(ResourceLocation.withDefaultNamespace("block/water_still")));
        }
    });

    public static final RegistryObject<FluidType> OAKSKIN_ELIXIR_TYPE = FLUID_TYPES.register("oakskin_elixir", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.withDefaultNamespace("block/water_still"), MobEffectRegistry.OAKSKIN.get().getColor()));
        }
    });

    public static final RegistryObject<FluidType> GREATER_OAKSKIN_ELIXIR_TYPE = FLUID_TYPES.register("greater_oakskin_elixir", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.withDefaultNamespace("block/water_still"), MobEffectRegistry.OAKSKIN.get().getColor()));
        }
    });

    public static final RegistryObject<FluidType> EVASION_ELIXIR_TYPE = FLUID_TYPES.register("evasion_elixir", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.withDefaultNamespace("block/water_still"), MobEffectRegistry.EVASION.get().getColor()));
        }
    });

    public static final RegistryObject<FluidType> GREATER_EVASION_ELIXIR_TYPE = FLUID_TYPES.register("greater_evasion_elixir", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.withDefaultNamespace("block/water_still"), MobEffectRegistry.EVASION.get().getColor()));
        }
    });

    public static final RegistryObject<FluidType> INVISIBILITY_ELIXIR_TYPE = FLUID_TYPES.register("invisibility_elixir", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.withDefaultNamespace("block/water_still"), MobEffectRegistry.TRUE_INVISIBILITY.get().getColor()));
        }
    });

    public static final RegistryObject<FluidType> GREATER_INVISIBILITY_ELIXIR_TYPE = FLUID_TYPES.register("greater_invisibility_elixir", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.withDefaultNamespace("block/water_still"), MobEffectRegistry.TRUE_INVISIBILITY.get().getColor()));
        }
    });

    public static final RegistryObject<FluidType> GREATER_HEALING_ELIXIR_TYPE = FLUID_TYPES.register("greater_healing_elixir", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.withDefaultNamespace("block/water_still"), MobEffects.HEAL.getColor()));
        }
    });

    public static final RegistryObject<FluidType> ICE_VENOM_TYPE = FLUID_TYPES.register("ice_venom", () -> new FluidType(FluidType.Properties.create()) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new SimpleTintedClientFluidType(ResourceLocation.fromNamespaceAndPath("forge", "block/milk_still"), 0x73baba));
        }
    });



    public static final RegistryObject<Fluid> BLOOD = registerNoop("blood", BLOOD_TYPE);
    public static final RegistryObject<Fluid> COMMON_INK = registerNoop("common_ink", COMMON_INK_TYPE);
    public static final RegistryObject<Fluid> UNCOMMON_INK = registerNoop("uncommon_ink", UNCOMMON_INK_TYPE);
    public static final RegistryObject<Fluid> RARE_INK = registerNoop("rare_ink", RARE_INK_TYPE);
    public static final RegistryObject<Fluid> EPIC_INK = registerNoop("epic_ink", EPIC_INK_TYPE);
    public static final RegistryObject<Fluid> LEGENDARY_INK = registerNoop("legendary_ink", LEGENDARY_INK_TYPE);
    public static final RegistryObject<Fluid> POTION_FLUID = registerNoop("potion", POTION_FLUID_TYPE);
    public static final RegistryObject<Fluid> OAKSKIN_ELIXIR_FLUID = registerNoop("oakskin_elixir", OAKSKIN_ELIXIR_TYPE);
    public static final RegistryObject<Fluid> GREATER_OAKSKIN_ELIXIR_FLUID = registerNoop("greater_oakskin_elixir", GREATER_OAKSKIN_ELIXIR_TYPE);
    public static final RegistryObject<Fluid> INVISIBILITY_ELIXIR_FLUID = registerNoop("invisibility_elixir", INVISIBILITY_ELIXIR_TYPE);
    public static final RegistryObject<Fluid> GREATER_INVISIBILITY_ELIXIR_FLUID = registerNoop("greater_invisibility_elixir", GREATER_INVISIBILITY_ELIXIR_TYPE);
    public static final RegistryObject<Fluid> EVASION_ELIXIR_FLUID = registerNoop("evasion_elixir", EVASION_ELIXIR_TYPE);
    public static final RegistryObject<Fluid> GREATER_EVASION_ELIXIR_FLUID = registerNoop("greater_evasion_elixir", GREATER_EVASION_ELIXIR_TYPE);
    public static final RegistryObject<Fluid> GREATER_HEALING_ELIXIR_FLUID = registerNoop("greater_healing_elixir", GREATER_HEALING_ELIXIR_TYPE);
    public static final RegistryObject<Fluid> TIMELESS_SLURRY_FLUID = registerNoop("timeless_slurry", TIMELESS_SLURRY_TYPE);
    public static final RegistryObject<Fluid> ICE_VENOM_FLUID = registerNoop("ice_venom", ICE_VENOM_TYPE);

    private static RegistryObject<Fluid> registerNoop(String name, Supplier<FluidType> fluidType) {
        RegistryObject<Fluid> holder = RegistryObject.create(io.redspace.ironsspellbooks.IronsSpellbooks.id(name), ForgeRegistries.FLUIDS);
        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(fluidType, holder, holder).bucket(() -> Items.AIR);
        FLUIDS.register(name, () -> new NoopFluid(properties));
        return holder;
    }
}
