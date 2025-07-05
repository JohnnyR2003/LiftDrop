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
                message = "Pedido de entrega aceite com sucesso.",
                detail = "O pedido foi aceite pelo estafeta com sucesso.",
            )

        fun declineSuccessMessage(): ResultMessage =
            ResultMessage(
                type = ResultType.SUCCESS,
                subType = ResultSubType.DECLINE,
                message = "Pedido de entrega recusado com sucesso.",
                detail = "O pedido foi recusado pelo estafeta com sucesso.",
            )

        fun acceptErrorMessage(): ResultMessage =
            ResultMessage(
                type = ResultType.ERROR,
                subType = ResultSubType.ACCEPT,
                message = "Ocorreu um erro ao aceitar o pedido de entrega.",
                detail = "O pedido não pôde ser aceite pelo estafeta.",
            )

        fun declineErrorMessage(): ResultMessage =
            ResultMessage(
                type = ResultType.ERROR,
                subType = ResultSubType.DECLINE,
                message = "Ocorreu um erro ao recusar o pedido de entrega.",
                detail = "O pedido não pôde ser recusado pelo estafeta.",
            )
    }
}
