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
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.zeroturnaround.zip.ZipUtil
import tec.ac.cr.marape.app.adapter.EDITED_PRODUCT
import tec.ac.cr.marape.app.adapter.ExportProductAdapter
import tec.ac.cr.marape.app.databinding.ActivityExportProductsBinding
import tec.ac.cr.marape.app.model.Product
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class ExportProductsActivity : AppCompatActivity() {
  private lateinit var db: FirebaseFirestore
  private lateinit var storage: FirebaseStorage
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
    db = FirebaseFirestore.getInstance()
    storage = FirebaseStorage.getInstance()

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.shareProducts.setOnClickListener(::shareProducts)



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

  private fun makeProducts(sheet: Sheet) {
    val header = sheet.createRow(0)
    val fields = listOf(
      "Código de barras",
      "Nombre",
      "Marca",
      "Descripción",
      "Color",
      "Material",
      "Cantidad",
      "Tamaño",
      "Precio Target",
      "Precio Nuestro"
    )

    fields.forEachIndexed { idx, field ->
      header.createCell(idx).setCellValue(field)
      val mn = field.length * 100
      if (sheet.getColumnWidth(idx) < mn) {
        sheet.setColumnWidth(idx, mn)
      }
    }


    this.productList.forEachIndexed { idx, product ->
      val row = sheet.createRow(idx + 1)
      val values = listOf(
        product.barcode,
        product.name,
        product.brand,
        product.description,
        product.color,
        product.material,
        product.amount.toString(),
        product.targetPrice.toString(),
        product.price.toString()
      )
      values.forEachIndexed { jdx, field ->
        row.createCell(jdx).setCellValue(field)
        val mn = field.length * 100
        if (sheet.getColumnWidth(jdx) < mn) {
          sheet.setColumnWidth(jdx, mn)
        }
      }
    }
  }


  private fun makeExcelWorkbook(): Workbook {
    val workbook = XSSFWorkbook()
    val productsSheet = workbook.createSheet("Productos")
    makeProducts(productsSheet)
    return workbook
  }

  private fun shareProducts(view: View) {
    val dialog = AlertDialog.Builder(this).setMessage(R.string.creating_excel_file).show()
    val timestamp = Date().time
    val zipname = "productos-$timestamp"

    lifecycleScope.launch {
      val workbook = makeExcelWorkbook()

      val dir = baseContext.getExternalFilesDir("files/$zipname")
      val file = File(dir, "Productos.xlsx")
      if (file.exists()) {
        file.delete()
      }

      val outFile = FileOutputStream(file)
      workbook.write(outFile)
      outFile.close()

      // Download images and add them to the thingy.
      productList.forEach { product ->
        if (product.images.isNotEmpty()) {
          val imagesDir = baseContext.getExternalFilesDir("files/$zipname/${product.barcode}")
          product.images.forEach { image ->
            val child = storage.reference.child(image)
            val metadata = child.metadata.await()
            val extension = metadata.contentType?.split('/')?.last()!!
            child.getFile(File(imagesDir, "${File(image).name}.${extension}")).await()
          }
        }
      }

      val exported = baseContext.getExternalFilesDir("exported")
      val zipFile = File(exported, "productos.zip")
      ZipUtil.pack(file.parentFile, zipFile)

      val shareIntent = Intent(Intent.ACTION_SEND)
      shareIntent.setType("*/*")
      shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Detalles de Todos los Productos")
      shareIntent.putExtra(Intent.EXTRA_TEXT, "Compartido desde MARAPE-APP")
      val fileUri = FileProvider.getUriForFile(
        this@ExportProductsActivity, "tec.ac.cr.marape.app.provider", zipFile
      )
      shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
      dialog.cancel()
      startActivity(Intent.createChooser(shareIntent, "Compartir Productos Vía..."))
    }
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