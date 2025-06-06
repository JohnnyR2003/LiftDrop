package pt.isel.liftdrop.services.http


sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val problem: Problem) : Result<Nothing>()
}