package tec.ac.cr.marape.app.model.state

import com.google.api.Context
import tec.ac.cr.marape.app.model.User

class State(var context: Context) {
  public lateinit var user: User

  companion object {
    private var instance: State? = null

    fun getInstance(context: Context): State {
      if (instance == null) {
        instance = State(context)
      }
      return instance!!
    }
  }
}
