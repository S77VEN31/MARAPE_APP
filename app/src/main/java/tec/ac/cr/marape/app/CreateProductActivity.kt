package tec.ac.cr.marape.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.databinding.ActivityCreateProductBinding
import tec.ac.cr.marape.app.model.Product


const val FOUND_IN_API = 1
const val FOUND_IN_DATABASE = 2
const val NOT_FOUND = 3


class CreateProductActivity : AppCompatActivity() {

  private var _binding: ActivityCreateProductBinding? = null
  private val binding get() = _binding!!

  private val product = Product()
  private lateinit var db: FirebaseFirestore
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private var requestCamera: ActivityResultLauncher<String>? = null

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

      NOT_FOUND -> {
        Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    _binding = ActivityCreateProductBinding.inflate(layoutInflater)
    setContentView(binding.root)
    db = FirebaseFirestore.getInstance()
    launcher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::resultCallback)

    requestCamera = registerForActivityResult(
      ActivityResultContracts
        .RequestPermission(),
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

  fun createProduct(view: View) {
    val products = db.collection("products")
    val doc = when (product.barcode) {
      "" -> products.document()
      else -> products.document(product.barcode)
    }
    product.id = doc.id
    doc.set(product).addOnSuccessListener {
      Toast.makeText(
        this@CreateProductActivity,
        R.string.create_product_success,
        Toast.LENGTH_LONG
      ).show()
      finish()
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