// Create an activity to handle the details of an inventory

package tec.ac.cr.marape.app


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.databinding.ActivityInventoryDetailsBinding
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User
import tec.ac.cr.marape.app.state.State
import tec.ac.cr.marape.app.ui.dashboard.EDITED_INVENTORY
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class InventoryDetailsActivity : AppCompatActivity() {
  private lateinit var db: FirebaseFirestore
  private lateinit var owner: User
  private var inventory: Inventory? = null
  private var position = RecyclerView.NO_POSITION
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private lateinit var binding: ActivityInventoryDetailsBinding
  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)
  private lateinit var state: State

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    state = State.getInstance()
    binding = ActivityInventoryDetailsBinding.inflate(layoutInflater)
    launcher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::resultHandler)
    db = FirebaseFirestore.getInstance()
    setContentView(binding.root)

    // This is the back button
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    // TODO: The position will be used to update the inventory in the local sharedInventories array.

    loadInventoryData(intent)

    if (state.user.email.compareTo(inventory!!.ownerEmail) != 0) {
      binding.floatingActionButtonEditShared.visibility = View.GONE
    }
  }

  private fun loadInventoryData(intent: Intent) {
    inventory = intent.getSerializableExtra("inventory") as Inventory?
    position = intent.getIntExtra("position", RecyclerView.NO_POSITION)
    inventory?.let {
      binding.sharedInventoryName.text = it.name
      binding.sharedInventoryCreationDate.text = formatter.format(Date(it.creationDate))

      // TODO: Find a different way of doing this, somethig more idiomatic, or somethingdsds
      //  that uses the string resources.
      binding.sharedInventoryStatus.text = if (it.active) {
        "Activo"
      } else {
        "Inactivo"
      }

      db.document(
        "/users/${it.ownerEmail}"
      ).get().addOnSuccessListener { doc ->
        owner = doc.toObject(User::class.java)!!
        binding.sharedInventoryOwner.text = owner.name
      }
    }
  }

  fun listInvitedUsers(view: View) {
    // launch the activity to list the users
    val clazz: Class<*> = if (state.user.email.compareTo(inventory!!.ownerEmail) == 0) {
      GuestListActivity::class.java
    } else {
      InvitedUsersListGuestActivity::class.java
    }
    val intent = Intent(this, clazz)
    intent.putExtra("position", position)
    intent.putExtra("inventory", inventory)
    launcher.launch(intent)
  }

  fun editInventory(view: View) {
    val intent = Intent(this, EditInventoryActivity::class.java)
    intent.putExtra("position", position)
    intent.putExtra("inventory", inventory)
    launcher.launch(intent)
  }

  fun listProducts(view: View) {
    val intent = Intent(this, ProductListActivity::class.java)
    intent.putExtra("position", position)
    intent.putExtra("inventory", inventory)
    launcher.launch(intent)
  }

  private fun resultHandler(result: ActivityResult) {
    when (result.resultCode) {
      EDITED_INVENTORY -> {
        loadInventoryData(result.data!!)
        setResult(
          EDITED_INVENTORY, result.data
        )
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