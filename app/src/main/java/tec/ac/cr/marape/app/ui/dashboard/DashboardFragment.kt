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
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.AddGuestActivity
import tec.ac.cr.marape.app.CreateInventoryActivity
import tec.ac.cr.marape.app.DELETE_GUEST_INVENTORY
import tec.ac.cr.marape.app.EditInventoryActivity
import tec.ac.cr.marape.app.GuestListActivity
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.adapter.InventoryAdapter
import tec.ac.cr.marape.app.databinding.FragmentDashboardBinding
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.state.State

const val CREATED_INVENTORY = 1
const val EDITED_INVENTORY = 2
const val ADDED_GUEST_INVENTORY = 3

class DashboardFragment : Fragment() {

  private var _binding: FragmentDashboardBinding? = null
  private var recyclerView: RecyclerView? = null
  private lateinit var state: State
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private lateinit var db: FirebaseFirestore
  private lateinit var inventoriesRef: CollectionReference
  private lateinit var inventoryAdapter: InventoryAdapter


  private val binding get() = _binding!!

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  @SuppressLint("NotifyDataSetChanged")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recyclerView = binding.ownedInventoriesRecycler
    recyclerView!!.setHasFixedSize(false)

    inventoryAdapter.setDeleteHandler(::handleInventoryDeletion)
    inventoryAdapter.setDisablingHandler(::handleDisablingInventory)
    inventoryAdapter.setOnClickListener(::handleItemClick)
    inventoryAdapter.setAddCollaboratorClickListener(::handleAddPartner)
    inventoryAdapter.setCollaboratorsHandler(::handleListCollaborators)

    recyclerView!!.adapter = inventoryAdapter
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
            return false
          }

          override fun onQueryTextChange(newText: String?): Boolean {
            inventoryAdapter.filter.filter(newText)
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

  private fun handleDisablingInventory(
    view: View,
    inventory: Inventory,
    checked: Boolean,
    position: Int
  ) {
    inventoriesRef.document(inventory.id).update("active", checked)
      .addOnSuccessListener {
        inventoryAdapter.toggle(position, inventory, checked)
      }
      .addOnFailureListener {
        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
      }
  }

  private fun handleListCollaborators(view: View, inventory: Inventory, position: Int) {
    val intent = Intent(requireContext(), GuestListActivity::class.java)
    intent.putExtra("inventory", inventory)
    intent.putExtra("position", position)
    launcher.launch(intent)
  }

  private fun handleItemClick(view: View, inventory: Inventory, position: Int) {
    val intent = Intent(requireContext(), EditInventoryActivity::class.java)
    intent.putExtra("position", position)
    intent.putExtra("inventory", inventory)
    launcher.launch(intent)
  }

  private fun handleAddPartner(inventory: Inventory, position: Int) {
    val intent = Intent(activity, AddGuestActivity::class.java)
    intent.putExtra("position", position)
    intent.putExtra("inventory", inventory)
    launcher.launch(intent)
  }

  private fun handleInventoryDeletion(view: View, inventory: Inventory, position: Int) {
    AlertDialog.Builder(requireContext())
      .setTitle(R.string.inventory_deletion_title)
      .setMessage(R.string.inventory_deletion_message)
      .setCancelable(true)
      .setPositiveButton(R.string.account_deletion_confirm_button_text) { _, _ ->
        inventoriesRef.document(inventory.id).delete().addOnSuccessListener {
          inventoryAdapter.remove(position, inventory)
        }
      }
      .setNegativeButton(R.string.action_cancel) { self, _ ->
        self.cancel()
      }
      .show()
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun resultCallback(result: ActivityResult) {
    when (result.resultCode) {
      CREATED_INVENTORY -> {
        val createdInventory = result.data?.getSerializableExtra("created") as Inventory
        createdInventory.let { inventory ->
          inventoryAdapter.add(inventory)
        }
      }

      EDITED_INVENTORY -> {
        val position = result.data?.getIntExtra("position", RecyclerView.NO_POSITION)
        val editedInventory = result.data?.getSerializableExtra("edited") as Inventory
        if (position != RecyclerView.NO_POSITION) {
          editedInventory.let { inventory ->
            inventoryAdapter.update(position!!, inventory)
          }
        }
      }

      DELETE_GUEST_INVENTORY -> {
        val position = result.data?.getIntExtra("position", RecyclerView.NO_POSITION)
        val updated = result.data?.getSerializableExtra("addGuest") as Inventory
        if (position != RecyclerView.NO_POSITION) {
          inventoryAdapter.update(position!!, updated)
        }
      }

      ADDED_GUEST_INVENTORY -> {
        val position = result.data?.getIntExtra("position", RecyclerView.NO_POSITION)
        val addGuestInventory = result.data?.getSerializableExtra("addGuest") as Inventory
        if (position != null && position != RecyclerView.NO_POSITION) {
          inventoryAdapter.update(position, addGuestInventory)
        }

      }
    }
  }

  private fun createInventory(view: View) {
    val intent = Intent(activity, CreateInventoryActivity::class.java)
    launcher.launch(intent)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    state = State.getInstance(null)
    _binding = FragmentDashboardBinding.inflate(inflater, container, false)
    db = FirebaseFirestore.getInstance()
    inventoriesRef = db.collection("inventories")
    inventoryAdapter = InventoryAdapter(state.inventories)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    recyclerView = null
  }
}
