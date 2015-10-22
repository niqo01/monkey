package com.googleapiclient.observable;

import android.content.Context;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import java.util.Arrays;
import rx.Observable;
import rx.Observer;

public class SensorObservable extends GoogleApiClientObservable<DataPoint> {

  private final SensorRequest sensorRequest;
  private OnDataPointListener listener;

  public static Observable<DataPoint> createObservable(Context ctx, SensorRequest sensorRequest) {
    return Observable.create(new SensorObservable(ctx, sensorRequest));
  }

  private SensorObservable(Context ctx, SensorRequest sensorRequest) {
    super(ctx, Arrays.asList(Fitness.SENSORS_API), Arrays.asList(Fitness.SCOPE_ACTIVITY_READ));
    this.sensorRequest = sensorRequest;
  }

  @Override
  protected void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super DataPoint> observer) {
    listener = new OnDataPointListener() {
      @Override public void onDataPoint(DataPoint dataPoint) {
        observer.onNext(dataPoint);
      }
    };
    PendingResult<Status> pendingResult = Fitness.SensorsApi.add(
        apiClient,
        sensorRequest,
        listener);
    Status status = pendingResult.await();
    if (!status.isSuccess()){
      observer.onError(new StatusException(status));
    }
  }

  @Override
  protected void onUnsubscribed(GoogleApiClient apiClient) {
    if (apiClient.isConnected()) {
      Fitness.SensorsApi.remove(apiClient, listener);
    }
  }
}