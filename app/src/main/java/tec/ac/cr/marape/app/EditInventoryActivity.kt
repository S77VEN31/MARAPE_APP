package tec.ac.cr.marape.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.model.Inventory
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class EditInventoryActivity : AppCompatActivity() {

  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)
  private val EDITED_INVENTORY = 2
  private lateinit var states: Array<String>
  private lateinit var inventory: Inventory
  private var position: Int = RecyclerView.NO_POSITION
  private lateinit var db: FirebaseFirestore

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_inventory)

    states = resources.getStringArray(R.array.create_inventory_states)
    db = FirebaseFirestore.getInstance()

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val inventoryName: TextView = findViewById(R.id.edit_inventory_name)

    val inventoryCreationDate: TextView = findViewById(R.id.edit_inventory_creation_date)

    // NOTE: I removed the status changer because there really is no reason for that to exist
    // other than to be annoying.

    // TODO: See if there's a better way of passing the position here
    position = intent.getIntExtra("position", RecyclerView.NO_POSITION)

    intent.getSerializableExtra("inventory", Inventory::class.java)?.let {
      inventory = it
    } ?: run {
      // WARNING: If for some reason the inventory is null the activity will just finish
      finish()
    }

    inventoryName.text = inventory.name
    inventoryCreationDate.text = formatter.format(Date(inventory.creationDate)).toString()

    inventoryName.addTextChangedListener {
      inventory.name = it.toString()
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

  fun saveInventory(view: View) {
    db.collection("inventories").document(inventory.id).set(inventory).addOnSuccessListener {
      val result = Intent()
      result.putExtra("edited", inventory)
      result.putExtra("position", position)
      setResult(EDITED_INVENTORY, result)
      finish()
    }.addOnFailureListener {
      Toast.makeText(this@EditInventoryActivity, it.message, Toast.LENGTH_LONG).show()
    }
  }
}