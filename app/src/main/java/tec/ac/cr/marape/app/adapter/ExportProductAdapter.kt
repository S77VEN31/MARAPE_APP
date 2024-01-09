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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import me.xdrop.fuzzywuzzy.FuzzySearch
import tec.ac.cr.marape.app.EditProductActivity
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.Product


class ExportProductAdapter(var productList: List<Product>) :
  RecyclerView.Adapter<ExportProductAdapter.ViewHolder>(), Filterable {
  private val db = FirebaseFirestore.getInstance()
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
    var title = "Confirmación"
    var message = ""

    holder.productDelete.setOnClickListener {
      message =
        "Este producto no se puede eliminar ya que se encuentra asociado. ¿Desea desligar y eliminar?"
      sendConfirmation(holder.itemView.context, title, message) {
        //showResultDelete(holder.itemView.context, currentProduct.id)
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

  private fun goToEditProduct(context: Context, productId: String) {
    val intent = Intent(context, EditProductActivity::class.java)
    intent.putExtra("product_id", productId)
    context.startActivity(intent)
  }


  private fun sendConfirmation(
    context: Context, title: String, message: String, onYes: () -> Unit
  ) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(title)
    builder.setMessage(message)

    builder.setPositiveButton("Sí") { dialog, which ->
      onYes()
    }

    builder.setNegativeButton("No") { dialog, which ->
      dialog.dismiss()
    }

    val dialog = builder.create()
    dialog.show()
  }

  private fun deletePerformance(productId: String, onComplete: (Boolean) -> Unit) {
    db.collection("products").document(productId).delete().addOnSuccessListener {
      onComplete(true)
    }.addOnFailureListener {
      onComplete(false)
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

