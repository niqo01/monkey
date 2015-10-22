package com.monkeysarmy.fit;

import android.app.Application;
import com.monkeysarmy.fit.data.DataModule;
import com.monkeysarmy.fit.ui.UiModule;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(
    includes = {
        UiModule.class,
        DataModule.class
    },
    injects = {
        MonkeyApp.class
    }
)
public final class U2020Module {
  private final MonkeyApp app;

  public U2020Module(MonkeyApp app) {
    this.app = app;
  }

  @Provides @Singleton Application provideApplication() {
    return app;
  }
}
