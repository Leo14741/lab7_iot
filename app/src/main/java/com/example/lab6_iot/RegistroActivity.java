package com.example.lab6_iot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.lab6_iot.databinding.ActivityRegistroBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class RegistroActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    ActivityRegistroBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        binding.registro2.setOnClickListener(v -> {

            String correo = binding.email.getEditableText().toString();
            String contrasena = binding.editTextContrasena.getEditableText().toString();
            String rol = "Usuario";

            if (correo.isEmpty()) {
                showError("El campo 'Nombre' no puede estar vacío.");
            } else {
                // Agregar  lógica de registro
                // ...
                userdto usuario = new userdto();
                usuario.setCorreo(correo);
                usuario.setTipo(rol);


                db.collection("users")
                        .document(correo)
                        .set(usuario)
                        .addOnSuccessListener(unused -> {

                            Log.d("msg-test", " Entre a la coleccion" );

                            mAuth.createUserWithEmailAndPassword(correo, contrasena)
                                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            Log.d("msg-test", " OnComplete" );
                                            if (task.isSuccessful()) {
                                                Log.d("msg-test", " isSuccessful" );
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                updateUI(user);
                                                Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                                                intent.putExtra("registroExitoso", true); // Agregar una marca de registro exitoso al intent
                                                startActivity(intent);
                                                finish(); // Finalizar la actividad actual
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.d("msg-test", " Fail" );
                                                updateUI(null);
                                                Toast.makeText(RegistroActivity.this, "Algo pasó al guardar ", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(e -> {
                                        // Maneja la excepción que ocurra al intentar obtener los documentos
                                        Log.e("msg-test", "Excepción al ingresar datos al documento de la colección usuarios: ", e);
                                        Toast.makeText(RegistroActivity.this, "Error al ingresar datos del usuario.", Toast.LENGTH_SHORT).show();
                                    });




                            /*Intent intent = new Intent(this, MainActivity.class);
                            intent.putExtra("registroExitoso", true); // Agregar una marca de registro exitoso al intent
                            startActivity(intent);
                            finish(); // Finalizar la actividad actual*/
                        })
                        .addOnFailureListener(e -> {
                            Log.e("msg-test", "Excepción al ingresar datos al documento de la colección usuarios: ", e);
                            Toast.makeText(this, "Algo pasó al guardar ", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void updateUI(FirebaseUser user) {

    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidPassword(String password) {
        return password.length() >= 8 && Pattern.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*", password);
    }
}