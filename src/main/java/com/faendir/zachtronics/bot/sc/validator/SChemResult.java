package com.faendir.zachtronics.bot.sc.validator;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 <tt>{
 "level_name": "Fuming Nitric Acid",
 "resnet_id": [3, 7, 2], # null for main game
 "cycles": 115,
 "reactors": 1,
 "symbols": 6,
 "author": "12345ieee",
 "solution_name": "s",
 "precog": false
 }</tt>
 */
@Value
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SChemResult {
    @NotNull String levelName;
    @Nullable int[] resnetId;
    int cycles;
    int reactors;
    int symbols;
    @NotNull String author;
    @Nullable String solutionName;
    boolean precog;
}
