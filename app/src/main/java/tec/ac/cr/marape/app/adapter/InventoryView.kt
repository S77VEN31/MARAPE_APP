package tec.ac.cr.marape.app.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.xdrop.fuzzywuzzy.FuzzySearch
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.Inventory
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class InventoryView(var inventories: ArrayList<Inventory>) :
  RecyclerView.Adapter<InventoryView.ViewHolder>(), Filterable {


  var filteredInventories = ArrayList(inventories)

  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)

  private lateinit var deleteHandler: (View, Inventory, Int) -> Unit
  private lateinit var disablingHandler: (View, Inventory, Boolean, Int) -> Unit
  private lateinit var clickHandler: (View, Inventory, Int) -> Unit

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
    holder.inventoryName.text = currentInventory.name
    holder.creationDate.text = formatter.format(Date(currentInventory.creationDate)).toString()
    holder.statusSwitch.isChecked = currentInventory.active
    holder.collaborators.text = currentInventory.invitedUsers.size.toString()
    holder.deleteInventoryButton.setOnClickListener {
      deleteHandler(it, currentInventory, position)
    }

    holder.statusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
      disablingHandler(buttonView, currentInventory, isChecked, position)
    }
    holder.itemView.setOnClickListener {
      clickHandler(it, currentInventory, position)
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
      inventories.removeAt(idx)
    }

    filteredInventories.removeAt(position)
    notifyItemRemoved(position)
  }

  fun add(inventory: Inventory) {
    inventories.add(0, inventory)
    filteredInventories.add(0, inventory)
    notifyItemInserted(0)
  }

  // TODO: Make this function also work for all cases, right now if I update an inventory while in
  //  search mode it won't update the correct one
  fun update(position: Int, inventory: Inventory) {
    val idx = inventories.indexOfFirst {
      it.id.compareTo(inventory.id) == 0
    }
    // These are NOT the same indices, because the user could update
    // an inventory in search mode, and that inventory will have a different index
    // in either list, so it's best to not use the same index, the position can be used
    // to update the filteredInventories array because that's the one that's being shown
    // on screen.
    inventories[idx] = inventory
    filteredInventories[position] = inventory
    notifyItemChanged(position)
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

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val inventoryName: TextView = itemView.findViewById(R.id.entry_inventory_name)
    val creationDate: TextView = itemView.findViewById(R.id.entry_inventory_creation_date)

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    val statusSwitch: Switch = itemView.findViewById(R.id.entry_inventory_status_switch)
    val collaborators: TextView = itemView.findViewById(R.id.entry_inventory_collaborators)
    val addCollaboratorButton: ImageButton =
      itemView.findViewById(R.id.entry_add_collaborator_button)
    val deleteInventoryButton: ImageButton =
      itemView.findViewById(R.id.entry_delete_inventory_button)
  }

  private val itemsFilter: Filter = object : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {

      val query = constraint?.toString() ?: ""

      filteredInventories = if (query.isEmpty()) inventories else {
        inventories.filter {
          FuzzySearch.ratio(query, it.name) > 20
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
