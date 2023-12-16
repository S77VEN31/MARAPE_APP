package tec.ac.cr.marape.app.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.result.registerForActivityResult
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tec.ac.cr.marape.app.CreateInventoryActivity
import tec.ac.cr.marape.app.adapter.InventoryView
import tec.ac.cr.marape.app.databinding.FragmentDashboardBinding
import tec.ac.cr.marape.app.state.State
import tec.ac.cr.marape.app.R

class DashboardFragment : Fragment() {

  private var _binding: FragmentDashboardBinding? = null
  private var recyclerView: RecyclerView? = null
  private lateinit var state: State
  private lateinit var launcher: ActivityResultLauncher<Intent>

  private val binding get() = _binding!!

  @SuppressLint("NotifyDataSetChanged")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recyclerView = binding.ownedInventoriesRecycler
    recyclerView?.setHasFixedSize(false)
    recyclerView?.adapter = InventoryView(state.inventories)
    recyclerView?.layoutManager = LinearLayoutManager(activity)

    launcher = registerForActivityResult(StartActivityForResult()) {
      // Receive update from the launched intent
      recyclerView?.adapter?.notifyDataSetChanged()
    }

    binding.floatingActionButton.setOnClickListener { _ ->
      val intent = Intent(activity, CreateInventoryActivity::class.java)
      launcher.launch(intent)
    }

  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
    state = State.getInstance(null)
    _binding = FragmentDashboardBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    recyclerView = null
  }
}
