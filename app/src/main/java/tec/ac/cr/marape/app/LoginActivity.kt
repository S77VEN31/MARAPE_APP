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
import com.google.firebase.auth.FirebaseUser


class LoginActivity : AppCompatActivity() {

    private lateinit var btnInicio: Button
    private lateinit var emailEntry: EditText
    private lateinit var passwordEntry: EditText

    private lateinit var dialogoInicio: AlertDialog.Builder
    private lateinit var mAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


      btnInicio = findViewById(R.id.btnInicio)
      emailEntry = findViewById(R.id.correo)
      passwordEntry = findViewById(R.id.contrasenia)

      dialogoInicio = AlertDialog.Builder(this)

      mAuth = FirebaseAuth.getInstance()

    }

  fun callRegisterActivity(view: View) {
    // Lógica para iniciar la actividad de registro
    val intent = Intent(this, RegisterActivity::class.java)
    startActivity(intent)
  }

  fun verifyCredentialsHome(view: View){
    var email = emailEntry.text.toString()
    var contrasenia = passwordEntry.text.toString()

    if(email.isEmpty() || !email.contains("@")){
      emailEntry.error = "Correo no válido"
    }else if(contrasenia.isEmpty() || contrasenia.length < 8){
      passwordEntry.error = "Contraseña no válida"
    }else{
      dialogoInicio.setTitle("Iniciar Sesión")
      dialogoInicio.setMessage("Iniciando sesión, espere un momento...")
      dialogoInicio.setCancelable(false)
      dialogoInicio.show()


      //Verificar Usuario
      mAuth.signInWithEmailAndPassword(email, contrasenia)
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            //Redireccionar al MainActivity
            val principal = Intent(this, MainActivity::class.java)
            //Iniciar la activity
            startActivity(principal)

          } else {
            Toast.makeText(
              this@LoginActivity,
              "No se pudo iniciar sesión. Verifique correo o contraseña",
              Toast.LENGTH_SHORT
            ).show()
          }
        }
    }
  }

  override fun onStart() {
    super.onStart()
    val user: FirebaseUser? = mAuth.currentUser
    if (user != null) {
      val principal = Intent(this, MainActivity::class.java)
      // Iniciar la activity
      startActivity(principal)
    }
  }

}