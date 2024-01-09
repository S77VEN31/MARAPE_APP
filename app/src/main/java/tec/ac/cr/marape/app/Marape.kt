package tec.ac.cr.marape.app

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.state.State

class Marape : Application() {
  private lateinit var state: State
  private lateinit var db: FirebaseFirestore
  override fun onCreate() {
    super.onCreate()
    state = State.getInstance()
    db = FirebaseFirestore.getInstance()

    state.inventories = ArrayList()
    state.sharedInventories = ArrayList()
  }
}