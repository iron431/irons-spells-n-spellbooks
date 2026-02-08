package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.fluids.NoopFluid;
import io.redspace.ironsspellbooks.fluids.PotionFluidType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class FluidRegistry {
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, IronsSpellbooks.MODID);
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }

    public static final DeferredHolder<FluidType, FluidType> BLOOD_TYPE = FLUID_TYPES.register("blood", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> COMMON_INK_TYPE = FLUID_TYPES.register("common_ink", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> UNCOMMON_INK_TYPE = FLUID_TYPES.register("uncommon_ink", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> RARE_INK_TYPE = FLUID_TYPES.register("rare_ink", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> EPIC_INK_TYPE = FLUID_TYPES.register("epic_ink", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> LEGENDARY_INK_TYPE = FLUID_TYPES.register("legendary_ink", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> POTION_FLUID_TYPE = FLUID_TYPES.register("potion", () -> new PotionFluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> EVASION_ELIXIR_TYPE = FLUID_TYPES.register("evasion_elixir", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> GREATER_EVASION_ELIXIR_TYPE = FLUID_TYPES.register("greater_evasion_elixir", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> OAKSKIN_ELIXIR_TYPE = FLUID_TYPES.register("oakskin_elixir", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> GREATER_OAKSKIN_ELIXIR_TYPE = FLUID_TYPES.register("greater_oakskin_elixir", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> INVISIBILITY_ELIXIR_TYPE = FLUID_TYPES.register("invisibility_elixir", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> GREATER_INVISIBILITY_ELIXIR_TYPE = FLUID_TYPES.register("greater_invisibility_elixir", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> GREATER_HEALING_ELIXIR_TYPE = FLUID_TYPES.register("greater_healing_elixir", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> TIMELESS_SLURRY_TYPE = FLUID_TYPES.register("timeless_slurry", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> ICE_VENOM_TYPE = FLUID_TYPES.register("ice_venom", () -> new FluidType(FluidType.Properties.create()));
    //todo: reimplement ice spider lure mechanics
//    public static final DeferredHolder<FluidType, FluidType> ICE_SPIDER_PHEROMONE_TYPE = FLUID_TYPES.register("ice_spider_pheromone", () -> new FluidType(FluidType.Properties.create()));


    public static final DeferredHolder<Fluid, NoopFluid> BLOOD = registerNoop("blood", BLOOD_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> COMMON_INK = registerNoop("common_ink", COMMON_INK_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> UNCOMMON_INK = registerNoop("uncommon_ink", UNCOMMON_INK_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> RARE_INK = registerNoop("rare_ink", RARE_INK_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> EPIC_INK = registerNoop("epic_ink", EPIC_INK_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> LEGENDARY_INK = registerNoop("legendary_ink", LEGENDARY_INK_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> POTION_FLUID = registerNoop("potion", POTION_FLUID_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> OAKSKIN_ELIXIR_FLUID = registerNoop("oakskin_elixir", OAKSKIN_ELIXIR_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> GREATER_OAKSKIN_ELIXIR_FLUID = registerNoop("greater_oakskin_elixir", GREATER_OAKSKIN_ELIXIR_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> INVISIBILITY_ELIXIR_FLUID = registerNoop("invisibility_elixir", INVISIBILITY_ELIXIR_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> GREATER_INVISIBILITY_ELIXIR_FLUID = registerNoop("greater_invisibility_elixir", GREATER_INVISIBILITY_ELIXIR_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> EVASION_ELIXIR_FLUID = registerNoop("evasion_elixir", EVASION_ELIXIR_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> GREATER_EVASION_ELIXIR_FLUID = registerNoop("greater_evasion_elixir", GREATER_EVASION_ELIXIR_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> GREATER_HEALING_ELIXIR_FLUID = registerNoop("greater_healing_elixir", GREATER_HEALING_ELIXIR_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> TIMELESS_SLURRY_FLUID = registerNoop("timeless_slurry", TIMELESS_SLURRY_TYPE::value);
    public static final DeferredHolder<Fluid, NoopFluid> ICE_VENOM_FLUID = registerNoop("ice_venom", ICE_VENOM_TYPE::value);
    //todo: reimplement ice spider lure mechanics
//    public static final DeferredHolder<Fluid, NoopFluid> ICE_SPIDER_PHEROMONE_FLUID = registerNoop("ice_spider_pheromone", ICE_SPIDER_PHEROMONE_TYPE::value);

    private static DeferredHolder<Fluid, NoopFluid> registerNoop(String name, Supplier<FluidType> fluidType) {
        DeferredHolder<Fluid, NoopFluid> holder = DeferredHolder.create(Registries.FLUID, IronsSpellbooks.id(name));
        BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(fluidType, holder::value, holder::value).bucket(() -> Items.AIR);
        FLUIDS.register(name, () -> new NoopFluid(properties));
        return holder;
    }
}
