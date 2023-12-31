package tec.ac.cr.marape.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.state.State
import android.content.pm.ActivityInfo

class ViewProduct : AppCompatActivity() {

  private lateinit var txtBarcode: TextView
  private lateinit var btnScan: Button

  private lateinit var txtBrand: TextView
  private lateinit var txtColor: TextView
  private lateinit var txtDescription: TextView
  private lateinit var txtId: TextView
  private lateinit var txtImage: TextView
  private lateinit var txtMaterial: TextView
  private lateinit var txtName: TextView
  private lateinit var txtPrice: TextView
  private lateinit var txtSize: TextView
  private lateinit var txtTargetPrice: TextView

  private lateinit var db: FirebaseFirestore
  private lateinit var state: State

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_view_product)

    db = FirebaseFirestore.getInstance()
    state = State.getInstance(this)


    btnScan = findViewById(R.id.button4)

    txtName = findViewById(R.id.txtName)
    txtBrand = findViewById(R.id.txtBrand)
    txtColor = findViewById(R.id.txtColor)
    txtDescription = findViewById(R.id.txtDescrption)
    txtId = findViewById(R.id.txtId)
    txtImage = findViewById(R.id.txtImage)
    txtMaterial = findViewById(R.id.txtMaterial)
    txtPrice = findViewById(R.id.txtPrice)
    txtSize = findViewById(R.id.txtSize)
    txtTargetPrice = findViewById(R.id.txtTargetPrice)

    btnScan.setOnClickListener {
      // Iniciar la actividad de escaneo

      val integrator = IntentIntegrator(this)
      integrator.setOrientationLocked(true)
      integrator.setBeepEnabled(false)
      integrator.initiateScan()
    }


  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)


    // Manejar el resultado del escaneo
    val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

    if (result != null) {
      if (result.contents != null) {
        val scannedBarcode = result.contents
        buscarProducto(scannedBarcode)
      } else {
        Toast.makeText(this, "No se ha escaneado ningún código de barras", Toast.LENGTH_SHORT).show()
      }
    } else {
      Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
    }
  }

  private fun buscarProducto(barcode: String) {
    val cleanedBarcode = barcode.trim() // Elimina espacios en blanco al principio y al final

    db.collection("products")
      .document(cleanedBarcode)
      .get()
      .addOnSuccessListener { document ->
        if (document != null && document.exists()) {
          // Muestra los datos en los TextViews
          val producto = document.data
          mostrarDatos(producto)
        } else {
          limpiarDatos()
          // En caso en que el código de barras no exista
          Toast.makeText(this@ViewProduct, "Producto no encontrado", Toast.LENGTH_SHORT).show()
        }
      }
  }
  private fun mostrarDatos(producto: Map<String, Any>?) {
    // Carga los datos del producto en el estado
    state.product = Product(
      id = producto?.get("id").toString(),
      name = producto?.get("name").toString(),
      barcode = producto?.get("barcode").toString(),
      brand = producto?.get("brand").toString(),
      description = producto?.get("description").toString(),
      color = producto?.get("color").toString(),
      material = producto?.get("material").toString(),
      amount = (producto?.get("amount") as? Long)?.toInt() ?: 0,
      size = producto?.get("size").toString(),
      images = (producto?.get("images") as? List<*>)?.map { it.toString() } ?: emptyList(),
      targetPrice = (producto?.get("targetPrice") as? Double)?.toFloat() ?: 0.0f,
      price = (producto?.get("price") as? Double)?.toFloat() ?: 0.0f
    )

    // Carga los datos del producto en los elementos de la interfaz de usuario
    txtBrand.text = "${state.product.brand}"
    txtColor.text = "${state.product.color}"
    txtDescription.text = "${state.product.description}"
    txtId.text = "${state.product.id}"
    txtImage.text = "${state.product.images.joinToString(", ")}"
    txtMaterial.text = "${state.product.material}"
    txtName.text = "${state.product.name}"
    txtPrice.text = "${state.product.price}"
    txtSize.text = "${state.product.size}"
    txtTargetPrice.text = "${state.product.targetPrice}"
  }

  private fun limpiarDatos() {
    // Limpia los TextViews
    txtBrand.text = ""
    txtColor.text = ""
    txtDescription.text = ""
    txtId.text = ""
    txtName.text = ""
    txtMaterial.text = ""
    txtName.text = ""
    txtPrice.text = ""
    txtSize.text = ""
    txtTargetPrice.text = ""
  }
}
