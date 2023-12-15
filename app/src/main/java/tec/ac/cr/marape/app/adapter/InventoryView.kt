package tec.ac.cr.marape.app.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.Inventory
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale


class InventoryView(private var inventories: ArrayList<Inventory>) :
  RecyclerView.Adapter<InventoryView.ViewHolder>() {

  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_inventory_entry, parent, false)
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
    holder.collaborators.text = currentInventory.invitedUsers?.size.toString()
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
}
