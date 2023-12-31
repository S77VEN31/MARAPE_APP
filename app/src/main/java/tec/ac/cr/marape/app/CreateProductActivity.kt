package tec.ac.cr.marape.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import tec.ac.cr.marape.app.databinding.ActivityCreateProductBinding
import tec.ac.cr.marape.app.model.Product
import java.util.Date


const val FOUND_IN_API = 1
const val FOUND_IN_DATABASE = 2
const val NOT_FOUND = 3


class CreateProductActivity : AppCompatActivity() {

  private var _binding: ActivityCreateProductBinding? = null
  private val binding get() = _binding!!

  private val product = Product()
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private var requestCamera: ActivityResultLauncher<String>? = null
  private lateinit var mediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>
  private lateinit var db: FirebaseFirestore
  private lateinit var storage: FirebaseStorage
  private var selectedImages = emptyList<Uri>()


  private fun handleImageSelection(result: List<Uri>) {
    selectedImages = result
    binding.createProductImageCount.text = result.size.toString()
  }

  private fun resultCallback(result: ActivityResult) {
    when (result.resultCode) {
      FOUND_IN_API -> {
        val prod = result.data!!.getSerializableExtra("product") as Product
        // Llenar los campos.
        binding.createProductBarcode.setText(prod.barcode)
        binding.createProductName.setText(prod.name)
        binding.createProductBrand.setText(prod.brand)
        binding.createProductDescription.setText(prod.description)
        binding.createProductColor.setText(prod.color)
        binding.createProductMaterial.setText(prod.material)
        binding.createProductSize.setText(prod.size)
      }

      FOUND_IN_DATABASE -> {
        Toast.makeText(this, "Producto agregado al inventario", Toast.LENGTH_SHORT).show()
        finish()
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityCreateProductBinding.inflate(layoutInflater)
    setContentView(binding.root)
    db = FirebaseFirestore.getInstance()
    storage = FirebaseStorage.getInstance()
    mediaLauncher = registerForActivityResult(
      ActivityResultContracts.PickMultipleVisualMedia(), ::handleImageSelection
    )

    launcher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::resultCallback)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    requestCamera = registerForActivityResult(
      ActivityResultContracts.RequestPermission(),
    ) {
      if (it) {
        val intent = Intent(this, BCScan::class.java)
        launcher.launch(intent)
      } else {
        Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
      }
    }
    binding.scanProduct.setOnClickListener {
      requestCamera?.launch(android.Manifest.permission.CAMERA)
    }
    binding.createProductName.addTextChangedListener {
      product.name = it.toString()
    }

    binding.createProductBrand.addTextChangedListener {
      product.brand = it.toString()
    }

    binding.createProductBarcode.doAfterTextChanged {
      product.barcode = it.toString()
    }

    binding.createProductAmount.addTextChangedListener {
      product.amount = it.toString().toInt()
    }

    binding.createProductPrice.addTextChangedListener {
      product.targetPrice = it.toString().toFloat()
    }

    binding.createProductDescription.addTextChangedListener {
      product.description = it.toString()
    }

    binding.createProductColor.addTextChangedListener {
      // TODO: Change the color input for something else.
      product.color = it.toString()
    }

    binding.createProductMaterial.addTextChangedListener {
      product.material = it.toString()
    }

    binding.createProductSize.addTextChangedListener {
      product.size = it.toString()
    }

    binding.createProductOurPrice.addTextChangedListener {
      product.price = it.toString().toFloat()
    }
  }

  private fun validate(): Boolean {
    val validationErrors = mutableListOf<Pair<EditText, String>>()
    if (binding.createProductName.text.isEmpty()) {
      validationErrors.add(binding.createProductName to "El nombre no puede estar vacío.")
    }

    if (binding.createProductPrice.text.isEmpty()) {
      validationErrors.add(binding.createProductPrice to "El precio no puede estar vacío")
    }

    if (binding.createProductBrand.text.isEmpty()) {
      validationErrors.add(binding.createProductBrand to "La marca no puede estar vacía.")
    }

    if (binding.createProductMaterial.text.isEmpty()) {
      validationErrors.add(binding.createProductMaterial to "El material no puede estar vacío.")
    }

    if (binding.createProductSize.text.isEmpty()) {
      validationErrors.add(binding.createProductSize to "El tamaño no puede estar vacío.")
    }

    if (binding.createProductColor.text.isEmpty()) {
      validationErrors.add(binding.createProductColor to "El color no puede estar vacío.")
    }

    if (binding.createProductAmount.text.isEmpty()) {
      validationErrors.add(binding.createProductAmount to "La cantidad no puede estar vacía.")
    }

    validationErrors.forEach { (view, error) ->
      view.error = error
    }

    return validationErrors.isEmpty()
  }

  fun createProduct(view: View) {
    if (!validate()) return

    val tasks = selectedImages.map { image ->
      val timestamp = Date().time
      val child = storage.reference.child("product-images/${timestamp}")
      product.images.add(child.path)
      child.putFile(image)
    } // TODO: Check if there's an error down the line and delete the images, or cancel the upload.

    val products = db.collection("products")
    val doc = when (product.barcode) {
      "" -> products.document()
      else -> products.document(product.barcode)
    }
    product.id = doc.id
    doc.set(product).addOnSuccessListener {
      Toast.makeText(
        this@CreateProductActivity, R.string.create_product_success, Toast.LENGTH_LONG
      ).show()
      finish()
    }.addOnFailureListener {
      // TODO: This might or might not do anything, that depends on whether or not the images have
      //  already been uploaded to the cloud.
      tasks.forEach { task ->
        task.cancel()
      }
    }
  }

  fun addImages(view: View) {
    mediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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