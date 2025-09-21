package io.redspace.ironsspellbooks.registries;

public class ComponentRegistry {
//    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, IronsSpellbooks.MODID);
//
//    public static void register(IEventBus eventBus) {
//        COMPONENTS.register(eventBus);
//    }
//
//    private static <T> RegistryObject<DataComponentType<?>> register(String pName, UnaryOperator<DataComponentType.Builder<T>> pBuilder) {
//        return COMPONENTS.register(pName, () -> pBuilder.apply(DataComponentType.builder()).build());
//    }

//    public static final RegistryObject<DataComponentType<?>> AFFINITY_COMPONENT = register("affinity_data", (builder) -> builder.persistent(AffinityData.CODEC).networkSynchronized(AffinityData.STREAM_CODEC).cacheEncoding());
//    public static final RegistryObject<DataComponentType<?>> FURLED_MAP_COMPONENT = register("furled_map_data", (builder) -> builder.persistent(FurledMapItem.FurledMapData.CODEC).networkSynchronized(FurledMapItem.FurledMapData.STREAM_CODEC).cacheEncoding());
//    public static final RegistryObject<DataComponentType<?>> UPGRADE_DATA = register("upgrade_data", (builder) -> builder.persistent(UpgradeData.CODEC).networkSynchronized(UpgradeData.STREAM_CODEC).cacheEncoding());
//    public static final RegistryObject<DataComponentType<?>> SPELL_CONTAINER = register("spell_container", (builder) -> builder.persistent(SpellContainer.CODEC).networkSynchronized(SpellContainer.STREAM_CODEC).cacheEncoding());
//    public static final RegistryObject<DataComponentType<?>> WAYWARD_COMPASS = register("wayward_compass", (builder) -> builder.persistent(WaywardCompassData.CODEC).networkSynchronized(WaywardCompassData.STREAM_CODEC).cacheEncoding());
//    public static final RegistryObject<DataComponentType<?>> CROSSBOW_LOAD_STATE = register("crossbow_load_state", (builder) -> builder.persistent(AutoloaderCrossbow.LoadStateComponent.CODEC).networkSynchronized(AutoloaderCrossbow.LoadStateComponent.STREAM_CODEC).cacheEncoding());
//    public static final RegistryObject<DataComponentType<?>> CASTING_IMPLEMENT = register("casting_implement", (builder) -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).cacheEncoding());
//    public static final RegistryObject<DataComponentType<?>> MULTIHAND_WEAPON = register("multihand_weapon", (builder) -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)).cacheEncoding());
//    public static final RegistryObject<DataComponentType<?>> CLOTHING_VARIANT = register("clothing_variant", (builder) -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).cacheEncoding());
//    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<UpgradeOrbType>>> UPGRADE_ORB_TYPE = register("upgrade_orb_type", (builder) -> builder.persistent(ResourceKey.codec(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY)).networkSynchronized(ByteBufCodecs.fromCodec(ResourceKey.codec(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY))).cacheEncoding());
//    public static final RegistryObject<DataComponentType<?>> POTION_BOTTLE_TYPE = register("potion_bottle_type", (builder) -> builder.persistent(PotionFluid.BottleType.CODEC).networkSynchronized(PotionFluid.BottleType.STREAM_CODEC).cacheEncoding());
}
