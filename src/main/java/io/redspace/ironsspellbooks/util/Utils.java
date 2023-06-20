package io.redspace.ironsspellbooks.util;

import com.mojang.math.Vector3f;
import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.network.ServerboundCancelCast;
import io.redspace.ironsspellbooks.network.ServerboundQuickCast;
import io.redspace.ironsspellbooks.network.spell.ClientboundSyncTargetingData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.Messages;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.compat.tetra.TetraProxy;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class Utils {

    public static String getStackTraceAsString() {
        var trace = Arrays.stream(Thread.currentThread().getStackTrace());
        StringBuffer sb = new StringBuffer();
        trace.forEach(item -> {
            sb.append(item.toString());
            sb.append("\n");
        });
        return sb.toString();
    }

    public static void spawnInWorld(Level level, BlockPos pos, ItemStack remaining) {
        if (!remaining.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, remaining);
            itemEntity.setPickUpDelay(40);
            itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().multiply(0, 1, 0));
            level.addFreshEntity(itemEntity);
        }
    }

    public static boolean canBeUpgraded(ItemStack stack) {
        return !ServerConfigs.UPGRADE_BLACKLIST.get().contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString())
                && (stack.getItem() instanceof SpellBook || stack.is(ModTags.CAN_BE_UPGRADED)
                || ServerConfigs.UPGRADE_WHITELIST.get().contains(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString())
        );
    }

    public static String timeFromTicks(float ticks, int decimalPlaces) {
        float ticks_to_seconds = 20;
        float seconds_to_minutes = 60;
        String affix = "s";
        float time = ticks / ticks_to_seconds;
        if (time > seconds_to_minutes) {
            time /= seconds_to_minutes;
            affix = "m";
        }
        return stringTruncation(time, decimalPlaces) + affix;
    }

//    public static double getAttributeMultiplier(LivingEntity entity, Attribute attribute, boolean reductive/*, @Nullable ItemStack activeItem*/) {
//        double baseValue = entity.getAttributeValue(attribute);
////        if (activeItem != null && entity.getMainHandItem() != activeItem) {
////            var itemAttributes = entity.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(attribute);
////            for (AttributeModifier modifier : itemAttributes)
////                if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
////                    baseValue -= modifier.getAmount();
////        }
//        if (!reductive) {
//            return baseValue;
//        } else {
//            return 2 - baseValue <= 1.7 ? baseValue : 2 - Math.pow(Math.E, -(baseValue - 0.6) * (baseValue - 0.6));
//        }
//    }

    /**
     * X should be between 0-2, and has a horizontal asymptote of 2 applied to soft-cap it for reductive attribute calculations
     */
    public static double softCapFormula(double x) {
        //Softcap (https://www.desmos.com/calculator/cokngo3opu)
        return x <= 1.7 ? x : 2 - Math.pow(Math.E, -(x - 0.6) * (x - 0.6));
    }

    public static boolean isPlayerHoldingSpellBook(Player player) {
        return player.getMainHandItem().getItem() instanceof SpellBook || player.getOffhandItem().getItem() instanceof SpellBook;
    }

    public static ServerPlayer getServerPlayer(Level level, UUID uuid) {
        return level.getServer().getPlayerList().getPlayer(uuid);
    }

    public static String stringTruncation(double f, int places) {
        return String.format("%." + (f % 1 == 0 ? 0 : places) + "f", f);
    }

    public static float getAngle(Vec2 a, Vec2 b) {
        return (float) (Math.atan2(b.y - a.y, b.x - a.x)) + 3.141f;// + (a.x > b.x ? Math.PI : 0));
    }

    public static BlockHitResult getTargetOld(Level level, Player player, ClipContext.Fluid clipContext, double reach) {
        float f = player.getXRot();
        float f1 = player.getYRot();
        Vec3 vec3 = player.getEyePosition();
        float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3 vec31 = vec3.add((double) f6 * reach, (double) f5 * reach, (double) f7 * reach);
        return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, clipContext, player));
    }

    public static BlockHitResult getTargetBlock(Level level, LivingEntity entity, ClipContext.Fluid clipContext, double reach) {
        var rotation = entity.getLookAngle().normalize().scale(reach);
        var pos = entity.getEyePosition();
        var dest = rotation.add(pos);
        return level.clip(new ClipContext(pos, dest, ClipContext.Block.COLLIDER, clipContext, entity));
    }

