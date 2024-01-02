package tec.ac.cr.marape.app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import me.xdrop.fuzzywuzzy.FuzzySearch
import tec.ac.cr.marape.app.adapter.GuestView
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User

const val DELETE_GUEST_INVENTORY = 4

class GuestListActivity : AppCompatActivity() {
  private val db = FirebaseFirestore.getInstance()
  private lateinit var recyclerView: RecyclerView
  private var position: Int = 0
  private lateinit var inventory: Inventory
  private lateinit var guests: MutableList<User>
  private lateinit var adapter: GuestView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_guest_list)

    inventory = intent.getSerializableExtra("inventory") as Inventory
    position = intent.getIntExtra("position", 0)
    // searchGuest = findViewById(R.id.entry_guest_search)
    recyclerView = findViewById(R.id.recycle_guests)
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

            if (guests.isNotEmpty()) {
              val filteredList = fuzzySearchGuest(guests, query)
              adapter.updateDataGuest(filteredList)
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

    showGuests(inventory)
  }

  private fun showGuests(inventory: Inventory) {
    getGuests(inventory.invitedUsers) { guests ->
      adapter = GuestView(guests, inventory)
      recyclerView.adapter = adapter
      this.guests = guests
    }
  }

  private fun getGuests(invitedUsers: List<String>, onComplete: (MutableList<User>) -> Unit) {
    val availableGuests = mutableListOf<User>()
    if (invitedUsers.isNotEmpty()) {
      db.collection("users").whereIn("email", invitedUsers).get().addOnSuccessListener { result ->
        for (document in result) {
          val user = document.toObject(User::class.java)
          availableGuests.add(user)
        }
        onComplete(availableGuests)
      }.addOnFailureListener { exception ->
        Log.d(TAG, "Error getting documents", exception)
        onComplete(availableGuests)
      }
    } else {
      onComplete(availableGuests)
    }

  }

  private fun showAlertDialog() {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Sin invitados")
      .setMessage("No ha invitado a ningún usuario a este inventario")
      .setPositiveButton("Aceptar") { dialog, _ ->
        dialog.dismiss()
      }
    val dialog = builder.create()
    dialog.show()
  }

  private fun fuzzySearchGuest(guestList: MutableList<User>, query: String): MutableList<User> {
    return if (query.isNotEmpty()) {
      val newList = mutableListOf<User>()

      for (user in guestList) {
        val userFields = listOf(user.name, user.email)

        val matches = userFields.any { field ->
          FuzzySearch.ratio(
            query.lowercase(), field.lowercase()
          ) >= 80
        }

        if (matches) {
          newList.add(user)
        }

      }
      newList
    } else {
      guestList
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        val result = Intent()
        result.putExtra("addGuest", inventory)
        result.putExtra("position", position)
        setResult(DELETE_GUEST_INVENTORY, result)
        finish()
        true
      }

      else -> super.onOptionsItemSelected(item)
    }
  }


}