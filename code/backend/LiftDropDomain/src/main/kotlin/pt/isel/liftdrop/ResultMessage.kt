package pt.isel.liftdrop

data class ResultMessage(
    val type: String,
    val subType: String?,
    val message: String,
    val detail: String? = null,
) {
    companion object {
        fun acceptSuccessMessage(): ResultMessage =
            ResultMessage(
                type = "SUCCESS",
                subType = "ACCEPT",
                message = "Delivery request accepted successfully.",
                detail = "The request has been accepted by the courier.",
            )

        fun declineSuccessMessage(): ResultMessage =
            ResultMessage(
                type = "SUCCESS",
                subType = "DECLINE",
                message = "Delivery request declined successfully.",
                detail = "The request has been declined by the courier.",
            )

        fun acceptErrorMessage(): ResultMessage =
            ResultMessage(
                type = "ERROR",
                subType = "ACCEPT",
                message = "Failed to accept the delivery request.",
                detail = "The request could not be accepted by the courier.",
            )

        fun declineErrorMessage(): ResultMessage =
            ResultMessage(
                type = "ERROR",
                subType = "DECLINE",
                message = "Failed to decline the delivery request.",
                detail = "The request could not be declined by the courier.",
            )
    }
}
