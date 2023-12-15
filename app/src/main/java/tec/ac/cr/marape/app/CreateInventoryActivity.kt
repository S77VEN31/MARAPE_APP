package tec.ac.cr.marape.app

import android.annotation.SuppressLint
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
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.InventoryStatus
import tec.ac.cr.marape.app.state.State
import java.time.Instant

class CreateInventoryActivity : AppCompatActivity() {
  private lateinit var inventory: Inventory
  private lateinit var state: State
  private lateinit var db: FirebaseFirestore

  private lateinit var states: Array<String>
  private lateinit var text: TextView
  private lateinit var emptyNameError: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    state = State.getInstance(this.baseContext)
    db = FirebaseFirestore.getInstance()
    states = resources.getStringArray(R.array.create_inventory_states)
    emptyNameError = resources.getString(R.string.create_product_empty_name_error)

    inventory = Inventory()
    inventory.ownerEmail = state.user.email

    setContentView(R.layout.activity_create_inventory)
    inflateStateSpinner()
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    text = findViewById(R.id.create_inventory_name_input)
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

  fun addProduct(view: View) {
    TODO("Implement functionality for adding products to the inventory")
  }

  @SuppressLint("NewApi")
  fun createInventory(view: View) {
    if (inventory.name.isEmpty()) {
      text.error = emptyNameError
      return
    }
    inventory.creationDate = Instant.now().toEpochMilli()
    val inventoryDoc = db.collection("inventories")
      .document()
    inventory.id = inventoryDoc.id
    inventoryDoc.set(inventory)
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
