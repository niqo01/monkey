package com.monkeysarmy.fit.data.api;

import com.monkeysarmy.fit.data.api.oauth.OauthInterceptor;
import com.monkeysarmy.fit.data.api.oauth.OauthService;
import com.squareup.moshi.Moshi;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import javax.inject.Singleton;
import retrofit.MoshiConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

@Module(
    complete = false,
    library = true,
    injects = {
        OauthService.class
    }
)
public final class ApiModule {
  public static final HttpUrl PRODUCTION_API_URL = HttpUrl.parse("https://api.github.com");

  @Provides @Singleton HttpUrl provideBaseUrl() {
    return PRODUCTION_API_URL;
  }

  @Provides @Singleton @Named("Api") OkHttpClient provideApiClient(OkHttpClient client,
      OauthInterceptor oauthInterceptor) {
    return createApiClient(client, oauthInterceptor);
  }

  @Provides @Singleton
  Retrofit provideRetrofit(HttpUrl baseUrl, @Named("Api") OkHttpClient client, Moshi moshi) {
    return new Retrofit.Builder() //
        .client(client) //
        .baseUrl(baseUrl) //
        .addConverterFactory(MoshiConverterFactory.create(moshi)) //
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) //
        .build();
  }

  @Provides @Singleton GithubService provideGithubService(Retrofit retrofit) {
    return retrofit.create(GithubService.class);
  }

  static OkHttpClient createApiClient(OkHttpClient client, OauthInterceptor oauthInterceptor) {
    client = client.clone();
    client.interceptors().add(oauthInterceptor);
    return client;
  }
}
