package io.redspace.ironsspellbooks.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.KilledByCrossbowTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class Advancements {

    //Advancement advancement = Advancement.Builder.advancement().display(Items.CROSSBOW, Component.translatable("test advancment title"), Component.translatable("test advancment body"), (ResourceLocation)null, FrameType.CHALLENGE, true, true, true)/*.rewards(AdvancementRewards.Builder.experience(85))*/.addCriterion("arbalistic", new ImpossibleTrigger.TriggerInstance()).save(p_123983_, "adventure/arbalistic");

}
