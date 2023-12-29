package tec.ac.cr.marape.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.model.Product

class EditProductActivity : AppCompatActivity() {
  private val db = FirebaseFirestore.getInstance()
  private lateinit var name: EditText
  private lateinit var brand: EditText
  private lateinit var quantity: EditText
  private lateinit var description: EditText
  private lateinit var price: EditText
  private lateinit var color: EditText
  private lateinit var material: EditText
  private lateinit var size: EditText
  private lateinit var ourPrice: EditText
  private lateinit var save: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_product)

    name = findViewById(R.id.edit_product_name)
    brand = findViewById(R.id.edit_product_brand)
    quantity = findViewById(R.id.edit_product_quantity)
    description = findViewById(R.id.edit_product_description)
    price = findViewById(R.id.edit_product_price)
    color = findViewById(R.id.edit_product_color)
    material = findViewById(R.id.edit_product_material)
    size = findViewById(R.id.edit_product_size)
    ourPrice = findViewById(R.id.edit_product_our_price)
    save = findViewById(R.id.edit_product_save_changes)


    fillOutFields("weq123132342")
    save.setOnClickListener {
      if (!validateFields()){
        updateProduct("weq123132342")
      }
    }

  }

  private fun validateFields(): Boolean{
    return (name.text.isNullOrEmpty() || brand.text.isNullOrEmpty() || quantity.text.isNullOrEmpty() ||
      description.text.isNullOrEmpty() || price.text.isNullOrEmpty() || color.text.isNullOrEmpty()
      || material.text.isNullOrEmpty() || size.text.isNullOrEmpty() || ourPrice.text.isNullOrEmpty())
  }

  private fun updateProduct(productId: String){
    val productRef = db.collection("products").document(productId)

    productRef.update(
      "name", name.text.toString(),
      "brand", brand.text.toString(),
      "description", description.text.toString(),
      "color", color.text.toString(),
      "material", material.text.toString(),
      "size", size.text.toString(),
      "amount", quantity.text.toString().toInt(),
      "price", price.text.toString().toFloat(),
      "targetPrice", ourPrice.text.toString().toFloat()
    ).addOnSuccessListener {
      Toast.makeText(this, "Producto actualizado con éxito", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener {
      Toast.makeText(this, "Error al actualizar el producto", Toast.LENGTH_SHORT).show()
    }
  }

  
  private fun fillOutFields(productId: String){
    val productRef = db.collection("products").document(productId)
    productRef.get()
      .addOnSuccessListener { documentSnapshot ->
        if (documentSnapshot.exists()) {
          val product = documentSnapshot.toObject(Product::class.java)

          product?.let {
            name.setText(it.name)
            brand.setText(it.brand)
            description.setText(it.description)
            color.setText(it.color)
            material.setText(it.material)
            size.setText(it.size)
            quantity.setText(it.amount.toString())
            price.setText(it.price.toString())
            ourPrice.setText(it.targetPrice.toString())
          }
        } else {
          Toast.makeText(this, "Documento no existe", Toast.LENGTH_SHORT).show()
        }
      }
      .addOnFailureListener { e ->
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