package tec.ac.cr.marape.app.model

data class LookupResponse(var products: List<Product>) {
  data class Product(
    var barcode_number: String,
    var title: String,
    var stores: List<Store>,
  ) {
    data class Store(
      var name: String,
      var price: String,
    )
  }
}
