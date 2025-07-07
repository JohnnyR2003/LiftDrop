package pt.isel.services.client

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
import pt.isel.liftdrop.LocationDTO
import pt.isel.liftdrop.UserRole
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger
import pt.isel.services.assignment.AssignmentServices
import pt.isel.services.google.GeocodingServices
import pt.isel.services.utils.Codify.encodePassword
import pt.isel.services.utils.Codify.matchesPassword
import java.util.UUID

@Named
class ClientService(
    private val transactionManager: TransactionManager,
    private val assignmentServices: AssignmentServices,
    private val geocodingServices: GeocodingServices,
) {
    fun registerClient(
        email: String,
        password: String,
        name: String,
        address: Address,
    ): Either<ClientCreationError, Int> =
        transactionManager.run {
            val userRepository = it.usersRepository
            val clientRepository = it.clientRepository
            val locationRepository = it.locationRepository

            if (userRepository.findUserByEmail(email) != null) {
                return@run failure(ClientCreationError.UserAlreadyExists)
            }

            val userCreation =
                userRepository.createUser(
                    email = email,
                    password = password.encodePassword(),
                    name = name,
                    role = UserRole.CLIENT,
                )

            val clientCreation =
                clientRepository.createClient(
                    clientId = userCreation,
                    address = address,
                )

            // Create a DropOff Location
            val dropOff =
                geocodingServices.getLatLngFromAddress(address.toFormattedString())
                    ?: return@run failure(ClientCreationError.InvalidAddress)

            val locId = locationRepository.createLocation(LocationDTO(dropOff.first, dropOff.second), address)

            locationRepository.createDropOffLocation(userCreation, locId)
                ?: return@run failure(ClientCreationError.InvalidLocation)

            return@run success(clientCreation)
        }

    fun getClientById(clientId: Int): Either<ClientLoginError, Client> =
        transactionManager.run {
            val clientRepository = it.clientRepository
            val client = clientRepository.getClientByUserId(clientId)
            if (client == null) {
                return@run failure(ClientLoginError.ClientNotFound)
            } else {
                return@run success(client)
            }
        }

    fun loginClient(
        email: String,
        password: String,
    ): Either<ClientLoginError, String> =
        transactionManager.run {
            val clientRepository = it.clientRepository
            val userRepository = it.usersRepository

            if (email.isBlank() || password.isBlank()) {
                return@run failure(ClientLoginError.BlankEmailOrPassword)
            }

            val userId =
                userRepository.findUserByEmail(email)?.id
                    ?: return@run failure(ClientLoginError.ClientNotFound)

            val passwordFromDatabase =
                clientRepository
                    .loginClient(
                        email = email,
                        password = password,
                    )?.second ?: return@run failure(ClientLoginError.InvalidEmailOrPassword)

            val sessionToken = UUID.randomUUID().toString()

            clientRepository
                .createClientSession(
                    userId,
                    sessionToken,
                )
            when (matchesPassword(password, passwordFromDatabase)) {
                true -> {
                    return@run success(sessionToken)
                }

                false -> {
                    return@run failure(ClientLoginError.WrongPassword)
                }
            }
        }

    fun makeRequest(
        client: Client,
        description: String,
        quantity: Int,
        restaurantName: String,
        dropOffAddress: Address?,
    ): Either<RequestCreationError, Int> =
        transactionManager.run {
            val requestRepository = it.requestRepository
            val locationRepository = it.locationRepository

            val dropOffLocationId =
                if (dropOffAddress == null) {
                    locationRepository.getClientDropOffLocation(client.user.id)
                        ?: return@run failure(RequestCreationError.ClientAddressNotFound)
                } else {
                    val dropOff =
                        geocodingServices.getLatLngFromAddress(dropOffAddress.toFormattedString())
                            ?: return@run failure(RequestCreationError.InvalidAddress)

                    val locId = locationRepository.createLocation(LocationDTO(dropOff.first, dropOff.second), dropOffAddress)

                    locationRepository.createDropOffLocation(client.user.id, locId)
                        ?: return@run failure(RequestCreationError.InvalidLocation)
                }

            val restaurantLocation =
                locationRepository.getClosestRestaurantLocation(restaurantName, dropOffLocationId)
                    ?: return@run failure(RequestCreationError.RestaurantNotFound)

            val itemETA =
                locationRepository.itemExistsAtRestaurant(description, restaurantName)
                    ?: return@run failure(RequestCreationError.ItemNotFound)

            val requestId =
                requestRepository.createRequest(
                    clientId = client.user.id,
                    eta = itemETA,
                    pickupCode = generateRandomCode(6),
                    dropoffCode = generateRandomCode(6),
                ) ?: return@run failure(RequestCreationError.ClientNotFound)

            val pickupLocationId = restaurantLocation.first

            requestRepository.createRequestDetails(
                requestId = requestId,
                description = description,
                quantity = quantity,
                pickupLocationId = pickupLocationId,
                dropoffLocationId = dropOffLocationId,
            )

            GlobalLogger.log(
                "Request created with ID: $requestId, " +
                    "pickup location ID: $pickupLocationId, " +
                    "drop-off location ID: $dropOffLocationId",
            )

            CoroutineScope(Dispatchers.Default).launch {
                assignmentServices.handleCourierAssignment(
                    restaurantLocation.second.latitude,
                    restaurantLocation.second.longitude,
                    requestId,
                    deliveryKind = "DEFAULT",
                )
            }

            return@run success(requestId)
        }

    fun logoutClient(token: String): Either<ClientLogoutError, Boolean> =
        transactionManager.run {
            val clientRepository = it.clientRepository
            val result = clientRepository.logoutClient(token)
            if (result) {
                return@run success(true)
            } else {
                return@run failure(ClientLogoutError.SessionNotFound)
            }
        }

    fun addDropOffLocation(
        clientId: Int,
        address: Address,
    ): Either<DropOffCreationError, Int> =
        transactionManager.run {
            val locationRepository = it.locationRepository

            val loc =
                geocodingServices.getLatLngFromAddress(address.toFormattedString())
                    ?: return@run failure(DropOffCreationError.InvalidAddress)

            val locationId = locationRepository.createLocation(LocationDTO(loc.first, loc.second), address)

            locationRepository.createDropOffLocation(clientId, locationId)
                ?: return@run failure(DropOffCreationError.ClientNotFound)

            return@run success(locationId)
        }

    fun getRequestStatus(
        clientId: Int,
        requestId: Int,
    ): Either<ClientGetRequestStatusError, Pair<String, String>> =
        transactionManager.run {
            val clientRepository = it.clientRepository
            val status =
                clientRepository.getRequestStatus(clientId, requestId)
                    ?: return@run failure(ClientGetRequestStatusError.RequestNotFound)

            if (status.second == "PENDING" || status.second == "PENDING_REASSIGNMENT") {
                return@run success(Pair("N/A", status.second))
            } else {
                // Convert eta from seconds to minutes and seconds
                val etaMinutes = status.first.toInt() / 60
                val etaSeconds = status.first.toInt() % 60
                val formattedEta = String.format("%02d:%02d", etaMinutes, etaSeconds)

                return@run success(Pair(formattedEta, status.second))
            }
        }

    fun giveRating(
        clientId: Int,
        rating: Int,
    ): Either<ClientRatingError, Boolean> =
        transactionManager.run {
            val requestRepository = it.requestRepository
            val requestId =
                requestRepository.getMostRecentRequestIdForClient(clientId)
                    ?: return@run failure(ClientRatingError.RequestNotFound)
            val result = requestRepository.giveRatingToCourier(clientId, requestId, rating)
            if (result) {
                return@run success(true)
            } else {
                return@run failure(ClientRatingError.RatingAlreadyDone)
            }
        }
}

fun Address.toFormattedString(): String {
    val components =
        listOfNotNull(
            streetNumber?.takeIf { it.isNotBlank() },
            street.takeIf { it.isNotBlank() },
            floor?.takeIf { it.isNotBlank() },
            city.takeIf { it.isNotBlank() },
            zipCode.takeIf { it.isNotBlank() },
            country.takeIf { it.isNotBlank() },
        )
    return components.joinToString(separator = ", ")
}

fun generateRandomCode(length: Int): String {
    val allowedChars = "0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
