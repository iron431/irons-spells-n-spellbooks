package io.redspace.ironsspellbooks.api.util;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.attribute.IMagicAttribute;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.events.SpellTeleportEvent;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.compat.Curios;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.root.PreventDismount;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import io.redspace.ironsspellbooks.item.CastingItem;
import io.redspace.ironsspellbooks.item.Scroll;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.network.casting.CancelCastPacket;
import io.redspace.ironsspellbooks.network.casting.SyncTargetingDataPacket;
import io.redspace.ironsspellbooks.particle.FallingBlockParticleOption;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.COOLDOWN_REDUCTION;

public class Utils {

    public static final RandomSource random = RandomSource.createThreadSafe();

    public static final Predicate<Holder<Attribute>> ONLY_MAGIC_ATTRIBUTES = (attribute) -> attribute.value() instanceof IMagicAttribute;
    public static final Predicate<Holder<Attribute>> NON_BASE_ATTRIBUTES = (attribute) -> !(attribute == Attributes.ENTITY_INTERACTION_RANGE || attribute == Attributes.ATTACK_DAMAGE || attribute == Attributes.ATTACK_SPEED || attribute == Attributes.ATTACK_KNOCKBACK);

    public static long getServerTick() {
        return IronsSpellbooks.OVERWORLD.getGameTime();
    }

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
        Item item = stack.getItem();
        boolean isUpgradeable = stack.is(ModTags.CAN_BE_UPGRADED);

