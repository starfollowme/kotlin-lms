package com.mext.utbk.tutor.data.remote.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<ApiChatMessage>
)

data class ApiChatMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class ChatResponse(
    @SerializedName("choices") val choices: List<Choice>
)

data class Choice(
    @SerializedName("message") val message: ApiChatMessage
)
