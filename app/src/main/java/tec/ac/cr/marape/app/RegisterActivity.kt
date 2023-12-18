package tec.ac.cr.marape.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.model.User
import tec.ac.cr.marape.app.state.State


class RegisterActivity : AppCompatActivity() {

  private lateinit var btnRegister: Button
  private lateinit var userNameEntry: EditText
  private lateinit var emailEntry: EditText
  private lateinit var passwordEntry: EditText
  private lateinit var confirmPasswordEntry: EditText

  private lateinit var mAuth: FirebaseAuth
  private lateinit var dialogo: AlertDialog.Builder
  private lateinit var db: FirebaseFirestore
  private lateinit var state: State

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register)


    btnRegister = findViewById(R.id.btnEditar)
    userNameEntry = findViewById(R.id.edit_user)
    emailEntry = findViewById(R.id.edit_email)
    passwordEntry = findViewById(R.id.contrasenia)
    confirmPasswordEntry = findViewById(R.id.confirmarContrasena)


    mAuth = FirebaseAuth.getInstance()

    dialogo = AlertDialog.Builder(this)

    db = FirebaseFirestore.getInstance()
    state = State.getInstance(baseContext)

  }

  fun verifyCredentials(view: View) {
    var userName = userNameEntry.text.toString()
    var email = emailEntry.text.toString()
    var password = passwordEntry.text.toString()
    var confirmPassword = confirmPasswordEntry.text.toString()

    when {
      userName.isEmpty() -> userNameEntry.error = "El nombre de usuario no puede estar vacio"
      userName.length < 5 -> userNameEntry.error = "El nombre no puede ser menor a 5 caracteres"
      !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> emailEntry.error =  "El correo es invalido"
      password.isEmpty() -> passwordEntry.error = "La contraseña no puede estar vacia"
      password.length < 8 -> passwordEntry.error =
        "La contraseña no puede ser menor de 8 caracteres"

      confirmPassword.isEmpty() -> confirmPasswordEntry.error =
        "La confirmacion de contraseña no puede estar vacia"

      !confirmPassword.equals(password) -> confirmPasswordEntry.error =
        "Las contraseñas no coinciden"

      else -> {
        dialogo.setTitle("Proceso de Registro")
        dialogo.setMessage("Registrando usuario, espere un momento")
        dialogo.setCancelable(false)
        dialogo.show()


        //Registrar Usuario
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
          val user = User()
          user.name = userName
          user.email = email

          db.collection("users").document(email).set(user).addOnSuccessListener {
            state.user = user

            //Redireccionar al MainActivity
            val principal = Intent(this, MainActivity::class.java)
            //Iniciar la activity
            startActivity(principal)

            Toast.makeText(
              this@RegisterActivity,
              "Registrado Correctamente",
              Toast.LENGTH_SHORT
            ).show()
          }.addOnFailureListener {
            Toast.makeText(
              this@RegisterActivity,
              "No se pudo registrar",
              Toast.LENGTH_SHORT
            ).show()
          }

        }
      }
    }
  }

  fun callLoginActivity(view: View) {
    // Lógica para iniciar la actividad de registro
    val intent = Intent(this, LoginActivity::class.java)
    startActivity(intent)
  }

}