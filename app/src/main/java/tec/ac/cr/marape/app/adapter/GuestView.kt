package tec.ac.cr.marape.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.User

class GuestView (private var guestList:MutableList<User>, private var idInventory: String):
  RecyclerView.Adapter<GuestView.GuestViewHolder>() {

  inner class GuestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    val username: TextView = itemView.findViewById(R.id.entry_guest_name)
    val email: TextView = itemView.findViewById(R.id.entry_guest_email)
    val phone: TextView = itemView.findViewById(R.id.entry_guest_telephone)
    val country: TextView = itemView.findViewById(R.id.entry_guest_country)
    val deleteGuest: ImageButton = itemView.findViewById(R.id.entry_delete_guestList_button)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_guest_entry,
      parent, false)
    return GuestViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
    val currentGuest = guestList[position]
    holder.username.text = currentGuest.name
    holder.email.text = currentGuest.email

    if(currentGuest.phone.isNotEmpty()){
      holder.phone.text = currentGuest.phone
    }else{
      holder.phone.text = "Sin número"
    }

    if(currentGuest.country.isNotEmpty()){
      holder.country.text = currentGuest.country
    }else{
      holder.country.text = "Sin país"
    }
    
  }

  override fun getItemCount(): Int {
    return guestList.size
  }

}