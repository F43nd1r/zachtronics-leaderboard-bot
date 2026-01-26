/*
 * Copyright (c) 2026
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
package com.faendir.zachtronics.bot.om.validation

/** see http://events.critelli.technology/static/metrics.html */
@Suppress("ClassName", "Unused")
sealed class OmSimMetric<T: Number>(val id: String) {
    object PARSED_CYCLES : OmSimMetric<Int>("parsed cycles")
    object PARSED_COST : OmSimMetric<Int>("parsed cost")
    object PARSED_AREA : OmSimMetric<Int>("parsed area")
    object PARSED_INSTRUCTIONS : OmSimMetric<Int>("parsed instructions")
    object CYCLES : OmSimMetric<Int>("cycles")
    object COST : OmSimMetric<Int>("cost")
    object AREA : OmSimMetric<Int>("area")
    object INSTRUCTIONS : OmSimMetric<Int>("instructions")
    class INSTRUCTIONS_WITH_HOTKEY(hotkeys: String) : OmSimMetric<Int>("instructions with hotkey $hotkeys")
    object EXECUTED_INSTRUCTIONS : OmSimMetric<Int>("executed instructions")
    object INSTRUCTION_EXECUTION : OmSimMetric<Int>("instruction executions")
    class INSTRUCTION_EXECUTIONS_WITH_HOTKEY(hotkeys: String) : OmSimMetric<Int>("instruction executions with hotkey $hotkeys")
    object INSTRUCTION_TAPE_PERIOD : OmSimMetric<Int>("instruction tape period")
    object HEIGHT : OmSimMetric<Int>("height")
    object WIDTH_TIMES_TWO : OmSimMetric<Int>("width*2")
    object WIDTH : OmSimMetric<Double>("width")
    object OMNI_HEIGHT : OmSimMetric<Int>("omniheight")
    object OMNI_WIDTH_TIMES_TWO : OmSimMetric<Int>("omniwidth*2")
    object HEIGHT_AT_0_DEGREES : OmSimMetric<Int>("height at 0 degrees")
    object HEIGHT_AT_60_DEGREES : OmSimMetric<Int>("height at 60 degrees")
    object HEIGHT_AT_120_DEGREES : OmSimMetric<Int>("height at 120 degrees")
    object WIDTH_TIMES_TWO_AT_0_DEGREES : OmSimMetric<Int>("width*2 at 0 degrees")
    object WIDTH_TIMES_TWO_AT_60_DEGREES : OmSimMetric<Int>("width*2 at 60 degrees")
    object WIDTH_TIMES_TWO_AT_120_DEGREES : OmSimMetric<Int>("width*2 at 120 degrees")
    object MINIMUM_HEXAGON : OmSimMetric<Int>("minimum hexagon")
//    object THROUGHPUT_CYCLES : OmSimMetric<Int("throughput cycles")
    object PER_REPETITION_CYCLES : OmSimMetric<Int>("per repetition cycles")
//    object THROUGHPUT_OUTPUTS : OmSimMetric<Int("throughput outputs")
    object PER_REPETITION_OUTPUTS : OmSimMetric<Int>("per repetition outputs")
    object PER_REPETITION_AREA : OmSimMetric<Int>("per repetition area")
    object PER_REPETITION_SQUARED_AREA : OmSimMetric<Double>("per repetition^2 area")
    object THROUGHPUT_WASTE : OmSimMetric<Int>("throughput waste")
    class PRODUCT_N_METRIC<T : Number>(n: Int, metric: OmSimMetric<T>) : OmSimMetric<T>("product $n ${metric.id}")
    class CYCLE_N_METRIC<T : Number>(n: Int, metric: OmSimMetric<T>) : OmSimMetric<T>("cycle $n ${metric.id}")
    object REACHES_STEADY_STATE : OmSimMetric<Int>("reaches steady state")
    class STEADY_STATE<T : Number>(metric: OmSimMetric<T>) : OmSimMetric<T>("steady state ${metric.id}")
    class PARTS_OF_TYPE(partType: PartType) : OmSimMetric<Int>("parts of type ${partType.id}")
    object ATOMS_GRABBED : OmSimMetric<Int>("atoms grabbed")
    class ATOMS_GRABBED_OF_TYPE(atomType: AtomType) : OmSimMetric<Int>("atoms grabbed of type ${atomType.id}")
    object NUMBER_OF_ATOMS : OmSimMetric<Int>("number of atoms")
    class NUMBER_OF_ATOMS_OF_TYPE(atomType: AtomType) : OmSimMetric<Int>("number of atoms of type ${atomType.id}")
    object NUMBER_OF_TRACK_SEGMENTS : OmSimMetric<Int>("number of track segments")
    object NUMBER_OF_ARMS : OmSimMetric<Int>("number of arms")
    object MAXIMUM_ABSOLUTE_ARM_ROTATION : OmSimMetric<Int>("maximum absolute arm rotation")
    object OVERLAP : OmSimMetric<Int>("overlap")
    object DUPLICATE_REAGENTS : OmSimMetric<Int>("duplicate reagents")
    object DUPLICATE_PRODUCTS : OmSimMetric<Int>("duplicate products")
    object MAXIMUM_TRACK_GAP_POW_2 : OmSimMetric<Int>("maximum track gap^2")
    object VISUAL_LOOP_START_CYCLE : OmSimMetric<Int>("visual loop start cycle")
    object VISUAL_LOOP_END_CYCLE : OmSimMetric<Int>("visual loop end cycle")
}

@Suppress("Unused")
enum class PartType(val id: String) {
    ARM1("arm1"),
    ARM2("arm2"),
    ARM3("arm3"),
    ARM6("arm6"),
    PISTON("piston"),
    VAN_BERLOS_WHEEL("baron"),
    BONDER("bonder"),
    MULTI_BONDER("bonder-speed"),
    TRIPLEX_BONDER("bonder-prisma"),
    UNBONDER("unbonder"),
    CALCIFICATION("glyph-calcification"),
    DUPLICATION("glyph-duplication"),
    PROJECTION("glyph-projection"),
    PURIFICATION("glyph-purification"),
    ANIMISMUS("glyph-life-and-death"),
    DISPOSAL("glyph-disposal"),
    UNIFICATION("glyph-unification"),
    DISPERSION("glyph-dispersion"),
    EQUILIBRIUM("glyph-marker"),
    INPUT("input"),
    OUTPUT("out-std"),
    POLYMER("out-rep"),
    TRACK("track"),
    CONDUIT("pipe")
}

@Suppress("Unused")
enum class AtomType(val id: String) {
    SALT("salt"),
    AIR("air"),
    EARTH("earth"),
    FIRE("fire"),
    WATER("water"),
    QUICKSILVER("quicksilver"),
    GOLD("gold"),
    SILVER("silver"),
    COPPER("copper"),
    IRON("iron"),
    TIN("tin"),
    LEAD("lead"),
    VITAE("vitae"),
    MORS("mors"),
    QUINTESSENCE("quintessence"),
}
