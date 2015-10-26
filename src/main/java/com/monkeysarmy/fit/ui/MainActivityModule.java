package com.monkeysarmy.fit.ui;

import android.support.v4.widget.DrawerLayout;
import com.monkeysarmy.fit.MonkeyModule;
import com.monkeysarmy.fit.ui.fit.FitView;
import com.monkeysarmy.fit.ui.trending.TrendingView;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module(
    addsTo = MonkeyModule.class,
    injects = { TrendingView.class, FitView.class }) public final class MainActivityModule {
  private final MainActivity mainActivity;

  MainActivityModule(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
  }

  @Provides @Singleton DrawerLayout provideDrawerLayout() {
    return mainActivity.drawerLayout;
  }
}
