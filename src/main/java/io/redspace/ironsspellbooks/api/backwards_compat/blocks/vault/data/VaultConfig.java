package io.redspace.ironsspellbooks.api.backwards_compat.blocks.vault.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning.PlayerDetector;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record VaultConfig(
        ResourceLocation lootTable,
        double activationRange,
        double deactivationRange,
        ItemStack keyItem,
        Optional<ResourceLocation> overrideLootTableToDisplay,
        PlayerDetector playerDetector,
        PlayerDetector.EntitySelector entitySelector
) {
    static final String TAG_NAME = "config";
    static VaultConfig DEFAULT = new VaultConfig();
    static Codec<VaultConfig> CODEC = RecordCodecBuilder.<VaultConfig>create(
            p_335305_ -> p_335305_.group(
                            ResourceLocation.CODEC.optionalFieldOf("loot_table", DEFAULT.lootTable()).forGetter(VaultConfig::lootTable),
                            Codec.DOUBLE
                                    .optionalFieldOf("activation_range", Double.valueOf(DEFAULT.activationRange()))
                                    .forGetter(VaultConfig::activationRange),
                            Codec.DOUBLE
                                    .optionalFieldOf("deactivation_range", Double.valueOf(DEFAULT.deactivationRange()))
                                    .forGetter(VaultConfig::deactivationRange),
                            ItemStack.CODEC.optionalFieldOf("key_item").xmap(opt -> opt.orElse(ItemStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack)).forGetter(VaultConfig::keyItem),
                            ResourceLocation.CODEC
                                    .optionalFieldOf("override_loot_table_to_display")
                                    .forGetter(VaultConfig::overrideLootTableToDisplay)
                    )
                    .apply(p_335305_, VaultConfig::new)
    )
            /*.validate(VaultConfig::validate)*/;

    private VaultConfig() {
        this(
                /*BuiltInLootTables.TRIAL_CHAMBERS_REWARD*/ResourceLocation.withDefaultNamespace("empty"),
                4.0,
                4.5,
                new ItemStack(/*Items.TRIAL_KEY*/ItemRegistry.DECREPIT_KEY.get()),
                Optional.empty(),
                PlayerDetector.INCLUDING_CREATIVE_PLAYERS,
                PlayerDetector.EntitySelector.SELECT_FROM_LEVEL
        );
    }

    public VaultConfig(ResourceLocation p_335999_, double p_323704_, double p_323499_, ItemStack p_323661_, Optional<ResourceLocation> p_323481_) {
        this(p_335999_, p_323704_, p_323499_, p_323661_, p_323481_, DEFAULT.playerDetector(), DEFAULT.entitySelector());
    }

    private DataResult<VaultConfig> validate() {
        return this.activationRange > this.deactivationRange
                ? DataResult.error(
                () -> "Activation range must (" + this.activationRange + ") be less or equal to deactivation range (" + this.deactivationRange + ")"
        )
                : DataResult.success(this);
    }
}
