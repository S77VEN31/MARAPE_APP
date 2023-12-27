package tec.ac.cr.marape.app.model

import com.google.gson.annotations.SerializedName

data class LookupResponse(var products: List<Product>) {
  data class Product(
    @SerializedName("barcode_number")
    var barcode: String,
    var title: String,
    var stores: List<Store>,
    var brand: String,
    var description: String,
    var color: String,
    var material: String,
    var size: String,
  ) {
    data class Store(
      var name: String,
      var price: String,
    )
  }
}
