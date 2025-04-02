package pt.isel.liftdrop_services

import jakarta.inject.Named

@Named
class CourierService {
    fun deliver(packageId: String) {
        println("Delivering package $packageId")
    }
}
