package com.gaatho.rent.core.utils

import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.domain.model.Property
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

suspend fun seedDatabase(
    propertyRepo: PropertyRepository,
    tenantRepo: TenantRepository,
    ownerId: String
) {
    withContext(Dispatchers.IO) {
        val nepaliNames = listOf(
            "Aarav Shrestha", "Bina Maharjan", "Chirag Thapa", "Dipendra Karki", "Elisha Gurung",
            "Gopal Tamang", "Hari Khadka", "Isha Bhattarai", "Kamal Magar", "Laxmi Rai",
            "Manoj Lama", "Nisha Poudel", "Omkar Dhakal", "Pooja Bista", "Rabi Basnet",
            "Sita Joshi", "Tara Pandey", "Umesh Malla", "Vijay Shah", "Yogita Shahi"
        )
        
        val locations = listOf(
            "Baneshwor, Kathmandu", "Patan, Lalitpur", "Thamel, Kathmandu", "Baluwatar, Kathmandu", 
            "Bhaisepati, Lalitpur", "Lazimpat, Kathmandu", "Jhamsikhel, Lalitpur", "Maharajgunj, Kathmandu",
            "Kirtipur, Kathmandu", "Bhaktapur Durbar Square", "Gongabu, Kathmandu", "Koteshwor, Kathmandu"
        )
        
        val statuses = listOf("Active", "Pending", "Inactive")
        
        fun generateId(): String {
            val chars = "0123456789abcdef"
            return (1..32).map { chars.random() }.joinToString("")
        }

        // Seed 20 Properties
        val propertyIds = mutableListOf<String>()
        for (i in 1..20) {
            val id = generateId()
            propertyIds.add(id)
            val property = Property(
                id = id,
                ownerId = ownerId,
                name = "Property $i - ${locations.random().split(",").first()}",
                address = locations.random(),
                propertyType = if (i % 3 == 0) "APARTMENT" else "HOUSE",
                totalUnits = (1..10).random(),
                // Seed in smallest unit (Paisa)
                monthlyRent = (10..50).random() * 1000L * 100L,
                description = "A beautiful property located in a prime area with 24/7 water supply and parking."
            )
            propertyRepo.createProperty(property)
        }
        
        // Seed 20 Tenants
        for (i in 0 until 20) {
            val propId = propertyIds.random()
            val tenant = Tenant(
                id = generateId(),
                ownerId = ownerId,
                name = nepaliNames[i],
                email = "tenant$i@example.com",
                phone = "98${(10000000..99999999).random()}",
                propertyId = propId,
                propertyName = "Property $i",
                roomNumber = "${(1..5).random()}${(listOf("A", "B", "C").random())}",
                // Seed in smallest unit (Paisa)
                rentAmount = (10..30).random() * 1000L * 100L,
                status = statuses.random()
            )
            tenantRepo.createTenant(tenant)
        }
    }
}
