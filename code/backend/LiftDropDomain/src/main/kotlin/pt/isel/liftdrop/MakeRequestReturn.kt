package pt.isel.liftdrop

data class MakeRequestReturn (
    val requestId: Int,
    val pickupCode: String,
    val dropOffCode: String,
    )