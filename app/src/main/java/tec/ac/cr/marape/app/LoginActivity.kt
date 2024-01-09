package tec.ac.cr.marape.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import tec.ac.cr.marape.app.model.Inventory
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

    passwordEntry = findViewById(R.id.login_contrasenia)

    dialogoInicio = AlertDialog.Builder(this)

    mAuth = FirebaseAuth.getInstance()

    db = FirebaseFirestore.getInstance()
    state = State.getInstance()

  }

  fun launchRegisterActivity(view: View) {
    // Lógica para iniciar la actividad de registro
    val intent = Intent(this, RegisterActivity::class.java)
    startActivity(intent)
    finish()
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
        // TODO: Fix this, whoever made this forgot to use the builder like it was intended to be used.
        dialogoInicio.setTitle("Iniciar Sesión")
        dialogoInicio.setMessage("Iniciando sesión, espere un momento...")
        dialogoInicio.setCancelable(false)
        val dialog = dialogoInicio.show()

        //Verificar Usuario
        mAuth.signInWithEmailAndPassword(email, contrasenia).addOnSuccessListener {
          db.collection("users").document(email).get().addOnSuccessListener(::doInitialLogin)
            .addOnFailureListener {
              Toast.makeText(this@LoginActivity, it.toString(), Toast.LENGTH_LONG).show()
              dialog.cancel()
            }
        }.addOnFailureListener {
          Toast.makeText(
            this@LoginActivity, it.message, Toast.LENGTH_SHORT
          ).show()
          dialog.cancel()
        }
      }
    }
  }

  private fun doInitialLogin(userRef: DocumentSnapshot) {
    Log.d("Z:Login:User", userRef.reference.path)
    userRef.toObject(User::class.java)?.let {
      state.user = it
      state.user.ref = userRef.reference
      launchMainActivity()
    }
  }

  override fun onStart() {
    super.onStart()
    mAuth.currentUser?.let {
      it.email?.let {
        db.collection("users").document(it).get().addOnSuccessListener(::doInitialLogin)
          .addOnFailureListener {
            Toast.makeText(this@LoginActivity, R.string.login_error, Toast.LENGTH_SHORT).show()
            // TODO: see if this changes anything
            //finish()
          }
      } ?: run {
        // TODO: Actually fix the underlying issue or at least tell the user that there's something wrong gonig on
        finish()
      }
    }
  }

  private fun launchMainActivity() {
    val intent = Intent(this, MainActivity::class.java)

    db.collection("inventories").where(Filter.equalTo("ownerEmail", state.user.email)).get()
      .addOnSuccessListener { snapshot ->
        // Clear the inventories before loading any new ones
        state.inventories.clear()
        snapshot.documents.iterator().forEach { inventorySnapshot ->
          val inventory = inventorySnapshot.toObject(Inventory::class.java)
          inventory?.id = inventorySnapshot.id
          inventory?.let { state.inventories.add(it) }
        }

        startActivity(intent)
        finish()
      }
    // Add inventory if my email is in the invitedUsers list
    db.collection("inventories").where(Filter.arrayContains("invitedUsers", state.user.email)).get()
      .addOnSuccessListener { snapshot ->
        // Clear the inventories before loading any new ones
        state.sharedInventories.clear()
        snapshot.documents.iterator().forEach { inventorySnapshot ->
          val inventory = inventorySnapshot.toObject(Inventory::class.java)
          inventory?.id = inventorySnapshot.id
          inventory?.let { state.sharedInventories.add(it) }
        }
        // TODO: Find a non blocking way of doing this
        startActivity(intent)
        finish()
      }
  }
}

