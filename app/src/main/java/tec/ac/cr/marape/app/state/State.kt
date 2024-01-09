package tec.ac.cr.marape.app.state

import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User

class State {
  lateinit var user: User
  var inventories: ArrayList<Inventory> = arrayListOf()
  var sharedInventories: ArrayList<Inventory> = arrayListOf()

  companion object {
    private var instance: State? = null

    fun getInstance(): State {
      if (instance == null) {
        instance = State()
      }
      return instance!!
    }
  }
}
