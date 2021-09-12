/*
 * Copyright (c) 2021
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

import com.faendir.zachtronics.bot.model.Group;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScGroup implements Group {
    MAIN("Main Game", "index"),
    TF2("TF2", "index"),
    CORVI63("63 Corvi", "index"),
    RESEARCHNET1("ResearchNet Vol. 1", "researchnet"),
    RESEARCHNET2("ResearchNet Vol. 2", "researchnet"),
    RESEARCHNET3("ResearchNet Vol. 3", "researchnet"),
    RESEARCHNET4("ResearchNet Vol. 4", "researchnet"),
    RESEARCHNET5("ResearchNet Vol. 5", "researchnet2"),
    RESEARCHNET6("ResearchNet Vol. 6", "researchnet2"),
    RESEARCHNET7("ResearchNet Vol. 7", "researchnet2"),
    RESEARCHNET8("ResearchNet Vol. 8", "researchnet2"),
    RESEARCHNET9("ResearchNet Vol. 9", "researchnet2");

    private final String displayName;
    private final String wikiPage;
}
