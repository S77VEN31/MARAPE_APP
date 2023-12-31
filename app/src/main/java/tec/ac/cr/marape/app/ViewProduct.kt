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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


const val FROM_VIEW_PRODUCT = 1
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
  private lateinit var launcher: ActivityResultLauncher<Intent>
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_view_product)

    db = FirebaseFirestore.getInstance()

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

//      val integrator = IntentIntegrator(this)
//      integrator.setOrientationLocked(false)
//      integrator.setBeepEnabled(false)
//      integrator.initiateScan()
      val intent = Intent(this, BCScan::class.java)
      intent.putExtra("from", FROM_VIEW_PRODUCT)
      launcher.launch(intent)
    }

    launcher =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
          NOT_FOUND ->{
            limpiarDatos()
            Toast.makeText(this, "No se encontró el producto", Toast.LENGTH_SHORT).show()
          }
          FOUND_IN_DATABASE -> {
            val product = result.data?.getSerializableExtra("product") as Product
            mostrarDatos(product)
          }
        }
      }

  }

//  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//    super.onActivityResult(requestCode, resultCode, data)
//
//
//    // Manejar el resultado del escaneo
//    val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
//
//    if (result != null) {
//      if (result.contents != null) {
//        val scannedBarcode = result.contents
//        buscarProducto(scannedBarcode)
//      } else {
//        Toast.makeText(this, "No se ha escaneado ningún código de barras", Toast.LENGTH_SHORT)
//          .show()
//      }
//    } else {
//      Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
//    }
//  }

  private fun mostrarDatos(producto: Product) {

    // Carga los datos del producto en los elementos de la interfaz de usuario
    txtBrand.text = producto.brand
    txtColor.text = producto.color
    txtDescription.text = producto.description
    txtId.text = producto.id
    txtImage.text = producto.images.joinToString(", ")
    txtMaterial.text = producto.material
    txtName.text = producto.name
    txtPrice.text = producto.price.toString()
    txtSize.text = producto.size
    txtTargetPrice.text = producto.targetPrice.toString()
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
