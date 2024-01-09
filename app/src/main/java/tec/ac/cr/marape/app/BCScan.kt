package tec.ac.cr.marape.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.databinding.ActivityBcscanBinding
import tec.ac.cr.marape.app.model.LookupResponse
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.networking.RemoteApi
import java.io.IOException

class BCScan : AppCompatActivity() {
  private lateinit var binding: ActivityBcscanBinding
  private lateinit var barcodeDetector: BarcodeDetector
  private lateinit var cameraSource: CameraSource
  private lateinit var db: FirebaseFirestore
  var intentData = ""
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityBcscanBinding.inflate(layoutInflater)
    db = FirebaseFirestore.getInstance()
    setContentView(binding.root)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = resources.getString(R.string.title_scan_product)

//    binding.btnScan.setOnClickListener {
////      intentData = "9780140157376"
//
//      if (intentData != "") {
//        val resultIntent = Intent()
//        resultIntent.putExtra("product", intentData)
//        setResult(FOUND_IN_API, resultIntent)
//        finish()
//      } else {
//        Toast.makeText(applicationContext, "Ningún código encontrado", Toast.LENGTH_SHORT).show()
//      }
//    }
  }

  private fun iniBc() {
    barcodeDetector = BarcodeDetector.Builder(this)
      .setBarcodeFormats(Barcode.ALL_FORMATS)
      .build()
    cameraSource = CameraSource.Builder(this, barcodeDetector)
      .setRequestedPreviewSize(1920, 1080)
      .setAutoFocusEnabled(true)
      .setFacing(CameraSource.CAMERA_FACING_BACK)
      .build()
    binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
      @SuppressLint("MissingPermission")
      override fun surfaceCreated(holder: SurfaceHolder) {
        try {
          cameraSource.start(binding.surfaceView.holder)
        } catch (e: IOException) {
          e.printStackTrace()
        }
      }

      override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
      }

      override fun surfaceDestroyed(holder: SurfaceHolder) {
        cameraSource.stop()
      }
    })
    barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
      override fun release() {
//        Toast.makeText(applicationContext, "Escaneo finalizado", Toast.LENGTH_SHORT).show()
      }

      override fun receiveDetections(detections: Detector.Detections<Barcode>) {
        val barcodes = detections.detectedItems
        if (barcodes.size() != 0) {
          binding.tvBarcodeValue.post {
            intentData = barcodes.valueAt(0).displayValue
            binding.tvBarcodeValue.text = intentData
            buscarProducto(intentData)
          }
        }
      }
    })
  }

  private fun buscarProducto(barcode: String) {

    db.collection("products")
      .document(barcode)
      .get()
      .addOnSuccessListener { document ->
        if (document != null && document.exists()) {
          val product = document.toObject(Product::class.java)!!
          val intent = Intent()
          intent.putExtra("product", product)
          setResult(FOUND_IN_DATABASE, intent)
          finish()
        } else {
          // En caso en que el código de barras no exista en la DB
          val from = intent.getIntExtra("from", -1)
          if (from == FROM_VIEW_PRODUCT) {
            setResult(NOT_FOUND)
            finish()
          } else {
            fetchApiData(barcode)
          }
        }
      }
  }

  private fun fetchApiData(code: String) {
    RemoteApi.getProduct(code, { res ->
      setResult(NOT_FOUND)
      if (res.products.isNotEmpty()) {
        val product = Product()
        val prod = res.products[0]
        val target = prod.stores.find {
          it.name.contains(
            "target",
            true
          )
        }
        product.barcode = code
        product.name = prod.title
        product.brand = prod.brand
        product.description = prod.description
        product.color = prod.color
        product.material = prod.material
        product.size = prod.size
        target?.let {
          product.targetPrice = it.price.toFloat()
        }
        val intent = Intent()
        intent.putExtra("product", product)
        setResult(FOUND_IN_API, intent)
      }
      finish()

    }, {
      runOnUiThread {
        // TODO: agregar esta funcionalidad
      }
    })
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

  override fun onPause() {
    super.onPause()
    cameraSource.release()
  }

  override fun onResume() {
    super.onResume()
    iniBc()
  }
}