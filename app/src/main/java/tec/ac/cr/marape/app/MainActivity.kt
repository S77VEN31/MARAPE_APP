package tec.ac.cr.marape.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.databinding.ActivityMainBinding
import tec.ac.cr.marape.app.state.State

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private lateinit var db: FirebaseFirestore
  private lateinit var state: State

  @SuppressLint("NotifyDataSetChanged")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    db = FirebaseFirestore.getInstance()
    state = State.getInstance()


    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // TODO: find a better way of doing this, right now it works, but I want the loading of the user's
    // inventories to be done upon initialization of the application, not here.

    val navView: BottomNavigationView = binding.navView

    val navController = findNavController(R.id.nav_host_fragment_activity_main)
    val appBarConfiguration =
      AppBarConfiguration(
        setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_user_profile)
      )
    setupActionBarWithNavController(navController, appBarConfiguration)
    navView.setupWithNavController(navController)
  }
}
