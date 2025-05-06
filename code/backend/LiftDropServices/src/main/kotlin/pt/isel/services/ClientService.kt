package pt.isel.services

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import jakarta.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import liftdrop.repository.TransactionManager
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.UserRole
import pt.isel.liftdrop.LocationDTO
import pt.isel.services.google.GeocodingServices
import pt.isel.services.utils.Codify.encodePassword
import pt.isel.services.utils.Codify.matchesPassword
import java.util.UUID

sealed class ClientError {
    data object ClientNotFound : ClientError()

    data object UserNotFound : ClientError()

    data object InvalidEmailOrPassword : ClientError()

    data object ClientEmailAlreadyExists : ClientError()

    data object InvalidAddress : ClientError()
}

@Named
class ClientService(
    private val transactionManager: TransactionManager,
    private val geocodingServices: GeocodingServices,
) {
    fun registerClient(
        email: String,
        password: String,
        name: String,
        address: Address,
    ): Either<ClientError, Int> =
        transactionManager.run {
            val userRepository = it.usersRepository
            val clientRepository = it.clientRepository
            val locationRepository = it.locationRepository
            // Create user

            val userCreation =
                userRepository.createUser(
                    email = email,
                    password = password.encodePassword(),
                    name = name,
                    role = UserRole.CLIENT,
                )


            val user = userRepository.findUserByEmail(email) ?: throw IllegalStateException("User should be created")
            val clientId = user.id

            val clientCreation =
                clientRepository.createClient(
                    clientId = clientId,
                    address = address,
                )

            //Create a DropOff Location
            val loc = geocodingServices.getLatLngFromAddress(address.toFormattedString())
            if (loc == null) {
                return@run failure(ClientError.InvalidAddress)
            }
            println("longitude: ${loc.first} latitude: ${loc.second}")
            locationRepository.createLocation(LocationDTO(loc.first, loc.second), address)


            return@run success(clientCreation)
        }

    fun getClientById(clientId: Int): Either<ClientError, Client>? =
        transactionManager.run {
            val clientRepository = it.clientRepository
            val client = clientRepository.getClientByUserId(clientId)
            if (client == null) {
                return@run failure(ClientError.ClientNotFound)
            } else {
                return@run success(client)
            }
        }

    fun loginClient(
        email: String,
        password: String,
    ): Either<ClientError, String> =
        transactionManager.run {
            val clientRepository = it.clientRepository
            val userRepository = it.usersRepository
            println("I'm here")
            val passwordFromDatabase =
                clientRepository
                    .loginClient(
                        email = email,
                        password = password,
                    )?.second ?: return@run failure(ClientError.InvalidEmailOrPassword)
            println("I'm here")
            val userId =
                userRepository.findUserByEmail(email)?.id
                    ?: return@run failure(ClientError.UserNotFound)
            val sessionToken = UUID.randomUUID().toString()
            println("I'm here")
            clientRepository
                .createClientSession(
                    userId,
                    sessionToken,
                )
            println("I'm here")
            println("passwordFromDatabase: $passwordFromDatabase")
            println("password: $password")
            when (matchesPassword(password, passwordFromDatabase)) {
                true -> {
                    return@run success(sessionToken)
                }
                false -> {
                    return@run failure(ClientError.InvalidEmailOrPassword)
                }
            }
        }

    fun makeRequest(
        client: Client,
        description: String,
        restaurantName: String,
        dropOffLocation: LocationDTO,
    ): Either<ClientError, Int> =
        transactionManager.run {
            val requestRepository = it.requestRepository
            val locationRepository = it.locationRepository
            val requestId =
                requestRepository.createRequest(
                    clientId = client.user.id,
                    eta = 0,
                )

            val pickupLocation = locationRepository.getRestaurantLocationByItem(description, restaurantName)

            val pickupAddress: Address =
                geocodingServices.reverseGeocode(
                    pickupLocation.latitude,
                    pickupLocation.longitude,
                )

            val pickupLocationId = locationRepository.createLocation(pickupLocation, pickupAddress)

            val dropOffAddress: Address =
                geocodingServices.reverseGeocode(
                    dropOffLocation.latitude,
                    dropOffLocation.longitude,
                )

            val dropOffLocationId = locationRepository.createLocation(dropOffLocation, dropOffAddress)

            requestRepository
                .createRequestDetails(
                    requestId = requestId,
                    description = description,
                    pickupLocationId = pickupLocationId,
                    dropoffLocationId = dropOffLocationId,
                )

            CoroutineScope(Dispatchers.Default).launch {
                geocodingServices.handleCourierAssignment(pickupLocation.latitude, pickupLocation.longitude, requestId)
            }
            return@run success(requestId)
        }
}

fun Address.toFormattedString(): String {
    val components = listOfNotNull(
        streetNumber?.takeIf { it.isNotBlank() },
        street.takeIf { it.isNotBlank() },
        floor?.takeIf { it.isNotBlank() },
        city.takeIf { it.isNotBlank() },
        zipCode.takeIf { it.isNotBlank() },
        country.takeIf { it.isNotBlank() }
    )
    return components.joinToString(separator = ", ")
}