package com.faendir.zachtronics.bot.om.model

import com.faendir.om.parser.puzzle.PuzzleParser
import com.faendir.om.parser.solution.model.Position
import com.faendir.om.parser.solution.model.part.IO
import com.faendir.om.parser.solution.model.to
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.om.model.OmGroup.*
import com.faendir.zachtronics.bot.om.model.OmType.*
import com.faendir.zachtronics.bot.utils.getSingleMatchingPuzzle
import kotlinx.io.streams.asInput
import org.springframework.util.ResourceUtils

internal val SINGLE = setOf(0 to 0)
internal val FULL_CIRCLE = setOf(0 to 0, 1 to 0, -1 to 0, 0 to 1, -1 to 1, 0 to -1, 1 to -1)


@Suppress("unused", "SpellCheckingInspection")
enum class OmPuzzle(
    override val group: OmGroup,
    override val type: OmType,
    override val displayName: String,
    val id: String
) : Puzzle {
    STABILIZED_WATER(CHAPTER_1, NORMAL, "Stabilized Water", "P007"),
    REFINED_GOLD(CHAPTER_1, NORMAL, "Refined Gold", "P010"),
    FACE_POWDER(CHAPTER_1, NORMAL, "Face Powder", "P009"),
    WATERPROOF_SEALANT(CHAPTER_1, NORMAL, "Waterproof Sealant", "P011"),
    HANGOVER_CURE(CHAPTER_1, NORMAL, "Hangover Cure", "P013"),
    AIRSHIP_FUEL(CHAPTER_1, NORMAL, "Airship Fuel", "P008"),
    PRECISION_MACHINE_OIL(CHAPTER_1, NORMAL, "Precision Machine Oil", "P012"),
    HEALTH_TONIC(CHAPTER_1, NORMAL, "Health Tonic", "P014"),
    STAMINA_POTION(CHAPTER_1, NORMAL, "Stamina Potion", "P015"),

    HAIR_PRODUCT(CHAPTER_2, NORMAL, "Hair Product", "P016"),
    ROCKET_PROPELLANT(CHAPTER_2, NORMAL, "Rocket Propellant", "P019"),
    MIST_OF_INCAPACITATION(CHAPTER_2, NORMAL, "Mist of Incapacitation", "P018"),
    EXPLOSIVE_PHIAL(CHAPTER_2, NORMAL, "Explosive Phial", "P017"),
    ARMOR_FILAMENT(CHAPTER_2, INFINITE, "Armor Filament", "P020"),
    COURAGE_POTION(CHAPTER_2, NORMAL, "Courage Potion", "P021"),
    SURRENDER_FLARE(CHAPTER_2, NORMAL, "Surrender Flare", "P022"),

    ALCOHOL_SEPARATION(CHAPTER_3, NORMAL, "Alcohol Separation", "P024"),
    WATER_PURIFIER(CHAPTER_3, NORMAL, "Water Purifier", "P025"),
    SEAL_SOLVENT(CHAPTER_3, NORMAL, "Seal Solvent", "P026"),
    CLIMBING_ROPE_FIBER(CHAPTER_3, INFINITE, "Climbing Rope Fiber", "P027"),
    WARMING_TONIC(CHAPTER_3, NORMAL, "Warming Tonic", "P028"),
    LIFE_SENSING_POTION(CHAPTER_3, NORMAL, "Life-Sensing Potion", "P030b"),
    VERY_DARK_THREAD(CHAPTER_3, INFINITE, "Very Dark Thread", "P029"),

    LITHARGE_SEPARATION(CHAPTER_4, NORMAL, "Litharge Separation", "P031b"),
    STAIN_REMOVER(CHAPTER_4, NORMAL, "Stain Remover", "P034"),
    SWORD_ALLOY(CHAPTER_4, INFINITE, "Sword Alloy", "P033"),
    INVISIBLE_INK(CHAPTER_4, NORMAL, "Invisible Ink", "P032"),
    PURIFIED_GOLD(CHAPTER_4, NORMAL, "Purified Gold", "P036"),
    ALCHEMICAL_JEWEL(CHAPTER_4, NORMAL, "Alchemical Jewel", "P035"),
    GOLDEN_THREAD(CHAPTER_4, INFINITE, "Golden Thread", "P037"),

    MIST_OF_HALLUCINATION(CHAPTER_5, NORMAL, "Mist of Hallucination", "P038"),
    TIMING_CRYSTAL(CHAPTER_5, NORMAL, "Timing Crystal", "P042"),
    VOLTAIC_COIL(CHAPTER_5, INFINITE, "Voltaic Coil", "P039"),
    UNSTABLE_COMPOUND(CHAPTER_5, NORMAL, "Unstable Compound", "P040"),
    CURIOUS_LIPSTICK(CHAPTER_5, NORMAL, "Curious Lipstick", "P041"),
    UNIVERSAL_SOLVENT(CHAPTER_5, NORMAL, "Universal Solvent", "P043"),

    SILVER_PAINT(CHAPTER_PRODUCTION, PRODUCTION, "Silver Paint", "P076"),
    VISCOUS_SLUDGE(CHAPTER_PRODUCTION, PRODUCTION, "Viscous Sludge", "P080"),
    FRAGRANT_POWDERS(CHAPTER_PRODUCTION, PRODUCTION, "Fragrant Powders", "P075"),
    RAT_POISON(CHAPTER_PRODUCTION, PRODUCTION, "Rat Poison", "P074"),
    SPECIAL_AMARO(CHAPTER_PRODUCTION, PRODUCTION, "Special Amaro", "P083"),
    VAPOR_OF_LEVITY(CHAPTER_PRODUCTION, PRODUCTION, "Vapor of Levity", "P078"),
    ABRASIVE_PARTICLES(CHAPTER_PRODUCTION, PRODUCTION, "Abrasive Particles", "P079"),
    EYEDROPS_OF_REVELATION(CHAPTER_PRODUCTION, PRODUCTION, "Eyedrops of Revelation", "P081"),
    PARADE_ROCKET_FUEL(CHAPTER_PRODUCTION, PRODUCTION, "Parade-Rocket Fuel", "P082"),
    AETHER_DETECTOR(CHAPTER_PRODUCTION, PRODUCTION, "Aether Detector", "P077"),
    RECONSTRUCTED_SOLVENT(CHAPTER_PRODUCTION, PRODUCTION, "Reconstructed Solvent", "P084"),

    VAN_BERLO_S_WHEEL(JOURNAL_I, NORMAL, "Van Berlo's Wheel", "P054"),
    VAN_BERLO_S_CHAIN(JOURNAL_I, INFINITE, "Van Berlo's Chain", "P055"),
    REACTIVE_CINNABAR(JOURNAL_I, NORMAL, "Reactive Cinnabar", "P056"),
    SILVER_CAUSTIC(JOURNAL_I, NORMAL, "Silver Caustic", "P057"),
    LAMBENT_II_IX(JOURNAL_I, NORMAL, "Lambent II/IX", "P058"),

    EXPLORER_S_SALVE(
        JOURNAL_II, NORMAL, "Explorer's Salve", "P059"
    ),
    PRESERVATIVE_SALT(JOURNAL_II, NORMAL, "Preservative Salt", "P060"),
    SAILCLOTH_THREAD(JOURNAL_II, INFINITE, "Sailcloth Thread", "P061"),
    BUOYANT_CABLE(JOURNAL_II, INFINITE, "Buoyant Cable", "P062"),
    SPYGLASS_CRYSTAL(JOURNAL_II, NORMAL, "Spyglass Crystal", "P063"),

    RAVARI_S_WHEEL(JOURNAL_III, NORMAL, "Ravari's Wheel", "P064"),
    LUBRICATING_FILAMENT(JOURNAL_III, INFINITE, "Lubricating Filament", "P065"),
    RESONANT_CRYSTAL(JOURNAL_III, NORMAL, "Resonant Crystal", "P066"),
    REFINED_BRONZE(JOURNAL_III, INFINITE, "Refined Bronze", "P067"),
    ABLATIVE_CRYSTAL(JOURNAL_III, NORMAL, "Ablative Crystal", "P068"),

    PROOF_OF_COMPLETENESS(JOURNAL_IV, NORMAL, "Proof of Completeness", "P069"),
    WHEEL_REPRESENTATION(JOURNAL_IV, NORMAL, "Wheel Representation", "P070"),
    SYNTHESIS_VIA_ALCOHOL(JOURNAL_IV, NORMAL, "Synthesis via Alcohol", "P071"),
    UNIVERSAL_COMPOUND(JOURNAL_IV, NORMAL, "Universal Compound", "P072"),

    GENERAL_ANAESTHETIC(JOURNAL_V, NORMAL, "General Anaesthetic", "P086"),
    WAKEFULNESS_POTION(JOURNAL_V, NORMAL, "Wakefulness Potion", "P088"),
    SUTURE_THREAD(JOURNAL_V, INFINITE, "Suture Thread", "P085"),
    BLOOD_STANCHING_POWDER(
        JOURNAL_V, NORMAL, "Blood-Stanching Powder", "P087"
    ),
    TONIC_OF_HYDRATION(
        JOURNAL_V, NORMAL, "Tonic of Hydration", "P089"
    ),

    HEXSTABILIZED_SALT(JOURNAL_VI, PRODUCTION, "Hexstabilized Salt", "P091b"),
    LUSTRE(JOURNAL_VI, PRODUCTION, "Lustre", "P090"),
    LAMPLIGHT_GAS(JOURNAL_VI, PRODUCTION, "Lamplight Gas", "P092"),
    CONDUCTIVE_ENAMEL(JOURNAL_VI, PRODUCTION, "Conductive Enamel", "P093"),
    WELDING_THERMITE(JOURNAL_VI, PRODUCTION, "Welding Thermite", "P094"),

    VAN_BERLO_S_PIVOTS(JOURNAL_VII, NORMAL, "Van Berlo's Pivots", "P096"),
    REACTIVE_GOLD(JOURNAL_VII, NORMAL, "Reactive Gold", "P095"),
    ASSASSIN_S_FILAMENT(JOURNAL_VII, INFINITE, "Assassin's Filament", "P097"),
    VAPOROUS_SOLVENT(JOURNAL_VII, NORMAL, "Vaporous Solvent", "P098"),
    ALCHEMICAL_SLAG(JOURNAL_VII, NORMAL, "Alchemical Slag", "P099"),

    EXPLOSIVE_VICTRITE(JOURNAL_VIII, NORMAL, "Explosive Victrite", "P100"),
    CELESTIAL_THREAD(JOURNAL_VIII, INFINITE, "Celestial Thread", "P101"),
    VISILLARY_ANAESTHETIC(JOURNAL_VIII, NORMAL, "Visillary Anaesthetic", "P102"),
    ANIMISMUS_BUFFER(JOURNAL_VIII, NORMAL, "Animismus Buffer", "P104"),
    ELECTRUM_SEPARATION(JOURNAL_VIII, INFINITE, "Electrum Separation", "P103"),

    HYPER_VOLATILE_GAS(JOURNAL_IX, NORMAL, "Hyper-volatile Gas", "P106"),
    VANISHING_MATERIAL(JOURNAL_IX, PRODUCTION, "Vanishing Material", "P105"),
    SYNTHETIC_MALACHITE(JOURNAL_IX, PRODUCTION, "Synthetic Malachite", "P109"),
    EMBALMING_FLUID(JOURNAL_IX, NORMAL, "Embalming Fluid", "P108"),
    QUINTESSENTIAL_MEDIUM(JOURNAL_IX, NORMAL, "Quintessential Medium", "P107"),

    UNWINDING(TOURNAMENT_2019, NORMAL, "Unwinding", "w1611998067"),
    WIRE_FORMING_AND_UNFORMING(TOURNAMENT_2019, INFINITE, "Wire Forming and Unforming", "w1698784331"),
    CREATIVE_ACCOUNTING(TOURNAMENT_2019, NORMAL, "Creative Accounting", "w1698785633"),
    VIRULENT_VECTOR(TOURNAMENT_2019, NORMAL, "Virulent Vector", "w1698785238"),
    DWARVEN_FIRE_WINE(TOURNAMENT_2019, PRODUCTION, "Dwarven Fire Wine", "w1698786588"),
    MIRACULOUS_AUTOSALT(TOURNAMENT_2019, NORMAL, "Miraculous Autosalt", "w1698787102"),
    DO_YOU_REMEMBER(TOURNAMENT_2019, NORMAL, "Do You Remember", "w1698787731"),
    EVIL_ORE(TOURNAMENT_2019, NORMAL, "Evil Ore", "w1698788220"),
    PANACEA(TOURNAMENT_2019, NORMAL, "Panacea", "w1698789743"),

    POTENT_PORTABLES(TOURNAMENT_2020, NORMAL, "Potent Portables", "w2501727721"),
    AMALGATED_GOLD_RING(TOURNAMENT_2020, NORMAL, "Amalgated Gold Ring", "w2501727808"),
    SWAMP_FIBER(TOURNAMENT_2020, NORMAL, "Swamp Fiber", "w2501727889"),
    VOLATILITY_AND_TRANQUILITY(TOURNAMENT_2020, NORMAL, "Volatility and Tranquility", "w2501727977"),
    OVERLOADED(TOURNAMENT_2020, PRODUCTION, "Overloaded", "w2501728107"),
    ACTIVE_POLYMERASE(TOURNAMENT_2020, NORMAL, "Active Polymerase", "w2501728219"),
    HIGH_GLOSS_FINISH(TOURNAMENT_2020, NORMAL, "High Gloss Finish", "w2501728349"),

    A_WELCOME_TO_HOUSE_COLVAN(TOURNAMENT_2021, NORMAL, "A Welcome to House Colvan", "w2450560971"),
    IMPROVED_EXPLOSIVE_PHIAL(TOURNAMENT_2021, NORMAL, "Improved Explosive Phial", "w2450508212"),
    PANACEA_TO_POISON(TOURNAMENT_2021, NORMAL, "Panacea to Poison", "w2450511665"),
    MIST_OF_DOUSING(TOURNAMENT_2021, NORMAL, "Mist of Dousing", "w2450512021"),
    EMERGENCY_ANTIDOTE(TOURNAMENT_2021, PRODUCTION, "Emergency Antidote", "w2450512232"),
    CALM_BEFORE_THE_STORM(TOURNAMENT_2021, INFINITE, "Calm before the Storm", "w2450512434"),
    QUINTESSENTIAL_STABILIZER(TOURNAMENT_2021, NORMAL, "Quintessential Stabilizer", "w2450512626"),
    ELEMENTAL_JEWEL_SETTING(TOURNAMENT_2021, NORMAL, "Elemental Jewel Setting", "w2450512809"),
    ;

    val file by lazy {
        try {
            ResourceUtils.getFile("classpath:puzzle/$id.puzzle")
        } catch (e: Exception) {
            null
        }
    }

    val data by lazy {
        file?.inputStream()?.use { PuzzleParser.parse(it.asInput()) }
    }

    fun getReagentShape(io: IO): Set<Position> {
        return data?.inputs?.get(io.index)?.atoms?.map { it.second }?.map { it.x.toInt() to it.y.toInt() }?.toSet() ?: SINGLE
    }

    fun getProductShape(io: IO): Set<Position> {
        return data?.outputs?.get(io.index)?.atoms?.map { it.second }?.map { it.x.toInt() to it.y.toInt() }?.toSet() ?: SINGLE
    }

    companion object {
        fun parse(name: String): OmPuzzle = values().getSingleMatchingPuzzle(name)
    }
}