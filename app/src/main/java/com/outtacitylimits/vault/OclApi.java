package com.outtacitylimits.vault;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface OclApi {
    // This will connect to https://outta-city-limits.vercel.app/api/products
    @GET("api/products")
    Call<List<Product>> getProducts();
}
