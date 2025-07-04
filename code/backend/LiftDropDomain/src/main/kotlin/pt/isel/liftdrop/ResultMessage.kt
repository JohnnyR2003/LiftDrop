package pt.isel.liftdrop

data class ResultMessage(
    val type: ResultType,
    val subType: ResultSubType = ResultSubType.UNKNOWN,
    val message: String,
    val detail: String? = null,
) {
    companion object {
        fun acceptSuccessMessage(): ResultMessage =
            ResultMessage(
                type = ResultType.SUCCESS,
                subType = ResultSubType.ACCEPT,
                message = "Delivery request accepted successfully.",
                detail = "The request has been accepted by the courier.",
            )

        fun declineSuccessMessage(): ResultMessage =
            ResultMessage(
                type = ResultType.SUCCESS,
                subType = ResultSubType.DECLINE,
                message = "Delivery request declined successfully.",
                detail = "The request has been declined by the courier.",
            )

        fun acceptErrorMessage(): ResultMessage =
            ResultMessage(
                type = ResultType.ERROR,
                subType = ResultSubType.ACCEPT,
                message = "Failed to accept the delivery request.",
                detail = "The request could not be accepted by the courier.",
            )

        fun declineErrorMessage(): ResultMessage =
            ResultMessage(
                type = ResultType.ERROR,
                subType = ResultSubType.DECLINE,
                message = "Failed to decline the delivery request.",
                detail = "The request could not be declined by the courier.",
            )
    }
}
