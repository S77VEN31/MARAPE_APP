package tec.ac.cr.marape.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.networking.RemoteApi
import java.util.Timer
import kotlin.concurrent.schedule

class CreateProductActivity : AppCompatActivity() {

  private val product = Product()
  private var timer = Timer()
  private lateinit var nameEntry: TextView
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_create_product)

    nameEntry = findViewById<TextView>(R.id.create_product_name)
    nameEntry.addTextChangedListener {
      product.name = it.toString()
    }
    findViewById<TextView>(R.id.create_product_brand).addTextChangedListener {
      product.brand = it.toString()
    }
    val t = this
    findViewById<TextView>(R.id.create_product_barcode).doAfterTextChanged {
      Log.d("Z:Fetch", "Before stopping timer")
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
    findViewById<TextView>(R.id.create_product_price).addTextChangedListener {
      product.price = it.toString().toFloat()
    }
    findViewById<TextView>(R.id.create_product_description).addTextChangedListener {
      product.description = it.toString()
    }
    findViewById<TextView>(R.id.create_product_color).addTextChangedListener {
      // TODO: Change the color input for something else.
      product.color = it.toString()
    }
    findViewById<TextView>(R.id.create_product_material).addTextChangedListener {
      product.material = it.toString()
    }
    findViewById<TextView>(R.id.create_product_size).addTextChangedListener {
      product.size = it.toString()
    }
    findViewById<TextView>(R.id.create_product_our_price).addTextChangedListener {
      product.price = it.toString().toFloat()
    }
  }

  private fun fetchTargetPrice(code: String) {
    RemoteApi.getProduct(code) { res ->
      Log.d("Z:Fetch", "Fetching product $code")
      if (res.products.isNotEmpty()) {
        Log.d("Z:Fetch", "Wasn't empty")
        val product = res.products[0]
        val target = product.stores.find {
          it.name.contains("target", true)
        }
        target?.let {
          Log.d("Z:Fetch", "Something here")
          nameEntry.text = product.title
        }
      }
    }
  }

  fun createProduct(view: View) {

  }
}