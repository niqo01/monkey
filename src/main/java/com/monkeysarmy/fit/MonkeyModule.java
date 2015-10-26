package com.monkeysarmy.fit;

import android.app.Application;
import com.googleapiclient.ReactiveFitnessProvider;
import com.monkeysarmy.fit.data.DataModule;
import com.monkeysarmy.fit.ui.UiModule;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(
    includes = {
        UiModule.class, DataModule.class
    },
    injects = {
        MonkeyApp.class
    },
    library = true) public final class MonkeyModule {
  private final MonkeyApp app;

  public MonkeyModule(MonkeyApp app) {
    this.app = app;
  }

  @Provides @Singleton Application provideApplication() {
    return app;
  }

  @Provides @Singleton ReactiveFitnessProvider provideReactiveFitnessProvider() {
    return new ReactiveFitnessProvider(app);
  }
}
