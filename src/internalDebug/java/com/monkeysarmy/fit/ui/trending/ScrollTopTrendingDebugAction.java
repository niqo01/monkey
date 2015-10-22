package com.monkeysarmy.fit.ui.trending;

import com.monkeysarmy.fit.ui.debug.ContextualDebugActions.DebugAction;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public final class ScrollTopTrendingDebugAction extends DebugAction<TrendingView> {

  @Inject public ScrollTopTrendingDebugAction() {
    super(TrendingView.class);
  }

  @Override public String name() {
    return "Scroll to top";
  }

  @Override public void run(TrendingView view) {
    view.trendingView.smoothScrollToPosition(0);
  }
}
