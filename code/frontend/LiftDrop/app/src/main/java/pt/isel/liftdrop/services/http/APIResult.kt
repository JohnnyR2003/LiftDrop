package pt.isel.liftdrop.services.http

sealed class APIResult<out T> {
    data class Success<T>(val data: T) : APIResult<T>()
    data class Failure(val problem: Problem) : APIResult<Nothing>()
    data class Error(val exception: Throwable) : APIResult<Nothing>()

    companion object {
        fun <T> success(data: T): APIResult<T> = Success(data)
        fun failure(problem: Problem): APIResult<Nothing> = Failure(problem)
        fun error(exception: Throwable): APIResult<Nothing> = Error(exception)
    }

    val isSuccess get() = this is Success
    val isFailure get() = this is Failure
    val isError get() = this is Error

    fun getOrNull(): T? = (this as? Success<T>)?.data
    fun getProblemOrNull(): Problem? = (this as? Failure)?.problem
    fun getExceptionOrNull(): Throwable? = (this as? Error)?.exception
}
