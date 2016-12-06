package org.disciplestoday.disciplestoday.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by neil on 12/1/16.
 */
public interface WordPressService {


        public static String JEANIE_SHAW_BLOG_URL = "https://jeaniesjourneys.com/";

        @GET("/feed/")
        Call<ArticleResponse> getFeed(@Query("paged") String pageNumber);
        //e.g yields /feed?paged=2 if called with getFeed("2");
        // e.g https://jeaniesjourneys.com/feed/?paged=2

}
