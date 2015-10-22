package com.monkeysarmy.fit.data.api;

import com.monkeysarmy.fit.data.api.model.RepositoriesResponse;
import retrofit.Result;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface GithubService {
  @GET("/search/repositories") //
  Observable<Result<RepositoriesResponse>> repositories( //
      @Query("q") SearchQuery query, //
      @Query("sort") Sort sort, //
      @Query("order") Order order);
}
