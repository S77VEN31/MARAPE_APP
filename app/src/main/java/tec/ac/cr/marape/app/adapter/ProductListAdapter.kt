package tec.ac.cr.marape.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tec.ac.cr.marape.app.model.Product
import tec.ac.cr.marape.app.R

class ProductListAdapter(val productList: List<Product>) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_product_item, parent, false)
    return ViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return productList.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val currentProduct = productList[position]
    holder.productPrice.text = currentProduct.price.toString()
    holder.productName.text = currentProduct.name
    holder.productAmount.text = currentProduct.amount.toString()
  }

  class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    val productPrice: android.widget.TextView = itemView.findViewById(R.id.product_price)
    val productName: android.widget.TextView = itemView.findViewById(R.id.product_name)
    val productAmount: android.widget.TextView = itemView.findViewById(R.id.product_amount)

  }
}
