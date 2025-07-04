package pt.isel.liftdrop.controller

import com.example.utils.Either
import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.liftdrop.DeliveryKind
import pt.isel.liftdrop.DeliveryStatus
import pt.isel.liftdrop.Uris
import pt.isel.liftdrop.model.*
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger
import pt.isel.services.assignment.AssignmentServices
import pt.isel.services.courier.*
import pt.isel.services.google.GeocodingServices

@RestController
@RequestMapping(Uris.Courier.BASE)
class CourierController(
    val courierService: CourierService,
    val assignmentServices: AssignmentServices,
    val geocodingServices: GeocodingServices,
) {
    @PostMapping(Uris.Courier.REGISTER)
    fun registerCourier(
        @RequestBody registerInput: RegisterCourierInputModel,
    ): ResponseEntity<Any> {
        val courierCreationResult =
            courierService.registerCourier(
                email = registerInput.email,
                password = registerInput.password,
                name = registerInput.name,
            )
        return when (courierCreationResult) {
            is Success -> ResponseEntity.ok(RegisterCourierOutputModel(courierCreationResult.value))
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        CourierCreationError.CourierEmailAlreadyExists::class to {
                            Problem.courierAlreadyExists().response(HttpStatus.CONFLICT)
                        },
                    )
                errorResponseMap[courierCreationResult.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Courier.LOGIN)
    fun login(
        @RequestBody input: LoginInputModel,
    ): ResponseEntity<Any> {
        val courierLoginResult = courierService.loginCourier(input.email, input.password)
        return when (courierLoginResult) {
            is Success -> {
                GlobalLogger.log("Client logged in successfully with token: ${courierLoginResult.value}")
                val token = courierLoginResult.value.token
                val cookie =
                    ResponseCookie
                        .from("auth_token", token)
                        .path("/")
                        .maxAge(3600 * 12)
                        .build()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(
                        LoginOutputModel(
                            id = courierLoginResult.value.courierId,
                            username = courierLoginResult.value.username,
                            email = courierLoginResult.value.email,
                            bearer = courierLoginResult.value.token,
                        ),
                    )
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        CourierLoginError.BlankEmailOrPassword::class to {
                            Problem.invalidRequestContent("Email and Password must not be blank").response(HttpStatus.BAD_REQUEST)
                        },
                        CourierLoginError.CourierNotFound::class to {
                            Problem.courierNotFound().response(HttpStatus.NOT_FOUND)
                        },
                        CourierLoginError.InvalidEmailOrPassword::class to {
                            Problem.passwordIsIncorrect().response(HttpStatus.UNAUTHORIZED)
                        },
                        CourierLoginError.WrongPassword::class to {
                            Problem.passwordIsIncorrect().response(HttpStatus.UNAUTHORIZED)
                        },
                    )
                errorResponseMap[courierLoginResult.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @DeleteMapping(Uris.Courier.LOGOUT)
    fun logout(
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<Any> {
        val bearerToken = token.removePrefix("Bearer ").trim()
        val courierLogoutResult = courierService.logoutCourier(bearerToken)
        return when (courierLogoutResult) {
            is Either.Right -> {
                val expiredCookie =
                    ResponseCookie
                        .from("auth_token", "")
                        .path("/")
                        .maxAge(0)
                        .build()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                    .body(LogoutOutputModel(isLoggedOut = true))
            }
            is Either.Left -> {
                val errorResponseMap =
                    mapOf(
                        CourierLogoutError.SessionNotFound::class to {
                            Problem.sessionNotFound().response(HttpStatus.NOT_FOUND)
                        },
                    )
                errorResponseMap[courierLogoutResult.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Courier.UPDATE_LOCATION)
    fun updateCourierLocation(
        @RequestBody input: LocationUpdateInputModel,
    ): ResponseEntity<Any> {
        val address =
            geocodingServices.reverseGeocode(
                input.newLocation.latitude,
                input.newLocation.longitude,
            )
        return when (address) {
            is Success -> {
                val updateLocationResult =
                    courierService.updateCourierLocation(
                        input.courierId,
                        input.newLocation,
                        address.value,
                    )
                when (updateLocationResult) {
                    is Success -> {
                        GlobalLogger.log("Courier location updated successfully")
                        ResponseEntity.ok(true)
                    }
                    is Failure -> {
                        val errorResponseMap =
                            mapOf(
                                LocationUpdateError.CourierNotFound::class to {
                                    Problem.courierNotFound().response(HttpStatus.NOT_FOUND)
                                },
                            )
                        errorResponseMap[updateLocationResult.value::class]?.invoke()
                            ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        LocationUpdateError.InvalidCoordinates::class to {
                            Problem.invalidCoordinates().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[address.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Courier.WAITING_ORDERS)
    fun startListening(input: StartListeningInputModel): ResponseEntity<Any> {
        val request = courierService.stopListening(input.courierId)
        return when (request) {
            is Success -> {
                println("Courier is now available for orders")
                ResponseEntity.ok("Courier is now available for orders")
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        StateUpdateError.CourierWasAlreadyListening::class to {
                            Problem.courierNotFound().response(HttpStatus.NOT_FOUND)
                        },
                    )
                errorResponseMap[request.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Courier.TRY_PICKUP)
    fun isPickupSpotValid(
        @RequestBody input: PickupSpotInputModel,
    ): ResponseEntity<Any> {
        val request =
            courierService.checkCourierPickup(
                requestId = input.requestId,
                courierId = input.courierId,
            )
        return when (request) {
            is Success -> {
                println("Pickup spot is valid")
                ResponseEntity.ok(true)
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        CourierPickupError.CourierNotNearPickup::class to {
                            Problem.courierNotNearPickup().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[request.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Courier.PICKED_UP_ORDER)
    fun pickUpOrder(
        @RequestBody input: PickupOrderInputModel,
    ): ResponseEntity<Any> {
        println("Received pickup request: $input")
        val request =
            courierService.pickupDelivery(
                requestId = input.requestId,
                courierId = input.courierId,
                pickupPin = input.pickupCode,
            )
        val deliveryKind = DeliveryKind.fromString(input.deliveryKind)
        return when (request) {
            is Success -> {
                when (deliveryKind) {
                    DeliveryKind.DEFAULT -> ResponseEntity.ok(true)
                    DeliveryKind.RELAY -> {
                        assignmentServices.completeReassignment(input.requestId)
                        ResponseEntity.ok(true)
                    }
                }
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        CourierPickupError.PickupPINMismatch::class to {
                            Problem.pickupCodeIsIncorrect().response(HttpStatus.UNAUTHORIZED)
                        },
                        CourierPickupError.CourierNotNearPickup::class to {
                            Problem.courierNotNearPickup().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[request.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Courier.TRY_DELIVERY)
    fun isDropOffSpotValid(
        @RequestBody input: DropOffSpotInputModel,
    ): ResponseEntity<Any> {
        val request =
            courierService.checkCourierDropOff(
                requestId = input.requestId,
                courierId = input.courierId,
            )
        return when (request) {
            is Success -> {
                println("Drop-off spot is valid")
                ResponseEntity.ok(true)
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        CourierDeliveryError.CourierNotNearDropOff::class to {
                            Problem.courierNotNearDropOff().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[request.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Courier.DELIVERED_ORDER)
    fun deliverOrder(
        @RequestBody input: DeliverOrderInputModel,
    ): ResponseEntity<Any> {
        val request =
            courierService.deliver(
                requestId = input.requestId,
                courierId = input.courierId,
                dropOffPin = input.dropoffCode,
                deliveryEarnings = input.deliveryEarnings,
            )
        return when (request) {
            is Success -> {
                println("Order delivered successfully")
                ResponseEntity.ok(true)
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        CourierDeliveryError.PickupPINMismatch::class to {
                            Problem.dropOffCodeIsIncorrect().response(HttpStatus.UNAUTHORIZED)
                        },
                        CourierDeliveryError.CourierNotNearDropOff::class to {
                            Problem.courierNotNearDropOff().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[request.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Courier.CANCEL_DELIVERY)
    fun cancelDelivery(
        @RequestBody input: CancelDeliveryInputModel,
    ): ResponseEntity<Any> {
        val request =
            courierService.cancelDelivery(
                requestId = input.requestId,
                courierId = input.courierId,
            )
        val deliveryStatus = DeliveryStatus.fromString(input.deliveryStatus)
        return when (request) {
            is Success -> {
                GlobalLogger.log("Courier cancelled delivery while heading to dropoff")
                val reassignResult =
                    assignmentServices.handleRequestReassignment(
                        input.requestId,
                        input.courierId,
                        deliveryStatus,
                        input.pickupLocationDTO,
                    )
                if (!reassignResult) {
                    GlobalLogger.log("Failed to reassign request ${input.requestId}")
                    Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
                } else {
                    GlobalLogger.log("Request ${input.requestId} reassigned successfully")
                    ResponseEntity.ok(true)
                }
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        CourierCancelDeliveryError.PackageAlreadyDelivered::class to {
                            Problem.packageAlreadyDelivered().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[request.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @GetMapping(Uris.Courier.FETCH_DAILY_EARNINGS)
    fun fetchDailyEarnings(
        @PathVariable courierId: Int,
    ): ResponseEntity<Any> =
        when (val request = courierService.fetchDailyEarnings(courierId)) {
            is Either.Right -> ResponseEntity.ok(request.value)
            is Either.Left -> {
                val errorResponseMap =
                    mapOf(
                        CourierEarningsError.CourierNotFound::class to {
                            Problem.courierNotFound().response(HttpStatus.NOT_FOUND)
                        },
                    )
                errorResponseMap[request.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }

    @PostMapping(Uris.Courier.COMPLETE_ORDER)
    fun completeOrder() {
        TODO()
    }
}
