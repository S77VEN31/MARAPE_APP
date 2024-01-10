package tec.ac.cr.marape.app.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import me.xdrop.fuzzywuzzy.FuzzySearch
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.Product


class ExportProductAdapter(
  var productList: MutableList<Product>,
) : RecyclerView.Adapter<ExportProductAdapter.ViewHolder>(), Filterable {
  var filteredProducts = productList

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView = LayoutInflater.from(parent.context)
      .inflate(R.layout.layout_export_product_item, parent, false)
    return ViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return filteredProducts.size
  }

  private lateinit var editListener: (Int, Product) -> Unit
  fun addEditListener(listener: (Int, Product) -> Unit) {
    editListener = listener
  }

  private lateinit var deleteListener: (Int, Product) -> Unit
  fun addDeleteListener(listener: (Int, Product) -> Unit) {
    deleteListener = listener
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val currentProduct = filteredProducts[position]
    holder.productPrice.text = currentProduct.price.toString()
    holder.productName.text = currentProduct.name
    holder.productAmount.text = currentProduct.amount.toString()

    holder.productDelete.setOnClickListener {
      deleteListener(position, currentProduct)
    }

    holder.productEdit.setOnClickListener {
      editListener(position, currentProduct)
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


  // ----------------------------------------- DELETE PRODUCT -------------------------------------------------
  fun removeProduct(position: Int, product: Product) {
    val idx = productList.indexOfFirst {
      it.id == product.id
    }
    if (idx != -1) {
      productList.removeAt(idx)
    }
    if (filteredProducts != productList) {
      filteredProducts.removeAt(position)
    }
    notifyItemRemoved(position)
  }

  fun update(position: Int, product: Product) {
    val idx = productList.indexOfFirst {
      it.id == product.id
    }
    if (idx != -1) {
      productList[idx] = product
    }

    if (productList != filteredProducts) {
      filteredProducts[position] = product
    }
    notifyItemChanged(position)
  }
}

