package com.carpooling.app.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class ModerationRequest(val text: String)
data class ModerationResponse(val allowed: Boolean, val reason: String?, val modelRaw: String?)

interface ModerationApi {
    @POST("moderation-service/api/moderation/check")
    suspend fun check(@Body req: ModerationRequest): Response<ModerationResponse>
}
