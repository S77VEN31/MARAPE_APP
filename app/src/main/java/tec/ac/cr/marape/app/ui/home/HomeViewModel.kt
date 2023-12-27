package tec.ac.cr.marape.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tec.ac.cr.marape.app.model.Inventory

class HomeViewModel(var inventories: ArrayList<Inventory>) : ViewModel()
