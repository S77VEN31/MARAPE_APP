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


  var inventoriesFull = inventories.toList()

  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)
  private lateinit var deleteHandler: (Inventory, Int) -> Unit

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView =
      LayoutInflater.from(parent.context).inflate(R.layout.layout_inventory_entry, parent, false)
    return ViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return inventories.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val currentInventory = inventories[position]
    holder.inventoryName.text = currentInventory.name
    holder.creationDate.text = formatter.format(Date(currentInventory.creationDate)).toString()
    holder.statusSwitch.isChecked = currentInventory.status.status == 1
    holder.collaborators.text = currentInventory.invitedUsers.size.toString()
    holder.deleteInventoryButton.setOnClickListener {
      deleteHandler(currentInventory, position)
    }
  }

  fun setDeleteHandler(handler: (Inventory, Int) -> Unit) {
    deleteHandler = handler
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
      val filteredList = ArrayList<Inventory>()

      if (constraint == null || constraint.length == 0) {
        filteredList.addAll(inventoriesFull)
      } else {
        val query = constraint.toString().lowercase().trim()
        for (item in inventoriesFull) {
          if (FuzzySearch.ratio(query, item.name) > 30) {
            filteredList.add(item)
          }
        }
      }

      val results = FilterResults()
      results.values = filteredList
      return results
    }

    private var first = true

    @SuppressLint("NotifyDataSetChanged")
    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
      inventories.clear()
      inventories.addAll(results?.values as List<Inventory>)
      notifyDataSetChanged()
    }

  }

  override fun getFilter(): Filter {
    return itemsFilter
  }

}
