package com.monkeysarmy.fit.ui.fit;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.googleapiclient.ReactiveFitnessProvider;
import com.monkeysarmy.fit.R;
import com.monkeysarmy.fit.data.Injector;
import com.monkeysarmy.fit.data.Units;
import com.monkeysarmy.fit.ui.GoogleClientController;
import com.monkeysarmy.fit.ui.misc.BetterViewAnimator;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class FitView extends LinearLayout implements SwipeRefreshLayout.OnRefreshListener {

  @Bind(R.id.fit_toolbar) Toolbar toolbarView;
  @Bind(R.id.fit_animator) BetterViewAnimator animatorView;
  @Bind(R.id.fit_swipe_refresh) SwipeRefreshLayout swipeRefreshView;
  @Bind(R.id.fit_loading_message) TextView loadingMessageView;
  @Bind(R.id.fit_calories) TextView caloriesView;
  @Bind(R.id.fit_daily_calories) TextView dailyCaloriesView;
  @Bind(R.id.fit_history_calories) TextView historyCaloriesView;

  @Inject GoogleClientController googleClientController;
  @Inject ReactiveFitnessProvider reactiveFitnessProvider;
  @Inject DrawerLayout drawerLayout;

  private final PublishSubject<FitDataSettings> timespanSubject;
  private final Observable<Float> sevenDaysCache;

  private final CompositeSubscription subscriptions = new CompositeSubscription();

  public FitView(Context context, AttributeSet attrs) {
    super(context, attrs);
    if (!isInEditMode()) {
      Injector.obtain(context).inject(this);
    }

    timespanSubject = PublishSubject.create();
    sevenDaysCache = getSevenDaysAgoEnergy().cache();
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    AnimationDrawable ellipsis =
        (AnimationDrawable) getResources().getDrawable(R.drawable.dancing_ellipsis);
    loadingMessageView.setCompoundDrawablesWithIntrinsicBounds(null, null, ellipsis, null);
    ellipsis.start();

    toolbarView.setNavigationIcon(R.drawable.menu_icon);
    toolbarView.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

    swipeRefreshView.setColorSchemeResources(R.color.accent);
    swipeRefreshView.setOnRefreshListener(this);

    animatorView.setDisplayedChildId(R.id.fit_loading);
    caloriesView.setText("Sensor Received");
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    subscriptions.add(timespanSubject //
        .flatMap(fitDataSettings -> sevenDaysCache //
            .zipWith(reactiveFitnessProvider.readDailyHistory(DataType.TYPE_CALORIES_EXPENDED),
                (aFloat, dailyTotalResult) -> {
                  return new EnergyData(fitDataSettings.energy, Math.round(
                      dailyTotalResult.getTotal()
                          .getDataPoints()
                          .get(0)
                          .getValue(Field.FIELD_CALORIES)
                          .asFloat()), Math.round(aFloat));
                })) //
        .subscribeOn(Schedulers.computation()) //
        .observeOn(AndroidSchedulers.mainThread()) //
        .subscribe(fitData, fitError));

    subscriptions.add(googleClientController.onResolutionSucceed().subscribe(aBoolean -> {
      load();
    }));
    load();
  }

  void load() {
    if (animatorView.getDisplayedChildId() != R.id.fit_swipe_refresh) {
      animatorView.setDisplayedChildId(R.id.fit_loading);
    }

    // For whatever reason, the SRL's spinner does not draw itself when we call setRefreshing(true)
    // unless it is posted.
    post(() -> {
      swipeRefreshView.setRefreshing(true);
      timespanSubject.onNext(new FitDataSettings(Units.Energy.CALORIES));
    });
  }

  Observable<Float> getSevenDaysAgoEnergy() {
    return reactiveFitnessProvider.readHistory(buildDailyHistoryRequest(7)) //
        .map(dataReadResult -> dataReadResult.getBuckets()
            .get(0)
            .getDataSets()
            .get(0)
            .getDataPoints()
            .get(0)
            .getValue(Field.FIELD_CALORIES)
            .asFloat());
  }

  //private void connect(){
  //  subscriptions.add(reactiveFitnessProvider.getSensorUpdate(
  //      new SensorRequest.Builder().setDataType(DataType.TYPE_CALORIES_EXPENDED)
  //          .setSamplingRate(1, TimeUnit.SECONDS)
  //          .build())
  //      .subscribe(dataPoint -> {
  //        String calories = dataPoint.getValue(Field.FIELD_CALORIES).asString();
  //        caloriesView.setText("Sensor Received: " + calories);
  //        Timber.e("TEST %s", calories);
  //      }, throwable -> {
  //        Timber.e(throwable, "Error BLA");
  //        if (throwable instanceof GoogleAPIConnectionException) {
  //          GoogleAPIConnectionException ex = (GoogleAPIConnectionException) throwable;
  //          ConnectionResult result2 = ex.getConnectionResult();
  //          googleClientController.onConnectionFailed(result2);
  //        }
  //      }));

  private DataReadRequest buildDailyHistoryRequest(int daysAgo) {
    // Setting a start and end date using a range of 1 week before this moment.

    ZonedDateTime zdt = LocalDateTime.now().atZone(ZoneId.systemDefault()) //
        .minus(daysAgo, ChronoUnit.DAYS) //
        .truncatedTo(ChronoUnit.DAYS);
    long startTime = zdt.toInstant().toEpochMilli();
    long endTime = zdt.plus(1, ChronoUnit.DAYS).toInstant().toEpochMilli();

    return new DataReadRequest.Builder()
        // The data request can specify multiple data types to return, effectively
        // combining multiple data queries into one call.
        // In this example, it's very unlikely that the request is for several hundred
        // datapoints each consisting of a few steps and a timestamp.  The more likely
        // scenario is wanting to see how many steps were walked per day, for 7 days.
        .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            // Analogous to a "Group By" in SQL, defines how data should be aggregated.
            // bucketByTime allows for a time span, whereas bucketBySession would allow
            // bucketing by "sessions", which would need to be defined in code.
        .bucketByTime(1, TimeUnit.DAYS)
        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        .build();
  }

  private final Action1<EnergyData> fitData = data -> {
    dailyCaloriesView.setText("Today: " + data.todayEnergyValue);
    historyCaloriesView.setText("7 days ago: " + data.sevenDayAgoEnergyValue);
    animatorView.setDisplayedChildId(R.id.fit_swipe_refresh);
    swipeRefreshView.setRefreshing(false);
  };

  private final Action1<Throwable> fitError = throwable -> {
    Timber.e(throwable, "Failed to get fitness data");
    swipeRefreshView.setRefreshing(false);
    animatorView.setDisplayedChildId(R.id.fit_error);
  };

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    subscriptions.unsubscribe();
  }

  @Override public void onRefresh() {
    load();
  }

  private static class FitDataSettings {
    final Units.Energy energy;

    private FitDataSettings(Units.Energy energy) {
      this.energy = energy;
    }
  }

  private static class EnergyData {
    final Units.Energy energyUnit;
    final int todayEnergyValue;
    final int sevenDayAgoEnergyValue;

    private EnergyData(Units.Energy energyUnit, int todayEnergyValue, int sevenDayAgoEnergyValue) {
      this.energyUnit = energyUnit;
      this.todayEnergyValue = todayEnergyValue;
      this.sevenDayAgoEnergyValue = sevenDayAgoEnergyValue;
    }
  }
}
