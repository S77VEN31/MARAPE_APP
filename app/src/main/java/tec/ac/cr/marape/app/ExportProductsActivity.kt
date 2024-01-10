package tec.ac.cr.marape.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tec.ac.cr.marape.app.adapter.ExportProductAdapter
import tec.ac.cr.marape.app.databinding.ActivityExportProductsBinding
import tec.ac.cr.marape.app.model.Product

class ExportProductsActivity : AppCompatActivity() {
  private lateinit var db: FirebaseFirestore
  private lateinit var binding: ActivityExportProductsBinding
  private lateinit var productsAdapter: ExportProductAdapter
  private lateinit var productList: List<Product>
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityExportProductsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    db = FirebaseFirestore.getInstance()

    lifecycleScope.launch {
      productList = db.collection("products").get().await().toObjects(Product::class.java)

      productsAdapter = ExportProductAdapter(productList, this@ExportProductsActivity)
      binding.exportProductList.adapter = productsAdapter
      binding.exportProductList.layoutManager = LinearLayoutManager(this@ExportProductsActivity)
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