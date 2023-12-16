package tec.ac.cr.marape.app.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import tec.ac.cr.marape.app.LoginActivity
import tec.ac.cr.marape.app.databinding.FragmentNotificationsBinding


class NotificationsFragment : Fragment() {

  private var _binding: FragmentNotificationsBinding? = null
  private val binding get() = _binding!!

  private lateinit var mAuth: FirebaseAuth

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
    val root: View = binding.root

    mAuth = FirebaseAuth.getInstance()
    val user: FirebaseUser? = mAuth.currentUser

    // Accede a tus vistas a través de binding
    user?.email?.let {
      binding.etEmail.text = it
    }

    // Botón para cerrar sesión
    binding.btnSignOff.setOnClickListener {
      mAuth.signOut()
      // Aquí puedes realizar la transición a la actividad LoginActivity
      val intent = Intent(requireContext(), LoginActivity::class.java)
      startActivity(intent)
    }

    // Botón para eliminar la cuenta
    binding.deleteAccount.setOnClickListener {
      deleteAccount()
    }


    return root
  }

  private fun deleteAccount() {
    val user = mAuth.currentUser

    user?.delete()?.addOnSuccessListener {
      startActivity(Intent(requireContext(), LoginActivity::class.java))
    }?.addOnFailureListener {
      Toast.makeText(
        requireContext(),
        "Error al eliminar la cuenta: ${it.message}",
        Toast.LENGTH_SHORT
      ).show()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