//    public static Vec3 raycastForPosition(Level level, LivingEntity entity, double reach) {
//        var rotation = entity.getLookAngle().normalize().scale(reach);
//        var pos = entity.getEyePosition();
//        return rotation.add(pos);
//    }

    public static boolean hasLineOfSight(Level level, Vec3 start, Vec3 end, boolean checkForShields) {
        if (checkForShields) {
            List<ShieldEntity> shieldEntities = level.getEntitiesOfClass(ShieldEntity.class, new AABB(start, end));
            if (shieldEntities.size() > 0) {
                var shieldImpact = checkEntityIntersecting(shieldEntities.get(0), start, end, 0);
                if (shieldImpact.getType() != HitResult.Type.MISS)
                    end = shieldImpact.getLocation();
            }
        }
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getType() == HitResult.Type.MISS;

    }

    public static BlockHitResult raycastForBlock(Level level, Vec3 start, Vec3 end, ClipContext.Fluid clipContext) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, clipContext, null));
    }

    public static HitResult checkEntityIntersecting(Entity entity, Vec3 start, Vec3 end, float bbInflation) {
        Vec3 hitPos = null;
        if (entity.isMultipartEntity()) {
            for (PartEntity p : entity.getParts()) {
                var hit = p.getBoundingBox().inflate(bbInflation).clip(start, end).orElse(null);
                if (hit != null) {
                    hitPos = hit;
                    break;
                }
            }
        } else {
            hitPos = entity.getBoundingBox().inflate(bbInflation).clip(start, end).orElse(null);
        }
        if (hitPos != null)
            return new EntityHitResult(entity, hitPos);
        else
            return BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end));

    }

    public static Vec3 getPositionFromEntityLookDirection(Entity originEntity, float distance) {
        Vec3 start = originEntity.getEyePosition();
        return originEntity.getLookAngle().normalize().scale(distance).add(start);
    }

    public static HitResult raycastForEntity(Level level, Entity originEntity, float distance, boolean checkForBlocks) {
        Vec3 start = originEntity.getEyePosition();
        Vec3 end = originEntity.getLookAngle().normalize().scale(distance).add(start);

        return raycastForEntity(level, originEntity, start, end, checkForBlocks);
    }

    public static HitResult raycastForEntity(Level level, Entity originEntity, float distance, boolean checkForBlocks, float bbInflation) {
        Vec3 start = originEntity.getEyePosition();
        Vec3 end = originEntity.getLookAngle().normalize().scale(distance).add(start);

        return internalRaycastForEntity(level, originEntity, start, end, checkForBlocks, bbInflation, Utils::canHitWithRaycast);
    }

    public static HitResult raycastForEntity(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks) {
        return internalRaycastForEntity(level, originEntity, start, end, checkForBlocks, 0, Utils::canHitWithRaycast);
    }

    public static HitResult raycastForEntity(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, float bbInflation, Predicate<? super Entity> filter) {
        return internalRaycastForEntity(level, originEntity, start, end, checkForBlocks, bbInflation, filter);
    }

    public static HitResult raycastForEntityOfClass(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, Class<? extends Entity> c) {

        return internalRaycastForEntity(level, originEntity, start, end, checkForBlocks, 0, (entity) -> entity.getClass() == c);
    }

    public static void quickCast(int slot) {
        var player = Minecraft.getInstance().player;
        var hand = InteractionHand.MAIN_HAND;
        var itemStack = player.getItemInHand(hand);

        if (!(itemStack.getItem() instanceof SpellBook)) {
            hand = InteractionHand.OFF_HAND;
            itemStack = player.getItemInHand(hand);
        }

        if (itemStack.getItem() instanceof SpellBook) {
            var spellBookData = SpellBookData.getSpellBookData(itemStack);

            if (spellBookData.getSpellSlots() >= 1) {
                var spell = spellBookData.getSpell(slot);
                if (spell != null) {
                    Messages.sendToServer(new ServerboundQuickCast(slot, hand));
                }
            }
        }
    }

    public static void releaseUsingHelper(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            var pmd = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (pmd.isCasting() && (pmd.getCastType() != CastType.CHARGE ||
                    (pmd.getCastType() == CastType.CHARGE && pmd.getCastDurationRemaining() > 0))) {
                Utils.serverSideCancelCast(serverPlayer);
                serverPlayer.stopUsingItem();
            }
        }
    }

    private static HitResult internalRaycastForEntity(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, float bbInflation, Predicate<? super Entity> filter) {
        AABB range = originEntity.getBoundingBox().expandTowards(end.subtract(start));

        if (checkForBlocks) {
            BlockHitResult blockHitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, originEntity));
            end = blockHitResult.getLocation();
        }

        List<HitResult> hits = new ArrayList<>();
        List<? extends Entity> entities = level.getEntities(originEntity, range, filter);
        for (Entity target : entities) {
            HitResult hit = checkEntityIntersecting(target, start, end, bbInflation);
            if (hit.getType() != HitResult.Type.MISS)
                hits.add(hit);
        }

        if (hits.size() > 0) {
            hits.sort((o1, o2) -> (int) (o1.getLocation().distanceToSqr(start) - o2.getLocation().distanceToSqr(start)));
            return hits.get(0);
        } else {
            return BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end));
        }
    }

    public static void serverSideCancelCast(ServerPlayer serverPlayer) {
        ServerboundCancelCast.cancelCast(serverPlayer, SpellType.values()[PlayerMagicData.getPlayerMagicData(serverPlayer).getCastingSpellId()].getCastType() == CastType.CONTINUOUS);
    }

    public static void serverSideCancelCast(ServerPlayer serverPlayer, boolean triggerCooldown) {
        ServerboundCancelCast.cancelCast(serverPlayer, triggerCooldown);
    }

    public static float smoothstep(float a, float b, float x) {
        //6x^5 - 15x^4 + 10x^3
        x = 6 * (x * x * x * x * x) - 15 * (x * x * x * x) + 10 * (x * x * x);
        return a + (b - a) * x;
    }

    private static boolean canHitWithRaycast(Entity entity) {
        //IronsSpellbooks.LOGGER.debug("Utils.canHitWithRaycast: {} - {}", entity.getName().getString(), !(entity instanceof Projectile || entity instanceof AreaEffectCloud || entity instanceof ConePart));
        return entity.isPickable();
    }

    public static Vec2 rotationFromDirection(Vec3 vector) {
        float pitch = (float) Math.asin(vector.y);
        float yaw = (float) Math.atan2(vector.x, vector.z);
        return new Vec2(pitch, yaw);
    }

    public static boolean doMeleeAttack(Mob attacker, Entity target, DamageSource damageSource, @Nullable SchoolType damageSchool) {
        /*
        Copied from Mob#doHurtTarget
         */
        float f = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (target instanceof LivingEntity) {
            f += EnchantmentHelper.getDamageBonus(attacker.getMainHandItem(), ((LivingEntity) target).getMobType());
            f1 += (float) EnchantmentHelper.getKnockbackBonus(attacker);
        }

        int i = EnchantmentHelper.getFireAspect(attacker);
        if (i > 0) {
            target.setSecondsOnFire(i * 4);
        }

        boolean flag = DamageSources.applyDamage(target, f, damageSource, damageSchool);
        if (flag) {
            if (f1 > 0.0F && target instanceof LivingEntity livingTarget) {
                ((LivingEntity) target).knockback((double) (f1 * 0.5F), (double) Mth.sin(attacker.getYRot() * ((float) Math.PI / 180F)), (double) (-Mth.cos(attacker.getYRot() * ((float) Math.PI / 180F))));
                attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                livingTarget.setLastHurtByMob(attacker);
            }
            //disable shield
            if (target instanceof Player player) {
                var pMobItemStack = attacker.getMainHandItem();
                var pPlayerItemStack = player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY;
                if (!pMobItemStack.isEmpty() && !pPlayerItemStack.isEmpty() && pMobItemStack.getItem() instanceof AxeItem && pPlayerItemStack.is(Items.SHIELD)) {
                    float f2 = 0.25F + (float) EnchantmentHelper.getBlockEfficiency(attacker) * 0.05F;
                    if (attacker.getRandom().nextFloat() < f2) {
                        player.getCooldowns().addCooldown(Items.SHIELD, 100);
                        attacker.level().broadcastEntityEvent(player, (byte) 30);
                    }
                }
            }

            attacker.doEnchantDamageEffects(attacker, target);
            attacker.setLastHurtMob(target);
        }

        return flag;
    }

    public static void throwTarget(LivingEntity attacker, LivingEntity target, float multiplier, boolean ignoreKBResistance) {
        double d0 = attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK) * multiplier;
        double d1 = ignoreKBResistance ? 0 : target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double d2 = d0 - d1;
        if (!(d2 <= 0.0D)) {
            double d3 = target.getX() - attacker.getX();
            double d4 = target.getZ() - attacker.getZ();
            float f = (float) (attacker.level().random.nextInt(21) - 10);
            double d5 = d2 * (double) (attacker.level().random.nextFloat() * 0.5F + 0.2F);
            Vec3 vec3 = (new Vec3(d3, 0.0D, d4)).normalize().scale(d5).yRot(f);
            double d6 = d2 * (double) attacker.level().random.nextFloat() * 0.5D;
            target.push(vec3.x, d6, vec3.z);
            target.hurtMarked = true;
        }
    }

    public static double getRandomScaled(double scale) {
        return (2.0D * Math.random() - 1.0D) * scale;
    }

    public static Vec3 getRandomVec3(double scale) {
        return new Vec3(
                getRandomScaled(scale),
                getRandomScaled(scale),
                getRandomScaled(scale)
        );
    }
    public static Vector3f getRandomVec3f(double scale) {
        return new Vector3f(
                (float) getRandomScaled(scale),
                (float) getRandomScaled(scale),
                (float) getRandomScaled(scale)
        );
    }
    public static boolean shouldHealEntity(LivingEntity healer, LivingEntity target) {
        if (healer instanceof NeutralMob neutralMob && neutralMob.isAngryAt(target))
            return false;
        if (healer == target)
            return true;
        if (target.getType().is(ModTags.ALWAYS_HEAL) && !(healer.getMobType() == MobType.UNDEAD || healer.getMobType() == MobType.ILLAGER))
            //This tag is for things like iron golems, villagers, farm animals, etc
            return true;
        if (healer.isAlliedTo(target))
            //Generic ally-check. Precursory team check plus some mobs override it, such as summons
            return true;
        if (healer.getTeam() != null)
            //If we are on a team, only heal teammates
            return target.isAlliedTo(healer.getTeam());
        if (healer instanceof Player) {
            //If we are a player and not on a team, we only want to heal other players
            return target instanceof Player;
        } else {
            return healer.getMobType() == target.getMobType();
        }
    }

    public static boolean canImbue(ItemStack itemStack) {
        String id = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        if (ServerConfigs.IMBUE_BLACKLIST.get().contains(id))
            return false;
        if (ServerConfigs.IMBUE_WHITELIST.get().contains(id))
            return true;
        if ((itemStack.getItem() instanceof SwordItem swordItem && !(swordItem instanceof UniqueItem)))
            return true;

        return TetraProxy.PROXY.canImbue(itemStack);
    }

    public static InteractionResultHolder<ItemStack> onUseCastingHelper(@NotNull Level level, Player player, @NotNull InteractionHand hand, ItemStack stack, AbstractSpell spell) {
        //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.1");
        if (spell.getSpellType() != SpellType.NONE_SPELL) {
            //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.2");
            if (level.isClientSide) {
                //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.3");
                if (ClientMagicData.isCasting()) {
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.4");
                    return InteractionResultHolder.fail(stack);
                } else if (ClientMagicData.getCooldowns().isOnCooldown(spell.getSpellType()) || (ServerConfigs.SWORDS_CONSUME_MANA.get() && ClientMagicData.getPlayerMana() < spell.getManaCost())) {
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.5");
                    return InteractionResultHolder.pass(stack);
                } else {
                    //irons_spellbooks.LOGGER.debug("SwordItemMixin.use.6");
                    //spell.onClientPreCast(level, player, hand, null);
                    if (spell.getCastType().holdToCast()) {
                        //Ironsspellbooks.logger.debug("onUseCastingHelper.1");
                        player.startUsingItem(hand);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }

            if (spell.attemptInitiateCast(stack, level, player, CastSource.SWORD, true)) {
                if (spell.getCastType().holdToCast()) {
                    //Ironsspellbooks.logger.debug("onUseCastingHelper.2");
                    player.startUsingItem(hand);
                }
                return InteractionResultHolder.success(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        }
        return null;
    }

    public static boolean validAntiMagicTarget(Entity entity) {
        return entity instanceof AntiMagicSusceptible || (entity instanceof Player player/* && PlayerMagicData.getPlayerMagicData(player).isCasting()*/) || (entity instanceof AbstractSpellCastingMob castingMob /*&& PlayerMagicData.getPlayerMagicData(castingMob).isCasting()*/);
    }

    /**
     * From the given start position, this finds the first air block within +/- maxSteps
     */
    public static int findRelativeGroundLevel(Level level, Vec3 start, int maxSteps) {
        if (!level.getBlockState(BlockPos.containing(start)).isAir()) {
            for (int i = 0; i < maxSteps; i++) {
                start = start.add(0, 1, 0);
                if (level.getBlockState(BlockPos.containing(start)).isAir())
                    break;
            }
        }
        //Vec3 upper = level.clip(new ClipContext(start, start.add(0, maxSteps, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getLocation();
        Vec3 lower = level.clip(new ClipContext(start, start.add(0, maxSteps * -2, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getLocation();
        return lower.y < 0 ? (int) (lower.y - .76f) : (int) (lower.y + .76f);
    }

    public static Vec3 moveToRelativeGroundLevel(Level level, Vec3 start, int maxSteps) {
        return new Vec3(start.x, findRelativeGroundLevel(level, start, maxSteps), start.z);
    }

    public static boolean checkMonsterSpawnRules(ServerLevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
        //Omits monster from spawn where monsters are not allowed, as well as default monster spawning conditions
        return !pLevel.getBiome(pPos).is(Biomes.DEEP_DARK) && !pLevel.getBiome(pPos).is(Biomes.MUSHROOM_FIELDS) && Monster.checkMonsterSpawnRules(EntityType.ZOMBIE, pLevel, pSpawnType, pPos, pRandom);
    }

    public static void sendTargetedNotification(ServerPlayer target, LivingEntity caster, SpellType spell) {
        target.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.spell_target_warning", caster.getDisplayName().getString(), spell.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    public static boolean preCastTargetHelper(Level level, LivingEntity caster, PlayerMagicData playerMagicData, SpellType spellType, int range, float aimAssist) {
        return preCastTargetHelper(level, caster, playerMagicData, spellType, range, aimAssist, true);
    }

    public static boolean preCastTargetHelper(Level level, LivingEntity caster, PlayerMagicData playerMagicData, SpellType spellType, int range, float aimAssist, boolean sendFailureMessage) {
        var target = Utils.raycastForEntity(caster.level(), caster, range, true, aimAssist);
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity livingTarget) {
            playerMagicData.setAdditionalCastData(new CastTargetingData(livingTarget));
            if (caster instanceof ServerPlayer serverPlayer)
                Messages.sendToPlayer(new ClientboundSyncTargetingData(livingTarget, spellType), serverPlayer);
            if (livingTarget instanceof ServerPlayer serverPlayer)
                Utils.sendTargetedNotification(serverPlayer, caster, spellType);
            return true;
        } else if (sendFailureMessage && caster instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_target").withStyle(ChatFormatting.RED)));
        }
        return false;

    }
}
