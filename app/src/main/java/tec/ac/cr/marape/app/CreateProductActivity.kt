package tec.ac.cr.marape.app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.databinding.ActivityCreateProductBinding
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.networking.RemoteApi
import java.util.Timer
import kotlin.concurrent.schedule


class CreateProductActivity : AppCompatActivity() {

  private var _binding: ActivityCreateProductBinding? = null
  private val binding get() = _binding!!

  private val product = Product()
  private var timer = Timer()
  private lateinit var db: FirebaseFirestore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityCreateProductBinding.inflate(layoutInflater)
    setContentView(binding.root)
    db = FirebaseFirestore.getInstance()

    binding.createProductName.addTextChangedListener {
      product.name = it.toString()
    }

    binding.createProductBrand.addTextChangedListener {
      product.brand = it.toString()
    }

    binding.createProductBarcode.doAfterTextChanged {
      timer.cancel()
      timer = Timer()
      timer.schedule(500L) {
        product.barcode = it.toString()
        fetchTargetPrice(product.barcode)
      }
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

  private fun fetchTargetPrice(code: String) {
    RemoteApi.getProduct(code, { res ->
      if (res.products.isNotEmpty()) {
        val prod = res.products[0]
        val target = prod.stores.find {
          it.name.contains(
            "target",
            true
          )
        }

        binding.createProductName.setText(prod.title)
        binding.createProductBrand.setText(prod.brand)
        binding.createProductDescription.setText(prod.description)
        binding.createProductColor.setText(prod.color)
        binding.createProductMaterial.setText(prod.material)
        binding.createProductSize.setText(prod.size)

        target?.let {
          binding.createProductPrice.setText(it.price)
        }
      }
    }, {
      runOnUiThread {
        Toast.makeText(
          this@CreateProductActivity,
          R.string.barcode_not_found_error,
          Toast.LENGTH_LONG
        ).show()
      }
    })
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
}