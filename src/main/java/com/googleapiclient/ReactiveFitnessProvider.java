package com.googleapiclient;

import android.content.Context;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.googleapiclient.observable.ReadHistoryObservable;
import com.googleapiclient.observable.SensorObservable;
import rx.Observable;

public final class ReactiveFitnessProvider {
  private final Context ctx;

  public ReactiveFitnessProvider(Context ctx) {
    this.ctx = ctx;
  }

  public Observable<DataPoint> getSensorUpdate(SensorRequest sensorRequest) {
    return SensorObservable.createObservable(ctx, sensorRequest);
  }

  public Observable<DataReadResult> readHistory(DataReadRequest dataReadRequest) {
    return ReadHistoryObservable.createObservable(ctx, dataReadRequest);
  }
}
