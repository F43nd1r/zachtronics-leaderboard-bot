package com.faendir.zachtronics.bot.sc;

import com.faendir.zachtronics.bot.main.GamePackageMarker;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("unused")
public class SpaceChemMarker implements GamePackageMarker {
    @NotNull
    @Override
    public Class<?> getPackageConfiguration() {
        return SpaceChemConfiguration.class;
    }

    @Configuration
    @ComponentScan
    public static class SpaceChemConfiguration {
    }
}
