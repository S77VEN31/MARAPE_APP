package tec.ac.cr.marape.app

import android.app.Application
import tec.ac.cr.marape.app.model.User
import tec.ac.cr.marape.app.state.State

class Marape : Application() {
  private lateinit var state: State
  public override fun onCreate() {
    super.onCreate()
    state = State.getInstance(baseContext)

    state.user = User(
      "Aaron González",
      "erizojuan33@gmail.com",
      "Something here I guess lol",
      "Costa Rica",
      "6475-0398"
    )
  }
}