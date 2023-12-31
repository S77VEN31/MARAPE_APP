package tec.ac.cr.marape.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.databinding.ActivityBcscanBinding
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

    binding.btnScan.setOnClickListener {
//      intentData = "9780140157376"

      if (intentData != "") {
        val resultIntent = Intent()
        resultIntent.putExtra("product", intentData)
        setResult(FOUND_IN_API, resultIntent)
        finish()
      } else {
        Toast.makeText(applicationContext, "Ningún código encontrado", Toast.LENGTH_SHORT).show()
      }
    }
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

          }
        }
      }
    })
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