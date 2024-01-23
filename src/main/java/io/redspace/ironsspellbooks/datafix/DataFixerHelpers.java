package io.redspace.ironsspellbooks.datafix;

import com.google.common.collect.ImmutableMap;
import io.redspace.ironsspellbooks.datafix.fixers.*;
import io.redspace.ironsspellbooks.spells.blood.*;
import io.redspace.ironsspellbooks.spells.eldritch.AbyssalShroudSpell;
import io.redspace.ironsspellbooks.spells.eldritch.SculkTentaclesSpell;
import io.redspace.ironsspellbooks.spells.ender.*;
import io.redspace.ironsspellbooks.spells.evocation.*;
import io.redspace.ironsspellbooks.spells.fire.*;
import io.redspace.ironsspellbooks.spells.holy.*;
import io.redspace.ironsspellbooks.spells.ice.*;
import io.redspace.ironsspellbooks.spells.lightning.*;
import io.redspace.ironsspellbooks.spells.nature.*;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataFixerHelpers {
    public static final Map<Integer, String> LEGACY_SPELL_MAPPING = ImmutableMap.<Integer, String>builder()
            .put(1, new FireballSpell().getSpellId())
            .put(2, new BurningDashSpell().getSpellId())
            .put(3, new TeleportSpell().getSpellId())
            .put(4, new MagicMissileSpell().getSpellId())
            .put(5, new ElectrocuteSpell().getSpellId())
            .put(6, new ConeOfColdSpell().getSpellId())
            .put(7, new HealSpell().getSpellId())
            .put(8, new BloodSlashSpell().getSpellId())
            .put(9, new SummonVexSpell().getSpellId())
            .put(10, new FireboltSpell().getSpellId())
            .put(11, new FireBreathSpell().getSpellId())
            .put(12, new IcicleSpell().getSpellId())
            .put(13, new FirecrackerSpell().getSpellId())
            .put(14, new SummonHorseSpell().getSpellId())
            .put(15, new AngelWingsSpell().getSpellId())
            .put(16, new ShieldSpell().getSpellId())
            .put(17, new WallOfFireSpell().getSpellId())
            .put(18, new WispSpell().getSpellId())
            .put(19, new FangStrikeSpell().getSpellId())
            .put(20, new FangWardSpell().getSpellId())
            .put(21, new EvasionSpell().getSpellId())
            .put(22, new HeartstopSpell().getSpellId())
            .put(23, new LightningLanceSpell().getSpellId())
            .put(24, new LightningBoltSpell().getSpellId())
            .put(25, new RaiseDeadSpell().getSpellId())
            .put(26, new WitherSkullSpell().getSpellId())
            .put(27, new GreaterHealSpell().getSpellId())
            .put(28, new CloudOfRegenerationSpell().getSpellId())
            .put(29, new RayOfSiphoningSpell().getSpellId())
            .put(30, new MagicArrowSpell().getSpellId())
            .put(31, new LobCreeperSpell().getSpellId())
            .put(32, new ChainCreeperSpell().getSpellId())
            .put(33, new BlazeStormSpell().getSpellId())
            .put(34, new FrostStepSpell().getSpellId())
            .put(35, new AbyssalShroudSpell().getSpellId())
            .put(36, new FrostbiteSpell().getSpellId())
            .put(37, new AscensionSpell().getSpellId())
            .put(38, new InvisibilitySpell().getSpellId())
            .put(39, new BloodStepSpell().getSpellId())
            .put(40, new SummonPolarBearSpell().getSpellId())
            .put(41, new BlessingOfLifeSpell().getSpellId())
            .put(42, new DragonBreathSpell().getSpellId())
            .put(43, new FortifySpell().getSpellId())
            .put(44, new CounterspellSpell().getSpellId())
            .put(45, new SpectralHammerSpell().getSpellId())
            .put(46, new ChargeSpell().getSpellId())
            .put(47, new SculkTentaclesSpell().getSpellId())
            .put(48, new IceBlockSpell().getSpellId())
            .put(49, new PoisonBreathSpell().getSpellId())
            .put(50, new PoisonArrowSpell().getSpellId())
            .put(51, new PoisonSplashSpell().getSpellId())
            .put(52, new AcidOrbSpell().getSpellId())
            .put(53, new SpiderAspectSpell().getSpellId())
            .put(54, new BlightSpell().getSpellId())
            .put(55, new RootSpell().getSpellId())
            .put(56, new BlackHoleSpell().getSpellId())
            .put(57, new BloodNeedlesSpell().getSpellId())
            .put(58, new AcupunctureSpell().getSpellId())
            .put(59, new MagmaBombSpell().getSpellId())
            .put(60, new StarfallSpell().getSpellId())
            .put(61, new HealingCircleSpell().getSpellId())
            .put(62, new GuidingBoltSpell().getSpellId())
            .put(63, new SunbeamSpell().getSpellId())
            .put(64, new GustSpell().getSpellId())
            .put(65, new ChainLightningSpell().getSpellId())
            .put(66, new DevourSpell().getSpellId())
            .build();

    public static final Map<String, String> LEGACY_ITEM_IDS = ImmutableMap.<String, String>builder()
            .put("irons_spellbooks:poison_rune", "irons_spellbooks:nature_rune")
            .put("irons_spellbooks:poison_upgrade_orb", "irons_spellbooks:nature_upgrade_orb")
            .build();

    public static final Map<String, String> LEGACY_UPGRADE_TYPE_IDS = ImmutableMap.<String, String>builder()
            .put("fire_power", "irons_spellbooks:fire_power")
            .put("ice_power", "irons_spellbooks:ice_power")
            .put("lightning_power", "irons_spellbooks:lightning_power")
            .put("holy_power", "irons_spellbooks:holy_power")
            .put("ender_power", "irons_spellbooks:ender_power")
            .put("blood_power", "irons_spellbooks:blood_power")
            .put("evocation_power", "irons_spellbooks:evocation_power")
            .put("poison_power", "irons_spellbooks:nature_power")
            .put("cooldown", "irons_spellbooks:cooldown")
            .put("spell_resistance", "irons_spellbooks:spell_resistance")
            .put("mana", "irons_spellbooks:mana")
            .put("melee_damage", "irons_spellbooks:melee_damage")
            .put("melee_speed", "irons_spellbooks:melee_speed")
            .put("health", "irons_spellbooks:health")
            .build();

    public static final Map<String, String> NEW_SPELL_IDS = ImmutableMap.<String, String>builder()
            .put("irons_spellbooks:void_tentacles", "irons_spellbooks:sculk_tentacles")
            .build();

    public static List<DataFixerElement> DATA_FIXER_ELEMENTS = List.of(
            new FixIsbEnhance(),
            new FixTetra(),
            new FixApoth(),
            new FixIsbSpellbook(),
            new FixIsbSpell(),
            new FixItemNames(),
            new FixUpgradeType());

    public static List<byte[]> DATA_MATCHER_TARGETS = ((Supplier<List<byte[]>>)
            () -> {
                var bytesList = DATA_FIXER_ELEMENTS
                        .stream()
                        .flatMap(item -> item.preScanValueBytes().stream())
                        .collect(Collectors.toList());
                bytesList.add(IronsWorldUpgrader.INHABITED_TIME_MARKER);
                return (ArrayList<byte[]>) bytesList;
            }).get();

    /**
     * Returns true if data was updated
     */
    public static boolean doFixUps(CompoundTag tag) {
        var runningCount = new AtomicInteger();

        DATA_FIXER_ELEMENTS.forEach(fixerElement -> {
            if (fixerElement.runFixer(tag)) {
                runningCount.incrementAndGet();
            }
        });

        return runningCount.get() > 0;
    }
}
