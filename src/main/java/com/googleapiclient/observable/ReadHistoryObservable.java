package com.googleapiclient.observable;

import android.content.Context;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import java.util.Arrays;
import rx.Observable;
import rx.Observer;

public class ReadHistoryObservable extends GoogleApiClientObservable<DataReadResult> {

  private final DataReadRequest dataReadRequest;

  public static Observable<DataReadResult> createObservable(Context ctx,
      DataReadRequest dataReadRequest) {
    return Observable.create(new ReadHistoryObservable(ctx, dataReadRequest));
  }

  private ReadHistoryObservable(Context ctx, DataReadRequest dataReadRequest) {
    super(ctx, Arrays.asList(Fitness.HISTORY_API), Arrays.asList(Fitness.SCOPE_ACTIVITY_READ));
    this.dataReadRequest = dataReadRequest;
  }

  @Override protected void onGoogleApiClientReady(GoogleApiClient apiClient,
      Observer<? super DataReadResult> observer) {
    Fitness.HistoryApi.readData(apiClient, dataReadRequest).setResultCallback(dataReadResult -> {
      observer.onNext(dataReadResult);
      observer.onCompleted();
    });
  }
}