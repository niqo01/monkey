package com.monkeysarmy.fit.ui;

import com.monkeysarmy.fit.IsInstrumentationTest;
import com.monkeysarmy.fit.ui.debug.DebugAppContainer;
import com.monkeysarmy.fit.ui.debug.DebugView;
import com.monkeysarmy.fit.ui.debug.SocketActivityHierarchyServer;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(
    injects = {
        DebugAppContainer.class,
        DebugView.class,
    },
    complete = false,
    library = true,
    overrides = true
)
public class DebugUiModule {
  @Provides @Singleton AppContainer provideAppContainer(DebugAppContainer debugAppContainer,
      @IsInstrumentationTest boolean isInstrumentationTest) {
    // Do not add the debug controls for when we are running inside of an instrumentation test.
    return isInstrumentationTest ? AppContainer.DEFAULT : debugAppContainer;
  }

  @Provides @Singleton ActivityHierarchyServer provideActivityHierarchyServer() {
    return new SocketActivityHierarchyServer();
  }
}
