package com.jakewharton.u2020.ui.debug;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.jakewharton.madge.MadgeFrameLayout;
import com.jakewharton.scalpel.ScalpelFrameLayout;
import com.jakewharton.u2020.R;
import com.jakewharton.u2020.data.LumberYard;
import com.jakewharton.u2020.data.PixelGridEnabled;
import com.jakewharton.u2020.data.PixelRatioEnabled;
import com.jakewharton.u2020.data.ScalpelEnabled;
import com.jakewharton.u2020.data.ScalpelWireframeEnabled;
import com.jakewharton.u2020.data.SeenDebugDrawer;
import com.jakewharton.u2020.data.prefs.BooleanPreference;
import com.jakewharton.u2020.ui.AppContainer;
import com.jakewharton.u2020.ui.bugreport.BugReportLens;
import com.jakewharton.u2020.util.EmptyActivityLifecycleCallbacks;
import com.mattprecious.telescope.TelescopeLayout;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;
import rx.android.lifecycle.LifecycleObservable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static android.content.Context.POWER_SERVICE;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.ON_AFTER_RELEASE;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

/**
 * An {@link AppContainer} for debug builds which wrap the content view with a sliding drawer on
 * the right that holds all of the debug information and settings.
 */
@Singleton
public final class DebugAppContainer implements AppContainer {
  private final LumberYard lumberYard;
  private final BooleanPreference seenDebugDrawer;
  private final Observable<Boolean> pixelGridEnabled;
  private final Observable<Boolean> pixelRatioEnabled;
  private final Observable<Boolean> scalpelEnabled;
  private final Observable<Boolean> scalpelWireframeEnabled;

  private final CompositeSubscription subscriptions = new CompositeSubscription();

  private Activity activity;

  @Inject public DebugAppContainer(LumberYard lumberYard,
      @SeenDebugDrawer BooleanPreference seenDebugDrawer,
      @PixelGridEnabled Observable<Boolean> pixelGridEnabled,
      @PixelRatioEnabled Observable<Boolean> pixelRatioEnabled,
      @ScalpelEnabled Observable<Boolean> scalpelEnabled,
      @ScalpelWireframeEnabled Observable<Boolean> scalpelWireframeEnabled) {
    this.lumberYard = lumberYard;
    this.seenDebugDrawer = seenDebugDrawer;
    this.pixelGridEnabled = pixelGridEnabled;
    this.pixelRatioEnabled = pixelRatioEnabled;
    this.scalpelEnabled = scalpelEnabled;
    this.scalpelWireframeEnabled = scalpelWireframeEnabled;
  }

  @InjectView(R.id.debug_drawer_layout) DebugDrawerLayout drawerLayout;
  @InjectView(R.id.debug_drawer) ViewGroup debugDrawer;
  @InjectView(R.id.telescope_container) TelescopeLayout telescopeLayout;
  @InjectView(R.id.madge_container) MadgeFrameLayout madgeFrameLayout;
  @InjectView(R.id.debug_content) ScalpelFrameLayout content;

  @Override public ViewGroup get(final Activity activity) {
    this.activity = activity;

    activity.setContentView(R.layout.debug_activity_frame);
    ButterKnife.inject(this, activity);

    final Context drawerContext = new ContextThemeWrapper(activity, R.style.Theme_U2020_Debug);
    final DebugView debugView = new DebugView(drawerContext);
    debugDrawer.addView(debugView);

    // Set up the contextual actions to watch views coming in and out of the content area.
    ContextualDebugActions contextualActions = debugView.getContextualDebugActions();
    contextualActions.setActionClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        drawerLayout.closeDrawers();
      }
    });
    content.setOnHierarchyChangeListener(HierarchyTreeChangeListener.wrap(contextualActions));

    drawerLayout.setDrawerShadow(R.drawable.debug_drawer_shadow, Gravity.END);
    drawerLayout.setDrawerListener(new DebugDrawerLayout.SimpleDrawerListener() {
      @Override public void onDrawerOpened(View drawerView) {
        debugView.onDrawerOpened();
      }
    });

    TelescopeLayout.cleanUp(activity); // Clean up any old screenshots.
    telescopeLayout.setLens(new BugReportLens(activity, lumberYard));

    // If you have not seen the debug drawer before, show it with a message
    if (!seenDebugDrawer.get()) {
      drawerLayout.postDelayed(new Runnable() {
        @Override public void run() {
          drawerLayout.openDrawer(Gravity.END);
          Toast.makeText(drawerContext, R.string.debug_drawer_welcome, Toast.LENGTH_LONG).show();
        }
      }, 1000);
      seenDebugDrawer.set(true);
    }

    setupMadge();
    setupScalpel();

    final Application app = activity.getApplication();
    app.registerActivityLifecycleCallbacks(new EmptyActivityLifecycleCallbacks() {
      @Override public void onActivityDestroyed(Activity activity) {
        if (activity == DebugAppContainer.this.activity) {
          subscriptions.unsubscribe();
          app.unregisterActivityLifecycleCallbacks(this);
        }
      }
    });

    riseAndShine(activity);
    return content;
  }

  private void setupMadge() {
    subscriptions.add(pixelGridEnabled.subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean enabled) {
        madgeFrameLayout.setOverlayEnabled(enabled);
      }
    }));
    subscriptions.add(pixelRatioEnabled.subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean enabled) {
        madgeFrameLayout.setOverlayRatioEnabled(enabled);
      }
    }));
  }

  private void setupScalpel() {
    subscriptions.add(scalpelEnabled.subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean enabled) {
        content.setLayerInteractionEnabled(enabled);
      }
    }));
    subscriptions.add(scalpelWireframeEnabled.subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean enabled) {
        content.setDrawViews(!enabled);
      }
    }));
  }

  /**
   * Show the activity over the lock-screen and wake up the device. If you launched the app manually
   * both of these conditions are already true. If you deployed from the IDE, however, this will
   * save you from hundreds of power button presses and pattern swiping per day!
   */
  public static void riseAndShine(Activity activity) {
    activity.getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED);

    PowerManager power = (PowerManager) activity.getSystemService(POWER_SERVICE);
    PowerManager.WakeLock lock =
        power.newWakeLock(FULL_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP | ON_AFTER_RELEASE, "wakeup!");
    lock.acquire();
    lock.release();
  }
}