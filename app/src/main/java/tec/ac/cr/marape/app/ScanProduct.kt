package tec.ac.cr.marape.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import tec.ac.cr.marape.app.databinding.ActivityScanProductBinding
import tec.ac.cr.marape.app.networking.RemoteApi

class ScanProduct : AppCompatActivity() {
  private var requestCamera: ActivityResultLauncher<String>? = null
  private lateinit var binding: ActivityScanProductBinding
  private lateinit var scannedCode: String
//  private val fragment = FragmentScanData()


  private val resultLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val data: Intent? = result.data
        scannedCode = data?.getStringExtra("scanned_code")!!

//        fragment.hacerVisible()
        fetchApiData(scannedCode)
        Toast.makeText(this, "Código escaneado: $scannedCode", Toast.LENGTH_SHORT).show()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = resources.getString(R.string.title_scan_product)

    binding = ActivityScanProductBinding.inflate(layoutInflater)
    setContentView(binding.root)


//    supportFragmentManager.beginTransaction()
//      .replace(R.id.fragmentContainerView, fragment)
//      .commit()

    binding.btnCancel.setOnClickListener {
      if (::scannedCode.isInitialized && scannedCode.isNotEmpty()) {
        binding.tvCode.text = scannedCode
        Toast.makeText(this, "asd: $scannedCode", Toast.LENGTH_SHORT).show()
      }
//      fragment.hacerVisible()
    }
    requestCamera = registerForActivityResult(
      ActivityResultContracts
        .RequestPermission(),
    ) {
      if (it) {
        val intent = Intent(this, BCScan::class.java)
        resultLauncher.launch(intent)
      } else {
        Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
      }
    }
    binding.btnScan.setOnClickListener {
//      fragment.hacerVisible()
      requestCamera?.launch(android.Manifest.permission.CAMERA)
    }
  }

  private fun fetchApiData(code: String) {
    RemoteApi.getProduct(code, { res ->
      if (res.products.isNotEmpty()) {
        val prod = res.products[0]
        val target = prod.stores.find {
          it.name.contains(
            "target",
            true
          )
        }
        runOnUiThread {
          binding.tvCode.text = code
          binding.tvName.text = prod.title
          binding.tvBrand.text = prod.brand
          binding.tvDescription.text = prod.description
          binding.tvColor.text = prod.color
          binding.tvMaterial.text = prod.material
          binding.tvSize.text = prod.size

          target?.let {
            binding.tvTargetPrice.text = it.price
          }
        }
      }
    }, {
      runOnUiThread {
        Toast.makeText(
          this@ScanProduct,
          it.message,
          Toast.LENGTH_LONG
        ).show()
      }
    })
  }
}