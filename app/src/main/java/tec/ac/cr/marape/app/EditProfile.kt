package tec.ac.cr.marape.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.state.State

class EditProfile : AppCompatActivity() {

  private lateinit var mAuth: FirebaseAuth
  private lateinit var db: FirebaseFirestore
  private lateinit var state: State

  private lateinit var etEmail: EditText
  private lateinit var etUserName: EditText
  private lateinit var spinner: Spinner
  private lateinit var etPhone: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_profile)

    mAuth = FirebaseAuth.getInstance()
    db = FirebaseFirestore.getInstance()
    state = State.getInstance(this)

    etEmail = findViewById(R.id.edit_email)
    etUserName = findViewById(R.id.edit_user)
    spinner = findViewById(R.id.spinner)
    etPhone = findViewById(R.id.editTextPhone)

    // Configurar el campo de correo electrónico como no editable
    etEmail.isEnabled = false

    // Cargar los datos del usuario en los elementos de la interfaz de usuario
    etEmail.setText(state.user.email)
    etUserName.setText(state.user.name)
    etPhone.setText(state.user.phone)

    spinnerExample()
  }

  fun spinnerExample() {
    val elementos = listOf(
      "Argentina", "Brasil", "Canadá", "Dinamarca", "Egipto",
      "Francia", "Grecia", "Honduras", "India", "Japón",
      "Kenia", "Líbano", "México", "Noruega", "Omán",
      "Perú", "Qatar", "Rusia", "Suecia", "Tailandia"
    )
    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, elementos)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = adapter

    val userCountry = state.user.country
    val position = elementos.indexOf(userCountry)
    if (position != -1) {
      spinner.setSelection(position)
    }
  }

  fun saveChanges(view: View) {
    val newUserName = etUserName.text.toString()
    val newCountry = spinner.selectedItem.toString()
    val newPhone = etPhone.text.toString()

    val user = state.user
    user.name = newUserName
    user.country = newCountry
    user.phone = newPhone

    db.collection("users")
      .document(user.email)
      .set(user)
      .addOnSuccessListener {
        state.user = user
        Toast.makeText(
          this@EditProfile,
          "Cambios guardados correctamente",
          Toast.LENGTH_SHORT
        ).show()
        setResult(Activity.RESULT_OK)
        finish()
      }
      .addOnFailureListener {
        Toast.makeText(
          this@EditProfile,
          "No se pudieron guardar los cambios",
          Toast.LENGTH_SHORT
        ).show()
      }
  }


}
