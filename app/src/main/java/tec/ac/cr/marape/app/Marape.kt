package tec.ac.cr.marape.app

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.model.User
import tec.ac.cr.marape.app.state.State

class Marape : Application() {
  private lateinit var state: State
  private lateinit var db: FirebaseFirestore
  override fun onCreate() {
    super.onCreate()
    state = State.getInstance(baseContext)
    db = FirebaseFirestore.getInstance()

    // FIXME: Remove this, the only reason this is here is because proper authentication has not been properly setup yet
    state.user = User(
      "Aaron González",
      "erizojuan33@gmail.com",
      "Something here I guess lol",
      "Costa Rica",
      "6475-0398"
    )

    state.inventories = ArrayList()

  }
}