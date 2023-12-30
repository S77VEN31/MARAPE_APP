package tec.ac.cr.marape.app

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tec.ac.cr.marape.app.databinding.ActivityInvitedUsersListBinding
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.User

class InvitedUsersListActivity : AppCompatActivity() {

  private lateinit var inventory: Inventory
  private lateinit var db: FirebaseFirestore
  private lateinit var binding: ActivityInvitedUsersListBinding
  private lateinit var invitedUsers: List<User>

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    inventory = intent.getSerializableExtra("inventory") as Inventory
    db = FirebaseFirestore.getInstance()
    lifecycleScope.launch {
      invitedUsers = inventory.invitedUsers.map { invitedUser ->
        db.document("users/${invitedUser}").get().await().toObject(User::class.java) as User
      }
      Log.d("InvitedUsersListActivity", "invitedUsers: ${invitedUsers}")
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