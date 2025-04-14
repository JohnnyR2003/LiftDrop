package pt.isel.liftdrop

/**
 * Representa um cliente autenticado.
 * @property client O cliente.
 * @property token O token de autenticação.
 */
data class AuthenticatedClient(
    val client: Client,
    val token: String,
)