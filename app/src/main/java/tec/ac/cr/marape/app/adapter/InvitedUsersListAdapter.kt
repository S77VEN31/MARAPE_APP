package tec.ac.cr.marape.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tec.ac.cr.marape.app.model.User
import tec.ac.cr.marape.app.R

class InvitedUsersListAdapter(val invitedUsers: List<User>) : RecyclerView.Adapter<InvitedUsersListAdapter.ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_invited_user_item, parent, false)
    return ViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return invitedUsers.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val currentInvitedUser = invitedUsers[position]
    holder.invitedUserEmail.text = currentInvitedUser.email
    holder.invitedUserName.text = currentInvitedUser.name
  }

  class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    val invitedUserEmail: android.widget.TextView = itemView.findViewById(R.id.guest_user_email)
    val invitedUserName: android.widget.TextView = itemView.findViewById(R.id.guest_user_name)


  }
}
