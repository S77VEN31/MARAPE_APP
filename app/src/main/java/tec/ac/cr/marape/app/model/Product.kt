package tec.ac.cr.marape.app.model

import java.io.Serializable

data class Product(
  var id: String = "",
  var name: String = "",
  var barcode: String = "",
  var brand: String = "",
  var description: String = "",
  var color: String = "",
  var material: String = "",
  var amount: Int = 0,
  var size: String = "",
  var images: ArrayList<String> = ArrayList(),
  var targetPrice: Float = 0.0f,
  var price: Float = 0.0f
) : Serializable


fun a(): String {
  val hola = "hola"
  return "${hola.toString()}"
}