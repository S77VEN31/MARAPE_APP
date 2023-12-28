package tec.ac.cr.marape.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.Inventory
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.KFunction3

class SharedInventoryView(var inventories: ArrayList<Inventory>) :
  RecyclerView.Adapter<SharedInventoryView.ViewHolder>() {

  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)

  private lateinit var toggleHandler: (Inventory, Boolean, Int) -> Unit
  private lateinit var detailButtonHandler: (Inventory, Int) -> Unit

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView =
      LayoutInflater.from(parent.context).inflate(R.layout.layout_shared_inventory_entry, parent, false)
    return ViewHolder(itemView)
  }

  override fun getItemCount(): Int = inventories.size

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val currentInventory = inventories[position]
    holder.inventoryName.text = currentInventory.name
    holder.creationDate.text = formatter.format(Date(currentInventory.creationDate)).toString()
    holder.statusSwitch.isChecked = currentInventory.active

    holder.statusSwitch.setOnCheckedChangeListener { _, isChecked ->
      toggleHandler(currentInventory, isChecked, position)
    }

    holder.detailsButton.setOnClickListener {
      detailButtonHandler(currentInventory, position)
    }
  }

  fun toggle (position: Int, inventory: Inventory, isChecked: Boolean) {
    inventories[position].active = isChecked
    // Todo: Discuss if we should notify the change or not
  }

  fun setToggleHandler(handler: (Inventory, Boolean, Int) -> Unit) {
    toggleHandler = handler
  }

  fun setDetailButtonHandler(handler: (Inventory, Int) -> Unit)  {
    detailButtonHandler = handler
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val inventoryName: TextView = itemView.findViewById(R.id.entry_inventory_name)
    val creationDate: TextView = itemView.findViewById(R.id.entry_inventory_creation_date)
    val statusSwitch: Switch = itemView.findViewById(R.id.entry_inventory_status_switch)
    val detailsButton: Button = itemView.findViewById(R.id.entry_inventory_details_button)
  }
}
