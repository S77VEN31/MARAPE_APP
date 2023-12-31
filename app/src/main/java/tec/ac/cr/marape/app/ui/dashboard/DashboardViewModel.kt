package tec.ac.cr.marape.app.ui.dashboard

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import tec.ac.cr.marape.app.model.Inventory

class DashboardViewModel(var inventories: ArrayList<Inventory>) : ViewModel() {

  @SuppressLint("CheckResult")
  fun add(inventory: Inventory) {
    inventories.apply {
      add(0, inventory)
    }
  }

  fun remove(inventory: Inventory) {
    inventories.apply {
      remove(inventory)
    }
  }
}