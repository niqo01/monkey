package com.googleapiclient.observable;

import android.content.Context;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.result.DailyTotalResult;
import java.util.Arrays;
import rx.Observable;
import rx.Observer;

public class ReadDailyHistoryObservable extends GoogleApiClientObservable<DailyTotalResult> {

  private final DataType dataType;

  public static Observable<DailyTotalResult> createObservable(Context ctx, DataType dataType) {
    return Observable.create(new ReadDailyHistoryObservable(ctx, dataType));
  }

  private ReadDailyHistoryObservable(Context ctx, DataType dataType) {
    super(ctx, Arrays.asList(Fitness.HISTORY_API), Arrays.asList(Fitness.SCOPE_ACTIVITY_READ));
    this.dataType = dataType;
  }

  @Override protected void onGoogleApiClientReady(GoogleApiClient apiClient,
      Observer<? super DailyTotalResult> observer) {
    Fitness.HistoryApi.readDailyTotal(apiClient, dataType).setResultCallback(dailyTotalResult -> {
      observer.onNext(dailyTotalResult);
      observer.onCompleted();
    });
  }
}