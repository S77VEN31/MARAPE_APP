package tec.ac.cr.marape.app.model

data class Product(
    var name: String,
    var barcode: String,
    var brand: String,
    var description: String,
    var color: String,
    var material: String,
    var size: String,
    var images: List<String>,
    var targetPrice: Float,
    var price: Float
)
