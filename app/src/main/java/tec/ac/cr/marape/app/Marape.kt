package tec.ac.cr.marape.app

import android.app.Application
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User
import tec.ac.cr.marape.app.state.State

class Marape : Application() {
  private lateinit var state: State
  private lateinit var db: FirebaseFirestore
  override fun onCreate() {
    super.onCreate()
    state = State.getInstance(baseContext)
    db = FirebaseFirestore.getInstance()

    state.user = User(
      "Aaron González",
      "erizojuan33@gmail.com",
      "Something here I guess lol",
      "Costa Rica",
      "6475-0398"
    )

    state.inventories = ArrayList()

    val inventoriesRef =
      db.collection("inventories").where(Filter.equalTo("ownerEmail", state.user.email))
    inventoriesRef.get().addOnSuccessListener { snapshot ->
      snapshot.documents.iterator().forEach { inventorySnapshot ->
        val inventory = inventorySnapshot.toObject(Inventory::class.java)
        inventory?.id = inventorySnapshot.id
        inventory?.let { state.inventories.add(it) }
      }
    }

  }
}