package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ExtendedFireworkRocket;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

@AutoSpellConfig
public class FirecrackerSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "firecracker");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getSpellPower(spellLevel, caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(1.5)
            .build();

    public FirecrackerSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 shootAngle = entity.getLookAngle().normalize();
        Vec3 spawn = Utils.raycastForEntity(world, entity, getRange(spellLevel, entity), true).getLocation().subtract(shootAngle.scale(.25f));
        ExtendedFireworkRocket firework = new ExtendedFireworkRocket(world, randomFireworkRocket(), entity, spawn.x, spawn.y, spawn.z, true, getDamage(spellLevel, entity));
        world.addFreshEntity(firework);
        firework.shoot(shootAngle.x, shootAngle.y, shootAngle.z, 0, 0);
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getRange(int spellLevel, LivingEntity entity) {
        return 15 + (int) (getSpellPower(spellLevel, entity) * 2);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity);
    }

    private ItemStack randomFireworkRocket() {
        Random random = new Random();
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        FireworkExplosion.Shape shape;
        byte type = (byte) (random.nextInt(3) * 2);
        if (random.nextFloat() < .08f) { //8% chance for creeper face explosion
            type = 3;
        }
        shape = FireworkExplosion.Shape.values()[type];
        var fireworks = new Fireworks(-1, List.of(new FireworkExplosion(shape, new IntImmutableList(randomColors()), new IntImmutableList(new int[0]), random.nextInt(3) == 0, random.nextInt(3) == 0)));
        rocket.set(DataComponents.FIREWORKS, fireworks);
        return rocket;
    }

    private int[] randomColors() {
        int[] colors = new int[3];
        Random random = new Random();

        for (int i = 0; i < colors.length; i++) {
            colors[i] = DYE_COLORS[random.nextInt(DYE_COLORS.length)];
        }
        return colors;
    }

    //https://minecraft.fandom.com/wiki/Dye#Color_values
    private static final int[] DYE_COLORS = {
            //1908001,
            11546150,
            6192150,
            //8606770,
            3949738,
            8991416,
            1481884,
            //10329495,
            //4673362,
            15961002,
            8439583,
            16701501,
            3847130,
            13061821,
            16351261,
            16383998
    };
//    @Override
//    public MutableComponent getUniqueInfo() {
//        return uniqueText;
//    }
}
