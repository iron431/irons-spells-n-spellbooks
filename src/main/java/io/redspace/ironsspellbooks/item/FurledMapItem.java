package io.redspace.ironsspellbooks.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.CodecHelper;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.Optional;

public class FurledMapItem extends Item {
    public static String FURLED_MAP_NBT = "furledMapData";
    public static String FURLED_MAP_LOCATION = "destination";
    public static String FURLED_MAP_DESCRIPTION = "description";

    public FurledMapItem() {
        super(ItemPropertiesHelper.material().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverlevel) {
            ItemStack itemStack = player.getItemInHand(hand);
            CompoundTag tag = itemStack.getTag();
            level.playSound(null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0F, 1.0F);
            player.getCooldowns().addCooldown(ItemRegistry.FURLED_MAP.get(), 50);
            if (tag != null && tag.contains(FURLED_MAP_NBT, 10) && tag.getCompound(FURLED_MAP_NBT).contains(FURLED_MAP_LOCATION)) {
                ResourceLocation destinationResource = ResourceLocation.parse(tag.getCompound(FURLED_MAP_NBT).getString(FURLED_MAP_LOCATION));
                ResourceKey<Structure> structureResourceKey = ResourceKey.create(Registries.STRUCTURE, destinationResource);
                var holder = serverlevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(structureResourceKey).map(HolderSet::direct);
                //IronsSpellbooks.LOGGER.debug("FurledMapItem: found location: {}", structureResourceKey);
                if (holder.isPresent()) {
                    Pair<BlockPos, Holder<Structure>> pair = serverlevel.getChunkSource().getGenerator().findNearestMapStructure(serverlevel, holder.get(), player.blockPosition(), 100, ServerConfigs.FURLED_MAPS_SKIP_CHUNKS.get());
                    if (pair != null) {
                        var blockpos = pair.getFirst();
                        ItemStack mapStack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte) 2, true, true);
                        MapItem.renderBiomePreviewMap(serverlevel, mapStack);
                        MapItemSavedData.addTargetDecoration(mapStack, blockpos, "x", MapDecoration.Type.RED_X);

                        if (tag.getCompound(FURLED_MAP_NBT).contains(FURLED_MAP_DESCRIPTION)) {
                            Component mapTitle = Component.Serializer.fromJson(tag.getCompound(FURLED_MAP_NBT).getString(FURLED_MAP_DESCRIPTION));
                            mapStack.setHoverName(mapTitle);
                        }
                        replaceItem(player, mapStack, hand);
                        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
                    }
                }
            }
            replaceItem(player, new ItemStack(Items.MAP), hand);
        }
        return super.use(level, player, hand);
    }

    private static void replaceItem(Player player, ItemStack itemStack, InteractionHand hand) {
        boolean flag = player.getAbilities().instabuild;
        if (!flag) {
            //We set in hand because stack size is 1 and i don't wanna do logic. create filled result was bugging out in survival mode
            player.setItemInHand(hand, itemStack);
        } else {
            player.getInventory().add(itemStack);
        }
    }

    public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld"));
    public static final ResourceKey<Level> NETHER = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_nether"));

    public static ItemStack of(ResourceLocation structure, ResourceKey<Level> exclusiveDimension, MutableComponent descriptor) {
        return of(structure, exclusiveDimension, descriptor, false);
    }

    public static ItemStack of(ResourceLocation structure, ResourceKey<Level> exclusiveDimension, MutableComponent descriptor, boolean ancient) {
        ItemStack itemStack = new ItemStack(ancient ? ItemRegistry.ANCIENT_FURLED_MAP.get() : ItemRegistry.FURLED_MAP.get());
//        itemStack.set(ComponentRegistry.FURLED_MAP_COMPONENT.value(), new FurledMapData(structure, Optional.of(exclusiveDimension), Optional.of(descriptor)));
        FurledMapData.set(itemStack, new FurledMapData(structure, Optional.of(exclusiveDimension), Optional.of(descriptor)));
//        itemStack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.irons_spellbooks.furled_map_descriptor_framing", descriptor).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)))));
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("item.irons_spellbooks.furled_map_descriptor_framing", descriptor).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))));
        itemStack.getOrCreateTagElement("display").put("Lore", lore);
        return itemStack;
    }

    public record FurledMapData(ResourceLocation destinationResource, Optional<ResourceKey<Level>> dimension,
                                Optional<Component> descriptionOverride) {
        public static final Codec<FurledMapData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                ResourceLocation.CODEC.fieldOf("destination").forGetter(FurledMapData::destinationResource),
                ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(FurledMapData::dimension),
                ExtraCodecs.COMPONENT.optionalFieldOf("descriptionOverride").forGetter(FurledMapData::descriptionOverride)
        ).apply(builder, FurledMapData::new));

        private static final String NBT = FURLED_MAP_NBT;

        static FurledMapData get(ItemStack itemStack) {
            return CodecHelper.get(CODEC, itemStack.getOrCreateTag().getCompound(NBT));
        }

        static void set(ItemStack stack, FurledMapData container) {
            CodecHelper.set(stack, NBT, CODEC, container);
        }

        static boolean has(ItemStack itemStack) {
            return itemStack != null && !itemStack.isEmpty() && CodecHelper.has(itemStack, NBT);
        }
//        public static final StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> RESOURCELOCATION_STREAM_CODEC = StreamCodec.of((buf, loc) -> buf.writeUtf(loc.toString()), (buf) -> ResourceLocation.parse(buf.readUtf()));
//        public static final StreamCodec<RegistryFriendlyByteBuf, FurledMapData> STREAM_CODEC = StreamCodec.composite(
//                RESOURCELOCATION_STREAM_CODEC,
//                FurledMapData::destinationResource,
//                ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)),
//                FurledMapData::dimension,
//                ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC),
//                FurledMapData::descriptionOverride,
//                FurledMapData::new);

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof FurledMapData data && data.destinationResource.equals(this.destinationResource) && data.descriptionOverride.equals(this.descriptionOverride));
        }

        @Override
        public int hashCode() {
            return this.destinationResource.hashCode() + this.descriptionOverride.hashCode() * 31;
        }
    }
}
