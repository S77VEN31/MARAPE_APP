package tec.ac.cr.marape.app.adapter

import android.util.Log
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
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User

class GuestView(private var guestList: MutableList<User>, var inventory: Inventory) :
  RecyclerView.Adapter<GuestView.GuestViewHolder>() {
  private val db = FirebaseFirestore.getInstance()


  inner class GuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val username: TextView = itemView.findViewById(R.id.entry_guest_name)
    val email: TextView = itemView.findViewById(R.id.entry_guest_email)
    val phone: TextView = itemView.findViewById(R.id.entry_guest_telephone)
    val country: TextView = itemView.findViewById(R.id.entry_guest_country)
    val deleteGuest: ImageButton = itemView.findViewById(R.id.entry_delete_guestList_button)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(
      R.layout.layout_guest_entry, parent, false
    )
    return GuestViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
    val db = FirebaseFirestore.getInstance()
    val currentGuest = guestList[position]
    holder.username.text = currentGuest.name
    holder.email.text = currentGuest.email

    if (currentGuest.phone.isNotEmpty()) {
      holder.phone.text = currentGuest.phone
    } else {
      holder.phone.text = "Sin número"
    }

    if (currentGuest.country.isNotEmpty()) {
      holder.country.text = currentGuest.country
    } else {
      holder.country.text = "Sin país"
    }

    holder.deleteGuest.setOnClickListener {
      sendMessage(holder, currentGuest)
    }
  }

  override fun getItemCount(): Int {
    return guestList.size
  }

  fun updateDataGuest(newList: MutableList<User>) {
    guestList = newList
    notifyDataSetChanged()
  }

  private fun removeDatabase(currentUser: User, onComplete: (Boolean) -> Unit) {
    db.collection("inventories").document(inventory.id)
      .update("invitedUsers", FieldValue.arrayRemove(currentUser.email)).addOnSuccessListener {
        if (guestList.contains(currentUser)) {
          guestList.remove(currentUser)
        }
        onComplete(true)
      }.addOnFailureListener {
        onComplete(false)
      }
  }

  private fun removeLocal(currentUser: User) {
    val mutableInvitedUsers = inventory.invitedUsers.toMutableList()
    val removeInvite = currentUser.email
    mutableInvitedUsers.remove(removeInvite)
    inventory.invitedUsers = mutableInvitedUsers
    Log.i("TAG", "Inventario actualizado: $inventory")
  }

  private fun sendMessage(holder: GuestViewHolder, currentUser: User) {
    removeDatabase(currentUser) { success ->
      if (success) {
        Toast.makeText(
          holder.itemView.context, "Usuario eliminado del inventario", Toast.LENGTH_SHORT
        ).show()
        removeLocal(currentUser)
        notifyDataSetChanged()
      } else {
        Toast.makeText(
          holder.itemView.context, "Error al eliminar el invitado", Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

}