package tec.ac.cr.marape.app.model

import com.google.firebase.firestore.DocumentReference

data class User(
  var name: String = "",
  var email: String = "",
  var profile: String = "",
  var country: String = "",
  var phone: String = "",
  var ref: DocumentReference? = null
)
