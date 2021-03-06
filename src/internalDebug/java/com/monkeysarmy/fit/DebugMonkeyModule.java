package com.monkeysarmy.fit;

import com.monkeysarmy.fit.data.DebugDataModule;
import com.monkeysarmy.fit.ui.DebugUiModule;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(
    addsTo = MonkeyModule.class,
    includes = {
        DebugUiModule.class,
        DebugDataModule.class,
        DebugActionsModule.class
    },
    overrides = true
)
public final class DebugMonkeyModule {
  // Low-tech flag to force certain debug build behaviors when running in an instrumentation test.
  // This value is used in the creation of singletons so it must be set before the graph is created.
  static boolean instrumentationTest = false;

  @Provides @Singleton @IsInstrumentationTest boolean provideIsInstrumentationTest() {
    return instrumentationTest;
  }
}
