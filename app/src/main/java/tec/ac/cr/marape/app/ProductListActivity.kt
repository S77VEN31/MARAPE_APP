package tec.ac.cr.marape.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tec.ac.cr.marape.app.adapter.ProductListAdapter
import tec.ac.cr.marape.app.databinding.ActivityProductListBinding
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.ui.dashboard.EDITED_INVENTORY


class ProductListActivity : AppCompatActivity() {

  private lateinit var inventory: Inventory
  private lateinit var db: FirebaseFirestore
  private lateinit var binding: ActivityProductListBinding
  private lateinit var productsAdapter: ProductListAdapter
  private lateinit var productList: List<Product>
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityProductListBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    db = FirebaseFirestore.getInstance()
    inventory = intent.getSerializableExtra("inventory")!! as Inventory

    lifecycleScope.launch {
      productList = inventory.items.mapNotNull { item ->
        if (item.isNotBlank()) {
          val productDocument = db.document("products/$item").get().await()
          productDocument?.toObject(Product::class.java)
        } else {
          null
        }
      }

      binding.floatingActionButtonCreateProduct.setOnClickListener(::createProduct)
      productsAdapter = ProductListAdapter(productList, inventory)
      binding.productList.adapter = productsAdapter
      binding.productList.layoutManager = LinearLayoutManager(this@ProductListActivity)
    }

    (this as MenuHost).addMenuProvider(object : MenuProvider {
      override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search_inventory, menu)
        val searchItem: MenuItem = menu.findItem(R.id.search_inventory)
        (searchItem.actionView as SearchView).setOnQueryTextListener(object :
          SearchView.OnQueryTextListener {

          override fun onQueryTextSubmit(query: String?): Boolean {
            return false
          }

          override fun onQueryTextChange(newText: String?): Boolean {
            productsAdapter.filter.filter(newText)
            return true
          }

        })
      }

      override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
      }

    }, this)


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

  private fun createProduct(view: View) {
    val intent = Intent(this, CreateProductActivity::class.java)
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