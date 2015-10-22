package com.monkeysarmy.fit.ui;

import android.support.v4.widget.DrawerLayout;

import com.monkeysarmy.fit.U2020Module;
import com.monkeysarmy.fit.ui.trending.TrendingView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    addsTo = U2020Module.class,
    injects = TrendingView.class
)
public final class MainActivityModule {
  private final MainActivity mainActivity;

  MainActivityModule(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
  }

  @Provides @Singleton DrawerLayout provideDrawerLayout() {
    return mainActivity.drawerLayout;
  }
}
