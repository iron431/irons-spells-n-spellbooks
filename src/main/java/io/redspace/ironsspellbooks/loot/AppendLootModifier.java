package io.redspace.ironsspellbooks.loot;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class AppendLootModifier<V> extends LootModifier {
    //    public static final Supplier<Codec<AppendLootModifier>> CODEC = Suppliers.memoize(()
//            -> RecordCodecBuilder.create(inst -> codecStart(inst).and(Codec.STRING
//            .fieldOf("key").forGetter(m -> m.resourceLocationKey)).apply(inst, AppendLootModifier::new)));
    private final String resourceLocationKey;

    protected AppendLootModifier(LootItemCondition[] conditionsIn, String resourceLocationKey) {
        super(conditionsIn);
        this.resourceLocationKey = resourceLocationKey;
    }

    @NotNull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation path = new ResourceLocation(resourceLocationKey);
        var lootTable = context.getLevel().getServer().getLootTables().get(path);
        ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
        lootTable.getRandomItems(context, objectarraylist::add);

        //generatedLoot.addAll(lootTable.getRandomItems(context));
        generatedLoot.addAll(objectarraylist);
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<AppendLootModifier> {
        @Override
        public AppendLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] ailootcondition) {
            return new AppendLootModifier(ailootcondition, GsonHelper.getAsString(object, "key"));
        }

        @Override
        public JsonObject write(AppendLootModifier instance) {
            JsonObject jsonObject = makeConditions(instance.conditions);
            jsonObject.addProperty("key", instance.resourceLocationKey);
            return jsonObject;
        }
    }

}