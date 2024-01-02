package tec.ac.cr.marape.app

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
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
  private var position: Int = 0
  private lateinit var inventory: Inventory
  private lateinit var users: MutableList<User>
  private lateinit var adapter: UserView

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_guest)

    inventory = intent.getSerializableExtra("inventory") as Inventory
    position = intent.getIntExtra("position", 0)
    recyclerView = findViewById(R.id.recycle_users)
    recyclerView.layoutManager = LinearLayoutManager(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    (this as MenuHost).addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search_inventory, menu)
        val searchItem: MenuItem = menu.findItem(R.id.search_inventory)
        (searchItem.actionView as SearchView).setOnQueryTextListener(object :
          SearchView.OnQueryTextListener {
          override fun onQueryTextSubmit(query: String?): Boolean {
            return false
          }

          override fun onQueryTextChange(searchText: String?): Boolean {
            val query = searchText.toString().lowercase().trim()

            if (users.isNotEmpty()) {
              val filteredList = searchUser(users, query)
              adapter.updateData(filteredList)
            } else {
              showAlertDialog()
            }
            return true
          }

        })
      }

      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
      }

    }, this)

    showUsers(inventory)
  }


  private fun getUsers(invitedUsers: List<String>, onComplete: (MutableList<User>) -> Unit) {
    val availableUsers = mutableListOf<User>()

    val usersCollection = if (invitedUsers.isNotEmpty()) {
      db.collection("users").whereNotIn("email", invitedUsers)
    } else {
      db.collection("users")
    }

    usersCollection.get().addOnSuccessListener { result ->
      for (document in result) {
        val user = document.toObject(User::class.java)
        if (user.email != currentUserEmail) {
          availableUsers.add(user)
        }
      }
      onComplete(availableUsers)
    }.addOnFailureListener { exception ->
      Log.d(TAG, "Error getting documents", exception)
      onComplete(availableUsers)
    }
  }

  private fun showUsers(inventory: Inventory) {
    getUsers(inventory.invitedUsers) { users ->
      adapter = UserView(users, inventory)
      recyclerView.adapter = adapter
      this.users = users
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
        if (matches) {
          searchResults.add(user)
        }
      }
      searchResults
    } else {
      userList
    }
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
