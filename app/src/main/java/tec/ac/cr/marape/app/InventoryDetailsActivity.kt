// Create an activity to handle the details of an inventory

package tec.ac.cr.marape.app

import android.os.Build
import android.os.Bundle
import android.view.MenuItem


import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.databinding.ActivityInventoryDetailsBinding
import android.content.Intent
import android.view.View


import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User


import java.text.DateFormat
import java.util.Date
import java.util.Locale

class InventoryDetailsActivity : AppCompatActivity() {
  private lateinit var db: FirebaseFirestore
  private lateinit var owner: User
  private lateinit var inventory: Inventory
  private lateinit var binding: ActivityInventoryDetailsBinding
  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityInventoryDetailsBinding.inflate(layoutInflater)
    db = FirebaseFirestore.getInstance()
    setContentView(binding.root)

    // This is the back button
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    // TODO: The position will be used to update the inventory in the local sharedInventories array.
    intent.getIntExtra("position", RecyclerView.NO_POSITION)
    intent.getSerializableExtra("inventory", Inventory::class.java)?.let {
      inventory = it

      binding.sharedInventoryName.text = inventory.name
      binding.sharedInventoryCreationDate.text = formatter.format(Date(inventory.creationDate))

      // TODO: Find a different way of doing this, somethig more idiomatic, or somethingdsds
      // that uses the string resources.
      binding.sharedInventoryStatus.text = if (inventory.active) {
        "Activo"
      } else {
        "Inactivo"
      }

      db.document(
        "/users/${inventory.ownerEmail}"
      ).get().addOnSuccessListener { doc ->
        owner = doc.toObject(User::class.java)!!
        binding.sharedInventoryOwner.text = owner.name
      }
    }
  }

fun listInvitedUsers(view: View) {
  // launch the activity to list the users
  val intent = Intent(this, InvitedUsersListActivity::class.java)
  intent.putExtra("inventory", inventory)
  startActivity(intent)

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