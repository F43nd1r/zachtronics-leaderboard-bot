package com.faendir.zachtronics.bot.om.model

import com.faendir.om.parser.solution.model.Position
import com.faendir.om.parser.solution.model.to
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.om.model.OmGroup.*
import com.faendir.zachtronics.bot.om.model.OmType.*
import com.faendir.zachtronics.bot.utils.getSingleMatchingPuzzle

internal val SINGLE = setOf(0 to 0)
private val PAIR = SINGLE + (1 to 0)
private val TRIPLE_STRAIGHT = PAIR + (-1 to 0)
private val TRIPLE_UP = SINGLE + (-1 to 0) + (0 to 1)
private val TRIPLE_DOWN = SINGLE + (-1 to 0) + (1 to -1)
private val STAR_RIGHT = TRIPLE_DOWN + (0 to 1)
private val STAR_LEFT = PAIR + (-1 to 1) + (0 to -1)
internal val FULL_CIRCLE = TRIPLE_STRAIGHT + (0 to 1) + (-1 to 1) + (0 to -1) + (1 to -1)
private val JEWEL = FULL_CIRCLE + setOf(-1 to 2, 1 to 1, 2 to -1, 1 to -2, -1 to -1, -2 to 1)
private val CRYSTAL = JEWEL + setOf(0 to 2, 2 to 0, 2 to -2, 0 to -2, -2 to 0, -2 to 2)
private val AMARO = PAIR + setOf(2 to -1, 0 to -1, -1 to -1, 1 to -2)
private val TONIC = PAIR + setOf(-1 to 1, 0 to -1, 1 to 1, 2 to -1)
private val ROCKET_FUEL = TRIPLE_STRAIGHT + (1 to 1) + (-1 to -1)
private val CIRCLE = SINGLE + setOf(0 to 1, 1 to 1, 2 to 0, 2 to -1, 1 to -1)
private val CABLE = (CIRCLE + setOf(2 to 1, 3 to 1, 3 to -1, 4 to -1)).infinite(4)
private val ALLOY = (PAIR + setOf(0 to 1, 1 to 1, 1 to -1, 2 to -1)).infinite(2)
private val DIAMOND = setOf(0 to 0, 0 to 1, -1 to 1, -1 to 2)

private fun Set<Position>.infinite(width: Int): Set<Position> {
    return generateSequence(this) { set -> set.map { it.x + width to it.y }.toSet() }.take(6).flatten().toSet() + (width * 6 to 0)
}


