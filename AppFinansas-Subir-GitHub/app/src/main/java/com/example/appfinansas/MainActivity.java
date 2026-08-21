package com.example.appfinansas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appfinansas.adapter.TransaccionAdapter;
import com.example.appfinansas.firebase.FirestoreRepository;
import com.example.appfinansas.model.Transaccion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TransaccionAdapter.OnTransaccionClickListener {

    private FirestoreRepository firestoreRepository;
    private TransaccionAdapter adapter;
    private ListenerRegistration listenerRegistration;
    private LinearLayout layoutEmpty;
    private TextView textBalance;
    private TextView textIngresos;
    private TextView textGastos;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { /* La lista se actualiza sola vía SnapshotListener */ }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firestoreRepository = new FirestoreRepository();

        layoutEmpty = findViewById(R.id.layoutEmpty);
        textBalance = findViewById(R.id.textBalance);
        textIngresos = findViewById(R.id.textIngresos);
        textGastos = findViewById(R.id.textGastos);
        RecyclerView recyclerTransacciones = findViewById(R.id.recyclerTransacciones);
        FloatingActionButton fabAgregar = findViewById(R.id.fabAgregar);

        adapter = new TransaccionAdapter(this);
        recyclerTransacciones.setLayoutManager(new LinearLayoutManager(this));
        recyclerTransacciones.setAdapter(adapter);

        fabAgregar.setOnClickListener(v -> abrirFormulario(null));
    }

    @Override
    protected void onStart() {
        super.onStart();
        iniciarListenerTiempoReal();
    }

    private void iniciarListenerTiempoReal() {
        if (listenerRegistration != null) {
            return;
        }

        listenerRegistration = firestoreRepository.getTransaccionesQuery()
                .addSnapshotListener(MetadataChanges.INCLUDE, (snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, R.string.error_cargar_firestore, Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (snapshots == null) {
                        return;
                    }

                    List<Transaccion> transacciones = new ArrayList<>();
                    for (DocumentSnapshot documento : snapshots.getDocuments()) {
                        Transaccion transaccion = documento.toObject(Transaccion.class);
                        if (transaccion != null) {
                            transaccion.setId(documento.getId());
                            transacciones.add(transaccion);
                        }
                    }

                    Collections.sort(transacciones, (a, b) ->
                            Long.compare(b.getFechaOrden(), a.getFechaOrden()));

                    adapter.setTransacciones(transacciones);
                    actualizarResumen(transacciones);
                    layoutEmpty.setVisibility(transacciones.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void actualizarResumen(List<Transaccion> transacciones) {
        double totalIngresos = 0;
        double totalGastos = 0;

        for (Transaccion transaccion : transacciones) {
            if (transaccion.esIngreso()) {
                totalIngresos += transaccion.getMonto();
            } else {
                totalGastos += transaccion.getMonto();
            }
        }

        double balance = totalIngresos - totalGastos;
        textIngresos.setText(currencyFormat.format(totalIngresos));
        textGastos.setText(currencyFormat.format(totalGastos));
        textBalance.setText(currencyFormat.format(balance));
        textBalance.setTextColor(getColor(balance >= 0 ? R.color.white : R.color.gasto_light));
    }

    private void abrirFormulario(String documentId) {
        Intent intent = new Intent(this, TransaccionFormActivity.class);
        if (documentId != null && !documentId.isEmpty()) {
            intent.putExtra(TransaccionFormActivity.EXTRA_DOCUMENT_ID, documentId);
        }
        formLauncher.launch(intent);
    }

    @Override
    public void onTransaccionClick(Transaccion transaccion) {
        abrirFormulario(transaccion.getId());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
        super.onDestroy();
    }
}
