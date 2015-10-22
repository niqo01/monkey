package com.monkeysarmy.fit.data.api;

import retrofit.Result;
import rx.functions.Func1;

public final class Results {
  private static final Func1<Result<?>, Boolean> SUCCESS =
          result -> !result.isError() && result.response().isSuccess();

  public static Func1<Result<?>, Boolean> isSuccess() {
    return SUCCESS;
  }

  private Results() {
    throw new AssertionError("No instances.");
  }
}
