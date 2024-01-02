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

class UserView(private var userList:MutableList<User>, var inventory: Inventory): RecyclerView.Adapter<UserView.UserViewHolder>() {
  private val db = FirebaseFirestore.getInstance()

  inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    val username:TextView = itemView.findViewById(R.id.entry_user_name)
    val email:TextView = itemView.findViewById(R.id.entry_user_email)
    val phone:TextView = itemView.findViewById(R.id.entry_user_telephone)
    val country:TextView = itemView.findViewById(R.id.entry_user_country)
    val addUser:ImageButton = itemView.findViewById(R.id.add_user)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  UserViewHolder{
    val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_user_entry, parent, false)
    return UserViewHolder(view)
  }


  override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
    val currentUser = userList[position]
    holder.username.text = currentUser.name
    holder.email.text = currentUser.email

    if(currentUser.phone.isNotEmpty()){
      holder.phone.text = currentUser.phone
    }else{
      holder.phone.text = "Sin número"
    }

    if(currentUser.country.isNotEmpty()){
      holder.country.text = currentUser.country;
    }else{
      holder.country.text = "Sin país"
    }

    holder.addUser.setOnClickListener {
      sendMessage(holder, currentUser)
    }

  }

  private fun addDatabase(currentUser: User, onComplete: (Boolean) -> Unit) {
    db.collection("inventories").document(inventory.id)
      .update("invitedUsers", FieldValue.arrayUnion(currentUser.email))
      .addOnSuccessListener {
        if (userList.contains(currentUser)) {
          userList.remove(currentUser)
        }
        onComplete(true)
      }
      .addOnFailureListener {
        onComplete(false)
      }
  }

  private fun addLocal(currentUser: User){
    val mutableInvitedUsers = inventory.invitedUsers.toMutableList()
    val newInvitee = currentUser.email
    mutableInvitedUsers.add(newInvitee)
    inventory.invitedUsers = mutableInvitedUsers.toList()
  }

  private fun sendMessage(holder: UserViewHolder, currentUser: User){
    addDatabase(currentUser) { success ->
      if (success) {
        Toast.makeText(holder.itemView.context, "Usuario agregado al inventario",
          Toast.LENGTH_SHORT).show()
        addLocal(currentUser)
        notifyDataSetChanged()
      } else {
        Toast.makeText(holder.itemView.context, "Error al agregar usuario al inventario",
          Toast.LENGTH_SHORT).show()
      }
    }
  }

  override fun getItemCount(): Int {
    return userList.size
  }

  fun updateData(newList: MutableList<User>){
    userList = newList
    notifyDataSetChanged()
  }
}
