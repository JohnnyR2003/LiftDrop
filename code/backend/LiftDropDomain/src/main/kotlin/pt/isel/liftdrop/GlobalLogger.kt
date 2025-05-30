package pt.isel.pipeline.pt.isel.liftdrop

import io.micrometer.common.util.internal.logging.Slf4JLoggerFactory

class GlobalLogger {
    companion object {
        private val logger = Slf4JLoggerFactory.getInstance("GlobalLogger")

        fun log(message: String) {
            logger.info(message)
        }

        fun logError(message: String) {
            logger.error(message)
        }
    }
}
