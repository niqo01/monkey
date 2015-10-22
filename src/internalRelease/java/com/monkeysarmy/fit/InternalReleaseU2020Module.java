package com.monkeysarmy.fit;

import com.monkeysarmy.fit.U2020Module;
import com.monkeysarmy.fit.ui.InternalReleaseUiModule;
import dagger.Module;

@Module(
    addsTo = U2020Module.class,
    includes = InternalReleaseUiModule.class,
    overrides = true
)
public final class InternalReleaseU2020Module {
}
