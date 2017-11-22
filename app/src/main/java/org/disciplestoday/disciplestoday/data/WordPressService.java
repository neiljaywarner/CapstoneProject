package org.disciplestoday.disciplestoday.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by neil on 12/1/16.
 */
public interface WordPressService {

        // https://docs.google.com/document/d/1OX1a5Jxj2PqZx4Cs70Fm1E1hnRZeNiy3k1Y0YvqGxfA/edit
        public static String JEANIE_SHAW_BLOG_URL = "https://douglasjacoby.com/";

        @GET("/category/q-and-a/feed/")
        Call<ArticleResponse> getFeed(@Query("paged") String pageNumber);
        //e.g yields /feed?paged=2 if called with getFeed("2");

        @GET("/tag/{tag}/feed/")
        Call<ArticleResponse> getTagFeed(@Path("tag") String tag);

        @GET("/category/{category}/feed/")
        Call<ArticleResponse> getCategoryFeed(@Path("category") String tag);

        @GET("/feed/")
        Call<ArticleResponse> getSearcnFeed(@Query("s") String searchTerm);

        // TODO: Implement categories in nav drawer

        // get

        //e.g. https://jeaniesjourneys.com/tag/prayer/feed/

                /*
        https://www.douglasjacoby.com/category/q-and-a/feed/?paged=2

        https://www.douglasjacoby.com/feed?s=love

        https://www.douglasjacoby.com/category/leadership/feed/

        https://www.douglasjacoby.com/category/womens-corner/feed/


        https://www.douglasjacoby.com/category/articles/apologetics/feed/
        */



}
