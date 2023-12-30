package tec.ac.cr.marape.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tec.ac.cr.marape.app.adapter.InvitedUsersListAdapter
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
    binding = ActivityInvitedUsersListBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    db = FirebaseFirestore.getInstance()
    inventory = intent.getSerializableExtra("inventory")!! as Inventory
    lifecycleScope.launch {
      invitedUsers = inventory.invitedUsers.map { invitedUser ->
        db.document("users/${invitedUser}").get().await().toObject(User::class.java)!!
      }
      binding.invitedUsersList.adapter = InvitedUsersListAdapter(invitedUsers)
      binding.invitedUsersList.layoutManager = LinearLayoutManager(this@InvitedUsersListActivity)
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