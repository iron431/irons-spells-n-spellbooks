package io.redspace.ironsspellbooks.loot;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class AppendLootModifier<V> extends LootModifier {
    public static final Supplier<Codec<AppendLootModifier>> CODEC = Suppliers.memoize(()
            -> RecordCodecBuilder.create(inst -> codecStart(inst).and(Codec.STRING
            .fieldOf("key").forGetter(m -> m.resourceLocationKey)).apply(inst, AppendLootModifier::new)));
    private final String resourceLocationKey;

    protected AppendLootModifier(LootItemCondition[] conditionsIn, String resourceLocationKey) {
        super(conditionsIn);
        this.resourceLocationKey = resourceLocationKey;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation path = new ResourceLocation(resourceLocationKey);
        var lootTable = context.getLevel().getServer().getLootData().getLootTable(path);
        ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
        lootTable.getRandomItems(context, objectarraylist::add);

        //generatedLoot.addAll(lootTable.getRandomItems(context));
        generatedLoot.addAll(objectarraylist);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
    public class LootResourceCodec implements Codec<V> {

        @Override
        public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
            return null;
        }

        @Override
        public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
            return null;
        }
    }
}