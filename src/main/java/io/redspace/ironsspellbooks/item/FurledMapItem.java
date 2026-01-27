package io.redspace.ironsspellbooks.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

    public FurledMapItem() {
        super(ItemPropertiesHelper.material().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverlevel) {
            level.playSound(null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0F, 1.0F);
            ItemStack itemStack = player.getItemInHand(hand);
            player.getCooldowns().addCooldown(itemStack.getItem(), 50);

            if (FurledMapData.has(itemStack)) {
                FurledMapData furledMapData = FurledMapData.get(itemStack);
                ResourceKey<Structure> structureResourceKey = ResourceKey.create(Registries.STRUCTURE, furledMapData.destinationResource);
                var holder = serverlevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(structureResourceKey).map(HolderSet::direct);
                if (furledMapData.dimension().isPresent()) {
                    var dimensionRestriction = furledMapData.dimension().get();
                    if (!serverlevel.dimension().equals(dimensionRestriction)) {
                        ((ServerPlayer) player).connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("item.irons_spellbooks.furled_map.dimension_fail").withStyle(ChatFormatting.RED)));
                        return InteractionResultHolder.fail(itemStack);
                    }
                }
                if (holder.isPresent()) {
                    Pair<BlockPos, Holder<Structure>> pair = serverlevel.getChunkSource().getGenerator().findNearestMapStructure(serverlevel, holder.get(), player.blockPosition(), 100, ServerConfigs.FURLED_MAPS_SKIP_CHUNKS.get());
                    if (pair != null) {
                        var blockpos = pair.getFirst();
                        ItemStack mapStack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte) 2, true, true);
                        MapItem.renderBiomePreviewMap(serverlevel, mapStack);
                        MapItemSavedData.addTargetDecoration(mapStack, blockpos, "x", MapDecoration.Type.RED_X);
                        furledMapData.descriptionOverride.ifPresent(mapStack::setHoverName);
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
            //dumb hardcode becuase create filled results isnt working :/
            player.setItemInHand(hand, itemStack);
        } else {
            player.getInventory().add(itemStack);
        }
    }

    public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld"));
    public static final ResourceKey<Level> NETHER = ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("the_nether"));

    public static ItemStack of(ResourceLocation structure, MutableComponent descriptor) {
        ItemStack itemStack = new ItemStack(ItemRegistry.FURLED_MAP.get());
        FurledMapData.set(itemStack, new FurledMapData(structure, Optional.empty(), Optional.of(descriptor)));
        FurledMapData.setLoreHelper(itemStack, Component.translatable("item.irons_spellbooks.furled_map_descriptor_framing", descriptor).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        return itemStack;
    }

    public static ItemStack of(ResourceLocation structure, ResourceKey<Level> exclusiveDimension, MutableComponent descriptor) {
        return of(structure, exclusiveDimension, descriptor, false);
    }

    public static ItemStack of(ResourceLocation structure, ResourceKey<Level> exclusiveDimension, MutableComponent descriptor, boolean ancient) {
        ItemStack itemStack = new ItemStack(ancient ? ItemRegistry.ANCIENT_FURLED_MAP.get() : ItemRegistry.FURLED_MAP.get());
        FurledMapData.set(itemStack, new FurledMapData(structure, Optional.of(exclusiveDimension), Optional.of(descriptor)));
        FurledMapData.setLoreHelper(itemStack, Component.translatable("item.irons_spellbooks.furled_map_descriptor_framing", descriptor).setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        return itemStack;
    }


    public record FurledMapData(ResourceLocation destinationResource, Optional<ResourceKey<Level>> dimension,
                                Optional<Component> descriptionOverride) {

        public static final String NBT = "irons_spellbooks:furled_map_data";
        public static final String LEGACY_NBT = "furledMapData";
        public static final String FURLED_MAP_LOCATION = "destination";
        public static final String FURLED_MAP_DESCRIPTION = "description";
        public static final Codec<FurledMapData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                ResourceLocation.CODEC.fieldOf("destination").forGetter(FurledMapData::destinationResource),
                ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension").forGetter(FurledMapData::dimension),
                ExtraCodecs.COMPONENT.optionalFieldOf("descriptionOverride").forGetter(FurledMapData::descriptionOverride)
        ).apply(builder, FurledMapData::new));
        public static final Codec<FurledMapData> LEGACY_CODEC = CodecHelper.createLegacyCodec(tag -> {
            CompoundTag nbt = (CompoundTag) tag;
            String destination = nbt.getString("destination");
            String rawDesc = nbt.getString("description");
            Optional<Component> desc = Optional.empty();
            if (!rawDesc.isEmpty()) {
                desc = Optional.ofNullable(Component.Serializer.fromJson(rawDesc));
            }
            return new FurledMapData(ResourceLocation.parse(destination), Optional.empty(), desc);
        });


        public static boolean has(ItemStack stack) {
            return CodecHelper.hasWithLegacy(stack, NBT, LEGACY_NBT);
        }

        public static FurledMapData get(ItemStack stack) {
            return CodecHelper.getWithLegacy(CODEC, stack, NBT, LEGACY_NBT, LEGACY_CODEC);
        }

        public static void set(ItemStack stack, FurledMapData data) {
            CodecHelper.set(stack, NBT, CODEC, data);
        }

        public static void setLoreHelper(ItemStack stack, Component line) {
            ListTag lore = new ListTag();
            lore.add(StringTag.valueOf(Component.Serializer.toJson(line)));
            stack.getOrCreateTagElement("display").put("Lore", lore);
        }

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

