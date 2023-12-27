package tec.ac.cr.marape.app

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.adapter.UserView
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User

class AddGuestActivity : AppCompatActivity() {
  private val currentUserEmail: String = FirebaseAuth.getInstance().currentUser?.email ?: ""
  private lateinit var userList: MutableList<User>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_guest)

    val inventory = intent.getStringExtra("idInventory")
    val searchUser: EditText = findViewById(R.id.entry_user_search);
    val recyclerView: RecyclerView = findViewById(R.id.recycle_users);
    recyclerView.layoutManager = LinearLayoutManager(this);

    if(inventory != null){
      userList = getUsers(inventory);
      val adapter = UserView(userList, inventory)

      if(userList.isEmpty()){
        showAlertDialog()
      }else{
        recyclerView.adapter = adapter
      }

      searchUser.addTextChangedListener { searchText ->
        val query = searchText.toString().lowercase().trim();

        if(userList.isNotEmpty()){
          val filteredList = performFuzzySearch(userList, query)
          adapter.updateData(filteredList)
        }else{
          showAlertDialog()
        }
      }
    }
  }

  private fun getUsers(idInventory: String): MutableList<User>{
    val db = FirebaseFirestore.getInstance();
    val userAvailableList = mutableListOf<User>();

    db.collection("inventories").document(idInventory).get().addOnSuccessListener { inventoryDoc ->
      val invitedUsers = inventoryDoc.toObject(Inventory::class.java)?.invitedUsers

      if(invitedUsers != null){
        db.collection("users").get().addOnSuccessListener { result ->
          for (document in result) {
            val user = document.toObject(User::class.java)

            if (user.email != currentUserEmail && user.email !in invitedUsers) {
              userAvailableList.add(user)
            }
          }
        }.addOnFailureListener { exception ->
          Log.d(TAG, "Error getting documents", exception)
        }
      }else{
        Log.d(TAG, "No invited users found")
      }

    }

    return userAvailableList;
  }

  private fun showAlertDialog() {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Invitaciones Completas")
      .setMessage("No hay más usuarios para invitar en este inventario")
      .setPositiveButton("Aceptar") { dialog, _ ->
        dialog.dismiss() // close
      }
    val dialog = builder.create()
    dialog.show()
  }

  private fun performFuzzySearch(userList: MutableList<User>, query: String): MutableList<User> {
    return if (query.isNotEmpty()) {
      val searchResults = mutableListOf<User>()

      for (user in userList) {
        val userFields = listOf(user.name, user.email, user.phone, user.country)

        // Checks if any of the strings contain the query
        val matches = userFields.any { field ->
          field.lowercase().contains(query.lowercase())
        }

        if (matches) { searchResults.add(user) }
      }

      searchResults
    } else { userList }
  }
}
