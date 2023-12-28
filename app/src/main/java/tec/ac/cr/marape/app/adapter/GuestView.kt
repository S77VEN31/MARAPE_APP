package tec.ac.cr.marape.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
    val db = FirebaseFirestore.getInstance()
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

    holder.deleteGuest.setOnClickListener{
      db.collection("inventories").document(idInventory).update("invitedUsers",
        FieldValue.arrayRemove(currentGuest.email)).addOnSuccessListener{
          Toast.makeText(holder.itemView.context, "Usuario eliminado del inventario",
            Toast.LENGTH_SHORT).show()

          if(guestList.contains(currentGuest)){
            guestList.remove(currentGuest)
          }
          notifyDataSetChanged()
      }.addOnFailureListener {
        Toast.makeText(holder.itemView.context, "Error al eliminar el invitado",
          Toast.LENGTH_SHORT).show()
      }
    }

  }

  override fun getItemCount(): Int {
    return guestList.size
  }

  fun updateDataGuest(newList: MutableList<User>){
    guestList = newList
    notifyDataSetChanged()
  }

}