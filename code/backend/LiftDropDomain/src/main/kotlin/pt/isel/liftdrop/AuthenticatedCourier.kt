package pt.isel.liftdrop

/**
 * Representa um entregador autenticado.
 * @property courier O entregador.
 * @property token O token de autenticação.
 */
data class AuthenticatedCourier(
    val courier: Courier,
    val token: String,
)