package tec.ac.cr.marape.app.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import me.xdrop.fuzzywuzzy.FuzzySearch
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.model.Inventory
import tec.ac.cr.marape.app.model.Product


const val EDITED_PRODUCT = 7

class ProductListAdapter(
  var productList: MutableList<Product>, private val inventory: Inventory
) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>(), Filterable {
  private lateinit var launcher: ActivityResultLauncher<Intent>
  private var filteredProducts = productList

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView =
      LayoutInflater.from(parent.context).inflate(R.layout.layout_product_item, parent, false)
    return ViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return filteredProducts.size
  }

  private lateinit var unlinkHandler: (Int, Product) -> Unit
  fun addUnlinkListener(handler: (Int, Product) -> Unit) {
    unlinkHandler = handler
  }

  private lateinit var editHandler: (Int, Product) -> Unit
  fun addEditListener(handler: (Int, Product) -> Unit) {
    editHandler = handler
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val currentProduct = filteredProducts[position]
    holder.productPrice.text = currentProduct.price.toString()
    holder.productName.text = currentProduct.name
    holder.productAmount.text = currentProduct.amount.toString()

    holder.productEdit.setOnClickListener {
      editHandler(position, currentProduct)
    }

    holder.productUnlink.setOnClickListener {
      unlinkHandler(position, currentProduct)
    }

  }

  class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    val productPrice: android.widget.TextView = itemView.findViewById(R.id.product_price)
    val productName: android.widget.TextView = itemView.findViewById(R.id.product_name)
    val productAmount: android.widget.TextView = itemView.findViewById(R.id.product_amount)
    val productEdit: ImageButton = itemView.findViewById(R.id.edit_product_button)
    val productUnlink: ImageButton = itemView.findViewById(R.id.unlink_product_button)
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

  fun removeProduct(position: Int, product: Product) {
    val idx = productList.indexOfFirst {
      it.id == product.id
    }

    if (idx != -1) {
      productList.removeAt(idx)
    }

    if (productList != filteredProducts) {
      filteredProducts.removeAt(position)
    }

    notifyItemRemoved(position)
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

