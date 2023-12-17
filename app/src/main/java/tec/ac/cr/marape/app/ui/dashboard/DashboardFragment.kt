package tec.ac.cr.marape.app.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.CreateInventoryActivity
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.adapter.InventoryView
import tec.ac.cr.marape.app.databinding.FragmentDashboardBinding
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.state.State

class DashboardFragment : Fragment() {

  private var _binding: FragmentDashboardBinding? = null
  private var recyclerView: RecyclerView? = null
  private lateinit var state: State
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private lateinit var viewModel: Lazy<DashboardViewModel>
  private lateinit var db: FirebaseFirestore


  private val binding get() = _binding!!

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  @SuppressLint("NotifyDataSetChanged")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recyclerView = binding.ownedInventoriesRecycler
    recyclerView!!.setHasFixedSize(false)
    val customAdapter = InventoryView(viewModel.value.inventories)
    customAdapter.setDeleteHandler(::handleInventoryDeletion)
    recyclerView!!.adapter = customAdapter
    recyclerView!!.layoutManager = LinearLayoutManager(activity)

    launcher = registerForActivityResult(StartActivityForResult(), ::resultCallback)

    binding.floatingActionButton.setOnClickListener(::createInventory)

    (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search_inventory, menu)
        val searchItem: MenuItem = menu.findItem(R.id.search_inventory)
        (searchItem.actionView as SearchView).setOnQueryTextListener(object :
          SearchView.OnQueryTextListener {
          override fun onQueryTextSubmit(query: String?): Boolean {
            return true
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

    recyclerView!!.adapter?.notifyDataSetChanged()
  }

  private fun handleInventoryDeletion(inventory: Inventory, position: Int) {
    AlertDialog.Builder(requireContext())
      .setTitle(R.string.inventory_deletion_title)
      .setMessage(R.string.inventory_deletion_message)
      .setCancelable(true)
      .setPositiveButton(R.string.account_deletion_confirm_button_text) { _, _ ->
        db.collection("inventories").document(inventory.id).delete().addOnSuccessListener {
          viewModel.value.remove(inventory)
          recyclerView!!.adapter?.notifyItemRemoved(position)
        }
      }
      .setNegativeButton(R.string.action_cancel) { self, _ ->
        self.cancel()
      }
      .show()
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun resultCallback(result: ActivityResult) {
    val createdInventory = result.data?.getSerializableExtra("created", Inventory::class.java)
    createdInventory?.let { inventory ->
      viewModel.value.add(inventory)
      recyclerView!!.adapter?.notifyItemInserted(0)
    }
  }

  private fun createInventory(view: View) {
    val intent = Intent(activity, CreateInventoryActivity::class.java)
    launcher.launch(intent)
  }

  @Suppress("UNCHECKED_CAST")
  class ViewModelProducer(val state: State) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return DashboardViewModel(state.inventories) as T
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    //val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
    state = State.getInstance(null)
    _binding = FragmentDashboardBinding.inflate(inflater, container, false)
    viewModel = viewModels<DashboardViewModel>(factoryProducer = { ViewModelProducer(state) })
    db = FirebaseFirestore.getInstance()
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    recyclerView = null
  }
}
