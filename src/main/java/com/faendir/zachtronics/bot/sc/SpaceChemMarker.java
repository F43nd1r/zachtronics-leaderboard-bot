package com.faendir.zachtronics.bot.sc;

import com.faendir.zachtronics.bot.main.GamePackageMarker;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.ComponentScan;

@SuppressWarnings("unused")
public class SpaceChemMarker implements GamePackageMarker {
    @NotNull
    @Override
    public Class<?> getPackageConfiguration() {
        return SpaceChemConfiguration.class;
    }

    @ComponentScan
    public static class SpaceChemConfiguration {
    }
}
