package tec.ac.cr.marape.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tec.ac.cr.marape.app.adapter.EDITED_PRODUCT
import tec.ac.cr.marape.app.adapter.ExportProductAdapter
import tec.ac.cr.marape.app.databinding.ActivityExportProductsBinding
import tec.ac.cr.marape.app.model.Product

class ExportProductsActivity : AppCompatActivity() {
  private lateinit var db: FirebaseFirestore
  private lateinit var binding: ActivityExportProductsBinding
  private lateinit var productsAdapter: ExportProductAdapter
  private val launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult(), ::resultHandler
  )
  private lateinit var productList: ArrayList<Product>
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityExportProductsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    db = FirebaseFirestore.getInstance()

    lifecycleScope.launch {
      productList =
        db.collection("products").get().await().toObjects(Product::class.java) as ArrayList<Product>

      productsAdapter = ExportProductAdapter(productList)
      productsAdapter.addEditListener(::editListener)
      productsAdapter.addDeleteListener(::deleteListener)
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


  private fun isProductAssociated(productId: String, onComplete: (Boolean) -> Unit) {
    // Consulta para verificar si el producto está presente en algún inventario
    db.collection("inventories").whereArrayContains("items", productId).get()
      .addOnSuccessListener { inventoryQuerySnapshot ->
        onComplete(!inventoryQuerySnapshot.isEmpty)
      }.addOnFailureListener {
        onComplete(false) // Error al consultar los inventarios
      }
  }

  private fun resultHandler(result: ActivityResult) {
    when (result.resultCode) {
      EDITED_PRODUCT -> {
        val position = result.data?.getIntExtra("position", RecyclerView.NO_POSITION)!!
        val product = result.data?.getSerializableExtra("product")!! as Product
        productsAdapter.update(position, product)
      }
    }
  }

  private fun sendProductAssociatedDialog() {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Producto asociado")
    builder.setMessage("Este producto no se puede eliminar porque está asociado a algún inventario.")

    builder.setPositiveButton("Aceptar") { dialog, _ ->
      dialog.dismiss()
    }

    val dialog = builder.create()
    dialog.show()
  }

  private fun deleteListener(position: Int, product: Product) {
    isProductAssociated(product.id) { success ->
      if (success) {
        sendProductAssociatedDialog()
      } else {
        sendConfirmation {
          showResultDelete(position, product)
        }
      }
    }
  }

  // ---------------------------------------- EDIT PRODUCT ---------------------------------------------
  private fun editListener(position: Int, product: Product) {
    val intent = Intent(this, EditProductActivity::class.java)
    intent.putExtra("position", position)
    intent.putExtra("product", product)
    launcher.launch(intent)
  }

  private fun deletePerformance(
    position: Int, currentProduct: Product, onComplete: (Boolean) -> Unit
  ) {
    db.collection("products").document(currentProduct.id).delete().addOnSuccessListener {
      productsAdapter.removeProduct(position, currentProduct)
      onComplete(true)
    }.addOnFailureListener {
      onComplete(false)
    }
  }

  private fun showResultDelete(position: Int, product: Product) {
    deletePerformance(position, product) { success ->
      if (success) {
        Toast.makeText(
          this, "Producto eliminado con éxito", Toast.LENGTH_SHORT
        ).show()
      } else {
        Toast.makeText(
          this, "Error al eliminar el producto", Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

  private fun sendConfirmation(
    onYes: () -> Unit
  ) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Confirmación")
    builder.setMessage("¿Está seguro de eliminar este producto?")

    builder.setPositiveButton("Sí") { dialog, which ->
      onYes()
    }

    builder.setNegativeButton("No") { dialog, which ->
      dialog.dismiss()
    }

    val dialog = builder.create()
    dialog.show()
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