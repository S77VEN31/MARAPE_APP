package tec.ac.cr.marape.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.InventoryDetailsActivity
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.adapter.SharedInventoryView
import tec.ac.cr.marape.app.databinding.FragmentHomeBinding
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.state.State


class HomeFragment : Fragment() {

  private var _binding: FragmentHomeBinding? = null
  private val binding get() = _binding!!
  private lateinit var db: FirebaseFirestore
  private lateinit var sharedInventoriesRef: CollectionReference
  private lateinit var customAdapter: SharedInventoryView
  private lateinit var viewModel: Lazy<HomeViewModel>
  private lateinit var state: State

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    state = State.getInstance()
    viewModel = viewModels<HomeViewModel>(factoryProducer = { ViewModelProducer(state) })
    db = FirebaseFirestore.getInstance()
    sharedInventoriesRef = db.collection("inventories")
    customAdapter = SharedInventoryView(viewModel.value.inventories)

    (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search_inventory, menu)
        val searchItem: MenuItem = menu.findItem(R.id.search_inventory)
        (searchItem.actionView as SearchView).setOnQueryTextListener(object :
          SearchView.OnQueryTextListener {

          override fun onQueryTextSubmit(query: String?): Boolean {
            return false
          }

          override fun onQueryTextChange(newText: String?): Boolean {
            customAdapter.filter.filter(newText)
            return true
          }

        })
      }

      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
      }

    }, viewLifecycleOwner)

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val recyclerView: RecyclerView = binding.sharedInventoriesRecycler
    recyclerView.setHasFixedSize(true)
    recyclerView.adapter = customAdapter
    recyclerView.layoutManager = LinearLayoutManager(activity)

    customAdapter.setToggleHandler(::handleInventoryToggle)
    customAdapter.setDetailButtonHandler(::handleInventoryDetails)
  }

  private fun handleInventoryToggle(inventory: Inventory, isChecked: Boolean, position: Int) {
    sharedInventoriesRef.document(inventory.id).update("active", isChecked).addOnSuccessListener {
      customAdapter.toggle(position, inventory, isChecked)
    }
  }

  private fun handleInventoryDetails(inventory: Inventory, position: Int) {
    val intent = Intent(requireContext(), InventoryDetailsActivity::class.java)
    intent.putExtra("position", position)
    intent.putExtra("inventory", inventory)
    startActivity(intent)
  }

  @Suppress("UNCHECKED_CAST")
  class ViewModelProducer(val state: State) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return HomeViewModel(state.sharedInventories) as T
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
