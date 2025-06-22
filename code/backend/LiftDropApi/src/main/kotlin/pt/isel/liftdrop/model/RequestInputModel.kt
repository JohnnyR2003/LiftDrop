package pt.isel.liftdrop.model

data class RequestInputModel(
    val restaurantName: String,
    val itemDesignation: String,
    val dropOffAddress: AddressInputModel? = null,
)
