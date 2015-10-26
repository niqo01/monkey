package com.monkeysarmy.fit;

import com.monkeysarmy.fit.ui.InternalReleaseUiModule;
import dagger.Module;

@Module(
    addsTo = MonkeyModule.class,
    includes = InternalReleaseUiModule.class,
    overrides = true
)
public final class InternalReleaseMonkeyModule {
}
