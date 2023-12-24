package tec.ac.cr.marape.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import tec.ac.cr.marape.app.databinding.ActivityScanProductBinding

class ScanProduct : AppCompatActivity() {
  private var requestCamera: ActivityResultLauncher<String>? = null
  private lateinit var binding: ActivityScanProductBinding

  companion object {
    const val RESULT = "RESULT"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = resources.getString(R.string.title_scan_product)

    binding = ActivityScanProductBinding.inflate(layoutInflater)
    setContentView(binding.root)

    requestCamera = registerForActivityResult(
      ActivityResultContracts
        .RequestPermission(),
    ) {
      if (it) {
        val intent = Intent(this, BCScan::class.java)
        startActivity(intent)

      } else {
        Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
      }
    }

    binding.btnScan.setOnClickListener {
      requestCamera?.launch(android.Manifest.permission.CAMERA)
    }


    //val result = intent.getStringExtra(RESULT)

    //if (result != null) {
    //  if (result.contains("https://") || result.contains("http://")) {
    //    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
    //    startActivity(intent)
    //  } else {
    //    binding.result.text = result.toString()
    //  }
    //}


  }


}