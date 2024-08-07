package io.redspace.ironsspellbooks.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ReplaceLootModifier extends LootModifier {
    public static final Supplier<MapCodec<ReplaceLootModifier>> CODEC = Suppliers.memoize(()
            -> RecordCodecBuilder.mapCodec(builder -> codecStart(builder).and(
            builder.group(
                    Codec.STRING.fieldOf("key").forGetter(m -> m.resourceLocationKey),
                    Codec.DOUBLE.fieldOf("chanceToReplace").forGetter(m -> m.chanceToReplace))

    ).apply(builder, ReplaceLootModifier::new)));
    private final String resourceLocationKey;
    private final double chanceToReplace;

    protected ReplaceLootModifier(LootItemCondition[] conditionsIn, String resourceLocationKey, double chanceToReplace) {
        super(conditionsIn);
        this.resourceLocationKey = resourceLocationKey;
        this.chanceToReplace = Mth.clamp(chanceToReplace, 0, 1.0);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        double roll = context.getRandom().nextDouble();
        IronsSpellbooks.LOGGER.debug("InjectPoolLootModifier.doApply {}: {} < {}", resourceLocationKey, roll, chanceToReplace);
        if (roll < chanceToReplace) {
            ResourceLocation path = ResourceLocation.parse(resourceLocationKey);
            var lootTable = context.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, path));
            ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
            //use raw to avoid stack overflow/recursively adding all global loot modifiers
            lootTable.getRandomItemsRaw(context, objectarraylist::add);
            return objectarraylist;
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}