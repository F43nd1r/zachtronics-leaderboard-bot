package com.faendir.zachtronics.bot.sz;

import com.faendir.zachtronics.bot.main.GamePackageMarker;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.ComponentScan;

@SuppressWarnings("unused")
public class ShenzhenIOMarker implements GamePackageMarker {
    @NotNull
    @Override
    public Class<?> getPackageConfiguration() {
        return ShenzhenIOConfiguration.class;
    }

    @ComponentScan
    static class ShenzhenIOConfiguration {
    }
}
