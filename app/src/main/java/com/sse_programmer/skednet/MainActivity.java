package com.sse_programmer.skednet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.RegexpValidator;
import com.sse_programmer.skednet.Models.User;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnRegister;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);

        root = findViewById(R.id.root_element);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterWindow();
            }
        });

    }

    private final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Регистрация");
        dialog.setMessage("Введите данные");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);

        final MaterialEditText email = register_window.findViewById(R.id.emailField);
        final MaterialEditText password = register_window.findViewById(R.id.passField);
        final MaterialEditText name = register_window.findViewById(R.id.nameField);
        final MaterialEditText surname = register_window.findViewById(R.id.surnameField);
        final MaterialEditText nickname = register_window.findViewById(R.id.nicknameField);
        final MaterialEditText phone = register_window.findViewById(R.id.phoneField);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Принять", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {}
        });

        final AlertDialog builder = dialog.create();
        builder.show();

        builder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                Boolean validFields = true;
                if (TextUtils.isEmpty(email.getText().toString()))
                {
                    email.setError("Введите email");
                    validFields = false;
                } else if (!isValidEmail(email.getText())) {
                        //Snackbar.make(root, "Введите корректный email", Snackbar.LENGTH_LONG).show();
                        email.setError("Email недопустимого формата");
                        validFields = false;
                    }

                if (TextUtils.isEmpty(password.getText().toString()))
                {
                    password.setError("Введите пароль");
                    validFields = false;
                } else if (password.getText().toString().length() < 8) {
                        password.setError("Введите пароль длиной не короче 8 символов");
                        //Snackbar.make(root, "Введите пароль длиной не короче 8 символов", Snackbar.LENGTH_LONG).show();
                        validFields = false;
                    }

                if (TextUtils.isEmpty(name.getText().toString()))
                {
                    name.setError("Введите имя");
                    validFields = false;
                } else if (!name.validateWith(new RegexpValidator("Имя может содержать только буквы", "^[a-zA-Zа-яёА-ЯЁ]+(?:\\s[a-zA-Zа-яёА-ЯЁ]+)*$"))) {
                        //Snackbar.make(root, "Введите ваше имя", Snackbar.LENGTH_LONG).show();
                        validFields = false;
                    }

                if (TextUtils.isEmpty(surname.getText().toString()))
                {
                    surname.setError("Введите фамилию");
                    validFields = false;
                } else if (!surname.validateWith(new RegexpValidator("Фамилия может содержать только буквы", "^[a-zA-Zа-яёА-ЯЁ]+(?:\\s[a-zA-Zа-яёА-ЯЁ]+)*$"))) {
                        //Snackbar.make(root, "Введите вашу фамилию", Snackbar.LENGTH_LONG).show();
                        validFields = false;
                    }

                if (TextUtils.isEmpty(nickname.getText().toString()))
                {
                    nickname.setError("Введите никнейм");
                    validFields = false;
                } else if (!nickname.validateWith(new RegexpValidator("Никнейм может содержать только латинские буквы, цифры и '.'", "^[a-zA-Z0-9.]+(?:\\s[a-zA-Z0-9.]+)*$"))) {
                        //Snackbar.make(root, "Введите ваш никнейм", Snackbar.LENGTH_LONG).show();
                        validFields = false;
                    }

                if (TextUtils.isEmpty(phone.getText().toString()))
                {
                    phone.setError("Введите телефон");
                    validFields = false;
                } else if (phone.getText().toString().length() < 9 ||
                        !phone.validateWith(new RegexpValidator("Номер телефона должен состоять из 9 цифр", "^[0-9]+(?:\\s[0-9]+)*$"))) {
                        //Snackbar.make(root, "Введите ваш телефон", Snackbar.LENGTH_LONG).show();
                        validFields = false;
                    }

                /*Регистрация пользователя*/
                if (validFields == true) {
                    auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    User user = new User();
                                    user.setEmail(email.getText().toString());
                                    user.setName(name.getText().toString());
                                    user.setPhone(phone.getText().toString());
                                    user.setPassword(password.getText().toString());

                                    users.child(user.getEmail()) //первый параметр child() - ключевое поле таблицы.
                                            .setValue(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Snackbar.make(root, "Регистрация прошла успешно", Snackbar.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            });
                    wantToCloseDialog = true;
                }

                if(wantToCloseDialog)
                    builder.dismiss();
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
    }
}