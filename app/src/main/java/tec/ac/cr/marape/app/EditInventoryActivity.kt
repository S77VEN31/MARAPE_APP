package tec.ac.cr.marape.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.databinding.ActivityEditInventoryBinding
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.ui.dashboard.EDITED_INVENTORY
import java.text.DateFormat
import java.util.Date
import java.util.Locale


class EditInventoryActivity : AppCompatActivity() {

  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)
  private lateinit var states: Array<String>
  private lateinit var inventory: Inventory
  private var position: Int = RecyclerView.NO_POSITION
  private lateinit var db: FirebaseFirestore
  private lateinit var binding: ActivityEditInventoryBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityEditInventoryBinding.inflate(layoutInflater)
    setContentView(binding.root)

    states = resources.getStringArray(R.array.create_inventory_states)
    db = FirebaseFirestore.getInstance()

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    // NOTE: I removed the status changer because there really is no reason for that to exist
    // other than to be annoying.

    // TODO: See if there's a better way of passing the position here
    position = intent.getIntExtra("position", RecyclerView.NO_POSITION)

    inventory = intent.getSerializableExtra("inventory") as Inventory

    binding.editInventoryName.setText(inventory.name)
    binding.editInventoryCreationDate.setText(
      formatter.format(Date(inventory.creationDate)).toString()
    )
    binding.editInventoryName.addTextChangedListener {
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
      result.putExtra("inventory", inventory)
      result.putExtra("position", position)
      setResult(EDITED_INVENTORY, result)
      finish()
    }.addOnFailureListener {
      Toast.makeText(this@EditInventoryActivity, it.message, Toast.LENGTH_LONG).show()
    }
  }
}