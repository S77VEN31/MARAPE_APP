package tec.ac.cr.marape.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.networking.RemoteApi
import java.util.Timer
import kotlin.concurrent.schedule

class CreateProductActivity : AppCompatActivity() {

  private val product = Product()
  private var timer = Timer()
  private lateinit var db: FirebaseFirestore
  private lateinit var nameEntry: TextView
  private lateinit var priceEntry: TextView
  private lateinit var brandEntry: TextView
  private lateinit var descriptionEntry: TextView
  private lateinit var colorEntry: TextView
  private lateinit var materialEntry: TextView
  private lateinit var sizeEntry: TextView
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create_product)

    db = FirebaseFirestore.getInstance()
    nameEntry = findViewById<TextView>(R.id.create_product_name)
    nameEntry.addTextChangedListener {
      product.name = it.toString()
    }
    brandEntry = findViewById<TextView>(R.id.create_product_brand)
    brandEntry.addTextChangedListener {
      product.brand = it.toString()
    }
    val t = this
    findViewById<TextView>(R.id.create_product_barcode).doAfterTextChanged {
      timer.cancel()
      timer = Timer()
      timer.schedule(500L) {
        product.barcode = it.toString()
        t.fetchTargetPrice(product.barcode)
      }
    }
    findViewById<TextView>(R.id.create_product_amount).addTextChangedListener {
      product.amount = it.toString().toInt()
    }
    priceEntry = findViewById<TextView>(R.id.create_product_price)
    priceEntry.addTextChangedListener {
      product.targetPrice = it.toString().toFloat()
    }
    descriptionEntry = findViewById<TextView>(R.id.create_product_description)
    descriptionEntry.addTextChangedListener {
      product.description = it.toString()
    }
    colorEntry = findViewById<TextView>(R.id.create_product_color)
    colorEntry.addTextChangedListener {
      // TODO: Change the color input for something else.
      product.color = it.toString()
    }
    materialEntry = findViewById<TextView>(R.id.create_product_material)
    materialEntry.addTextChangedListener {
      product.material = it.toString()
    }
    sizeEntry = findViewById<TextView>(R.id.create_product_size)
    sizeEntry.addTextChangedListener {
      product.size = it.toString()
    }
    findViewById<TextView>(R.id.create_product_our_price).addTextChangedListener {
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

        nameEntry.text = prod.title
        brandEntry.text = prod.brand
        descriptionEntry.text = prod.description
        colorEntry.text = prod.color
        materialEntry.text = prod.material
        sizeEntry.text = prod.size

        target?.let {
          priceEntry.text = it.price
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
    doc.set(product).addOnFailureListener {
      Log.e("Z:Fetch", it.message.toString())
    }
      .addOnSuccessListener {
        Toast.makeText(
          this@CreateProductActivity,
          R.string.create_product_success,
          Toast.LENGTH_LONG
        ).show()
        finish()
      }
  }
}