        return !ServerConfigs.UPGRADE_BLACKLIST_ITEMS.contains(item)
                && (stack.getItem() instanceof SpellBook || stack.getItem() instanceof ArmorItem || stack.getItem() instanceof CastingItem
                || ServerConfigs.UPGRADE_WHITELIST_ITEMS.contains(item)
                || isUpgradeable
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

    public static boolean handleSpellTeleport(AbstractSpell spell, Entity entity, Vec3 destination) {
        var event = new SpellTeleportEvent(spell, entity, destination.x, destination.y, destination.z);
        NeoForge.EVENT_BUS.post(event);
        boolean canceled = event.isCanceled();
        if (!canceled) {
            entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        }
        return canceled;
    }

    /**
     * adds a horizontal asymptote of y = 2 to soft-cap reductive attribute calculations
     */
    public static double softCapFormula(double x) {
        return x <= 1.5 ? x : -.25 * (1 / (x - 1)) + 2;

        //Softcap (https://www.desmos.com/calculator/tuooig12pf)
        //return x <= 1.75 ? x : 1 / (-16 * (x - 1.5)) + 2;
    }

    @Nullable
    public static ItemStack getPlayerSpellbookStack(@NotNull Player player) {
        return CuriosApi.getCuriosInventory(player).flatMap(curios -> curios.findCurio(Curios.SPELLBOOK_SLOT, 0).map(SlotResult::stack)).orElse(null);
    }

    public static void setPlayerSpellbookStack(@NotNull Player player, ItemStack itemStack) {
        CuriosApi.getCuriosInventory(player).
                ifPresent(curios -> curios.setEquippedCurio(Curios.SPELLBOOK_SLOT, 0, itemStack));
    }

    public static String stringTruncation(double f, int decimalPlaces) {
        if (f == Math.floor(f)) {
            return Integer.toString((int) f);
        }

        double multiplier = Math.pow(10, decimalPlaces);
        double truncatedValue = Math.floor(f * multiplier) / multiplier;

        // Convert the truncated value to a string
        String result = Double.toString(truncatedValue);

        // Remove trailing zeros
        result = result.replaceAll("0*$", "");

        // Remove the decimal point if there are no decimal places
        result = result.endsWith(".") ? result.substring(0, result.length() - 1) : result;

        return result;
    }

    public static float intPow(float f, int exponent) {
        if (exponent == 0) {
            return 1;
        }
        float b = f;
        for (int i = 1; i < Math.abs(exponent); i++) {
            b *= f;
        }
        return exponent < 0 ? 1 / b : b;
    }

    public static double intPow(double d, int exponent) {
        if (exponent == 0) {
            return 1;
        }
        double b = d;
        for (int i = 1; i < Math.abs(exponent); i++) {
            b *= d;
        }
        return exponent < 0 ? 1 / b : b;
    }

    public static float getAngle(Vec2 a, Vec2 b) {
        return getAngle(a.x, a.y, b.x, b.y);
    }

    public static float getAngle(double ax, double ay, double bx, double by) {
        return (float) (Math.atan2(by - ay, bx - ax)) + 3.141f;// + (a.x > b.x ? Math.PI : 0));
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
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getType() == HitResult.Type.MISS;
    }

    public static boolean hasLineOfSight(Level level, Entity entity1, Entity entity2, boolean checkForShields) {
        return hasLineOfSight(level, entity1.getEyePosition(), entity2.getBoundingBox().getCenter(), checkForShields);
    }

    public static BlockHitResult raycastForBlock(Level level, Vec3 start, Vec3 end, ClipContext.Fluid clipContext) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, clipContext, CollisionContext.empty()));
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

    public static void releaseUsingHelper(LivingEntity entity, ItemStack itemStack, int ticksUsed) {
        if (entity instanceof ServerPlayer serverPlayer) {
            var pmd = MagicData.getPlayerMagicData(serverPlayer);
            if (pmd.isCasting()) {
                Utils.serverSideCancelCast(serverPlayer);
                serverPlayer.stopUsingItem();
            }
        }
    }

    public static boolean serverSideInitiateCast(ServerPlayer serverPlayer) {
        var ssm = new SpellSelectionManager(serverPlayer);
        var spellItem = ssm.getSelection();
        if (spellItem != null) {
            var spellData = ssm.getSelectedSpellData();
            if (spellData != SpellData.EMPTY) {
                var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
                if (playerMagicData.isCasting() && !playerMagicData.getCastingSpellId().equals(spellData.getSpell().getSpellId())) {
                    CancelCastPacket.cancelCast(serverPlayer, playerMagicData.getCastType() != CastType.LONG);
                }

                return spellData.getSpell().attemptInitiateCast(ItemStack.EMPTY, spellData.getSpell().getLevelFor(spellData.getLevel(), serverPlayer), serverPlayer.level, serverPlayer, spellItem.getCastSource(), true, spellItem.slot);
            }
        } else if (Utils.getPlayerSpellbookStack(serverPlayer) == null) {
            //Helper for beginners (they tried casting with the spellbook in their hand, not their spell book slot
            ItemStack heldSpellbookStack = serverPlayer.getMainHandItem();
            if (!(heldSpellbookStack.getItem() instanceof SpellBook)) {
                heldSpellbookStack = serverPlayer.getOffhandItem();
            }
            if (heldSpellbookStack.getItem() instanceof SpellBook spellBook) {
                spellBook.onEquipFromUse(new SlotContext(Curios.SPELLBOOK_SLOT, serverPlayer, 0, false, true), heldSpellbookStack);
                Utils.setPlayerSpellbookStack(serverPlayer, heldSpellbookStack.split(1));
                //serverPlayer.level.playSound(null, serverPlayer.blockPosition(), SoundRegistry.EQUIP_SPELL_BOOK.get(), SoundSource.PLAYERS, 1, 1);
            }
        }
        return false;
    }

    /**
     * @return min(| A |, | B |) with sign of a
     */
    public static double signedMin(double a, double b) {
        return (a < 0 ? -1 : 1) * Math.min(Math.abs(a), Math.abs(b));
    }

    public static boolean serverSideInitiateQuickCast(ServerPlayer serverPlayer, int slot) {
        var spellSelection = new SpellSelectionManager(serverPlayer).getSpellSlot(slot);
        if (spellSelection != null) {
            var spellData = spellSelection.spellData;
            if (spellData != SpellData.EMPTY) {
                var playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
                if (playerMagicData.isCasting() && !playerMagicData.getCastingSpellId().equals(spellData.getSpell().getSpellId())) {
                    CancelCastPacket.cancelCast(serverPlayer, playerMagicData.getCastType() != CastType.LONG);
                }

                return spellData.getSpell().attemptInitiateCast(ItemStack.EMPTY, spellData.getSpell().getLevelFor(spellData.getLevel(), serverPlayer), serverPlayer.level, serverPlayer, spellSelection.getCastSource(), true, spellSelection.slot);
            }
        }
        return false;
    }

    private static HitResult internalRaycastForEntity(Level level, Entity originEntity, Vec3 start, Vec3 end, boolean checkForBlocks, float bbInflation, Predicate<? super Entity> filter) {
        BlockHitResult blockHitResult = null;
        if (checkForBlocks) {
            blockHitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, originEntity));
            end = blockHitResult.getLocation();
        }
        AABB range = originEntity.getBoundingBox().expandTowards(end.subtract(start));

        List<HitResult> hits = new ArrayList<>();
        List<? extends Entity> entities = level.getEntities(originEntity, range, filter);
        for (Entity target : entities) {
            HitResult hit = checkEntityIntersecting(target, start, end, bbInflation);
            if (hit.getType() != HitResult.Type.MISS) {
                hits.add(hit);
            }
        }

        if (!hits.isEmpty()) {
            hits.sort(Comparator.comparingDouble(o -> o.getLocation().distanceToSqr(start)));
            return hits.get(0);
        } else if (checkForBlocks) {
            return blockHitResult;
        }
        return BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end));
    }

    public static void serverSideCancelCast(ServerPlayer serverPlayer) {
        CancelCastPacket.cancelCast(serverPlayer, MagicData.getPlayerMagicData(serverPlayer).getCastingSpell().getSpell().getCastType() == CastType.CONTINUOUS);
    }

    public static void serverSideCancelCast(ServerPlayer serverPlayer, boolean triggerCooldown) {
        CancelCastPacket.cancelCast(serverPlayer, triggerCooldown);
    }

    /**
     * Smoothsteps from a to b by percentage x
     */
    public static float smoothstep(float a, float b, float x) {
        //6x^5 - 15x^4 + 10x^3
        x = 6 * (x * x * x * x * x) - 15 * (x * x * x * x) + 10 * (x * x * x);
        return a + (b - a) * x;
    }

    public static boolean canHitWithRaycast(Entity entity) {
        //IronsSpellbooks.LOGGER.debug("Utils.canHitWithRaycast: {} - {}", entity.getName().getString(), !(entity instanceof Projectile || entity instanceof AreaEffectCloud || entity instanceof ConePart));
        return entity.isPickable() && entity.isAlive() && !entity.isSpectator();
    }

    public static int applyCooldownReduction(int baseTicks, @Nullable LivingEntity livingEntity) {
        double modifier = livingEntity == null ? 1 : livingEntity.getAttributeValue(COOLDOWN_REDUCTION);
        return (int) (baseTicks * (2 - Utils.softCapFormula(modifier)));
    }

    public static Vec2 rotationFromDirection(Vec3 vector) {
        float pitch = (float) Math.asin(vector.y);
        float yaw = (float) Math.atan2(vector.x, vector.z);
        return new Vec2(pitch, yaw);
    }

    public static boolean doMeleeAttack(Mob attacker, Entity target, DamageSource damageSource) {
        if (attacker.level.isClientSide) {
            return false;
        }
        float f = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = (float) attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (target instanceof LivingEntity) {
            f = EnchantmentHelper.modifyDamage((ServerLevel) attacker.level, attacker.getMainHandItem(), ((LivingEntity) target), damageSource, f);
            f1 = EnchantmentHelper.modifyKnockback((ServerLevel) attacker.level, attacker.getMainHandItem(), ((LivingEntity) target), damageSource, f1);
        }

        boolean flag = DamageSources.applyDamage(target, f, damageSource);
        if (flag) {
            if (f1 > 0.0F && target instanceof LivingEntity livingTarget) {
                ((LivingEntity) target).knockback((double) (f1 * 0.5F), (double) Mth.sin(attacker.getYRot() * ((float) Math.PI / 180F)), (double) (-Mth.cos(attacker.getYRot() * ((float) Math.PI / 180F))));
                attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                livingTarget.setLastHurtByMob(attacker);
            }
            EnchantmentHelper.doPostAttackEffects((ServerLevel) attacker.level, attacker, damageSource);
            attacker.setLastHurtMob(target);
        }

        return flag;
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

    public static Vector3f v3f(Vec3 vec3) {
        return new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
    }

    public static Vec3 v3d(Vector3f vec3) {
        return new Vec3(vec3.x, vec3.y, vec3.z);
    }

    public static Vec3 lerp(float f, Vec3 a, Vec3 b) {
        return a.add(b.subtract(a).scale(f));
    }

    @Deprecated(forRemoval = true)
    public static boolean shouldHealEntity(LivingEntity healer, LivingEntity target) {
        return shouldHealEntity((Entity) healer, (Entity) target);
    }

    public static boolean shouldHealEntity(Entity healer, Entity target) {
        if (healer instanceof NeutralMob neutralMob && target instanceof LivingEntity livingEntity && neutralMob.isAngryAt(livingEntity)) {
            return false;
        } else if (healer == target) {
            return true;
        } else if (target.getType().is(ModTags.ALWAYS_HEAL) && !(healer instanceof Enemy)) {
            //This tag is for things like iron golems, villagers, farm animals, etc
            return true;
        } else if (target.isAlliedTo(healer) || healer.isAlliedTo(target)) {
            //Generic ally-check. Some mobs override it, such as summons
            return true;
        } else if (healer.getTeam() != null) {
            //If we are on a team, only heal teammates
            return target.isAlliedTo(healer.getTeam());
        } else if (healer instanceof Player) {
            //If we are a player and not on a team, we only want to heal other players
            return target instanceof Player;
        } else {
            //Otherwise, heal like kind (ie undead to undead), but also xor check "enemy" status (most mob types are undefined)
            return healer.getType().getCategory() == target.getType().getCategory() && (healer instanceof Enemy ^ target instanceof Enemy);
        }
    }

    public static boolean canImbue(ItemStack itemStack) {
        if (itemStack.getItem() instanceof UniqueItem) {
            return false;
        }
        Item item = itemStack.getItem();
        if (ServerConfigs.IMBUE_BLACKLIST_ITEMS.contains(item)) {
            return false;
        }
        if (ServerConfigs.IMBUE_WHITELIST_ITEMS.contains(item)) {
            return true;
        }
        if (itemStack.getItem() instanceof SwordItem) {
            return true;
        }
        if (ISpellContainer.isSpellContainer(itemStack) && !(itemStack.getItem() instanceof Scroll || itemStack.getItem() instanceof SpellBook)) {
            return true;
        }
        if (itemStack.is(ModTags.CAN_BE_IMBUED)) {
            return true;
        }

        return false;
    }

    /**
     * Returns a result item, or ItemStack.EMPTY if there is no result
     *
     * @param baseStack
     * @return
     */
    public static ItemStack handleShriving(ItemStack baseStack) {
        ItemStack result = baseStack.copy();
        if (result.is(ItemRegistry.SCROLL.get())) {
            return ItemStack.EMPTY;
        }
        boolean hasResult = false;

        if (ISpellContainer.isSpellContainer(result) && !(result.getItem() instanceof SpellBook) && !(result.getItem() instanceof UniqueItem)) {
            if (result.getItem() instanceof IPresetSpellContainer) {
                var spellContainer = ISpellContainer.get(result).mutableCopy();
                spellContainer.getActiveSpells().forEach(spellData -> spellContainer.removeSpell(spellData.getSpell()));
                ISpellContainer.set(result, spellContainer.toImmutable());
            } else {
                ISpellContainer.remove(result);
            }
            hasResult = true;
        }
        if (UpgradeData.hasUpgradeData(result)) {
            UpgradeData.removeUpgradeData(result);
            hasResult = true;
        }
        if (hasResult) {
            return result;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static boolean validAntiMagicTarget(Entity entity) {
        return !entity.isSpectator() && (entity instanceof AntiMagicSusceptible || (entity instanceof Player) || (entity instanceof IMagicEntity));
    }

    /**
     * From the given start position, this finds the first non-suffocating y level within +/- maxSteps, biased towards the ground
     */
    public static float findRelativeGroundLevel(Level level, Vec3 start, int maxSteps) {
        if (level.getBlockState(BlockPos.containing(start)).isSuffocating(level, BlockPos.containing(start))) {
            for (int i = 0; i < maxSteps; i++) {
                start = start.add(0, 1, 0);
                BlockPos pos = BlockPos.containing(start);
                if (!level.getBlockState(pos).isSuffocating(level, pos)) {
                    return pos.getY();
                }
            }
        }
        return (float) level.clip(new ClipContext(start, start.add(0, -maxSteps, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getLocation().y;
    }

    public static Vec3 moveToRelativeGroundLevel(Level level, Vec3 start, int maxSteps) {
        return moveToRelativeGroundLevel(level, start, maxSteps, maxSteps);
    }

    public static Vec3 moveToRelativeGroundLevel(Level level, Vec3 start, int maxStepsUp, int maxStepsDown) {
        var blockcollisions = new BlockCollisions<>(level, null, new AABB(0, 0, 0, .5, .5, .5).move(start), true, (p_286215_, p_286216_) -> p_286216_);
        if (blockcollisions.hasNext()) {
            for (int i = 1; i < maxStepsUp; i++) {
                blockcollisions = new BlockCollisions<>(level, null, new AABB(0, 0, 0, .5, .5, .5).move(start.add(0, i, 0)), true, (p_286215_, p_286216_) -> p_286216_);
                if (!blockcollisions.hasNext()) {
                    start = start.add(0, i, 0);
                    break;
                }
            }
        }
        return level.clip(new ClipContext(start, start.add(0, -maxStepsDown, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getLocation();
    }

    public static boolean checkMonsterSpawnRules(ServerLevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
        //Omits monster from spawn where monsters are not allowed, as well as default monster spawning conditions
        return !pLevel.getBiome(pPos).is(Tags.Biomes.NO_DEFAULT_MONSTERS) && pLevel.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(pLevel, pPos, pRandom) && Monster.checkMobSpawnRules(EntityRegistry.NECROMANCER.get(), pLevel, pSpawnType, pPos, pRandom);
    }

    public static void sendTargetedNotification(ServerPlayer target, LivingEntity caster, AbstractSpell spell) {
        target.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.spell_target_warning", caster.getDisplayName().getString(), spell.getDisplayName(target)).withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    public static boolean preCastTargetHelper(Level level, LivingEntity caster, MagicData playerMagicData, AbstractSpell spell, int range, float aimAssist) {
        return preCastTargetHelper(level, caster, playerMagicData, spell, range, aimAssist, true);
    }

    public static boolean preCastTargetHelper(Level level, LivingEntity caster, MagicData playerMagicData, AbstractSpell spell, int range, float aimAssist, boolean sendFailureMessage) {
        return preCastTargetHelper(level, caster, playerMagicData, spell, range, aimAssist, sendFailureMessage, x -> true);

    }

    public static boolean preCastTargetHelper(Level level, LivingEntity caster, MagicData playerMagicData, AbstractSpell spell, int range, float aimAssist, boolean sendFailureMessage, Predicate<LivingEntity> filter) {
        var target = Utils.raycastForEntity(caster.level, caster, range, true, aimAssist);
        LivingEntity livingTarget = null;
        if (target instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof LivingEntity livingEntity && filter.test(livingEntity)) {
                livingTarget = livingEntity;
            } else if (entityHit.getEntity() instanceof PartEntity<?> partEntity &&
                    partEntity.getParent() instanceof LivingEntity livingParent && !caster.equals(livingParent)
                    && filter.test(livingParent)) {
                livingTarget = livingParent;
            } else if (entityHit.getEntity() instanceof PreventDismount) {
                if (entityHit.getEntity().getFirstPassenger() instanceof LivingEntity livingRooted) {
                    livingTarget = livingRooted;
                }
            }
        }

        if (livingTarget != null) {
            playerMagicData.setAdditionalCastData(new TargetEntityCastData(livingTarget));
            if (caster instanceof ServerPlayer serverPlayer) {
                if (spell.getCastType() != CastType.INSTANT) {
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncTargetingDataPacket(livingTarget, spell));
                }
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.spell_target_success", livingTarget.getDisplayName().getString(), spell.getDisplayName(serverPlayer)).withStyle(ChatFormatting.GREEN)));
            }
            if (livingTarget instanceof ServerPlayer serverPlayer) {
                Utils.sendTargetedNotification(serverPlayer, caster, spell);
            }
            return true;
        } else if (sendFailureMessage && caster instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("ui.irons_spellbooks.cast_error_target").withStyle(ChatFormatting.RED)));
        }
        return false;
    }

    public static void doMobBreakSuffocatingBlocks(LivingEntity entity) {
        doMobBreakSuffocatingBlocks(entity, Vec3.ZERO);
    }

    public static void doMobBreakSuffocatingBlocks(LivingEntity entity, Vec3 offset) {
        if (EventHooks.canEntityGrief(entity.level, entity)) {
            int l = Mth.floor(entity.getBbWidth() / 2.0F + 1.0F);
            int i1 = Mth.ceil(entity.getBbHeight());
            Vec3i o = new Vec3i(Math.round((float) offset.x), Math.round((float) offset.y), Math.round((float) offset.z));
            for (BlockPos blockpos : BlockPos.betweenClosed(
                    entity.getBlockX() - l + o.getX(), entity.getBlockY() + o.getY(), entity.getBlockZ() - l + o.getZ(),
                    entity.getBlockX() + l + o.getX(), entity.getBlockY() + i1 + o.getY(), entity.getBlockZ() + l + o.getZ()
            )) {
                BlockState blockstate = entity.level.getBlockState(blockpos);
                if (blockstate.canEntityDestroy(entity.level(), blockpos, entity) && EventHooks.onEntityDestroyBlock(entity, blockpos, blockstate)) {
                    if (entity.level.destroyBlock(blockpos, true, entity)) {
                        entity.level.levelEvent(null, 1022, entity.blockPosition(), 0);
                    }
                }
            }
        }
    }

    public static Vector3f deconstructRGB(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return new Vector3f(red / 255.0f, green / 255.0f, blue / 255.0f);
    }

    public static int packRGB(Vector3f color) {
        int red = (int) (color.x() * 255.0f);
        int green = (int) (color.y() * 255.0f);
        int blue = (int) (color.z() * 255.0f);

        return (red << 16) | (green << 8) | blue;
    }

    /**
     * Implementation of ContainerHelper#saveAllItems that takes the save location as parameter
     */
    public static CompoundTag saveAllItems(CompoundTag pTag, NonNullList<ItemStack> pItems, String location, HolderLookup.Provider pLevelRegistry) {
        ListTag listtag = new ListTag();

        for (int i = 0; i < pItems.size(); i++) {
            ItemStack itemstack = pItems.get(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte) i);
                listtag.add(itemstack.save(pLevelRegistry, compoundtag));
            }
        }

        if (!listtag.isEmpty()) {
            pTag.put(location, listtag);
        }

        return pTag;
    }

    public static void loadAllItems(CompoundTag pTag, NonNullList<ItemStack> pItems, String location, HolderLookup.Provider pLevelRegistry) {
        ListTag listtag = pTag.getList(location, 10);
        for (int i = 0; i < listtag.size(); i++) {
            CompoundTag compoundtag = listtag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            if (j >= 0 && j < pItems.size()) {
                pItems.set(j, ItemStack.parse(pLevelRegistry, compoundtag).orElse(ItemStack.EMPTY));
            }
        }
    }

    public static float getWeaponDamage(LivingEntity entity) {
        if (entity != null) {
            float weaponDamage = (float) (entity.getAttributeValue(Attributes.ATTACK_DAMAGE));
            float fistDamage = (float) (entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE));
            if (weaponDamage <= fistDamage) {
                // if no weapon is being used, return 0 instead of their base attribute value
                return 0;
            }
            var weaponItem = entity.getWeaponItem();
            if (!weaponItem.isEmpty() && weaponItem.has(DataComponents.ENCHANTMENTS)) {
                weaponDamage += processEnchantment(entity.level, Enchantments.SHARPNESS, EnchantmentEffectComponents.DAMAGE, weaponItem.get(DataComponents.ENCHANTMENTS));
            }
            return weaponDamage;
        }
        return 0;
    }

    /**
     * @return A factor used to dampen values based on given entity's knockback resistance. Returns max if the entity has no knockback resistance.
     */
    public static float clampedKnockbackResistanceFactor(Entity entity, float min, float max) {
        if (entity instanceof LivingEntity living) {
            return Mth.clamp(1 - (float) living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), min, max);
        } else {
            return max;
        }
    }

    public static float processEnchantment(Level level, ResourceKey<Enchantment> enchantmentKey, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> component, ItemEnchantments enchantments) {
        if (enchantments != null) {
            var reg = level.registryAccess().registry(Registries.ENCHANTMENT).orElse(null);
            if (reg != null) {
                var enchantment = reg.getHolder(enchantmentKey).orElse(null);
                if (enchantment != null && enchantments.keySet().contains(enchantment)) {
                    var enchantmentLevel = enchantments.getLevel(enchantment);
                    var effectList = enchantment.value().effects().get(component);
                    if (effectList != null && !effectList.isEmpty()) {
                        return effectList.getFirst().effect().process(enchantmentLevel, Utils.random, 0f);
                    }
                }
            }
        }
        return 0f;
    }

    public static int getEnchantmentLevel(Level level, ResourceKey<Enchantment> enchantmentKey, ItemEnchantments enchantments) {
        if (enchantments != null) {
            var enchantment = enchantmentFromKey(level.registryAccess(), enchantmentKey);
            if (enchantment != null) {
                return enchantments.getLevel(enchantment);
            }
        }
        return 0;
    }

    public static int getEnchantmentLevel(Level level, ItemStack stack, ResourceKey<Enchantment> enchantmentKey) {
        var enchantment = enchantmentFromKey(level.registryAccess(), enchantmentKey);
        if (enchantment != null) {
            return stack.getEnchantmentLevel(enchantment);
        }
        return 0;
    }

    @Nullable
    public static Holder<Enchantment> enchantmentFromKey(RegistryAccess registryAccess, ResourceKey<Enchantment> enchantmentkey) {
        var reg = registryAccess.registry(Registries.ENCHANTMENT).orElse(null);
        if (reg != null) {
            var enchantment = reg.get(enchantmentkey);
            if (enchantment != null) {
                return reg.wrapAsHolder(enchantment);
            }
        }
        return null;
    }

    public static void enchant(ItemStack stack, RegistryAccess access, ResourceKey<Enchantment> enchantmentKey, int level) {
        var enchantment = enchantmentFromKey(access, enchantmentKey);
        if (enchantment != null) {
            stack.enchant(enchantment, level);
        }
    }

    public static void createTremorBlock(Level level, BlockPos blockPos, float impulseStrength) {
        if (level.isClientSide) {
            return;
        }
        if (level.getBlockState(blockPos.above()).isAir() || level.getBlockState(blockPos.above().above()).isAir()) {
            MagicManager.spawnParticles(level, new FallingBlockParticleOption(level.getBlockState(blockPos), new Vec3(0, impulseStrength, 0)), blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5, 1, 0, 0, 0, 0, true);
            if (!level.getBlockState(blockPos.above()).isAir()) {
                // if non-solid block (ie snow, grass, fire, etc) is on top, also create a tremor of that
                MagicManager.spawnParticles(level, new FallingBlockParticleOption(level.getBlockState(blockPos.above()), new Vec3(0, impulseStrength, 0)), blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, 1, 0, 0, 0, 0, true);
            }
        }
    }

    public static void createTremorBlockWithState(Level level, BlockState state, BlockPos blockPos, float impulseStrength) {
        MagicManager.spawnParticles(level, new FallingBlockParticleOption(state, new Vec3(0, impulseStrength, 0)), blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5, 1, 0, 0, 0, 0, true);

    }

    public static ItemStack setPotion(ItemStack itemStack, Holder<Potion> potion) {
        itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return itemStack;
    }

    public static void performTaunt(LivingEntity newTarget, float range, Predicate<Entity> selector) {
        performTaunt(newTarget, newTarget.level.getEntities(newTarget, newTarget.getBoundingBox().inflate(range, range, range),
                entity -> entity.distanceToSqr(newTarget) < range * range && selector.test(entity)));
    }

    public static void performTaunt(LivingEntity newTarget, List<Entity> targets) {
        targets.forEach(entity -> {
            if (entity instanceof Mob tauntmob) {
                MagicManager.spawnParticles(tauntmob.level, ParticleTypes.ANGRY_VILLAGER, tauntmob.getX(), tauntmob.getEyeY() + (tauntmob.getBoundingBox().maxY - tauntmob.getEyeY()) * 2, tauntmob.getZ(), 5, 0.3, 0.3, 0.3, 0, false);
                tauntmob.setTarget(newTarget);
            }
        });
    }

    public static void particleTrail(Level level, Vec3 a, Vec3 b, ParticleOptions particleType) {
        double d = a.distanceTo(b) * 4;
        for (int i = 0; i < d; i++) {
            double p = i / d;
            Vec3 vec = a.add(b.subtract(a).scale(p));
            MagicManager.spawnParticles(level, particleType, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0, true);
        }
    }

    public static Quaternionf rotationBetweenVectors(Vector3f from, Vector3f to) {
        // thanks yeepeetee
        Vector3f fromNorm = new Vector3f(from).normalize();
        Vector3f toNorm = new Vector3f(to).normalize();

        float dot = fromNorm.dot(toNorm);

        if (dot >= 0.9999f) { // Vectors are nearly identical
            return new Quaternionf().identity();
        } else if (dot <= -0.9999f) { // Vectors are opposite
            // Find an arbitrary perpendicular vector
            Vector3f perpendicular = new Vector3f(1, 0, 0);
            if (Math.abs(fromNorm.x) > 0.9f) {
                perpendicular.set(0, 1, 0);
            }
            perpendicular.cross(fromNorm).normalize();
            return new Quaternionf().rotationAxis((float) Math.PI, perpendicular);
        }

        // Compute rotation axis and angle
        Vector3f axis = new Vector3f(fromNorm).cross(toNorm).normalize();
        float angle = (float) Math.acos(dot);

        return new Quaternionf().rotationAxis(angle, axis);
    }

    public static void addFreezeTicks(LivingEntity target, int ticks) {
        addFreezeTicks(target, ticks, target.getTicksRequiredToFreeze() * 5);
    }

    public static void addFreezeTicks(LivingEntity target, int ticks, int cap) {
        target.setTicksFrozen(Math.min(target.getTicksFrozen() + ticks, cap < 0 ? Integer.MAX_VALUE : cap));
    }

    public static Vec3 slerp(double t, Vec3 from, Vec3 to) {
        from = from.normalize();
        to = to.normalize();
        double dot = from.dot(to);
        double theta = Math.acos(dot) * t;
        Vec3 relative = to.subtract(from.scale(dot)).normalize();
        Vec3 result = from.scale(Math.cos(theta)).add(relative.scale(Math.sin(theta)));
        return result;
    }

    public static boolean isSameItemSameComponentsIgnoreDurability(ItemStack a, ItemStack b) {
        a = a.copy();
        b = b.copy();
        a.remove(DataComponents.DAMAGE);
        b.remove(DataComponents.DAMAGE);
        return ItemStack.isSameItemSameComponents(a, b);
    }
}
