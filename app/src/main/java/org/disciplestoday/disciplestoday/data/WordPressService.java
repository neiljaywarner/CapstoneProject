package org.disciplestoday.disciplestoday.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by neil on 12/1/16.
 */
public interface WordPressService {

                        //https://beboldforspiritualchange.wordpress.com/blog/feed/
        public static String BLOG_URL = "https://beboldforspiritualchange.wordpress.com/blog/";

        @GET("/feed/")
        Call<ArticleResponse> getFeed(@Query("paged") String pageNumber);
        //e.g yields /feed?paged=2 if called with getFeed("2");
        // e.g https://jeaniesjourneys.com/feed/?paged=2

        @GET("/tag/{tag}/feed/")
        Call<ArticleResponse> getTagFeed(@Path("tag") String tag);

        //e.g. https://jeaniesjourneys.com/tag/prayer/feed/

}
