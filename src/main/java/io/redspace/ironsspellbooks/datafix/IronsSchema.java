package io.redspace.ironsspellbooks.datafix;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V99;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Supplier;

public class IronsSchema extends Schema {
    static final Map<String, String> ITEM_TO_BLOCKENTITY = DataFixUtils.make(Maps.newHashMap(), (p_145919_) -> {
        p_145919_.put("minecraft:furnace", "Furnace");
        p_145919_.put("minecraft:lit_furnace", "Furnace");
        p_145919_.put("minecraft:chest", "Chest");
        p_145919_.put("minecraft:trapped_chest", "Chest");
        p_145919_.put("minecraft:ender_chest", "EnderChest");
        p_145919_.put("minecraft:jukebox", "RecordPlayer");
        p_145919_.put("minecraft:dispenser", "Trap");
        p_145919_.put("minecraft:dropper", "Dropper");
        p_145919_.put("minecraft:sign", "Sign");
        p_145919_.put("minecraft:mob_spawner", "MobSpawner");
        p_145919_.put("minecraft:noteblock", "Music");
        p_145919_.put("minecraft:brewing_stand", "Cauldron");
        p_145919_.put("minecraft:enhanting_table", "EnchantTable");
        p_145919_.put("minecraft:command_block", "CommandBlock");
        p_145919_.put("minecraft:beacon", "Beacon");
        p_145919_.put("minecraft:skull", "Skull");
        p_145919_.put("minecraft:daylight_detector", "DLDetector");
        p_145919_.put("minecraft:hopper", "Hopper");
        p_145919_.put("minecraft:banner", "Banner");
        p_145919_.put("minecraft:flower_pot", "FlowerPot");
        p_145919_.put("minecraft:repeating_command_block", "CommandBlock");
        p_145919_.put("minecraft:chain_command_block", "CommandBlock");
        p_145919_.put("minecraft:standing_sign", "Sign");
        p_145919_.put("minecraft:wall_sign", "Sign");
        p_145919_.put("minecraft:piston_head", "Piston");
        p_145919_.put("minecraft:daylight_detector_inverted", "DLDetector");
        p_145919_.put("minecraft:unpowered_comparator", "Comparator");
        p_145919_.put("minecraft:powered_comparator", "Comparator");
        p_145919_.put("minecraft:wall_banner", "Banner");
        p_145919_.put("minecraft:standing_banner", "Banner");
        p_145919_.put("minecraft:structure_block", "Structure");
        p_145919_.put("minecraft:end_portal", "Airportal");
        p_145919_.put("minecraft:end_gateway", "EndGateway");
        p_145919_.put("minecraft:shield", "Banner");
    });
    protected static final Hook.HookFunction ADD_NAMES = new Hook.HookFunction() {
        public <T> T apply(DynamicOps<T> p_18312_, T p_18313_) {
            return IronsSchema.addNames(new Dynamic<>(p_18312_, p_18313_), IronsSchema.ITEM_TO_BLOCKENTITY, "ArmorStand");
        }
    };

    public IronsSchema(int pVersionKey, Schema pParent) {
        super(pVersionKey, pParent);
    }

    protected static TypeTemplate equipment(Schema pSchema) {
        return DSL.optionalFields("Equipment", DSL.list(References.ITEM_STACK.in(pSchema)));
    }

    protected static void registerMob(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
        pSchema.register(pMap, pName, () -> {
            return equipment(pSchema);
        });
    }

    protected static void registerThrowableProjectile(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
        pSchema.register(pMap, pName, () -> {
            return DSL.optionalFields("inTile", References.BLOCK_NAME.in(pSchema));
        });
    }

