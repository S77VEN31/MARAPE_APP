package tec.ac.cr.marape.app.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import me.xdrop.fuzzywuzzy.FuzzySearch
import tec.ac.cr.marape.app.EditProductActivity
import tec.ac.cr.marape.app.ExportProductsActivity
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.Product


class ExportProductAdapter(var productList: List<Product>, private val activity: ExportProductsActivity) :
  RecyclerView.Adapter<ExportProductAdapter.ViewHolder>(), Filterable {
  private val db = FirebaseFirestore.getInstance()
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private var filteredProducts = productList

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView =
      LayoutInflater.from(parent.context).inflate(R.layout.layout_export_product_item, parent, false)
    return ViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return filteredProducts.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val currentProduct = filteredProducts[position]
    holder.productPrice.text = currentProduct.price.toString()
    holder.productName.text = currentProduct.name
    holder.productAmount.text = currentProduct.amount.toString()

    holder.productDelete.setOnClickListener {
      isProductAssociated(currentProduct.id) { success ->
        if (success) {
          sendProductAssociatedDialog(holder.itemView.context)
        } else {
          sendConfirmation(holder.itemView.context) {
            showResultDelete(holder.itemView.context, currentProduct)
          }
        }
      }
    }

    holder.productEdit.setOnClickListener {
      goToEditProduct(holder.itemView.context, currentProduct.id)
    }
  }

  class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    val productPrice: android.widget.TextView = itemView.findViewById(R.id.export_product_price)
    val productName: android.widget.TextView = itemView.findViewById(R.id.export_product_name)
    val productAmount: android.widget.TextView = itemView.findViewById(R.id.export_product_amount)
    val productEdit: ImageButton = itemView.findViewById(R.id.export_edit_product_button)
    val productDelete: ImageButton = itemView.findViewById(R.id.export_delete_product_button)
  }

  private val filter: Filter = object : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {

      val query = constraint?.toString() ?: ""

      val filteredInventories = if (query.isEmpty()) productList else {
        productList.filter {
          FuzzySearch.partialRatio(it.name, query) > 50
        }
      }

      return FilterResults().apply { values = filteredInventories }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
      filteredProducts =
        if (results?.values == null) ArrayList() else results.values as ArrayList<Product>
      notifyDataSetChanged()
    }

  }

  override fun getFilter(): Filter {
    return filter
  }

  // ---------------------------------------- EDIT PRODUCT ---------------------------------------------

  init {
    setupLauncher()
  }

  private fun setupLauncher() {
    launcher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == REQUEST_CODE_EDIT_PRODUCT) {
        val updatedProductId = result.data?.getStringExtra("updatedProductId")
        updatedProductId?.let { productId ->
          // Obtener el producto actualizado de la base de datos con el ID
          db.document("products/$productId").get().addOnSuccessListener { documentSnapshot ->
            val updatedProduct = documentSnapshot.toObject(Product::class.java)
            updatedProduct?.let { product ->
              // Actualizar el producto en la lista productList
              val index = productList.indexOfFirst { it.id == productId }
              if (index != -1) {
                productList = productList.toMutableList().apply {
                  set(index, product)
                }
                notifyDataSetChanged()
              }
            }
          }
        }
      }
    }
  }

  private fun goToEditProduct(context: Context, productId: String) {
    val intent = Intent(context, EditProductActivity::class.java)
    intent.putExtra("product_id", productId)
    launcher.launch(intent)
  }

  // ----------------------------------------- DELETE PRODUCT -------------------------------------------------

  private fun sendConfirmation(
    context: Context, onYes: () -> Unit
  ) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Confirmación")
    builder.setMessage("¿Está seguro de eliminar este producto?")

    builder.setPositiveButton("Sí") { dialog, which ->
      onYes()
    }

    builder.setNegativeButton("No") { dialog, which ->
      dialog.dismiss()
    }

    val dialog = builder.create()
    dialog.show()
  }

  private fun sendProductAssociatedDialog(context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Producto asociado")
    builder.setMessage("Este producto no se puede eliminar porque está asociado a algún inventario.")

    builder.setPositiveButton("Aceptar") { dialog, _ ->
      dialog.dismiss()
    }

    val dialog = builder.create()
    dialog.show()
  }


  private fun isProductAssociated(productId: String, onComplete: (Boolean) -> Unit) {
    // Consulta para verificar si el producto está presente en algún inventario
    db.collection("inventories")
      .whereArrayContains("items", productId)
      .get()
      .addOnSuccessListener { inventoryQuerySnapshot ->
        onComplete(!inventoryQuerySnapshot.isEmpty)
      }.addOnFailureListener {
        onComplete(false) // Error al consultar los inventarios
      }
  }

  private fun removeProduct(removeProduct: Product) {
    val mutableProductList = productList.toMutableList()
    mutableProductList.remove(removeProduct)
    productList = mutableProductList.toList()
  }

  private fun deletePerformance(currentProduct: Product, onComplete: (Boolean) -> Unit) {
    db.collection("products").document(currentProduct.id).delete().addOnSuccessListener {
      removeProduct(currentProduct)
      onComplete(true)
    }.addOnFailureListener {
      onComplete(false)
    }
  }

  private fun showResultDelete(context: Context, currentProduct: Product) {
    deletePerformance(currentProduct) { success ->
      if (success) {
        Toast.makeText(
          context, "Producto eliminado con éxito", Toast.LENGTH_SHORT
        ).show()
        notifyDataSetChanged()
      } else {
        Toast.makeText(
          context, "Error al eliminar el producto", Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

}

