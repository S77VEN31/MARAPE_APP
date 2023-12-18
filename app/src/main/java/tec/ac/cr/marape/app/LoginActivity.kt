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


class LoginActivity : AppCompatActivity() {

  private lateinit var btnInicio: Button
  private lateinit var emailEntry: EditText
  private lateinit var passwordEntry: EditText

  private lateinit var dialogoInicio: AlertDialog.Builder
  private lateinit var mAuth: FirebaseAuth

  private lateinit var db: FirebaseFirestore
  private lateinit var state: State


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)


    btnInicio = findViewById(R.id.btnInicio)
    emailEntry = findViewById(R.id.edit_email)
    passwordEntry = findViewById(R.id.contrasenia)

    dialogoInicio = AlertDialog.Builder(this)

    mAuth = FirebaseAuth.getInstance()

    db = FirebaseFirestore.getInstance()
    state = State.getInstance(baseContext)

  }

  fun callRegisterActivity(view: View) {
    // Lógica para iniciar la actividad de registro
    val intent = Intent(this, RegisterActivity::class.java)
    startActivity(intent)
  }

  fun verifyCredentialsHome(view: View) {
    var email = emailEntry.text.toString()
    var contrasenia = passwordEntry.text.toString()

    // TODO: Extract these strings into resource files.
    when {
      !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> emailEntry.error =
        "El correo es invalido"

      contrasenia.isEmpty() -> passwordEntry.error = "La contraseña no puede estar vacia"
      contrasenia.length < 8 -> passwordEntry.error =
        "La contraseña no puede ser menor de 8 caracteres"

      else -> {
        dialogoInicio.setTitle("Iniciar Sesión")
        dialogoInicio.setMessage("Iniciando sesión, espere un momento...")
        dialogoInicio.setCancelable(false)
        dialogoInicio.show()

        //Verificar Usuario
        mAuth.signInWithEmailAndPassword(email, contrasenia).addOnSuccessListener {
          db.collection("users").document(email).get().addOnSuccessListener {
            val user = it.toObject(User::class.java)
            user?.let {
              state.user = it

              //Redireccionar al MainActivity
              val principal = Intent(this, MainActivity::class.java)
              //Iniciar la activity
              startActivity(principal)
            } ?: run {
              // TODO: Same as below, either fix the issue or at least tell the user that there's been something wrong
              finish()
            }
          }
        }.addOnFailureListener {
          Toast.makeText(
            this@LoginActivity,
            it.message,
            Toast.LENGTH_SHORT
          ).show()
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    mAuth.currentUser?.let {
      it.email?.let {
        db.collection("users").document(it).get().addOnSuccessListener {
          it.toObject(User::class.java)?.let {
            state.user = it
            val principal = Intent(this, MainActivity::class.java)
            startActivity(principal)
          }
        }.addOnFailureListener {
          Toast.makeText(this@LoginActivity, R.string.login_error, Toast.LENGTH_SHORT).show()
          // TODO: actually fix the issue instead of just killing the app
          finish()
        }
      } ?: run {
        // TODO: Actually fix the underlying issue or at least tell the user that there's something wrong gonig on
        finish()
      }
    }
  }
}