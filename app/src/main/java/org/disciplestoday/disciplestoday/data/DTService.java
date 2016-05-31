package org.disciplestoday.disciplestoday.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by neil on 5/25/16.
 */
public interface DTService {
    @GET("component/k2/itemlist?format=json&moduleID=353")
    Call<Feed> listHighlights();

    @GET("component/k2/itemlist?format=json&moduleID=273")
    Call<Feed> listSingles();

    @GET("component/k2/itemlist?format=json")
    Call<Feed> listFeed(@Query("moduleID") String moduleId);


    // http://www.disciplestoday.org/component/k2/itemlist?format=json&moduleID=353
}
