package tec.ac.cr.marape.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import kotlin.collections.listOf
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.InventoryStatus
import tec.ac.cr.marape.app.model.User
import tec.ac.cr.marape.app.state.State

class CreateInventoryActivity : AppCompatActivity() {
  private lateinit var inventory: Inventory
  private lateinit var state: State
  private lateinit var db: FirebaseFirestore

  override fun onCreate(savedInstanceState: Bundle?) {
    state = State.getInstance(this.baseContext)
    db = FirebaseFirestore.getInstance()
    // XXX: This is testing code
    state.user = User("Aaron", "erizojuan33@gmail.com", "Something I guess")
    // XXX: END OF TESTING CODE
    inventory =
        Inventory(
            "",
            0, // NOTE: This value will be set upon creation
            InventoryStatus.INACTIVE,
            state.user.email,
            listOf()
        )
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create_inventory)
    inflateStateSpinner()

    val text: TextView = findViewById(R.id.create_inventory_name_input)
    text.addTextChangedListener(
        object : TextWatcher {
          override fun afterTextChanged(s: Editable?) {
            inventory.name = s.toString()
          }

          override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

          override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    )
  }

  public fun addProduct(view: View) {
    TODO("Implement functionality for adding products to the inventory")
  }

  public fun createInventory(view: View) {
    // TODO: validations, right now we won't be doing validations
    inventory.creationDate = Instant.now().toEpochMilli()
    db.collection("inventories")
        .document()
        .set(inventory)
        .addOnSuccessListener { _ ->
          Toast.makeText(
                  this@CreateInventoryActivity,
                  "Inventory created successfully",
                  Toast.LENGTH_SHORT
              )
              .show()
          finish()
        }
        .addOnFailureListener { ex ->
          Toast.makeText(
                  this@CreateInventoryActivity,
                  "An error occurred ${ex}",
                  Toast.LENGTH_SHORT
              )
              .show()
        }
  }

  fun inflateStateSpinner() {
    // TODO: extract these hard coded values into a string-array resource, the string-array
    // resource works by taking string resources from the string resource file and joining them into
    // a string-array resource in the arrays resource file.
    val states = listOf("Inactivo", "Activo")
    val stateSpinner: Spinner = findViewById(R.id.create_inventory_state_spinner)
    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    stateSpinner.adapter = adapter

    stateSpinner.onItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
          override fun onItemSelected(
              parent: AdapterView<*>?,
              view: View?,
              position: Int,
              id: Long
          ) {
            inventory.status = InventoryStatus.fromInt(position)
          }

          override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Implement this, honestly I don't know what this does lmao 💀")
          }
        }
  }
}
