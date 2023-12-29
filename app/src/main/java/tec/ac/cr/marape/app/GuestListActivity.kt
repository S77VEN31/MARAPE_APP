package tec.ac.cr.marape.app

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.adapter.GuestView
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User
import me.xdrop.fuzzywuzzy.FuzzySearch

class GuestListActivity : AppCompatActivity() {
  private val db = FirebaseFirestore.getInstance()
  private lateinit var searchGuest: EditText
  private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_guest_list)

      val inventory: Inventory? = intent.getSerializableExtra("inventory") as? Inventory
      searchGuest = findViewById(R.id.entry_guest_search)
      recyclerView = findViewById(R.id.recycle_guests)
      recyclerView.layoutManager = LinearLayoutManager(this)


      Log.i("TAG", "Inventario actual: $inventory")
      inventory?.let {
        showGuests(inventory)
      }
    }

  private fun showGuests(inventory: Inventory){
    getGuests(inventory.invitedUsers) { guests ->
      val adapter = GuestView(guests, inventory)
      recyclerView.adapter = adapter

      searchGuest.addTextChangedListener { searchText ->
        val query = searchText.toString().lowercase().trim()

        if(guests.isNotEmpty()){
          val filteredList = fuzzySearchGuest(guests, query)
          adapter.updateDataGuest(filteredList)
        }else{
          showAlertDialog()
        }
      }
    }
  }

  private fun getGuests(invitedUsers: List<String>, onComplete: (MutableList<User>) -> Unit) {
    val availableGuests = mutableListOf<User>()
    db.collection("users")
      .whereIn("email", invitedUsers)
      .get()
      .addOnSuccessListener { result ->
        for (document in result) {
          val user = document.toObject(User::class.java)
          availableGuests.add(user)
        }
        onComplete(availableGuests)
      }
      .addOnFailureListener { exception ->
        Log.d(TAG, "Error getting documents", exception)
        onComplete(availableGuests)
      }
  }

  private fun showAlertDialog(){
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Sin invitados").setMessage("No ha invitado a ningún usuario a este inventario")
      .setPositiveButton("Aceptar") {
        dialog, _ -> dialog.dismiss()
      }
    val dialog = builder.create()
    dialog.show()
  }

  private fun fuzzySearchGuest(guestList: MutableList<User>, query: String): MutableList<User>{
    return if(query.isNotEmpty()) {
      val newList = mutableListOf<User>()

      for (user in guestList){
        val userFields = listOf(user.name, user.email)

        val matches = userFields.any { field -> FuzzySearch.ratio(query.lowercase(),
          field.lowercase()) >= 80 }

        if(matches){
          newList.add(user)
        }

      }
      newList
    }else{
       guestList
    }
  }
}