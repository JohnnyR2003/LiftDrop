package pt.isel.liftdrop.config

import liftdrop.repository.jdbi.JdbiTransactionManager
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@Configuration
class TransactionManagerConfiguration {
    private val jdbi =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL("")
            },
        ).configureWithAppRequirements()

//    private val jdbiDocker =
//        Jdbi
//            .create(
//                PGSimpleDataSource().apply {
//                    setURL(Environment.getDbUrl())
//                },
//            ).configureWithAppRequirements()


    @Bean
    @Profile("jdbi")
    fun transactionManagerJdbi(): JdbiTransactionManager = JdbiTransactionManager(jdbi)

//    @Bean
//    @Profile("dockerJdbi")
//    fun transactionManagerDockerJdbi(): TransactionManagerJdbi = TransactionManagerJdbi(jdbiDocker)
}
