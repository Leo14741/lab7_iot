package com.example.lab6_iot;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.lab6_iot.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    HashMap<String, String> credencial = new HashMap<>();
    ActivityMainBinding binding;
    String channelId = "channelDefaultPri";
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private List<userdto> usuarioLista = new ArrayList<>();
    userdto usuario = new userdto();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        createNotificationChannel();

        if (getIntent().getBooleanExtra("registroExitoso", false)) {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
        }

        binding.iniciarSesion.setOnClickListener(v -> {
            String email = binding.email.getEditableText().toString();
            String pass = binding.editTextContrasena.getEditableText().toString();
            Log.d("msg-test", email + " " + pass);

            if (isValidEmail(email)) {
                Log.d("msg-test", "email valido");
                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("msg-test", "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String email = user.getEmail();
                                    Log.d("msg-test", "El correo es: " + email);

                                    db.collection("usuarios")
                                            .get()
                                            .addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    QuerySnapshot usuariosCollection = task2.getResult();
                                                    Log.d("msg-test", "taskowo ha sido valido");
                                                    for (QueryDocumentSnapshot document : usuariosCollection) {
                                                        String correo = (String) document.get("correo");
                                                        String rol = (String) document.get("tipo");

                                                        if (correo.equals(email)) {
                                                            usuario.setCorreo(correo);
                                                            usuario.setTipo(rol);
                                                            break;
                                                        }
                                                    }
                                                    if (usuario.getTipo().equals("Gestor de salon de belleza")) {
                                                        Log.d("msg-test", "Entra rol usuario");
                                                        Intent intent = new Intent(MainActivity.this, GestorActivity.class);
                                                        startActivity(intent);
                                                    } else if (usuario.getTipo().equals("Cliente")) {
                                                        Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                                                        Log.d("msg-test", "Entra rol delegado actividad");
                                                        //intent.putExtra("usuario", usuario);
                                                        startActivity(intent);
                                                    }

                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                // Maneja la excepción que ocurra al intentar obtener los documentos
                                                Log.e("msg-test", "Excepción al obtener documentos de la colección usuarios: ", e);
                                                Toast.makeText(MainActivity.this, "Error al obtener datos del usuario.", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            }
                        })
                        .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Manejar la excepción
                                Log.e("msg-test", "Exception: " + e.getMessage());
                            }
                        });
            } else {
                Toast.makeText(this, "El correo electrónico ingresado no es correcto", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void registrarse(View view) {
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }

    public void createNotificationChannel() {
        //android.os.Build.VERSION_CODES.O == 26
        NotificationChannel channel = new NotificationChannel(channelId,
                "Canal notificaciones default",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("oa");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        askPermission();

    }

    public void askPermission() {
        //android.os.Build.VERSION_CODES.TIRAMISU == 33
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{POST_NOTIFICATIONS}, 101);
        }
    }


    // Función para validar el formato del correo electrónico
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.matches(emailRegex, email);
    }
}
