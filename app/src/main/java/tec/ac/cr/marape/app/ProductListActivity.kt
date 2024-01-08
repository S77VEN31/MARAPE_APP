package tec.ac.cr.marape.app

import androidx.appcompat.app.AppCompatActivity
import tec.ac.cr.marape.app.databinding.ActivityProductListBinding
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.adapter.ProductListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tec.ac.cr.marape.app.model.Inventory


class ProductListActivity : AppCompatActivity() {

  private lateinit var inventory: Inventory
  private lateinit var db: FirebaseFirestore
  private lateinit var binding: ActivityProductListBinding
  private lateinit var productList: List<Product>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        db = FirebaseFirestore.getInstance()
        inventory = intent.getSerializableExtra("inventory")!! as Inventory

        lifecycleScope.launch {
          productList = inventory.items.mapNotNull { invitedUser ->
            if (invitedUser.isNotBlank()) {
              val productDocument = db.document("products/$invitedUser").get().await()
              productDocument?.toObject(Product::class.java)
            } else {
              null // Si invitedUser es nulo o vacío, devolveremos null
            }
          } ?: listOf() // Si inventory es nulo, asignamos una lista vacía

          binding.productList.adapter = ProductListAdapter(productList ?: listOf(), inventory)
          binding.productList.layoutManager = LinearLayoutManager(this@ProductListActivity)
      }


    }

  /*

        lifecycleScope.launch {
          productList = inventory.items.map { invitedUser ->
            db.document("products/${invitedUser}").get().await().toObject(Product::class.java)!!
          }
          binding.productList.adapter = ProductListAdapter(productList, inventory)
          binding.productList.layoutManager = LinearLayoutManager(this@ProductListActivity)
        }
   */

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