    protected static void registerMinecart(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
        pSchema.register(pMap, pName, () -> {
            return DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(pSchema));
        });
    }

    protected static void registerInventory(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
        pSchema.register(pMap, pName, () -> {
            return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)));
        });
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema pSchema) {
        Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        pSchema.register(map, "Item", (p_18301_) -> {
            return DSL.optionalFields("Item", References.ITEM_STACK.in(pSchema));
        });
        pSchema.registerSimple(map, "XPOrb");
        registerThrowableProjectile(pSchema, map, "ThrownEgg");
        pSchema.registerSimple(map, "LeashKnot");
        pSchema.registerSimple(map, "Painting");
        pSchema.register(map, "Arrow", (p_18298_) -> {
            return DSL.optionalFields("inTile", References.BLOCK_NAME.in(pSchema));
        });
        pSchema.register(map, "TippedArrow", (p_18295_) -> {
            return DSL.optionalFields("inTile", References.BLOCK_NAME.in(pSchema));
        });
        pSchema.register(map, "SpectralArrow", (p_18292_) -> {
            return DSL.optionalFields("inTile", References.BLOCK_NAME.in(pSchema));
        });
        registerThrowableProjectile(pSchema, map, "Snowball");
        registerThrowableProjectile(pSchema, map, "Fireball");
        registerThrowableProjectile(pSchema, map, "SmallFireball");
        registerThrowableProjectile(pSchema, map, "ThrownEnderpearl");
        pSchema.registerSimple(map, "EyeOfEnderSignal");
        pSchema.register(map, "ThrownPotion", (p_18289_) -> {
            return DSL.optionalFields("inTile", References.BLOCK_NAME.in(pSchema), "Potion", References.ITEM_STACK.in(pSchema));
        });
        registerThrowableProjectile(pSchema, map, "ThrownExpBottle");
        pSchema.register(map, "ItemFrame", (p_18284_) -> {
            return DSL.optionalFields("Item", References.ITEM_STACK.in(pSchema));
        });
        registerThrowableProjectile(pSchema, map, "WitherSkull");
        pSchema.registerSimple(map, "PrimedTnt");
        pSchema.register(map, "FallingSand", (p_18279_) -> {
            return DSL.optionalFields("Block", References.BLOCK_NAME.in(pSchema), "TileEntityData", References.BLOCK_ENTITY.in(pSchema));
        });
        pSchema.register(map, "FireworksRocketEntity", (p_18274_) -> {
            return DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(pSchema));
        });
        pSchema.registerSimple(map, "Boat");
        pSchema.register(map, "Minecart", () -> {
            return DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(pSchema), "Items", DSL.list(References.ITEM_STACK.in(pSchema)));
        });
        registerMinecart(pSchema, map, "MinecartRideable");
        pSchema.register(map, "MinecartChest", (p_18269_) -> {
            return DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(pSchema), "Items", DSL.list(References.ITEM_STACK.in(pSchema)));
        });
        registerMinecart(pSchema, map, "MinecartFurnace");
        registerMinecart(pSchema, map, "MinecartTNT");
        pSchema.register(map, "MinecartSpawner", () -> {
            return DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(pSchema), References.UNTAGGED_SPAWNER.in(pSchema));
        });
        pSchema.register(map, "MinecartHopper", (p_18264_) -> {
            return DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(pSchema), "Items", DSL.list(References.ITEM_STACK.in(pSchema)));
        });
        registerMinecart(pSchema, map, "MinecartCommandBlock");
        registerMob(pSchema, map, "ArmorStand");
        registerMob(pSchema, map, "Creeper");
        registerMob(pSchema, map, "Skeleton");
        registerMob(pSchema, map, "Spider");
        registerMob(pSchema, map, "Giant");
        registerMob(pSchema, map, "Zombie");
        registerMob(pSchema, map, "Slime");
        registerMob(pSchema, map, "Ghast");
        registerMob(pSchema, map, "PigZombie");
        pSchema.register(map, "Enderman", (p_18259_) -> {
            return DSL.optionalFields("carried", References.BLOCK_NAME.in(pSchema), equipment(pSchema));
        });
        registerMob(pSchema, map, "CaveSpider");
        registerMob(pSchema, map, "Silverfish");
        registerMob(pSchema, map, "Blaze");
        registerMob(pSchema, map, "LavaSlime");
        registerMob(pSchema, map, "EnderDragon");
        registerMob(pSchema, map, "WitherBoss");
        registerMob(pSchema, map, "Bat");
        registerMob(pSchema, map, "Witch");
        registerMob(pSchema, map, "Endermite");
        registerMob(pSchema, map, "Guardian");
        registerMob(pSchema, map, "Pig");
        registerMob(pSchema, map, "Sheep");
        registerMob(pSchema, map, "Cow");
        registerMob(pSchema, map, "Chicken");
        registerMob(pSchema, map, "Squid");
        registerMob(pSchema, map, "Wolf");
        registerMob(pSchema, map, "MushroomCow");
        registerMob(pSchema, map, "SnowMan");
        registerMob(pSchema, map, "Ozelot");
        registerMob(pSchema, map, "VillagerGolem");
        pSchema.register(map, "EntityHorse", (p_18254_) -> {
            return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(pSchema)), "ArmorItem", References.ITEM_STACK.in(pSchema), "SaddleItem", References.ITEM_STACK.in(pSchema), equipment(pSchema));
        });
        registerMob(pSchema, map, "Rabbit");
        pSchema.register(map, "Villager", (p_18245_) -> {
            return DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(pSchema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", References.ITEM_STACK.in(pSchema), "buyB", References.ITEM_STACK.in(pSchema), "sell", References.ITEM_STACK.in(pSchema)))), equipment(pSchema));
        });
        pSchema.registerSimple(map, "EnderCrystal");
        pSchema.registerSimple(map, "AreaEffectCloud");
        pSchema.registerSimple(map, "ShulkerBullet");
        registerMob(pSchema, map, "Shulker");
        return map;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema pSchema) {
        Map<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
//        registerInventory(pSchema, map, "Furnace");
        registerInventory(pSchema, map, "Chest");
        pSchema.registerSimple(map, "EnderChest");
//        pSchema.register(map, "RecordPlayer", (p_18235_) -> {
//            return DSL.optionalFields("RecordItem", References.ITEM_STACK.in(pSchema));
//        });
//        registerInventory(pSchema, map, "Trap");
//        registerInventory(pSchema, map, "Dropper");
//        pSchema.registerSimple(map, "Sign");
//        pSchema.register(map, "MobSpawner", (p_18223_) -> {
//            return References.UNTAGGED_SPAWNER.in(pSchema);
//        });
//        pSchema.registerSimple(map, "Music");
//        pSchema.registerSimple(map, "Piston");
//        registerInventory(pSchema, map, "Cauldron");
//        pSchema.registerSimple(map, "EnchantTable");
//        pSchema.registerSimple(map, "Airportal");
//        pSchema.registerSimple(map, "Control");
//        pSchema.registerSimple(map, "Beacon");
//        pSchema.registerSimple(map, "Skull");
//        pSchema.registerSimple(map, "DLDetector");
//        registerInventory(pSchema, map, "Hopper");
//        pSchema.registerSimple(map, "Comparator");
//        pSchema.register(map, "FlowerPot", (p_18192_) -> {
//            return DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(pSchema)));
//        });
//        pSchema.registerSimple(map, "Banner");
        pSchema.registerSimple(map, "Structure");
//        pSchema.registerSimple(map, "EndGateway");
        return map;
    }

    public void registerTypes(Schema pSchema, Map<String, Supplier<TypeTemplate>> p_18308_, Map<String, Supplier<TypeTemplate>> p_18309_) {
        pSchema.registerType(false, References.LEVEL, DSL::remainder);
        pSchema.registerType(false, References.PLAYER, () -> {
            return DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(pSchema)), "EnderItems", DSL.list(References.ITEM_STACK.in(pSchema)));
        });
        pSchema.registerType(false, References.CHUNK, () -> {
            return DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(pSchema)), "TileEntities", DSL.list(DSL.or(References.BLOCK_ENTITY.in(pSchema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", References.BLOCK_NAME.in(pSchema)))));
        });
        pSchema.registerType(true, References.BLOCK_ENTITY, () -> {
            return DSL.taggedChoiceLazy("id", DSL.string(), p_18309_);
        });
        pSchema.registerType(true, References.ENTITY_TREE, () -> {
            return DSL.optionalFields("Riding", References.ENTITY_TREE.in(pSchema), References.ENTITY.in(pSchema));
        });
        pSchema.registerType(false, References.ENTITY_NAME, () -> {
            return DSL.constType(NamespacedSchema.namespacedString());
        });
        pSchema.registerType(true, References.ENTITY, () -> {
            return DSL.taggedChoiceLazy("id", DSL.string(), p_18308_);
        });
        pSchema.registerType(true, References.ITEM_STACK, () -> {
            return DSL.hook(DSL.optionalFields("id", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(pSchema)), "tag", DSL.optionalFields("EntityTag", References.ENTITY_TREE.in(pSchema), "BlockEntityTag", References.BLOCK_ENTITY.in(pSchema), "CanDestroy", DSL.list(References.BLOCK_NAME.in(pSchema)), "CanPlaceOn", DSL.list(References.BLOCK_NAME.in(pSchema)), "Items", DSL.list(References.ITEM_STACK.in(pSchema)))), ADD_NAMES, Hook.HookFunction.IDENTITY);
        });
        pSchema.registerType(false, References.OPTIONS, DSL::remainder);
        pSchema.registerType(false, References.BLOCK_NAME, () -> {
            return DSL.or(DSL.constType(DSL.intType()), DSL.constType(NamespacedSchema.namespacedString()));
        });
        pSchema.registerType(false, References.ITEM_NAME, () -> {
            return DSL.constType(NamespacedSchema.namespacedString());
        });
        pSchema.registerType(false, References.STATS, DSL::remainder);
        pSchema.registerType(false, References.SAVED_DATA, () -> {
            return DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(References.STRUCTURE_FEATURE.in(pSchema)), "Objectives", DSL.list(References.OBJECTIVE.in(pSchema)), "Teams", DSL.list(References.TEAM.in(pSchema))));
        });
        pSchema.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
        pSchema.registerType(false, References.OBJECTIVE, DSL::remainder);
        pSchema.registerType(false, References.TEAM, DSL::remainder);
        pSchema.registerType(true, References.UNTAGGED_SPAWNER, DSL::remainder);
        pSchema.registerType(false, References.POI_CHUNK, DSL::remainder);
        pSchema.registerType(true, References.WORLD_GEN_SETTINGS, DSL::remainder);
        pSchema.registerType(false, References.ENTITY_CHUNK, () -> {
            return DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(pSchema)));
        });
    }

    protected static <T> T addNames(Dynamic<T> p_18206_, Map<String, String> p_18207_, String p_18208_) {
        return p_18206_.update("tag", (p_145917_) -> {
            return p_145917_.update("BlockEntityTag", (p_145912_) -> {
                String s = p_18206_.get("id").asString().result().map(NamespacedSchema::ensureNamespaced).orElse("minecraft:air");
                if (!"minecraft:air".equals(s)) {
                    String s1 = p_18207_.get(s);
                    if (s1 != null) {
                        return p_145912_.set("id", p_18206_.createString(s1));
                    }

                    IronsSpellbooks.LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", (Object)s);
                }

                return p_145912_;
            }).update("EntityTag", (p_145908_) -> {
                String s = p_18206_.get("id").asString("");
                return "minecraft:armor_stand".equals(NamespacedSchema.ensureNamespaced(s)) ? p_145908_.set("id", p_18206_.createString(p_18208_)) : p_145908_;
            });
        }).getValue();
    }
}