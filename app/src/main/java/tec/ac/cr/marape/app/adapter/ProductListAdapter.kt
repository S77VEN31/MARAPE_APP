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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import me.xdrop.fuzzywuzzy.FuzzySearch
import tec.ac.cr.marape.app.EditProductActivity
import tec.ac.cr.marape.app.ProductListActivity
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.Product


const val REQUEST_CODE_EDIT_PRODUCT = 7

class ProductListAdapter(var productList: List<Product>,
                         private val inventory: Inventory, private val activity: ProductListActivity ) :
  RecyclerView.Adapter<ProductListAdapter.ViewHolder>(), Filterable {
  private val db = FirebaseFirestore.getInstance()
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private var filteredProducts = productList

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

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView =
      LayoutInflater.from(parent.context).inflate(R.layout.layout_product_item, parent, false)
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

    holder.productEdit.setOnClickListener {
      goToEditProduct(holder.itemView.context, currentProduct.id)
    }

    holder.productUnlink.setOnClickListener {
      sendConfirmation(holder.itemView.context, onYes = {
        showResultUnlink(holder.itemView.context, currentProduct)
      })
    }

  }

  class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    val productPrice: android.widget.TextView = itemView.findViewById(R.id.product_price)
    val productName: android.widget.TextView = itemView.findViewById(R.id.product_name)
    val productAmount: android.widget.TextView = itemView.findViewById(R.id.product_amount)
    val productEdit: ImageButton = itemView.findViewById(R.id.edit_product_button)
    val productUnlink: ImageButton = itemView.findViewById(R.id.unlink_product_button)
  }

  private fun goToEditProduct(context: Context, productId: String) {
    val intent = Intent(context, EditProductActivity::class.java)
    intent.putExtra("product_id", productId)
    launcher.launch(intent)
  }


  private fun sendConfirmation(
    context: Context, onYes: () -> Unit
  ) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Confirmación")
    builder.setMessage("¿Estás seguro de que quieres desligar este producto del actual inventario?")

    builder.setPositiveButton("Sí") { dialog, which ->
      onYes()
    }

    builder.setNegativeButton("No") { dialog, which ->
      dialog.dismiss()
    }

    val dialog = builder.create()
    dialog.show()
  }


  private fun unlinkPerformance(currentProduct: Product, onComplete: (Boolean) -> Unit) {
    db.collection("inventories").document(inventory.id)
      .update("items", FieldValue.arrayRemove(currentProduct.id)).addOnSuccessListener {
        removeProduct(currentProduct)
        onComplete(true)
      }.addOnFailureListener {
        onComplete(false)
      }
  }


  private fun removeProduct(removeProduct: Product) {
    val mutableProductList = productList.toMutableList()
    mutableProductList.remove(removeProduct)
    productList = mutableProductList.toList()
  }


  private fun showResultUnlink(context: Context, currentProduct: Product) {
    unlinkPerformance(currentProduct) { success ->
      if (success) {
        Toast.makeText(
          context, "Producto desligado con éxito", Toast.LENGTH_SHORT
        ).show()
        notifyDataSetChanged()
      } else {
        Toast.makeText(
          context, "Error al desligar el producto", Toast.LENGTH_SHORT
        ).show()
      }
    }
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


}

