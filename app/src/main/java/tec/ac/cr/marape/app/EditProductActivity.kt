package tec.ac.cr.marape.app

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.adapter.REQUEST_CODE_EDIT_PRODUCT
import tec.ac.cr.marape.app.databinding.ActivityEditProductBinding
import tec.ac.cr.marape.app.model.Product

class EditProductActivity : AppCompatActivity() {
  private val db = FirebaseFirestore.getInstance()
  private lateinit var binding: ActivityEditProductBinding
  private lateinit var productId: String
  private lateinit var productRef: DocumentReference

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_product)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding = ActivityEditProductBinding.inflate(layoutInflater)
    setContentView(binding.root)

    productId = intent.getStringExtra("product_id").toString()
    productRef = db.collection("products").document(productId)

    fillOutFields()

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
    productRef.update(
      "name",
      binding.editProductName.text.toString(),
      "brand",
      binding.editProductBrand.text.toString(),
      "description",
      binding.editProductDescription.text.toString(),
      "color",
      binding.editProductColor.text.toString(),
      "material",
      binding.editProductMaterial.text.toString(),
      "size",
      binding.editProductSize.text.toString(),
      "amount",
      binding.editProductQuantity.text.toString().toInt(),
      "price",
      binding.editProductPrice.text.toString().toFloat(),
      "targetPrice",
      binding.editProductOurPrice.text.toString().toFloat()
    ).addOnSuccessListener {
      Toast.makeText(this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show()
      val result = Intent()
      result.putExtra("updatedProductId", productId)
      setResult(REQUEST_CODE_EDIT_PRODUCT, result)
      finish()
    }.addOnFailureListener {
      Toast.makeText(this, "Error al actualizar el producto", Toast.LENGTH_SHORT).show()
    }
  }


  private fun fillOutFields() {
    productRef.get().addOnSuccessListener { documentSnapshot ->
      if (documentSnapshot.exists()) {
        val product = documentSnapshot.toObject(Product::class.java)

        product?.let {
          binding.editProductName.setText(it.name)
          binding.editProductBrand.setText(it.brand)
          binding.editProductDescription.setText(it.description)
          binding.editProductColor.setText(it.color)
          binding.editProductMaterial.setText(it.material)
          binding.editProductSize.setText(it.size)
          binding.editProductQuantity.setText(it.amount)
          binding.editProductPrice.setText(it.price.toString())
          binding.editProductOurPrice.setText(it.targetPrice.toString())
        }
      } else {
        Toast.makeText(this, "Documento no existe", Toast.LENGTH_SHORT).show()
      }
    }.addOnFailureListener { e ->
      Toast.makeText(this, "Error al obtener el documento", Toast.LENGTH_SHORT).show()
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