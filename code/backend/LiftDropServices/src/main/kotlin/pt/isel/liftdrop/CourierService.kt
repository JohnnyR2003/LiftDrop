package pt.isel.liftdrop

import jakarta.inject.Named
import liftdrop.repository.TransactionManager

@Named
class CourierService(
    private val transactionManager: TransactionManager,
) {
    fun acceptRequest(
        courierId: Long,
        requestId: Long,
    ) {
        val updated =
            transactionManager.run {
                val requestRepository = it.requestRepository
                requestRepository.updateRequest(
                    requestId,
                    courierId,
                    null,
                    null,
                )
            }
        if (!updated) {
            throw IllegalStateException("Request $requestId not accepted")
        } else {
            val accept =
                transactionManager.run {
                    val courierRepository = it.courierRepository
                    courierRepository.acceptRequest(
                        requestId,
                        courierId,
                    )
                }
            if (!accept) {
                throw IllegalStateException("Request $requestId not accepted")
            } else {
                println("Request $requestId accepted by courier $courierId")
                TODO()
            }
        }
    }
}
