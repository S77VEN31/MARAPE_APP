package tec.ac.cr.marape.app.model

enum class InventoryStatus(var status: Int) {
  INACTIVE(0),
  ACTIVE(1);
  companion object {
    fun fromInt(value: Int) = InventoryStatus.values().first { it.status == value }
  }
}

data class Inventory(
    var name: String,
    // NOTE: the reason for the change is because we'll be using timestamps in the database,
    // however there are some conflicts with the way firebase tries to use the getters and
    // setters of POJOs and mapping those to field names, however when using LocalDate firebase
    // will find some methods that don't conform with its POJO expectatives so the application
    // will crash
    var creationDate: Long,
    var status: InventoryStatus,
    var ownerEmail: String,
    var invitedUsers: List<String>?,
)
