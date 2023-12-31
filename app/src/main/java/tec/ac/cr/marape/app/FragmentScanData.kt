package tec.ac.cr.marape.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.fragment.app.Fragment

class FragmentScanData : Fragment() {
  private var mostrar = false
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_scan_data, container, false)

//    if (!mostrar) {
//      view.visibility = View.GONE
//    }

    return view
  }
  fun hacerVisible() {
    mostrar = !mostrar
    if (view != null) {
      view?.visibility = if (mostrar) View.VISIBLE else View.GONE
    }
  }

  fun updateTV_Code(nuevoTexto: String) {
    view?.findViewById<TextView>(R.id.tv_code)?.text = nuevoTexto
  }
  fun updateTV_Name(nuevoTexto: String) {
    view?.findViewById<TextView>(R.id.tv_name)?.text = nuevoTexto
  }
  fun updateTV_Brand(nuevoTexto: String) {
    view?.findViewById<TextView>(R.id.tv_brand)?.text = nuevoTexto
  }
  fun updateTV_Price(nuevoTexto: String) {
    view?.findViewById<TextView>(R.id.tv_price)?.text = nuevoTexto
  }
  fun updateTV_TargetPrice(nuevoTexto: String) {
    view?.findViewById<TextView>(R.id.tv_target_price)?.text = nuevoTexto
  }
  fun updateTV_Description(nuevoTexto: String) {
    view?.findViewById<TextView>(R.id.tv_description)?.text = nuevoTexto
  }
  fun updateTV_Size(nuevoTexto: String) {
    view?.findViewById<TextView>(R.id.tv_size)?.text = nuevoTexto
  }
  fun updateTV_Color(nuevoTexto: String) {
    view?.findViewById<TextView>(R.id.tv_color)?.text = nuevoTexto
  }
  fun updateTV_Material(nuevoTexto: String) {
    view?.findViewById<TextView>(R.id.tv_material)?.text = nuevoTexto
  }


}