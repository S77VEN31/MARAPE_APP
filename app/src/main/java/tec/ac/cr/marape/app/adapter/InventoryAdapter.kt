package tec.ac.cr.marape.app.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import me.xdrop.fuzzywuzzy.FuzzySearch
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.ellipsize
import tec.ac.cr.marape.app.model.Inventory
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class InventoryAdapter(var inventories: ArrayList<Inventory>) :
  RecyclerView.Adapter<InventoryAdapter.ViewHolder>(), Filterable {
  var filteredInventories = inventories

  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)

  private lateinit var deleteHandler: (View, Inventory, Int) -> Unit
  private lateinit var disablingHandler: (View, Inventory, Boolean, Int) -> Unit
  private lateinit var clickHandler: (View, Inventory, Int) -> Unit
  private lateinit var collaboratorsHandler: (View, Inventory, Int) -> Unit
  private lateinit var clickAddCollaborator: (Inventory, Int) -> Unit

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView =
      LayoutInflater.from(parent.context).inflate(R.layout.layout_inventory_entry, parent, false)
    return ViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return filteredInventories.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val currentInventory = filteredInventories[position]
    holder.inventoryName.text = currentInventory.name.ellipsize(25)
    holder.creationDate.text = formatter.format(Date(currentInventory.creationDate)).toString()
    holder.statusSwitch.isChecked = currentInventory.active
    holder.collaborators.text = currentInventory.invitedUsers.size.toString()
    holder.collaborators.setOnClickListener {
      collaboratorsHandler(it, currentInventory, position)
    }
    holder.deleteInventoryButton.setOnClickListener {
      deleteHandler(it, currentInventory, position)
    }

    holder.statusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
      disablingHandler(buttonView, currentInventory, isChecked, position)
    }
    holder.itemView.setOnClickListener {
      clickHandler(it, currentInventory, position)
    }

    holder.addCollaboratorButton.setOnClickListener {
      clickAddCollaborator(currentInventory, position)
    }

  }


  // Removing an item uses its ID, because there's no way to know where that item might be in the
  // physical list, so instead of just using its position I'm looking for it in both arrays to
  // remove it
  fun remove(position: Int, inventory: Inventory) {
    val idx = inventories.indexOfFirst {
      it.id == inventory.id
    }

    if (idx != -1) {
      inventories.removeAt(idx) // This index is the internal index in the internal list.
    }
    if (inventories != filteredInventories) {
      filteredInventories.removeAt(position)
    }

    notifyItemRemoved(position) // This index is the external index on the recycler view, they're not always the same
  }

  fun add(inventory: Inventory) {
    inventories.add(0, inventory)
    notifyItemInserted(0)
  }

  fun update(position: Int, inventory: Inventory) {
    val idx = inventories.indexOfFirst {
      it.id.compareTo(inventory.id) == 0
    }

    if (idx != -1) {
      inventories[idx] = inventory
      notifyItemChanged(position)
    }
  }

  fun toggle(inventory: Inventory, state: Boolean) {
    val idx = inventories.indexOfFirst {
      it.id.compareTo(inventory.id) == 0
    }

    inventories[idx].active = state
  }

  fun setDeleteHandler(handler: (View, Inventory, Int) -> Unit) {
    deleteHandler = handler
  }

  fun setDisablingHandler(handler: (View, Inventory, Boolean, Int) -> Unit) {
    disablingHandler = handler
  }

  fun setOnClickListener(listener: (View, Inventory, Int) -> Unit) {
    clickHandler = listener
  }

  fun setCollaboratorsHandler(listener: (View, Inventory, Int) -> Unit) {
    collaboratorsHandler = listener
  }

  fun setAddCollaboratorClickListener(listener: (Inventory, Int) -> Unit) {
    clickAddCollaborator = listener
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val inventoryName: TextView = itemView.findViewById(R.id.entry_inventory_name)
    val creationDate: TextView = itemView.findViewById(R.id.entry_inventory_creation_date)

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    val statusSwitch: SwitchMaterial = itemView.findViewById(R.id.entry_inventory_status_switch)
    val collaborators: TextView = itemView.findViewById(R.id.entry_inventory_collaborators)
    val addCollaboratorButton: ImageButton =
      itemView.findViewById(R.id.entry_add_collaborator_button)
    val deleteInventoryButton: ImageButton =
      itemView.findViewById(R.id.entry_delete_inventory_button)
  }

  private val itemsFilter: Filter = object : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {

      val query = constraint?.toString() ?: ""

      val filteredInventories = if (query.isEmpty()) inventories else {
        inventories.filter {
          val values = mutableListOf(it.name)
          values.addAll(it.invitedUsers)
          values.any { value ->
            FuzzySearch.partialRatio(value, query) > 50
          }
        } as ArrayList<Inventory>
      }

      return FilterResults().apply { values = filteredInventories }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
      filteredInventories =
        if (results?.values == null) ArrayList() else results.values as ArrayList<Inventory>
      notifyDataSetChanged()
    }

  }

  override fun getFilter(): Filter {
    return itemsFilter
  }

}
