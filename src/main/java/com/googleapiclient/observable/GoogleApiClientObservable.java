package com.googleapiclient.observable;

import android.content.Context;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import java.util.List;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

abstract class GoogleApiClientObservable<T> implements Observable.OnSubscribe<T> {
  private final Context ctx;
  private final List<Api<? extends Api.ApiOptions.NotRequiredOptions>> services;
  private final List<Scope> scopes;

  protected GoogleApiClientObservable(Context ctx,
      List<Api<? extends Api.ApiOptions.NotRequiredOptions>> services, List<Scope> scopes) {
    this.ctx = ctx;
    this.services = services;
    this.scopes = scopes;
  }

  @Override public void call(Subscriber<? super T> subscriber) {

    final GoogleApiClient apiClient = createApiClient(subscriber);
    try {
      apiClient.connect();
    } catch (Throwable ex) {
      subscriber.onError(ex);
    }

    subscriber.add(Subscriptions.create(() -> {
      if (apiClient.isConnected() || apiClient.isConnecting()) {
        onUnsubscribed(apiClient);
        apiClient.disconnect();
      }
    }));
  }

  protected GoogleApiClient createApiClient(Subscriber<? super T> subscriber) {

    ApiClientConnectionCallbacks apiClientConnectionCallbacks =
        new ApiClientConnectionCallbacks(subscriber);

    GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(ctx);

    for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
      apiClientBuilder.addApi(service);
    }
    for (Scope scope : scopes) {
      apiClientBuilder.addScope(scope);
    }
    apiClientBuilder.addConnectionCallbacks(apiClientConnectionCallbacks);
    apiClientBuilder.addOnConnectionFailedListener(apiClientConnectionCallbacks);

    GoogleApiClient apiClient = apiClientBuilder.build();

    apiClientConnectionCallbacks.setClient(apiClient);

    return apiClient;
  }

  protected void onUnsubscribed(GoogleApiClient locationClient) {
  }

  protected abstract void onGoogleApiClientReady(GoogleApiClient apiClient,
      Observer<? super T> observer);

  private class ApiClientConnectionCallbacks
      implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final private Observer<? super T> observer;

    private GoogleApiClient apiClient;

    private ApiClientConnectionCallbacks(Observer<? super T> observer) {
      this.observer = observer;
    }

    @Override public void onConnected(Bundle bundle) {
      try {
        onGoogleApiClientReady(apiClient, observer);
      } catch (Throwable ex) {
        observer.onError(ex);
      }
    }

    @Override public void onConnectionSuspended(int cause) {
      observer.onError(new GoogleAPIConnectionSuspendedException(cause));
    }

    @Override public void onConnectionFailed(ConnectionResult connectionResult) {
      observer.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.",
          connectionResult));
    }

    public void setClient(GoogleApiClient client) {
      this.apiClient = client;
    }
  }
}