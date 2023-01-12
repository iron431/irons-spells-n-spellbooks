package com.example.testmod.spells.evocation;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.HitscanFireworkRocketEntity;
import com.example.testmod.entity.magic_missile.MagicMissileProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.C;

import java.util.Random;

public class FirecrackerSpell extends AbstractSpell {
    public FirecrackerSpell() {
        this(1);
    }

    //public final MutableComponent uniqueText;
    public FirecrackerSpell(int level) {
        super(SpellType.FIRECRACKER_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 5;
        this.cooldown = 0;
        //uniqueText = Component.translatable("ui.testmod.damage", Utils.stringTruncation(getSpellPower(null), 1));
    }

    @Override
    public void onCast(Level world, Player player, PlayerMagicData playerMagicData) {
        Vec3 shootAngle = player.getLookAngle().normalize();
        float speed = 2.5f;
        Vec3 hitPos = Utils.getTargetBlock(world, player, ClipContext.Fluid.NONE, getRange(player)).getLocation();
        EntityHitResult entityHit = Utils.getTargetEntity(world, player, player.getEyePosition(), hitPos);
        if (entityHit != null) {
            hitPos = entityHit.getLocation();
            TestMod.LOGGER.debug("FirecrackerSpell.onCast.raycastFoundEntity");
        }
        Vec3 spawn = hitPos.subtract(shootAngle.scale(.5f));

        HitscanFireworkRocketEntity firework = new HitscanFireworkRocketEntity(world, randomFireworkRocket(), player, spawn.x, spawn.y, spawn.z, true, getDamage(player));
        world.addFreshEntity(firework);
        firework.shoot(shootAngle.x, shootAngle.y, shootAngle.z, speed, 0);
    }

    private int getRange(Player player) {
        return 15 + (int) (getSpellPower(player) * 2);
    }

    private float getDamage(Player player) {
        return getSpellPower(player);
    }

    private ItemStack randomFireworkRocket() {
        Random random = new Random();
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag properties = new CompoundTag();
        //https://minecraft.fandom.com/wiki/Firework_Rocket#Data_values
        ListTag explosions = new ListTag();
        CompoundTag explosion = new CompoundTag();
        explosion.putByte("Type", (byte) (random.nextInt(3) * 2));
        if (random.nextInt(3) == 0)
            explosion.putByte("Trail", (byte) 1);
        if (random.nextInt(3) == 0)
            explosion.putByte("Flicker", (byte) 1);

        explosion.putIntArray("Colors", randomColors());

        explosions.add(explosion);

        properties.put("Explosions", explosions);
        properties.putByte("Flight", (byte) -1);

        rocket.addTagElement("Fireworks", properties);

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
