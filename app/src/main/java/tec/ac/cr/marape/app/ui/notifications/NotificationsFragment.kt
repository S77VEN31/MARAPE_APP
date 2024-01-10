package tec.ac.cr.marape.app.ui.notifications

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import tec.ac.cr.marape.app.EditProfile
import tec.ac.cr.marape.app.ExportProductsActivity
import tec.ac.cr.marape.app.LoginActivity
import tec.ac.cr.marape.app.R
import tec.ac.cr.marape.app.databinding.FragmentNotificationsBinding
import tec.ac.cr.marape.app.state.State


class NotificationsFragment : Fragment() {

  private var _binding: FragmentNotificationsBinding? = null
  private val binding get() = _binding!!

  private lateinit var mAuth: FirebaseAuth
  private var launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult(), ::activityResultHandler
  )

  private lateinit var state: State
  private var checked = false

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

    val root: View = binding.root

    mAuth = FirebaseAuth.getInstance()
    state = State.getInstance()

    val sharedPreferences = requireContext().getSharedPreferences(
      "user_preferences_${state.user.email}", Context.MODE_PRIVATE
    )
    checked = sharedPreferences.getBoolean("price_target", false)
    return root
  }

  private fun activityResultHandler(result: ActivityResult) {
    when (result.resultCode) {
      Activity.RESULT_OK -> {
        setUserData()
      }
    }
  }

  private fun setUserData() {
    binding.etEmail.text = state.user.email
    binding.etUser.text = state.user.name
    binding.etCountry.text = state.user.country
    binding.etPhone.text = state.user.phone
    binding.switchPriceTarget.isChecked = checked
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setUserData()

    // Botón para cerrar sesión
    binding.btnSignOff.setOnClickListener {
      mAuth.signOut()
      // Aquí puedes realizar la transición a la actividad LoginActivity
      val intent = Intent(requireContext(), LoginActivity::class.java)
      launcher.launch(intent)
      requireActivity().finish()
    }

    // Botón para eliminar la cuenta
    binding.deleteAccount.setOnClickListener {
      deleteAccount()
    }

    // Botón para editar perfil
    binding.floatingActionButton.setOnClickListener {
      val edit = Intent(requireContext(), EditProfile::class.java)
      launcher.launch(edit)
    }


    binding.switchPriceTarget.setOnClickListener {
      val currentState = binding.switchPriceTarget.isChecked
      savePreference(currentState, state.user.email)
    }


    binding.btnExportProducts.setOnClickListener(this::exportProducts)
  }

  private fun savePreference(currentState: Boolean, email: String) {
    val sharedPreferences =
      requireContext().getSharedPreferences("user_preferences_$email", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putBoolean("price_target", currentState)
    editor.apply()
  }


  private fun deleteAccount() {
    val builder = AlertDialog.Builder(context)
    builder.setCancelable(true)
    builder.setMessage(R.string.account_deletion_confirmation_message)
    builder.setTitle(R.string.account_deletion_title)
    builder.setPositiveButton(R.string.account_deletion_confirm_button_text) { _, _ ->
      mAuth.currentUser?.delete()?.addOnSuccessListener {
        launcher.launch(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
      }?.addOnFailureListener {
        Toast.makeText(
          requireContext(),
          it.message, // Here I removed the rest of the message because it wouldn't show, and it's more important
          // to let the user know what's wrong rather than to shadow it by an useless message.
          Toast.LENGTH_SHORT
        ).show()
      }
    }
    builder.setNegativeButton(R.string.action_cancel) { self, _ ->
      self.cancel()
    }
    builder.create().show()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private fun exportProducts(view: View) {
    val intent = Intent(requireContext(), ExportProductsActivity::class.java)
    launcher.launch(intent)
  }

}
