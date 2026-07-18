package com.mext.utbk.tutor.data.remote

import com.mext.utbk.tutor.data.remote.model.ChatRequest
import com.mext.utbk.tutor.data.remote.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterService {
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: ChatRequest
    ): ChatResponse
}
