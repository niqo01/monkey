package com.monkeysarmy.fit.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import javax.inject.Singleton;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

@Singleton public class GoogleClientController {

  private static final int REQUEST_OAUTH = GoogleClientController.class.hashCode();
  private static final String AUTH_PENDING = "auth_state_pending";

  private boolean authInProgress = false;
  private Activity activity;
  private PublishSubject<Boolean> googleResolutionSucceed;

  public GoogleClientController() {
    googleResolutionSucceed = PublishSubject.create();
  }

  public Observable<Boolean> onResolutionSucceed() {
    return googleResolutionSucceed.asObservable();
  }

  public void onCreate(Activity activity, Bundle savedInstanceState) {
    this.activity = activity;
    if (savedInstanceState != null) {
      authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
    }
  }

  public void onDestroy() {
    activity = null;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_OAUTH) {
      authInProgress = false;
      if (resultCode == Activity.RESULT_OK) {
        googleResolutionSucceed.onNext(Boolean.TRUE);
      } else {
        googleResolutionSucceed.onNext(Boolean.FALSE);
      }
    }
  }

  public void onSaveInstanceState(Bundle outState) {
    outState.putBoolean(AUTH_PENDING, authInProgress);
  }

  public void onConnectionFailed(ConnectionResult result) {
    Timber.i("Connection failed. Cause: %s", result.toString());
    if (!result.hasResolution()) {
      // Show the localized error dialog
      GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), activity, 0).show();
      return;
    }
    if (!authInProgress) {
      try {
        Timber.i("Attempting to resolve failed connection");
        authInProgress = true;
        result.startResolutionForResult(activity, REQUEST_OAUTH);
      } catch (IntentSender.SendIntentException e) {
        Timber.e(e, "Exception while starting resolution activity");
      }
    }
  }
}
