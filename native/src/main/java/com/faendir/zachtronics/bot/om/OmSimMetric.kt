/*
 * Copyright (c) 2023
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
package com.faendir.zachtronics.bot.om

@Suppress("ClassName")
sealed class OmSimMetric(val id: String) {
    // see http://events.critelli.technology/static/metrics.html
    object PARSED_CYCLES : OmSimMetric("parsed cycles")
    object PARSED_COST : OmSimMetric("parsed cost")
    object PARSED_AREA : OmSimMetric("parsed area")
    object PARSED_INSTRUCTIONS : OmSimMetric("parsed instructions")
    object CYCLES : OmSimMetric("cycles")
    object COST : OmSimMetric("cost")
    object AREA : OmSimMetric("area")
    object INSTRUCTIONS : OmSimMetric("instructions")
    class INSTRUCTIONS_WITH_HOTKEY(hotkey: String) : OmSimMetric("instructions with hotkey $hotkey")
    object EXECUTED_INSTRUCTIONS : OmSimMetric("executed instructions")
    object INSTRUCTION_TAPE_PERIOD : OmSimMetric("instruction tape period")
    object HEIGHT : OmSimMetric("height")
    object WIDTH_TIMES_TWO : OmSimMetric("width*2")
    object OMNI_HEIGHT : OmSimMetric("omniheight")
    object OMNI_WIDTH_TIMES_TWO : OmSimMetric("omniwidth*2")
    object THROUGHPUT_CYCLES : OmSimMetric("throughput cycles")
    object THROUGHPUT_OUTPUTS : OmSimMetric("throughput outputs")
    object THROUGHPUT_CYCLES_UNRESTRICTED : OmSimMetric("throughput cycles (unrestricted)")
    object THROUGHPUT_OUTPUTS_UNRESTRICTED : OmSimMetric("throughput outputs (unrestricted)")
    object THROUGHPUT_WASTE : OmSimMetric("throughput waste")
    class PRODUCT_N_METRIC(n: Int, metric: OmSimMetric) : OmSimMetric("product $n ${metric.id}")
    class STEADY_STATE(metric: OmSimMetric) : OmSimMetric("steady state ${metric.id}")
    class PARTS_OF_TYPE(partType: PartType) : OmSimMetric("parts of type ${partType.id}")
    object NUMBER_OF_TRACK_SEGMENTS : OmSimMetric("number of track segments")
    object NUMBER_OF_ARMS : OmSimMetric("number of arms")
    object MAXIMUM_ABSOLUTE_ARM_ROTATION : OmSimMetric("maximum absolute arm rotation")
    object OVERLAP : OmSimMetric("overlap")
    object DUPLICATE_REAGENTS : OmSimMetric("duplicate reagents")
    object DUPLICATE_PRODUCTS : OmSimMetric("duplicate products")
    object MAXIMUM_TRACK_GAP_POW_2 : OmSimMetric("maximum track gap^2")
    object VISUAL_LOOP_START_CYCLE : OmSimMetric("visual loop start cycle")
    object VISUAL_LOOP_END_CYCLE : OmSimMetric("visual loop end cycle")
}

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
    INFINITE("out-rep"),
    TRACK("track"),
    CONDUIT("pipe")
}