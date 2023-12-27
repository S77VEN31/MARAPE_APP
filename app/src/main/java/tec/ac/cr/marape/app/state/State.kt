package tec.ac.cr.marape.app.state

import android.content.Context
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User

class State(var context: Context) {
  public lateinit var user: User
  public lateinit var inventories: ArrayList<Inventory>
  public lateinit var sharedInventories: ArrayList<Inventory>

  companion object {
    private var instance: State? = null

    fun getInstance(context: Context?): State {
      if (instance == null && context != null) {
        instance = State(context)
      }
      return instance!!
    }
  }
}
