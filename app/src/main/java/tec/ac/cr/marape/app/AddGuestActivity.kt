package tec.ac.cr.marape.app

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.adapter.UserView
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User

const val ADDED_GUEST_INVENTORY = 3

class AddGuestActivity : AppCompatActivity() {
  private val db = FirebaseFirestore.getInstance()
  private val currentUserEmail: String = FirebaseAuth.getInstance().currentUser?.email ?: ""
  private lateinit var recyclerView: RecyclerView
  private lateinit var searchUser: EditText
  private var position: Int = 0
  private lateinit var inventory: Inventory

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_guest)

    inventory = intent.getSerializableExtra("inventory") as Inventory
    position = intent.getIntExtra("position", 0)
    searchUser = findViewById(R.id.entry_user_search)
    recyclerView = findViewById(R.id.recycle_users)
    recyclerView.layoutManager = LinearLayoutManager(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    showUsers(inventory)
  }


  private fun getUsers(invitedUsers: List<String>, onComplete: (MutableList<User>) -> Unit) {
    val availableUsers = mutableListOf<User>()
    db.collection("users")
        .whereNotIn("email", invitedUsers)
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                val user = document.toObject(User::class.java)
                if(user.email != currentUserEmail){
                  availableUsers.add(user)
                }
            }
            onComplete(availableUsers)
        }
        .addOnFailureListener { exception ->
            Log.d(TAG, "Error getting documents", exception)
            onComplete(availableUsers)
        }
  }

  private fun showUsers(inventory: Inventory){
    getUsers(inventory.invitedUsers) { users ->
      val adapter = UserView(users, inventory)
      recyclerView.adapter = adapter

      searchUser.addTextChangedListener { searchText ->
        val query = searchText.toString().lowercase().trim();

        if (users.isNotEmpty()) {
          val filteredList = searchUser(users, query)
          adapter.updateData(filteredList)
        } else { showAlertDialog() }
      }
    }
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

  private fun searchUser(userList: MutableList<User>, query: String): MutableList<User> {
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



  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        val result = Intent()
        result.putExtra("addGuest", inventory)
        result.putExtra("position", position)
        setResult(ADDED_GUEST_INVENTORY, result)
        finish()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

}
