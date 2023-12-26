package tec.ac.cr.marape.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.state.State
import java.io.Serializable
import java.time.Instant

const val CREATED_INVENTORY = 1
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
    // This could honestly be a bit better tbh
    inventory.ownerEmail = state.user.email

    setContentView(R.layout.activity_create_inventory)
    inflateStateSpinner()

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    text = findViewById(R.id.create_inventory_name)
    text.addTextChangedListener {
      inventory.name = it.toString()
    }
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
        val result = Intent()
        result.putExtra("created", inventory as Serializable)
        setResult(CREATED_INVENTORY, result)
        finish()
      }
      .addOnFailureListener { ex ->
        Toast.makeText(
          this@CreateInventoryActivity,
          ex.message,
          Toast.LENGTH_SHORT
        )
          .show()
      }
  }

  private fun inflateStateSpinner() {
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
          inventory.active = position == 1
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
          // TODO("Implement this, honestly I don't know what this does lmao 💀")
        }
      }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        finish()
        true
      }

      else -> super.onOptionsItemSelected(item)
    }
  }
}
