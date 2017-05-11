package com.example.ilse.inventario;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

   NfcAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter= NfcAdapter.getDefaultAdapter(this);

       if ( adapter!=null&&adapter.isEnabled () ){
            Toast.makeText ( this,"NFC Esta Habilitado",Toast.LENGTH_LONG ).show ();
        }else{
            Toast.makeText ( this,"NFC No Esta Habilitado :(",Toast.LENGTH_LONG ).show ();
        }

        ImageButton consultar = (ImageButton) findViewById(R.id.imageButton_Escribir);
        ImageButton escribe = (ImageButton) findViewById(R.id.imageButton_Consultar);

        consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent abrir = new Intent(getApplicationContext(),ConsultarActivity.class);
                startActivity(abrir);
            }
        });
        escribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent abrir1 = new Intent(getApplicationContext(),EscribirActivity.class);
                startActivity(abrir1);
            }
        });


    }

}
