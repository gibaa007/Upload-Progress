package com.example.uploadprogress

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("StartVideoUpload")
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("ChildDetailsID") ChildDetailsID: Int,
        @Part("ContentType") ContentType: String,
        @Part("CreatedBy") CreatedBy: Int,
        @Part("FileType") FileType: String,
        @Part("Filename") Filename: String,
        @Part("FOID") FOID: Int,
        @Part("CommunityId") CommunityId: Int,
        @Part("SaveMode") SaveMode: String,
        @Part("Source") Source: String,
        @Part("ChildId") ChildId: String,
    ): Call<ResponseBody>
}
