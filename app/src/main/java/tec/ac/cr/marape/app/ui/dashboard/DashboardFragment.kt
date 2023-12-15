package tec.ac.cr.marape.app.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tec.ac.cr.marape.app.CreateInventoryActivity
import tec.ac.cr.marape.app.adapter.InventoryView
import tec.ac.cr.marape.app.databinding.FragmentDashboardBinding
import tec.ac.cr.marape.app.state.State

class DashboardFragment : Fragment() {

  private var _binding: FragmentDashboardBinding? = null
  private lateinit var recyclerView: RecyclerView
  private lateinit var state: State

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val dashboardViewModel =
      ViewModelProvider(this).get(DashboardViewModel::class.java)

    state = State.getInstance(null)
    _binding = FragmentDashboardBinding.inflate(inflater, container, false)
    val root: View = binding.root
    binding.floatingActionButton.setOnClickListener { _ ->
      val intent = Intent(activity, CreateInventoryActivity::class.java)
      startActivity(intent)
    }
    recyclerView = binding.ownedInventoriesRecycler
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.setHasFixedSize(false)
    recyclerView.adapter = InventoryView(state.inventories)
    return root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}