@Suppress("unused", "SpellCheckingInspection")
enum class OmPuzzle(
    override val group: OmGroup,
    override val type: OmType,
    override val displayName: String,
    val id: String,
    val productShapes: List<Set<Position>>,
    val reagentShapes: List<Set<Position>>
) : Puzzle {
    STABILIZED_WATER(CHAPTER_1, NORMAL, "Stabilized Water", "P007", listOf(PAIR), listOf(SINGLE, SINGLE)),
    REFINED_GOLD(CHAPTER_1, NORMAL, "Refined Gold", "P010", listOf(SINGLE), listOf(SINGLE, SINGLE)),
    FACE_POWDER(CHAPTER_1, NORMAL, "Face Powder", "P009", listOf(PAIR), listOf(SINGLE)),
    WATERPROOF_SEALANT(CHAPTER_1, NORMAL, "Waterproof Sealant", "P011", listOf(TRIPLE_UP), listOf(SINGLE, SINGLE)),
    HANGOVER_CURE(CHAPTER_1, NORMAL, "Hangover Cure", "P013", listOf(TRIPLE_DOWN), listOf(SINGLE)),
    AIRSHIP_FUEL(CHAPTER_1, NORMAL, "Airship Fuel", "P008", listOf(PAIR + (-1 to 1) + (2 to -1)), listOf(SINGLE, SINGLE, SINGLE)),
    PRECISION_MACHINE_OIL(CHAPTER_1, NORMAL, "Precision Machine Oil", "P012", listOf(TRIPLE_STRAIGHT), listOf(SINGLE, SINGLE, SINGLE)),
    HEALTH_TONIC(CHAPTER_1, NORMAL, "Health Tonic", "P014", listOf(STAR_RIGHT + (2 to -1)), listOf(TRIPLE_DOWN, SINGLE)),
    STAMINA_POTION(
        CHAPTER_1, NORMAL, "Stamina Potion", "P015", listOf(TRIPLE_STRAIGHT + setOf(-2 to 1, -1 to -1, 1 to 1, 2 to -1)),
        listOf(TRIPLE_DOWN, SINGLE, SINGLE)
    ),

    HAIR_PRODUCT(CHAPTER_2, NORMAL, "Hair Product", "P016", listOf(PAIR + (-1 to 1) + (1 to 1)), listOf(SINGLE)),
    ROCKET_PROPELLANT(CHAPTER_2, NORMAL, "Rocket Propellant", "P019", listOf(ROCKET_FUEL), listOf(SINGLE, SINGLE)),
    MIST_OF_INCAPACITATION(CHAPTER_2, NORMAL, "Mist of Incapacitation", "P018", listOf(PAIR, PAIR), listOf(SINGLE, SINGLE, SINGLE)),
    EXPLOSIVE_PHIAL(CHAPTER_2, NORMAL, "Explosive Phial", "P017", listOf(TRIPLE_STRAIGHT), listOf(SINGLE, SINGLE)),
    ARMOR_FILAMENT(CHAPTER_2, INFINITE, "Armor Filament", "P020", listOf(PAIR.infinite(2)), listOf(SINGLE, SINGLE)),
    COURAGE_POTION(CHAPTER_2, NORMAL, "Courage Potion", "P021", listOf(STAR_RIGHT), listOf(SINGLE, SINGLE)),
    SURRENDER_FLARE(CHAPTER_2, NORMAL, "Surrender Flare", "P022", listOf(FULL_CIRCLE), listOf(FULL_CIRCLE, SINGLE)),

    ALCOHOL_SEPARATION(CHAPTER_3, NORMAL, "Alcohol Separation", "P024", listOf(SINGLE, SINGLE, SINGLE, SINGLE), listOf(STAR_RIGHT)),
    WATER_PURIFIER(CHAPTER_3, NORMAL, "Water Purifier", "P025", listOf(FULL_CIRCLE), listOf(SINGLE, SINGLE)),
    SEAL_SOLVENT(CHAPTER_3, NORMAL, "Seal Solvent", "P026", listOf(TRIPLE_UP + (-2 to 1) + (1 to 1)), listOf(SINGLE, SINGLE, SINGLE)),
    CLIMBING_ROPE_FIBER(
        CHAPTER_3, INFINITE, "Climbing Rope Fiber", "P027", listOf((PAIR + setOf(-1 to 1, 0 to 1, 1 to -1, 2 to -1)).infinite(2)),
        listOf(SINGLE)
    ),
    WARMING_TONIC(CHAPTER_3, NORMAL, "Warming Tonic", "P028", listOf(TONIC), listOf(SINGLE, SINGLE, SINGLE)),
    LIFE_SENSING_POTION(CHAPTER_3, NORMAL, "Life-Sensing Potion", "P030b", listOf(CIRCLE), listOf(SINGLE, SINGLE, SINGLE)),
    VERY_DARK_THREAD(CHAPTER_3, INFINITE, "Very Dark Thread", "P029", listOf((PAIR + (0 to 1) + (2 to -1)).infinite(2)), listOf(SINGLE, SINGLE, SINGLE)),

    LITHARGE_SEPARATION(CHAPTER_4, NORMAL, "Litharge Separation", "P031b", listOf(SINGLE, SINGLE), listOf(FULL_CIRCLE)),
    STAIN_REMOVER(
        CHAPTER_4, NORMAL, "Stain Remover", "P034", listOf(SINGLE + setOf(-1 to 1, -2 to 1, 0 to 1, 1 to 1, 0 to -1, -1 to -1, 1 to -1, 2 to -1)),
        listOf(SINGLE, SINGLE, SINGLE)
    ),
    SWORD_ALLOY(CHAPTER_4, INFINITE, "Sword Alloy", "P033", listOf(ALLOY), listOf(SINGLE, SINGLE)),
    INVISIBLE_INK(CHAPTER_4, NORMAL, "Invisible Ink", "P032", listOf(TRIPLE_STRAIGHT + (1 to 1), TRIPLE_STRAIGHT + (-1 to -1)), listOf(SINGLE, SINGLE)),
    PURIFIED_GOLD(CHAPTER_4, NORMAL, "Purified Gold", "P036", listOf(SINGLE), listOf(SINGLE, SINGLE, SINGLE, SINGLE)),
    ALCHEMICAL_JEWEL(CHAPTER_4, NORMAL, "Alchemical Jewel", "P035", listOf(JEWEL), listOf(SINGLE, SINGLE, SINGLE)),
    GOLDEN_THREAD(CHAPTER_4, INFINITE, "Golden Thread", "P037", listOf((SINGLE + setOf(0 to 1, 1 to 1, 1 to -1, 2 to -1)).infinite(2)), listOf(SINGLE, SINGLE)),

    MIST_OF_HALLUCINATION(CHAPTER_5, NORMAL, "Mist of Hallucination", "P038", listOf(STAR_LEFT + setOf(-2 to 1, 1 to -2)), listOf(SINGLE, SINGLE)),
    TIMING_CRYSTAL(CHAPTER_5, NORMAL, "Timing Crystal", "P042", listOf(CRYSTAL), listOf(SINGLE, SINGLE)),
    VOLTAIC_COIL(
        CHAPTER_5, INFINITE, "Voltaic Coil", "P039", listOf((PAIR + setOf(-1 to 1, 0 to 1, 1 to -1, 2 to -1, 1 to -2, 2 to -2)).infinite(2)),
        listOf(SINGLE, SINGLE, SINGLE)
    ),
    UNSTABLE_COMPOUND(CHAPTER_5, NORMAL, "Unstable Compound", "P040", listOf(FULL_CIRCLE + setOf(0 to 2, 2 to -2, -2 to 0)), listOf(SINGLE, SINGLE)),
    CURIOUS_LIPSTICK(
        CHAPTER_5, NORMAL, "Curious Lipstick", "P041",
        listOf(STAR_RIGHT + setOf(-1 to -1, 1 to -2, 2 to -1, 0 to 1, -1 to 2, -1 to 3, 0 to 3, 1 to 2, 1 to 1)), listOf(SINGLE, SINGLE)
    ),
    UNIVERSAL_SOLVENT(CHAPTER_5, NORMAL, "Universal Solvent", "P043", listOf(JEWEL), listOf(SINGLE, SINGLE, SINGLE, SINGLE, SINGLE)),

    SILVER_PAINT(CHAPTER_PRODUCTION, PRODUCTION, "Silver Paint", "P076", listOf(PAIR + setOf(1 to 1, -1 to 1, -1 to 2)), listOf(PAIR, SINGLE)),
    VISCOUS_SLUDGE(CHAPTER_PRODUCTION, PRODUCTION, "Viscous Sludge", "P080", listOf(STAR_RIGHT + setOf(2 to -1, -1 to 2, -1 to -1)), listOf(PAIR)),
    FRAGRANT_POWDERS(CHAPTER_PRODUCTION, PRODUCTION, "Fragrant Powders", "P075", listOf(TRIPLE_DOWN, TRIPLE_DOWN, TRIPLE_DOWN), listOf(PAIR, SINGLE)),
    RAT_POISON(CHAPTER_PRODUCTION, PRODUCTION, "Rat Poison", "P074", listOf(STAR_RIGHT), listOf(PAIR)),
    SPECIAL_AMARO(CHAPTER_PRODUCTION, PRODUCTION, "Special Amaro", "P083", listOf(AMARO), listOf(AMARO)),
    VAPOR_OF_LEVITY(CHAPTER_PRODUCTION, PRODUCTION, "Vapor of Levity", "P078", listOf(TONIC), listOf(PAIR)),
    ABRASIVE_PARTICLES(CHAPTER_PRODUCTION, PRODUCTION, "Abrasive Particles", "P079", listOf(STAR_LEFT), listOf(FULL_CIRCLE)),
    EYEDROPS_OF_REVELATION(CHAPTER_PRODUCTION, PRODUCTION, "Eyedrops of Revelation", "P081", listOf(STAR_LEFT, STAR_RIGHT), listOf(PAIR)),
    PARADE_ROCKET_FUEL(CHAPTER_PRODUCTION, PRODUCTION, "Parade-Rocket Fuel", "P082", listOf(ROCKET_FUEL), listOf(PAIR)),
    AETHER_DETECTOR(CHAPTER_PRODUCTION, PRODUCTION, "Aether Detector", "P077", listOf(FULL_CIRCLE), listOf(FULL_CIRCLE)),
    RECONSTRUCTED_SOLVENT(
        CHAPTER_PRODUCTION, PRODUCTION, "Reconstructed Solvent", "P084", listOf(TRIPLE_DOWN + setOf(-2 to 0, 2 to -1, 2 to 0, 1 to 1)),
        listOf(SINGLE, SINGLE)
    ),

    VAN_BERLO_S_WHEEL(JOURNAL_I, NORMAL, "Van Berlo's Wheel", "P054", listOf(FULL_CIRCLE), listOf(SINGLE, SINGLE)),
    VAN_BERLO_S_CHAIN(
        JOURNAL_I, INFINITE, "Van Berlo's Chain", "P055", listOf((PAIR + setOf(0 to 1, 1 to -1, 2 to 0, 2 to 1, 3 to 0, 3 to -1)).infinite(4)),
        listOf(SINGLE, SINGLE)
    ),
    REACTIVE_CINNABAR(JOURNAL_I, NORMAL, "Reactive Cinnabar", "P056", listOf(FULL_CIRCLE), listOf(SINGLE, SINGLE)),
    SILVER_CAUSTIC(
        JOURNAL_I, NORMAL, "Silver Caustic", "P057", listOf(SINGLE + setOf(-2 to 1, -1 to 1, 0 to 1, 0 to -1, 1 to -1, 2 to -1)),
        listOf(SINGLE, SINGLE, SINGLE, SINGLE)
    ),
    LAMBENT_II_IX(
        JOURNAL_I, NORMAL, "Lambent II/IX", "P058", listOf(FULL_CIRCLE + (-2 to 1) + (-1 to -1), FULL_CIRCLE + (1 to 1) + (2 to -1)),
        listOf(SINGLE, SINGLE, SINGLE)
    ),

    EXPLORER_S_SALVE(
        JOURNAL_II, NORMAL, "Explorer's Salve", "P059", listOf(FULL_CIRCLE + setOf(-2 to 2, -1 to 2, 2 to 0, 2 to -1, 0 to -2, -1 to -1)),
        listOf(STAR_RIGHT, PAIR, SINGLE)
    ),
    PRESERVATIVE_SALT(JOURNAL_II, NORMAL, "Preservative Salt", "P060", listOf(FULL_CIRCLE), listOf(STAR_RIGHT, SINGLE)),
    SAILCLOTH_THREAD(JOURNAL_II, INFINITE, "Sailcloth Thread", "P061", listOf((FULL_CIRCLE + (-1 to 1) + (0 to -1)).infinite(3)), listOf(STAR_RIGHT, SINGLE)),
    BUOYANT_CABLE(JOURNAL_II, INFINITE, "Buoyant Cable", "P062", listOf(CABLE), listOf(SINGLE, SINGLE)),
    SPYGLASS_CRYSTAL(JOURNAL_II, NORMAL, "Spyglass Crystal", "P063", listOf(CRYSTAL), listOf(SINGLE, SINGLE, SINGLE)),

    RAVARI_S_WHEEL(JOURNAL_III, NORMAL, "Ravari's Wheel", "P064", listOf(FULL_CIRCLE), listOf(SINGLE, SINGLE, SINGLE)),
    LUBRICATING_FILAMENT(JOURNAL_III, INFINITE, "Lubricating Filament", "P065", listOf(CABLE), listOf(SINGLE, SINGLE)),
    RESONANT_CRYSTAL(JOURNAL_III, NORMAL, "Resonant Crystal", "P066", listOf(CRYSTAL), listOf(SINGLE, SINGLE, SINGLE)),
    REFINED_BRONZE(JOURNAL_III, INFINITE, "Refined Bronze", "P067", listOf(ALLOY, STAR_RIGHT), listOf(FULL_CIRCLE, FULL_CIRCLE)),
    ABLATIVE_CRYSTAL(JOURNAL_III, NORMAL, "Ablative Crystal", "P068", listOf(JEWEL), listOf(FULL_CIRCLE)),

    PROOF_OF_COMPLETENESS(JOURNAL_IV, NORMAL, "Proof of Completeness", "P069", listOf(PAIR, PAIR, PAIR, PAIR), listOf(SINGLE)),
    WHEEL_REPRESENTATION(JOURNAL_IV, NORMAL, "Wheel Representation", "P070", listOf(FULL_CIRCLE), listOf(SINGLE)),
    SYNTHESIS_VIA_ALCOHOL(JOURNAL_IV, NORMAL, "Synthesis via Alcohol", "P071", listOf(SINGLE), listOf(STAR_RIGHT)),
    UNIVERSAL_COMPOUND(
        JOURNAL_IV, NORMAL, "Universal Compound", "P072", listOf(FULL_CIRCLE + setOf(0 to 2, 2 to 0, 2 to -2, 0 to -2, -2 to 0, -2 to 2)),
        listOf(SINGLE)
    ),

    GENERAL_ANAESTHETIC(
        JOURNAL_V, NORMAL, "General Anaesthetic", "P086",
        listOf(STAR_LEFT + setOf(-2 to 1, -2 to 0, -3 to 0, -1 to -1, 1 to -2, 2 to -2, 2 to -1, 3 to -1)), listOf(SINGLE, SINGLE, SINGLE, SINGLE)
    ),
    WAKEFULNESS_POTION(
        JOURNAL_V, NORMAL, "Wakefulness Potion", "P088",
        listOf(STAR_RIGHT + setOf(1 to 1, 1 to 2, 2 to 0, 1 to -2, 0 to -2, -1 to -1, -3 to 1, -2 to 1, -2 to 2)), listOf(TRIPLE_DOWN, PAIR)
    ),
    SUTURE_THREAD(JOURNAL_V, INFINITE, "Suture Thread", "P085", listOf((PAIR + setOf(0 to 1, 1 to 1, -1 to 2, 0 to 2)).infinite(2)), listOf(SINGLE, SINGLE)),
    BLOOD_STANCHING_POWDER(
        JOURNAL_V, NORMAL, "Blood-Stanching Powder", "P087",
        listOf(STAR_LEFT + setOf(1 to -2, -1 to -1, -1 to -2, -2 to 0, -3 to 0, -2 to 1, -3 to 2, -1 to 2, -2 to 3, 0 to 2, 0 to 3, 1 to 1, 2 to 1, 2 to -1)),
        listOf(SINGLE, SINGLE, SINGLE)
    ),
    TONIC_OF_HYDRATION(
        JOURNAL_V, NORMAL, "Tonic of Hydration", "P089", listOf((CRYSTAL - (0 to 0)).map { it.x + 1 to it.y }.toSet()),
        listOf(SINGLE, SINGLE, SINGLE, SINGLE)
    ),

    HEXSTABILIZED_SALT(JOURNAL_VI, PRODUCTION, "Hexstabilized Salt", "P091b", listOf(PAIR + setOf(2 to -1, 2 to -2, 1 to -2, 0 to -1)), listOf(FULL_CIRCLE)),
    LUSTRE(JOURNAL_VI, PRODUCTION, "Lustre", "P090", listOf(STAR_RIGHT), listOf(FULL_CIRCLE)),
    LAMPLIGHT_GAS(JOURNAL_VI, PRODUCTION, "Lamplight Gas", "P092", listOf(FULL_CIRCLE), listOf(FULL_CIRCLE)),
    CONDUCTIVE_ENAMEL(JOURNAL_VI, PRODUCTION, "Conductive Enamel", "P093", listOf(FULL_CIRCLE), listOf(PAIR, SINGLE)),
    WELDING_THERMITE(JOURNAL_VI, PRODUCTION, "Welding Thermite", "P094", listOf(FULL_CIRCLE), listOf(TRIPLE_STRAIGHT, SINGLE, SINGLE)),

    VAN_BERLO_S_PIVOTS(
        JOURNAL_VII, NORMAL, "Van Berlo's Pivots", "P096",
        listOf(SINGLE + (-1 to 0) + (-1 to 1), PAIR + (0 to 1), PAIR + (1 to -1), SINGLE + (-1 to 0) + (0 to -1)), listOf(FULL_CIRCLE)
    ),
    REACTIVE_GOLD(JOURNAL_VII, NORMAL, "Reactive Gold", "P095", listOf(PAIR), listOf(PAIR)),
    ASSASSIN_S_FILAMENT(
        JOURNAL_VII, INFINITE, "Assassin's Filament", "P097", listOf((0..3).flatMap { x -> (-1..1).map { y -> x to y } }.toSet().infinite(4)),
        listOf(SINGLE, SINGLE, SINGLE)
    ),
    VAPOROUS_SOLVENT(JOURNAL_VII, NORMAL, "Vaporous Solvent", "P098", listOf(JEWEL), listOf(PAIR, SINGLE)),
    ALCHEMICAL_SLAG(
        JOURNAL_VII, NORMAL, "Alchemical Slag", "P099", listOf(SINGLE, SINGLE),
        listOf(STAR_LEFT + (2 to -1), SINGLE + (1 to -1) + (-1 to 1), SINGLE + (-1 to 1))
    ),

    EXPLOSIVE_VICTRITE(JOURNAL_VIII, NORMAL, "Explosive Victrite", "P100", listOf(FULL_CIRCLE), listOf(SINGLE)),
    CELESTIAL_THREAD(
        JOURNAL_VIII, INFINITE, "Celestial Thread", "P101", listOf((PAIR + setOf(0 to 1, 1 to 1, 1 to -1, 2 to -1, 1 to -2, 2 to -2)).infinite(2)),
        listOf(PAIR, SINGLE)
    ),
    VISILLARY_ANAESTHETIC(
        JOURNAL_VIII, NORMAL, "Visillary Anaesthetic", "P102", listOf(STAR_RIGHT + (-1 to -1), TRIPLE_UP + (-2 to 1) + (-1 to -1)),
        listOf(TRIPLE_DOWN, PAIR)
    ),
    ANIMISMUS_BUFFER(JOURNAL_VIII, NORMAL, "Animismus Buffer", "P104", listOf(FULL_CIRCLE + setOf(-2 to 0, -2 to 1, -1 to -1)), listOf(TRIPLE_DOWN)),
    ELECTRUM_SEPARATION(
        JOURNAL_VIII, INFINITE, "Electrum Separation", "P103", listOf((PAIR + (2 to 0)).infinite(3), (PAIR + (2 to 0)).infinite(3), TRIPLE_DOWN),
        listOf(JEWEL + setOf(2 to 0, 0 to -2, -2 to 2))
    ),

    HYPER_VOLATILE_GAS(JOURNAL_IX, NORMAL, "Hyper-volatile Gas", "P106", listOf(FULL_CIRCLE), listOf(FULL_CIRCLE)),
    VANISHING_MATERIAL(JOURNAL_IX, PRODUCTION, "Vanishing Material", "P105", listOf(TRIPLE_STRAIGHT), listOf(SINGLE)),
    SYNTHETIC_MALACHITE(
        JOURNAL_IX, PRODUCTION, "Synthetic Malachite", "P109",
        listOf(setOf(0 to 0, 0 to 1, 0 to 2, 1 to 2, 2 to 2, 3 to 1, 4 to 0, 4 to -1, 4 to -2, 3 to -2, 2 to -2, 1 to -1)), listOf(PAIR, SINGLE)
    ),
    EMBALMING_FLUID(
        JOURNAL_IX, NORMAL, "Embalming Fluid", "P108",
        listOf(STAR_LEFT + setOf(1 to -2, 2 to -2, 2 to -1, 1 to 1, 0 to 2, -1 to 2, -2 to 1, -2 to 0, -1 to -1)), listOf(PAIR, PAIR, SINGLE)
    ),
    QUINTESSENTIAL_MEDIUM(JOURNAL_IX, NORMAL, "Quintessential Medium", "P107", listOf(TRIPLE_UP + (-1 to -1)), listOf(FULL_CIRCLE)),

    UNWINDING(
        TOURNAMENT_2019, NORMAL, "Unwinding", "w1611998067", listOf(TRIPLE_STRAIGHT + setOf(-2 to 0, -3 to 0, -4 to 0)),
        listOf(SINGLE + setOf(1 to -1, 0 to -1, 2 to -2, 1 to -2, 0 to -2))
    ),
    WIRE_FORMING_AND_UNFORMING(
        TOURNAMENT_2019, INFINITE, "Wire Forming and Unforming", "w1698784331",
        listOf((PAIR + setOf(0 to 1, 1 to 1, 0 to -1, 1 to -1)).infinite(2), TRIPLE_STRAIGHT + (0 to 1) + (0 to -1), SINGLE.infinite(1)), listOf(PAIR, PAIR)
    ),
    CREATIVE_ACCOUNTING(TOURNAMENT_2019, NORMAL, "Creative Accounting", "w1698785633", listOf(SINGLE), listOf(STAR_RIGHT)),
    VIRULENT_VECTOR(
        TOURNAMENT_2019, NORMAL, "Virulent Vector", "w1698785238",
        listOf(FULL_CIRCLE + setOf(-2 to 1, -2 to 2, -1 to 2, 0 to 2, 1 to 1, 1 to 2, 2 to 1, 2 to 0, 2 to -1, 2 to -2, 1 to -2)),
        listOf(SINGLE, SINGLE, SINGLE)
    ),
    DWARVEN_FIRE_WINE(
        TOURNAMENT_2019, PRODUCTION, "Dwarven Fire Wine", "w1698786588", listOf(STAR_LEFT + (1 to 1) + (0 to 2)),
        listOf(setOf(0 to 0, 0 to 1, -1 to 2))
    ),
    MIRACULOUS_AUTOSALT(TOURNAMENT_2019, NORMAL, "Miraculous Autosalt", "w1698787102", listOf(setOf(0 to 0, 0 to 1, 1 to 1, -1 to 2)), listOf(SINGLE)),
    DO_YOU_REMEMBER(TOURNAMENT_2019, NORMAL, "Do You Remember", "w1698787731", listOf(DIAMOND, DIAMOND, DIAMOND), listOf(SINGLE, SINGLE, SINGLE, SINGLE)),
    EVIL_ORE(
        TOURNAMENT_2019, NORMAL, "Evil Ore", "w1698788220", listOf(SINGLE),
        listOf(FULL_CIRCLE + setOf(-3 to 2, -3 to 3, -2 to 1, -2 to 2, -2 to 3, -1 to 2, -1 to 3, 0 to 2, 0 to 3, 1 to 1, 1 to 2))
    ),
    PANACEA(TOURNAMENT_2019, NORMAL, "Panacea", "w1698789743", listOf(STAR_LEFT + (2 to -1), SINGLE), listOf(PAIR));

    companion object {
        fun parse(name: String): OmPuzzle = values().getSingleMatchingPuzzle(name)
    }
}