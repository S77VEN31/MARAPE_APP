// Create an activity to handle the details of an inventory

package tec.ac.cr.marape.app


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.zeroturnaround.zip.ZipUtil
import tec.ac.cr.marape.app.databinding.ActivityInventoryDetailsBinding
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.model.User
import tec.ac.cr.marape.app.state.State
import tec.ac.cr.marape.app.ui.dashboard.EDITED_INVENTORY
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class InventoryDetailsActivity : AppCompatActivity() {
  private lateinit var db: FirebaseFirestore
  private lateinit var storage: FirebaseStorage
  private lateinit var owner: User
  private lateinit var inventory: Inventory
  private var position = RecyclerView.NO_POSITION
  private var launcher: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::resultHandler)
  private lateinit var binding: ActivityInventoryDetailsBinding
  private val locale = Locale("es", "CR")
  private val formatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale)
  private lateinit var state: State
  private lateinit var products: ArrayList<Product>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    state = State.getInstance()
    binding = ActivityInventoryDetailsBinding.inflate(layoutInflater)
    db = FirebaseFirestore.getInstance()
    storage = FirebaseStorage.getInstance()

    setContentView(binding.root)

    // This is the back button
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    // TODO: The position will be used to update the inventory in the local sharedInventories array.


    loadInventoryData(intent)

    if (state.user.email.compareTo(inventory.ownerEmail) != 0) {
      binding.floatingActionButtonEditShared.visibility = View.GONE
    }
    binding.shareInventoryButton.setOnClickListener(::shareInventory)

    lifecycleScope.launch {
      fetchProducts()
    }
  }

  private fun makeExcelWorkbook(): Workbook {
    val workbook = XSSFWorkbook()
    val detailsSheet = workbook.createSheet("Detalles")
    makeDetails(detailsSheet)
    val productsSheet = workbook.createSheet("Productos")
    makeProducts(productsSheet)
    return workbook
  }

  private fun makeDetails(sheet: Sheet) {
    val headers = listOf("Nombre", "Fecha de Creación", "Estado", "Propietario")
    val header = sheet.createRow(0)
    headers.forEachIndexed { idx, field ->
      header.createCell(idx).setCellValue(field)
      val mn = field.length * 100
      if (sheet.getColumnWidth(idx) < mn) {
        sheet.setColumnWidth(idx, mn)
      }
    }
    val values = sheet.createRow(1)
    val state = if (inventory.active) "Activo" else "Inactivo"
    listOf(
      inventory.name,
      formatter.format(Date(inventory.creationDate)).toString(),
      state,
      inventory.ownerEmail
    ).forEachIndexed { idx, field ->
      values.createCell(idx).setCellValue(field)
      val nm = field.length * 100
      if (sheet.getColumnWidth(idx) < nm) {
        sheet.setColumnWidth(idx, nm)
      }

    }
  }

  private suspend fun fetchProducts() {
    this.products = this@InventoryDetailsActivity.inventory.items.mapNotNull { item ->
      db.document("products/$item").get().await().toObject(Product::class.java)
    } as ArrayList<Product>
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


    this.products.forEachIndexed { idx, product ->
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


  private fun shareInventory(view: View) {
    // Perform excel file creation
    val dialog = AlertDialog.Builder(this).setMessage(R.string.creating_excel_file).show()
    val timestamp = Date().time
    val zipname = "${inventory.id}-$timestamp"
    lifecycleScope.launch {
      val workbook = makeExcelWorkbook()

      val dir = baseContext.getExternalFilesDir("files/$zipname")
      val file = File(dir, "${inventory.name}.xlsx")
      if (file.exists()) {
        file.delete()
      }

      val outFile = FileOutputStream(file)
      workbook.write(outFile)
      outFile.close()

      // Download images and add them to the thingy.
      products.forEach { product ->
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
      val zipFile = File(exported, "${inventory.name}.zip")
      ZipUtil.pack(file.parentFile, zipFile)

      val shareIntent = Intent(Intent.ACTION_SEND)
      shareIntent.setType("*/*")
      shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Detalles de inventario: ${inventory.name}")
      shareIntent.putExtra(Intent.EXTRA_TEXT, "Compartido desde MARAPE-APP")
      val fileUri = FileProvider.getUriForFile(
        this@InventoryDetailsActivity, "tec.ac.cr.marape.app.provider", zipFile
      )
      shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
      dialog.cancel()
      startActivity(Intent.createChooser(shareIntent, "Compartir Inventario Vía..."))
    }
  }

  private fun loadInventoryData(intent: Intent) {
    inventory = intent.getSerializableExtra("inventory")!! as Inventory
    position = intent.getIntExtra("position", RecyclerView.NO_POSITION)
    inventory.let {
      binding.sharedInventoryName.text = it.name
      binding.sharedInventoryCreationDate.text = formatter.format(Date(it.creationDate))

      // TODO: Find a different way of doing this, somethig more idiomatic, or somethingdsds
      //  that uses the string resources.
      binding.sharedInventoryStatus.text = if (it.active) {
        "Activo"
      } else {
        "Inactivo"
      }

      db.document(
        "/users/${it.ownerEmail}"
      ).get().addOnSuccessListener { doc ->
        owner = doc.toObject(User::class.java)!!
        binding.sharedInventoryOwner.text = owner.name
      }
    }
  }

  fun listInvitedUsers(view: View) {
    // launch the activity to list the users
    val clazz: Class<*> = if (state.user.email.compareTo(inventory.ownerEmail) == 0) {
      GuestListActivity::class.java
    } else {
      InvitedUsersListGuestActivity::class.java
    }
    val intent = Intent(this, clazz)
    intent.putExtra("position", position)
    intent.putExtra("inventory", inventory)
    launcher.launch(intent)
  }

  fun editInventory(view: View) {
    val intent = Intent(this, EditInventoryActivity::class.java)
    intent.putExtra("position", position)
    intent.putExtra("inventory", inventory)
    launcher.launch(intent)
  }

  fun listProducts(view: View) {
    val intent = Intent(this, ProductListActivity::class.java)
    intent.putExtra("position", position)
    intent.putExtra("inventory", inventory)
    launcher.launch(intent)
  }

  private fun resultHandler(result: ActivityResult) {
    when (result.resultCode) {
      EDITED_INVENTORY -> {
        loadInventoryData(result.data!!)
        setResult(
          EDITED_INVENTORY, result.data
        )
      }
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