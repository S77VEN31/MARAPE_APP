package tec.ac.cr.marape.app

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

  private lateinit var btnRegister: Button
  private lateinit var userNameEntry: EditText
  private lateinit var emailEntry: EditText
  private lateinit var passwordEntry: EditText
  private lateinit var confirmPasswordEntry: EditText

  private lateinit var mAuth: FirebaseAuth
  private lateinit var dialogo: AlertDialog.Builder

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register)


    btnRegister = findViewById(R.id.btnRegister)
    userNameEntry = findViewById(R.id.nombre)
    emailEntry = findViewById(R.id.correo)
    passwordEntry = findViewById(R.id.contrasenia)
    confirmPasswordEntry = findViewById(R.id.confirmarContrasena)


    mAuth = FirebaseAuth.getInstance()

    dialogo = AlertDialog.Builder(this)

    }

    fun verifyCredentials(view: View){
      var userName = userNameEntry.text.toString()
      var email = emailEntry.text.toString()
      var password = passwordEntry.text.toString()
      var confirmPassword = confirmPasswordEntry.text.toString()

      if (userName.isEmpty() || userName.length < 5){
        userNameEntry.error = "Usuario no válido"
      } else if (email.isEmpty() || !email.contains("@")){
        emailEntry.error = "Email no válido"
      }else if (password.isEmpty() || password.length < 8){
        passwordEntry.error = "Conraseña no válida, mínimo 8 caracteres"
      }else if (confirmPassword.isEmpty() || !confirmPassword.equals(password)){
        confirmPasswordEntry.error = "Contraseña no válida, no coincide"
      }else{
        dialogo.setTitle("Proceso de Registro")
        dialogo.setMessage("Registrando usuario, espere un momento")
        dialogo.setCancelable(false)
        dialogo.show()


        //Registrar Usuario
        mAuth.createUserWithEmailAndPassword(email, password)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              //Redireccionar al MainActivity
              val principal = Intent(this, MainActivity::class.java)
              //Iniciar la activity
              startActivity(principal)

              Toast.makeText(
                this@RegisterActivity,
                "Registrado Correctamente",
                Toast.LENGTH_SHORT
              ).show()

            } else {
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