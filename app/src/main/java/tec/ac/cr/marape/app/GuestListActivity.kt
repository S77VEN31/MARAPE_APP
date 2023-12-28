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
  private lateinit var guestList: MutableList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_guest_list)

      //val inventory = intent.getStringExtra("idInventory")
      val searchGuest: EditText = findViewById(R.id.entry_guest_search)
      val recyclerView: RecyclerView = findViewById(R.id.recycle_guests)
      recyclerView.layoutManager = LinearLayoutManager(this)

      val inventory = "2y4HukQgHtV8UBFuKGML"
      if(inventory != null){
        guestList = getGuests(inventory)
        val adapter = GuestView(guestList, inventory)
        recyclerView.adapter = adapter

        /*if(guestList.isEmpty()){
          showAlertDialog()
        }else{
          recyclerView.adapter = adapter
        }*/

        searchGuest.addTextChangedListener { searchText ->
          val query = searchText.toString().lowercase().trim()
          println(query)

          /*if(guestList.isNotEmpty()){
            val filteredList = fuzzySearchGuest(guestList, query)
            adapter.updateDataGuest(filteredList)
          }else{
            showAlertDialog()
          }*/
        }
      }
    }


  private fun getGuests(idInventory: String): MutableList<User>{
    val db = FirebaseFirestore.getInstance()
    val userList = mutableListOf<User>()

    db.collection("inventories").document(idInventory).get().addOnSuccessListener {
      inventoryDoc -> val invitedUsers = inventoryDoc.toObject(Inventory::class.java)?.invitedUsers

      Log.i(TAG, "USUARIOS INVITADOS "+ invitedUsers)
      if(invitedUsers != null){
        db.collection("users").get().addOnSuccessListener { result ->
          for(document in result){
            val user = document.toObject(User::class.java)
            if(user.email in invitedUsers){
              userList.add(user)
            }
          }
        }.addOnFailureListener {exception ->
          Log.d(TAG, "Error getting documents", exception)
        }
      }else{
        Log.d(TAG, "No invited users found")
      }
    }
    return userList
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