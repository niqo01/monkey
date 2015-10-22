package com.googleapiclient.observable;

public class GoogleAPIConnectionSuspendedException extends RuntimeException {
  private final int cause;

  GoogleAPIConnectionSuspendedException(int cause) {
    this.cause = cause;
  }

  public int getErrorCause() {
    return cause;
  }
}