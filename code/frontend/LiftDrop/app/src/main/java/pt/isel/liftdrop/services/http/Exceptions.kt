package pt.isel.liftdrop.services.http

import okhttp3.Response

abstract class ApiException(msg: String) : Exception(msg)

class UnexpectedResponseException(
    val response: Response? = null
) : ApiException("Unexpected ${response?.code} response from the API.")

class ResponseException(
    response: String
) : ApiException(response)

class InvalidResponseException(
    message: String
) : ApiException(message)