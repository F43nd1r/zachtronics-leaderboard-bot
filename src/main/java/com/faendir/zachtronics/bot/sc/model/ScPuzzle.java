/*
 * Copyright (c) 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.sc.model;

import com.faendir.zachtronics.bot.model.Puzzle;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ScPuzzle implements Puzzle<ScCategory> {
    research_example_1(ScGroup.MAIN, ScType.RESEARCH, "Of Pancakes and Spaceships", true),
    research_tutorial_1(ScGroup.MAIN, ScType.RESEARCH, "Slightly Different", true),
    research_tutorial_1point5(ScGroup.MAIN, ScType.RESEARCH, "Crossover", true),
    research_example_2(ScGroup.MAIN, ScType.RESEARCH, "An Introduction to Bonding", true),
    research_tutorial_2(ScGroup.MAIN, ScType.RESEARCH, "A Brief History of SpaceChem", true),
    research_tutorial_3(ScGroup.MAIN, ScType.RESEARCH, "Removing Bonds", true),

    research_tutorial_4(ScGroup.MAIN, ScType.RESEARCH, "Double Bonds", true),
    research_tutorial_5(ScGroup.MAIN, ScType.RESEARCH, "Best Left Unanswered", true),
    research_tutorial_6(ScGroup.MAIN, ScType.RESEARCH, "Multiple Outputs", true),
    production_tutorial_1(ScGroup.MAIN, ScType.PRODUCTION_TRIVIAL, "An Introduction to Pipelines", true),
    production_tutorial_2(ScGroup.MAIN, ScType.RESEARCH, "There's Something in the Fishcake", true),
    production_tutorial_3(ScGroup.MAIN, ScType.RESEARCH, "Sleepless on Sernimir IV", true),

    bonding_2(ScGroup.MAIN, ScType.RESEARCH, "Every Day is the First Day", true),
    bonding_3(ScGroup.MAIN, ScType.RESEARCH, "It Takes Three", true),
    bonding_4(ScGroup.MAIN, ScType.RESEARCH, "Split Before Bonding", true),
    bonding_6(ScGroup.MAIN, ScType.RESEARCH, "Settling into the Routine", true),
    bonding_7(ScGroup.MAIN, ScType.PRODUCTION, "Nothing Works", true),
    bonding_boss(ScGroup.MAIN, ScType.BOSS, "A Most Unfortunate Malfunction", true),
    bonding_5(ScGroup.MAIN, ScType.PRODUCTION, "Challenge: In-Place Swap", true),

    sensing_1(ScGroup.MAIN, ScType.RESEARCH, "An Introduction to Sensing", false),
    sensing_2(ScGroup.MAIN, ScType.RESEARCH, "Prelude to a Migraine", false),
    sensing_3(ScGroup.MAIN, ScType.RESEARCH, "Random Oxides", false),
    sensing_4(ScGroup.MAIN, ScType.PRODUCTION, "No Ordinary Headache", false),
    sensing_5(ScGroup.MAIN, ScType.PRODUCTION, "No Thanks Necessary", false),
    sensing_boss(ScGroup.MAIN, ScType.BOSS, "No Need for Introductions", false),
    sensing_6(ScGroup.MAIN, ScType.PRODUCTION, "Challenge: Going Green", false),

    fusion_1(ScGroup.MAIN, ScType.RESEARCH, "Ice to Meet You", true),
    fusion_2(ScGroup.MAIN, ScType.RESEARCH, "Under the Ice", true),
    fusion_3(ScGroup.MAIN, ScType.RESEARCH, "Unknown Sender", true),
    fusion_5(ScGroup.MAIN, ScType.PRODUCTION, "Falling", true),
    fusion_boss(ScGroup.MAIN, ScType.BOSS, "Exploding Head Syndrome", true),
    fusion_6(ScGroup.MAIN, ScType.PRODUCTION, "Challenge: Applied Fusion", true),

    mining_2(ScGroup.MAIN, ScType.RESEARCH, "Like a Boss", true),
    mining_3(ScGroup.MAIN, ScType.RESEARCH, "Sacré Bleu!", true),
    mining_7(ScGroup.MAIN, ScType.RESEARCH, "The Plot Thickens", true),
    mining_1(ScGroup.MAIN, ScType.RESEARCH, "Danger Zone", false),
    mining_5(ScGroup.MAIN, ScType.PRODUCTION, "Molecular Foundry", false),
    mining_4(ScGroup.MAIN, ScType.PRODUCTION, "Gas Works Park", false),
    mining_boss(ScGroup.MAIN, ScType.BOSS, "More than Machine", false),
    mining_6(ScGroup.MAIN, ScType.PRODUCTION, "Challenge: KOHCTPYKTOP", true),

    research_1(ScGroup.MAIN, ScType.RESEARCH, "The Blue Danube", false),
    research_3(ScGroup.MAIN, ScType.RESEARCH, "No Stomach for Lunch", true),
    research_6(ScGroup.MAIN, ScType.RESEARCH, "No Employment Record Found", false),
    research_5(ScGroup.MAIN, ScType.RESEARCH, "Right All Along", true),
    research_2(ScGroup.MAIN, ScType.RESEARCH, "Accidents Happen", false),
    research_boss(ScGroup.MAIN, ScType.BOSS_RANDOM, "Don't Fear the Reaper", true),

    organic_1(ScGroup.MAIN, ScType.RESEARCH, "Special Assignment", true),
    organic_2(ScGroup.MAIN, ScType.RESEARCH, "Suspicious Behavior", true),
    organic_3(ScGroup.MAIN, ScType.RESEARCH, "I Told You So", true),
    organic_5(ScGroup.MAIN, ScType.PRODUCTION_FIXED, "Ω-Pseudoethyne", false),
    organic_6(ScGroup.MAIN, ScType.PRODUCTION_FIXED, "Σ-Ethylene", true),
    organic_boss(ScGroup.MAIN, ScType.BOSS_FIXED, "Freedom of Choice", true),

    warp_boss(ScGroup.MAIN, ScType.BOSS_RANDOM, "End of the Line", true),


    tf2_1(ScGroup.TF2, ScType.RESEARCH, "Moustachium 602", true),
    tf2_2(ScGroup.TF2, ScType.RESEARCH, "Moustachium 604", true),
    tf2_3(ScGroup.TF2, ScType.RESEARCH, "Moustachium 608", true),


    quantum_1(ScGroup.CORVI63, ScType.RESEARCH, "QT-1", true),
    quantum_2(ScGroup.CORVI63, ScType.RESEARCH, "QT-2", true),
    quantum_4(ScGroup.CORVI63, ScType.PRODUCTION, "Teleporters", true),
    quantum_3(ScGroup.CORVI63, ScType.RESEARCH, "QT-3", true),
    quantum_5(ScGroup.CORVI63, ScType.RESEARCH, "QT-4", true),
    quantum_6(ScGroup.CORVI63, ScType.PRODUCTION, "Precursor Compounds", false),
    quantum_boss(ScGroup.CORVI63, ScType.BOSS, "Collapsar", true),


    published_1_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Tunnels I", true),
    published_1_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Tunnels II", true),
    published_1_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Tunnels III", true),
    published_2_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Fission I", true),
    published_2_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Fission II", true),
    published_2_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Fission III", true),
    published_3_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Chloroform", true),
    published_3_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Cycloaddition", true),
    published_3_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "KOHCTPYKTOP++", false),
    published_4_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Quantum Decomposition", true),
    published_4_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Propane Accessories", true),
    published_4_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Benzoic Acid", true),
    published_5_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Electrophilic Addition", true),
    published_5_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Diethyl Ether", true),
    published_5_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "2-Hexene", true),
    published_6_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Mazeite'", true),
    published_6_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Silica", true),
    published_6_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Galvanization", true),
    published_7_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Industrial Methanol", true),
    published_7_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Glyoxylic Acid", true),
    published_7_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Bioplastic", true),
    published_8_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Chlorination", true),
    published_8_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Sulfuric Acid (1-8-2)", true),
    published_8_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Bad Times", true),
    published_9_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Friedel-Crafts", true),
    published_9_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Wöhler Synthesis", true),
    published_9_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Diels-Alder", true),
    published_10_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Bad Times, Part II", true),
    published_10_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Freon", false),
    published_10_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Benzene Derivatives", false),
    published_11_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Portland Cement", true),
    published_11_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Solder Coarsening", true),
    published_11_3b(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Graphene", true),
    published_12_1(ScGroup.RESEARCHNET1, ScType.RESEARCH, "The Big Cleanup", false),
    published_12_2(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Going Green Part II", true),
    published_12_3(ScGroup.RESEARCHNET1, ScType.RESEARCH, "Waste Treatment", false),

    published_13_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Pyridine (2-1-1)", true),
    published_13_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Breakdown (2-1-2)", true),
    published_13_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Vitamin B3 (2-1-3)", true),
    published_14_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "PVC", true),
    published_14_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "PVA", true),
    published_14_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Naphthalene", true),
    published_15_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Anthraquinone", true),
    published_15_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Soap", true),
    published_15_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Glucose", true),
    published_16_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "PZA", true),
    published_16_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "INH", true),
    published_16_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "PAS", true),
    published_17_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Fusion - Germane", true),
    published_17_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Fusion - Silane", true),
    published_17_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Fermentation", true),
    published_18_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Hydrazine", true),
    published_18_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Organometallics", true),
    published_18_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Nonsense!", false),
    published_19_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Iron and Ozone", true),
    published_19_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Normalization", false),
    published_19_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Decomposition", true),
    published_20_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Phenol", true),
    published_20_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Uracil", true),
    published_20_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Pyridine (2-8-3)", false),
    published_21_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Phosgene", true),
    published_21_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Ignoble", true),
    published_21_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Alchemy", true),
    published_22_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Bosch-Meiser", true),
    published_22_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Beckmann Rearrangement", true),
    published_22_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Stock-Pohland", true),
    published_23_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Swapite", true),
    published_23_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Hardening", true),
    published_23_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Keying In", false),
    published_24_1(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Ethane", true),
    published_24_2(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Cyclobutane", true),
    published_24_3(ScGroup.RESEARCHNET2, ScType.RESEARCH, "Styrene", true),

    published_25_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Thinner Line", true),
    published_25_2(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Decomposition of Ethanolamine", false),
    published_25_3(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Anesthetics", false),
    published_26_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Fertigprodukt", false),
    published_26_2(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Novel Semiconductor", false),
    published_26_3(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Passivation", false),
    published_27_1(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Life: Prep Work", true),
    published_27_2(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Life: Cytosine", true),
    published_27_3(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Life: Thymine", true),
    published_28_1(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Cyanamide", true),
    published_28_2(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Acetic Acid", true),
    published_28_3(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Combustion Engine", false),
    published_29_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Smelting Iron", false),
    published_29_2(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Neodymium Magnet", true),
    published_29_3(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Carbide Swap", true),
    published_30_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Fuel Production", false),
    published_30_2(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Siliconheart Piece", true),
    published_30_3(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Raygun Mechanism", true),
    published_31_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Sulfuric Acid (3-7-1)", true),
    published_31_2(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Fuming Nitric Acid", true),
    published_31_3(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Rocket Fuel", false),
    published_32_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Hydroxides", false),
    published_32_2(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Nobility", false),
    published_32_3(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Sortite", false),
    published_33_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Radiation Treatment", true),
    published_33_2(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Boron Compounds", true),
    published_33_3(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Photovoltaic Cells", true),
    published_34_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Elementary", true),
    published_34_2(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Mixed Acids", false),
    published_34_3(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Squaric Acid", true),
    published_35_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Misproportioned", false),
    published_35_2(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Narkotikum", false),
    published_35_3(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Vereinheitlichung", false),
    published_36_1(ScGroup.RESEARCHNET3, ScType.PRODUCTION, "Haber-Bosch", false),
    published_36_2(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Mustard Oil", true),
    published_36_3(ScGroup.RESEARCHNET3, ScType.RESEARCH, "Nano Electric Motor", true),

    published_37_1(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Count", true),
    published_37_2(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Sharing Is Caring", false),
    published_37_3(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Reassembly", true),
    published_38_1(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Cyanogen", true),
    published_38_2(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Chloromethylsilane", true),
    published_38_3(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Glyceraldehyde", true),
    published_39_1(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Ethandiamin", true),
    published_39_2(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Radikal", false),
    published_39_3(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Kreisalkanol", true),
    published_40_1(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Inorganic Pigments", false),
    published_40_2(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Miller-Urey", false),
    published_40_3(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Getting Pumped", true),
    published_41_1(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Lewisite", true),
    published_41_2(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Novichok", true),
    published_41_3(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Ribulose", true),
    published_42_1(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Knockout Drops", true),
    published_42_2(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Strong Acids", true),
    published_42_3(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Vitamin B3 (4-6-3)", true),
    published_43_1(ScGroup.RESEARCHNET4, ScType.RESEARCH, "1,2,3-Triphenol", true),
    published_43_2(ScGroup.RESEARCHNET4, ScType.RESEARCH, "1,3-Dimetoxibencene", true),
    published_43_3(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Think in Spirals", true),
    published_44_1(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Yellowcake", false),
    published_44_2(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Thiourea", true),
    published_44_3(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Downgrade", true),
    published_45_1(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Wood Alcohol", true),
    published_45_2(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Allyl Alcohol", true),
    published_45_3(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Propargyl Alcohol", true),
    published_46_1(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Condensation", true),
    published_46_2(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Silane", true),
    published_46_3(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Phosphine", true),
    published_47_1(ScGroup.RESEARCHNET4, ScType.PRODUCTION, "Nuclear Medicine", true),
    published_47_2(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Oxygen Supply", true),
    published_47_3(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Red Cross", true),
    published_48_1(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Fluoromethanes", false),
    published_48_2(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Exercise", true),
    published_48_3(ScGroup.RESEARCHNET4, ScType.RESEARCH, "Nanoboxes", true),

    published_49_1(ScGroup.RESEARCHNET5, ScType.RESEARCH, "PAX Challenge 1", true),
    published_49_2(ScGroup.RESEARCHNET5, ScType.RESEARCH, "PAX Challenge 2", true),
    published_49_3(ScGroup.RESEARCHNET5, ScType.RESEARCH, "PAX Challenge 3", true),
    published_50_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Fill in the Blank", true),
    published_50_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Break-up", false),
    published_50_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Average Out", false),
    published_51_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Fake-out", false),
    published_51_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Breakdown (5-3-2)", false),
    published_51_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Master of Disguise", false),
    published_52_1(ScGroup.RESEARCHNET5, ScType.RESEARCH, "Thermoplastic", true),
    published_52_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Contaminated", false),
    published_52_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Running Low", true),
    published_53_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Not a Planet", true),
    published_53_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Back to Basics", true),
    published_53_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Not Helping", true),
    published_54_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Two-fer", true),
    published_54_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Oxygen Synthesis", true),
    published_54_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Stupor", true),
    published_55_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Oxygen Synthesis for Kids", true),
    published_55_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "+1", true),
    published_55_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Nuclear Sorting", false),
    published_56_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Fun with Oxides", false),
    published_56_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Overflow", false),
    published_56_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Conversion", true),
    published_57_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Benzene Machine", true),
    published_57_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Speedster", true),
    published_57_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Carbon Splatter", true),
    published_58_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Eth", false),
    published_58_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Large Scale", false),
    published_58_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Revenge", true),
    published_59_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Excess", false),
    published_59_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Carbon Compounds", false),
    published_59_3(ScGroup.RESEARCHNET5, ScType.RESEARCH, "Magnesium Bicarbonate", true),
    published_60_1(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Impostor", false),
    published_60_2(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Master Plan", false),
    published_60_3(ScGroup.RESEARCHNET5, ScType.PRODUCTION, "Rags to Riches", false),

    published_61_1(ScGroup.RESEARCHNET6, ScType.RESEARCH, "Plutonium Separation", false),
    published_61_2(ScGroup.RESEARCHNET6, ScType.RESEARCH, "You're Doing It Wrong", true),
    published_61_3(ScGroup.RESEARCHNET6, ScType.RESEARCH, "Synthetic Elerium", true),
    published_62_1(ScGroup.RESEARCHNET6, ScType.RESEARCH, "Deadly Soft Drinks", true),
    published_62_2(ScGroup.RESEARCHNET6, ScType.RESEARCH, "Deadly Fluorinated Water", true),
    published_62_3(ScGroup.RESEARCHNET6, ScType.RESEARCH, "Deadly Microwaves", true),
    published_63_1(ScGroup.RESEARCHNET6, ScType.RESEARCH, "Carbon Snakes", true),
    published_63_2(ScGroup.RESEARCHNET6, ScType.RESEARCH, "Catalyst", true),
    published_63_3(ScGroup.RESEARCHNET6, ScType.RESEARCH, "Catalyst II", true),
    published_64_1(ScGroup.RESEARCHNET6, ScType.RESEARCH, "Unbonding with no Bonders", true),
    published_64_2(ScGroup.RESEARCHNET6, ScType.PRODUCTION, "Breeder Reactor", true),
    published_64_3(ScGroup.RESEARCHNET6, ScType.PRODUCTION, "Nightmare Factory", false),

    published_71_1(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Fructose Factory", true),
    published_71_2(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Misfortune Modifier", false),
    published_71_3(ScGroup.RESEARCHNET7, ScType.PRODUCTION_TRIVIAL, "Particle Accelerator", true),
    published_72_1(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Sun Simulator", true),
    published_72_2(ScGroup.RESEARCHNET7, ScType.RESEARCH, "100", true),
    published_72_3(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Hypothetical Synthesis", true),
    published_73_1(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Halogen Sorting", false),
    published_73_2(ScGroup.RESEARCHNET7, ScType.PRODUCTION, "Metallica", false),
    published_73_3(ScGroup.RESEARCHNET7, ScType.PRODUCTION, "Homogenizer", false),
    published_74_1(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Dessication Station", false),
    published_74_2(ScGroup.RESEARCHNET7, ScType.PRODUCTION, "Vinegar Distillation", false),
    published_74_3(ScGroup.RESEARCHNET7, ScType.PRODUCTION, "A Glass of Water", false),
    published_75_1(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Pertetroxide Synthesis EX", true),
    published_75_2(ScGroup.RESEARCHNET7, ScType.PRODUCTION, "Fantastic Metals", true),
    published_75_3(ScGroup.RESEARCHNET7, ScType.PRODUCTION, "Reppe Chemistry", false),
    published_76_1(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Better Than Graphene", true),
    published_76_2(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Magnets", false),
    published_76_3(ScGroup.RESEARCHNET7, ScType.RESEARCH, "Precious Oxygen", true),

    published_77_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Dewer Benzene", true),
    published_77_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Natural Chemo", true),
    published_77_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "The Chem in SpaceChem", true),
    published_78_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Airborne Aldehyde", true),
    published_78_2(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Natural Gas", false),
    published_78_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Symmetry", true),
    published_79_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Fenton Industries 001", false),
    published_79_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Oxygen Candle", false),
    published_79_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Lies", false),
    published_80_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Liquid Glass", true),
    published_80_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "PVC V", true),
    published_80_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Memories", true),
    published_81_1(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Tumbler", true),
    published_81_2(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Molecular Key Carving", true),
    published_81_3(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Sensitive Explosives I", true),
    published_82_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Waste Gas", true),
    published_82_2(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Ethane Scrubbing", false),
    published_82_3(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Melanophlogite Clathrates", false),
    published_83_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Largest Prime", true),
    published_83_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Three", true),
    published_83_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Θne (Thetane)", true),
    published_84_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "A Small Push", true),
    published_84_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Pushed Together", true),
    published_84_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Repair Tungsten", true),
    published_85_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Shared Space I", true),
    published_85_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Shared Space II", true),
    published_85_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Taking Turns", true),
    published_86_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "How Ionic", true),
    published_86_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Two by Two", true),
    published_86_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "The Gold Standard", false),
    published_87_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "2 of Hearts", true),
    published_87_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Miracle Cure", true),
    published_87_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Oxygen Snake", true),
    published_88_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "That's a Relief", true),
    published_88_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Headaches", true),
    published_88_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Under the Counter", true),
    published_89_1(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "A New Beginning", false),
    published_89_2(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Discombobulate", false),
    published_89_3(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Magnum Opus", false),
    published_90_1(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Organic Fusion Chemistry", false),
    published_90_2(ScGroup.RESEARCHNET8, ScType.RESEARCH, "The Soul of Iron", true),
    published_90_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Serbalcide", true),
    published_91_1(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Precious Tears", true),
    published_91_2(ScGroup.RESEARCHNET8, ScType.PRODUCTION, "Desperate Measures", false),
    published_91_3(ScGroup.RESEARCHNET8, ScType.RESEARCH, "Hydrocarbon Stitch", false),

    published_92_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Prime Time", true),
    published_92_2(ScGroup.RESEARCHNET9, ScType.PRODUCTION, "Water not WAtEr", true),
    published_92_3(ScGroup.RESEARCHNET9, ScType.PRODUCTION, "Carbon Products", true),
    published_93_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Martian Respiration", true),
    published_93_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Blockheads", true),
    published_93_3(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Greed", true),
    published_94_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Comet Chemistry", true),
    published_94_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Ferrous Wheel", true),
    published_94_3(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Barbituric Acid", true),
    published_95_1(ScGroup.RESEARCHNET9, ScType.PRODUCTION_TRIVIAL, "Really Easy", true),
    published_95_2(ScGroup.RESEARCHNET9, ScType.PRODUCTION, "Modern Alchemy", true),
    published_95_3(ScGroup.RESEARCHNET9, ScType.PRODUCTION, "Gem Factory", true),
    published_96_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Unlikely", true),
    published_96_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Chalcogens", true),
    published_96_3(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Chlorine and Ammonia", true),
    published_97_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Combustion", true),
    published_97_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Low Power Polymers", true),
    published_97_3(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Photosynthesis", true),
    published_98_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Break Up", true),
    published_98_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Chemotherapy", true),
    published_98_3(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Stuck Together", false),
    published_99_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Antimony Mining", true),
    published_99_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "What a Waste", true),
    published_99_3(ScGroup.RESEARCHNET9, ScType.RESEARCH, "AC without AGW", true),
    published_100_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Strong Dimerization", true),
    published_100_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Philosopher's Cleanup", true),
    published_100_3(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Upscale Downscale", true),
    published_101_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Molting", true),
    published_101_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Unpacking", true),
    published_101_3(ScGroup.RESEARCHNET9, ScType.PRODUCTION, "Unpleasant Dreams", true),
    published_102_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Electroplating", true),
    published_102_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Doping", true),
    published_102_3(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Mass of Osmium", true),
    published_103_1(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Spaghetti Hoops", false),
    published_103_2(ScGroup.RESEARCHNET9, ScType.RESEARCH, "Handle With Care", true),
    published_103_3(ScGroup.RESEARCHNET9, ScType.PRODUCTION, "The Greatest Honor", false);

    private final ScGroup group;
    private final ScType type;
    private final String displayName;
    private final boolean isDeterministic;
    private final List<ScCategory> supportedCategories;
    private final String link;

    ScPuzzle(ScGroup group, ScType type, String displayName, boolean isDeterministic) {
        this.group = group;
        this.type = type;
        this.displayName = displayName;
        this.isDeterministic = isDeterministic;
        this.supportedCategories  = Arrays.stream(ScCategory.values())
                                          .filter(c -> c.getSupportedTypes().contains(type) &&
                                                       !(isDeterministic && (c.getAdmission() == ScMetric.NO_PRECOG ||
                                                                             c.getAdmission() == ScMetric.NO_FLAGS)))
                                          .toList();
        this.link = "https://zlbb.faendir.com/sc/" + name();
    }

    @NotNull
    public static List<ScPuzzle> findMatchingPuzzles(@NotNull String name) {
        return UtilsKt.fuzzyMatch(Arrays.stream(ScPuzzle.values()).toList(), name.trim(), ScPuzzle::getDisplayName);
    }

    @NotNull
    public static ScPuzzle findUniqueMatchingPuzzle(@NotNull String name) {
        List<ScPuzzle> puzzles = findMatchingPuzzles(name);
        return switch (puzzles.size()) {
            case 0 -> throw new IllegalStateException("I did not recognize the puzzle \"" + name + "\".");
            case 1 -> puzzles.get(0);
            default -> throw new IllegalStateException(
                    "your request for \"" + name + "\" was not precise enough. " + puzzles.size() + " matches" + ".");
        };
    }

    @NotNull
    public String getExportName() {
        return displayName.replaceFirst(" \\(.+\\)$", "");
    }
}
