package io.redspace.ironsspellbooks.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;
import java.util.Optional;

public class FurledMapItem extends Item {

    public FurledMapItem() {
        super(ItemPropertiesHelper.material().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverlevel) {
            level.playSound(null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0F, 1.0F);
            player.getCooldowns().addCooldown(ItemRegistry.FURLED_MAP.get(), 50);
            ItemStack itemStack = player.getItemInHand(hand);

            if (itemStack.has(ComponentRegistry.FURLED_MAP_COMPONENT)) {
                var furledMapData = itemStack.get(ComponentRegistry.FURLED_MAP_COMPONENT);
                ResourceLocation destinationResource = ResourceLocation.parse(furledMapData.destinationResource);
                ResourceKey<Structure> structureResourceKey = ResourceKey.create(Registries.STRUCTURE, destinationResource);
                var holder = serverlevel.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolder(structureResourceKey).map(HolderSet::direct);
                if (holder.isPresent()) {
                    Pair<BlockPos, Holder<Structure>> pair = serverlevel.getChunkSource().getGenerator().findNearestMapStructure(serverlevel, holder.get(), player.blockPosition(), 100, ServerConfigs.FURLED_MAPS_SKIP_CHUNKS.get());
                    if (pair != null) {
                        var blockpos = pair.getFirst();
                        ItemStack mapStack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte) 2, true, true);
                        MapItem.renderBiomePreviewMap(serverlevel, mapStack);
                        MapItemSavedData.addTargetDecoration(mapStack, blockpos, "x", MapDecorationTypes.RED_X);
                        furledMapData.descriptionOverride.ifPresent(component -> mapStack.set(DataComponents.CUSTOM_NAME, component));

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

    public static ItemStack of(ResourceLocation structure, MutableComponent descriptor) {
        ItemStack itemStack = new ItemStack(ItemRegistry.FURLED_MAP.get());
        itemStack.set(ComponentRegistry.FURLED_MAP_COMPONENT.value(), new FurledMapData(structure.toString(), Optional.of(descriptor)));
        itemStack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("item.irons_spellbooks.furled_map_descriptor_framing", descriptor).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))));
        return itemStack;
    }

    public record FurledMapData(String destinationResource, Optional<Component> descriptionOverride) {
        public static final Codec<FurledMapData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                ExtraCodecs.RESOURCE_PATH_CODEC.fieldOf("destination").forGetter(data -> data.destinationResource),
                ComponentSerialization.CODEC.optionalFieldOf("descriptionOverride").forGetter(data -> data.descriptionOverride)
        ).apply(builder, FurledMapData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, String> STRING_STREAM_CODEC = StreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf);
        public static final StreamCodec<RegistryFriendlyByteBuf, FurledMapData> STREAM_CODEC = StreamCodec.composite(
                STRING_STREAM_CODEC,
                FurledMapData::destinationResource,
                ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC),
                FurledMapData::descriptionOverride,
                FurledMapData::new);
    }
}
