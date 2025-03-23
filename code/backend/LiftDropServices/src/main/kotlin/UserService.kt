import jakarta.inject.Named
import pt.isel.liftdrop.User


@Named
class UserService {

    fun getUserByToken(token: String): User {
        TODO()
    }
}