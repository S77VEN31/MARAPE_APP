package tec.ac.cr.marape.app.model

import java.time.LocalDate

enum class InventoryStatus(var status: Int) {
    INACTIVE(0),
    ACTIVE(1);
    companion object {
        fun fromInt(value: Int) = InventoryStatus.values().first { it.status == value }
    }
}

data class Inventory(
        var name: String,
        var creationDate: LocalDate,
        val status: InventoryStatus,
        var adminUserId: String,
        val invitedUsers: List<String>
)
