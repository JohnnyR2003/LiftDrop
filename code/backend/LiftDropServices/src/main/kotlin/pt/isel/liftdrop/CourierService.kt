package pt.isel.liftdrop

import jakarta.inject.Named

@Named
class CourierService {
    fun deliver(packageId: String) {
        println("Delivering package $packageId")
    }
}
