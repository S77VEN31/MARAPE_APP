package tec.ac.cr.marape.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.adapter.EDITED_PRODUCT
import tec.ac.cr.marape.app.databinding.ActivityEditProductBinding
import tec.ac.cr.marape.app.model.Product

class EditProductActivity : AppCompatActivity() {
  private val db = FirebaseFirestore.getInstance()
  private lateinit var binding: ActivityEditProductBinding

  // private lateinit var productRef: DocumentReference
  private lateinit var product: Product
  private var position: Int = RecyclerView.NO_POSITION

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_product)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding = ActivityEditProductBinding.inflate(layoutInflater)
    setContentView(binding.root)

    position = intent.getIntExtra("position", RecyclerView.NO_POSITION)
    product = intent.getSerializableExtra("product")!! as Product
    Log.d("Z:Product", product.toString())

    fillOutFields()

    binding.editProductName.addTextChangedListener {
      product.name = it.toString()
    }
    binding.editProductBrand.addTextChangedListener {
      product.brand = it.toString()
    }
    binding.editProductDescription.addTextChangedListener {
      product.description = it.toString()
    }
    binding.editProductColor.addTextChangedListener {
      product.color = it.toString()
    }
    binding.editProductMaterial.addTextChangedListener {
      product.material = it.toString()
    }
    binding.editProductSize.addTextChangedListener {
      product.size = it.toString()
    }
    binding.editProductQuantity.addTextChangedListener {
      if (it!!.isNotEmpty()) {
        product.amount = it.toString().toInt()
      }
    }
    binding.editProductPrice.addTextChangedListener {
      if (it!!.isNotEmpty()) {
        product.targetPrice = it.toString().toFloat()
      }
    }
    binding.editProductOurPrice.addTextChangedListener {
      if (it!!.isNotEmpty()) {
        product.price = it.toString().toFloat()
      }
    }
    binding.editProductSaveChanges.setOnClickListener {
      if (!validateFields()) {
        updateProduct()
      } else {
        Toast.makeText(this, "Debe completar todos los campos", Toast.LENGTH_SHORT).show()
      }
    }

  }

  private fun validateFields(): Boolean {
    return (binding.editProductName.text.isNullOrEmpty() || binding.editProductBrand.text.isNullOrEmpty() || binding.editProductDescription.text.isNullOrEmpty() || binding.editProductColor.text.isNullOrEmpty() || binding.editProductQuantity.text.isNullOrEmpty() || binding.editProductMaterial.text.isNullOrEmpty() || binding.editProductSize.text.isNullOrEmpty() || binding.editProductPrice.text.isNullOrEmpty())
  }

  private fun updateProduct() {
    db.collection("products").document(product.id).set(product).addOnSuccessListener {
      Toast.makeText(this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show()
      val result = Intent()
      result.putExtra("position", position)
      result.putExtra("product", product)
      setResult(EDITED_PRODUCT, result)
      finish()
    }.addOnFailureListener {
      Toast.makeText(this, "Error al actualizar el producto", Toast.LENGTH_SHORT).show()
    }
  }


  private fun fillOutFields() {
    product.let {
      binding.editProductName.setText(it.name)
      binding.editProductBrand.setText(it.brand)
      binding.editProductDescription.setText(it.description)
      binding.editProductColor.setText(it.color)
      binding.editProductMaterial.setText(it.material)
      binding.editProductSize.setText(it.size)
      binding.editProductQuantity.setText(it.amount.toString())
      binding.editProductPrice.setText(it.targetPrice.toString())
      binding.editProductOurPrice.setText(it.price.toString())
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