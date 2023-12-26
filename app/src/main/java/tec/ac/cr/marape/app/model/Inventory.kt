package tec.ac.cr.marape.app.model

import com.google.firebase.firestore.DocumentReference
import java.io.Serializable


data class Inventory(
  var id: String = "",
  var name: String = "",
  // NOTE: the reason for the change is because we'll be using timestamps in the database,
  // however there are some conflicts with the way firebase tries to use the getters and
  // setters of POJOs and mapping those to field names, however when using LocalDate firebase
  // will find some methods that don't conform with its POJO expectatives so the application
  // will crash
  var creationDate: Long = 0,
  var active: Boolean = false,
  var ownerEmail: String = "",
  var invitedUsers: List<DocumentReference> = emptyList(),
  var items: List<DocumentReference> = emptyList()
) : Serializable
