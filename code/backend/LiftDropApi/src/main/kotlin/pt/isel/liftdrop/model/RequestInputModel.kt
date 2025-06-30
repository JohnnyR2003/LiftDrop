package pt.isel.liftdrop.model

data class RequestInputModel(
    val restaurantName: String,
    val itemDesignation: String,
    val quantity: Int = 1,
    val dropOffAddress: AddressInputModel? = null,
)
