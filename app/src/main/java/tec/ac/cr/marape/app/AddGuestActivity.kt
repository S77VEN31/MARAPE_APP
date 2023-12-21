package tec.ac.cr.marape.app

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.adapter.UserView
import tec.ac.cr.marape.app.model.User

class AddGuestActivity : AppCompatActivity() {
  private val currentUserEmail: String = FirebaseAuth.getInstance().currentUser?.email ?: ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_guest)

    val inventory = intent.getStringExtra("idInventory")
    val recyclerView: RecyclerView = findViewById(R.id.recycle_users);
    recyclerView.layoutManager = LinearLayoutManager(this)

    if (inventory != null) {
      val userList = getUsers(inventory);

      val adapter = UserView(userList, inventory);
      recyclerView.adapter = adapter;
    }
  }

  private fun getUsers(idInventory: String): MutableList<User>{
    val db = FirebaseFirestore.getInstance();
    val userAvailableList = mutableListOf<User>();

    db.collection("users").get().addOnSuccessListener { result ->
      for (document in result) {
        val user = document.toObject(User::class.java)

        if (user.email != currentUserEmail) {
          userAvailableList.add(user)
        }
      }
    }.addOnFailureListener { exception ->
      Log.d(TAG, "Error getting documents", exception)
    }

    return userAvailableList;
  }

}
