package tec.ac.cr.marape.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tec.ac.cr.marape.app.adapter.EDITED_PRODUCT
import tec.ac.cr.marape.app.adapter.ProductListAdapter
import tec.ac.cr.marape.app.databinding.ActivityProductListBinding
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.state.State

const val REMOVED_PRODUCTS = 8

class ProductListActivity : AppCompatActivity() {

  private lateinit var inventory: Inventory
  private lateinit var db: FirebaseFirestore
  private lateinit var binding: ActivityProductListBinding
  private lateinit var productsAdapter: ProductListAdapter
  private lateinit var productList: ArrayList<Product>
  private lateinit var state: State
  private var inventoryPosition: Int = RecyclerView.NO_POSITION
  private var launcher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::resultHandler)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityProductListBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    db = FirebaseFirestore.getInstance()
    inventory = intent.getSerializableExtra("inventory")!! as Inventory
    inventoryPosition = intent.getIntExtra("position", RecyclerView.NO_POSITION)
    state = State.getInstance()

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

    lifecycleScope.launch {
      productList = inventory.items.mapNotNull { item ->
        if (item.isNotBlank()) {
          val productDocument = db.document("products/$item").get().await()
          productDocument?.toObject(Product::class.java)
        } else {
          null
        }
      } as ArrayList<Product>

      binding.floatingActionButtonCreateProduct.setOnClickListener(this@ProductListActivity::createProduct)
      productsAdapter = ProductListAdapter(productList, inventory)
      productsAdapter.addUnlinkListener(::handleUnlink)
      productsAdapter.addEditListener(::handleEdit)
      binding.productList.adapter = productsAdapter
      binding.productList.layoutManager = LinearLayoutManager(this@ProductListActivity)
    }

  }

  private fun handleEdit(position: Int, product: Product) {
    val intent = Intent(this, EditProductActivity::class.java)
    intent.putExtra("position", position)
    intent.putExtra("product", product)
    launcher.launch(intent)
  }

  private fun resultHandler(result: ActivityResult) {
    when (result.resultCode) {
      EDITED_PRODUCT -> {
        val position = result.data?.getIntExtra("position", RecyclerView.NO_POSITION)!!
        val product = result.data?.getSerializableExtra("product")!! as Product
        productsAdapter.update(position, product)
      }

      ADDED_PRODUCT_INVENTORY -> {
        val product = result.data?.getSerializableExtra("product")!! as Product
        productsAdapter.add(product)
      }
    }
  }

  private fun unlinkPerformance(
    position: Int, currentProduct: Product, onComplete: (Boolean) -> Unit
  ) {
    db.collection("inventories").document(inventory.id)
      .update("items", FieldValue.arrayRemove(currentProduct.id)).addOnSuccessListener {
        productsAdapter.removeProduct(position, currentProduct)
        state.sharedInventories[inventoryPosition].items.removeIf {
          it == currentProduct.id
        }

        // TODO: Find a better way of doing this.
        setResult(
          REMOVED_PRODUCTS,
          Intent().putExtra("inventory", state.sharedInventories[inventoryPosition])
        )
        onComplete(true)
      }.addOnFailureListener {
        onComplete(false)
      }
  }

  private fun handleUnlink(position: Int, product: Product) {
    sendConfirmation {
      showResultUnlink(position, product)
    }
  }

  private fun sendConfirmation(
    onYes: () -> Unit
  ) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Confirmación")
    builder.setMessage("¿Estás seguro de que quieres desligar este producto del actual inventario?")

    builder.setPositiveButton("Sí") { dialog, which ->
      onYes()
    }

    builder.setNegativeButton("No") { dialog, which ->
      dialog.dismiss()
    }

    val dialog = builder.create()
    dialog.show()
  }

  private fun showResultUnlink(position: Int, currentProduct: Product) {
    unlinkPerformance(position, currentProduct) { success ->
      if (success) {
        Toast.makeText(
          this, "Producto desligado con éxito", Toast.LENGTH_SHORT
        ).show()
      } else {
        Toast.makeText(
          this, "Error al desligar el producto", Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

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