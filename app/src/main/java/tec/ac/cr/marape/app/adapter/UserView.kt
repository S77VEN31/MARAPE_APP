package tec.ac.cr.marape.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.User

class UserView(private val userList:List<User>): RecyclerView.Adapter<UserView.UserViewHolder>() {

  inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    val username:TextView = itemView.findViewById(R.id.entry_user_name);
    val email:TextView = itemView.findViewById(R.id.entry_user_email);
    val phone:TextView = itemView.findViewById(R.id.entry_user_telephone);
    val country:TextView = itemView.findViewById(R.id.entry_user_country);
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_user_entry,
      parent, false);
    return UserViewHolder(itemView);
  }

  override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
    val currentUser = userList[position];
    holder.username.text = currentUser.name;
    holder.email.text = currentUser.email;

    if(currentUser.phone.isNotEmpty()){
      holder.phone.text = currentUser.phone;
    }else{
      holder.phone.text = "Sin número";
    }

    if(currentUser.country.isNotEmpty()){
      holder.country.text = currentUser.country;
    }else{
      holder.country.text = "Sin país";
    }
  }

  override fun getItemCount(): Int {
    return userList.size;
  }

}
