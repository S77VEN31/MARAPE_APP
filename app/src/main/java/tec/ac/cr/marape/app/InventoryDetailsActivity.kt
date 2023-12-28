// Create an activity to handle the details of an inventory

package tec.ac.cr.marape.app

import android.content.Intent
import android.os.Build
import androidx.core.widget.addTextChangedListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView
import androidx.annotation.RequiresApi
import tec.ac.cr.marape.app.model.Inventory

class InventoryDetailsActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var inventory: Inventory

    
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_details)

        // This is the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()
        val inventoryName: TextView = findViewById(R.id.shared_inventory_name)
        val inventoryCreationDate: TextView = findViewById(R.id.shared_inventory_creation_date)
        val inventoryStatus: TextView = findViewById(R.id.shared_inventory_status)
        val inventoryOwner: TextView = findViewById(R.id.shared_inventory_owner)



        intent.getSerializableExtra("inventory", Inventory::class.java)?.let {
            inventory = it
          } ?: run {
            // WARNING: If for some reason the inventory is null the activity will just finish
            finish()
          }


        inventoryName.text = inventory.name
    }
}