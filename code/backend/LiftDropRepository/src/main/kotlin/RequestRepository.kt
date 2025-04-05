package com.example

interface RequestRepository {
    fun createRequest(
        clientId: Int,
        // courierId: Int?, // maybe useless because it will be null when the request is created
        description: String,
        requestStatus: String,
        eta: String,
    ): Int

    fun updateRequest(
        requestId: Int,
        courierId: Int?,
        description: String,
        requestStatus: String,
        eta: String,
    ): Boolean

    fun deleteRequest(requestId: Int): Boolean
}
