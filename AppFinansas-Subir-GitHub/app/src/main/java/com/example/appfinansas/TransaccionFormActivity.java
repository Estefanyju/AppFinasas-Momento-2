package com.example.appfinansas;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appfinansas.firebase.FirestoreRepository;
import com.example.appfinansas.model.Transaccion;
import com.example.appfinansas.util.MontoParser;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TransaccionFormActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "extra_document_id";
    private static final long TIMEOUT_MS = 12000;

    private FirestoreRepository firestoreRepository;
    private TextInputLayout tilConcepto;
    private TextInputLayout tilMonto;
    private TextInputEditText editConcepto;
    private TextInputEditText editMonto;
    private RadioGroup radioGroupTipo;
    private RadioButton radioIngreso;
    private RadioButton radioGasto;
    private Button btnGuardar;
    private Button btnEliminar;

    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;

    private String documentId = null;
    private boolean guardando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaccion_form);

        firestoreRepository = new FirestoreRepository();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tilConcepto = findViewById(R.id.tilConcepto);
        tilMonto = findViewById(R.id.tilMonto);
        editConcepto = findViewById(R.id.editConcepto);
        editMonto = findViewById(R.id.editMonto);
        radioGroupTipo = findViewById(R.id.radioGroupTipo);
        radioIngreso = findViewById(R.id.radioIngreso);
        radioGasto = findViewById(R.id.radioGasto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);

        configurarValidacionesTiempoReal();

        documentId = getIntent().getStringExtra(EXTRA_DOCUMENT_ID);
        if (!TextUtils.isEmpty(documentId)) {
            toolbar.setTitle(R.string.titulo_editar);
            cargarTransaccion(documentId);
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            toolbar.setTitle(R.string.titulo_nueva);
            radioIngreso.setChecked(true);
        }

        btnGuardar.setOnClickListener(v -> guardarTransaccion());
        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
    }

    @Override
    protected void onDestroy() {
        cancelarTimeout();
        super.onDestroy();
    }

    private void configurarValidacionesTiempoReal() {
        editConcepto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarConceptoEnTiempoReal(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editMonto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validarMontoEnTiempoReal(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void validarConceptoEnTiempoReal(String concepto) {
        if (concepto.trim().isEmpty()) {
            tilConcepto.setError(null);
            return;
        }
        if (!Transaccion.esConceptoValido(concepto)) {
            tilConcepto.setError(getString(
                    R.string.error_concepto_corto,
                    Transaccion.getConceptoMinLength()
            ));
        } else {
            tilConcepto.setError(null);
        }
    }

    private void validarMontoEnTiempoReal(String montoTexto) {
        if (montoTexto.trim().isEmpty()) {
            tilMonto.setError(null);
            return;
        }

        try {
            double monto = MontoParser.parse(montoTexto);
            if (monto <= 0) {
                tilMonto.setError(getString(R.string.error_monto_cero));
            } else {
                tilMonto.setError(null);
            }
        } catch (NumberFormatException e) {
            tilMonto.setError(getString(R.string.error_monto_invalido));
        }
    }

    private void cargarTransaccion(String id) {
        setGuardando(true);
        iniciarTimeout();

        firestoreRepository.obtenerPorId(id)
                .addOnCompleteListener(task -> {
                    cancelarTimeout();
                    setGuardando(false);

                    if (!task.isSuccessful() || task.getResult() == null) {
                        mostrarErrorFirebase(task.getException());
                        finish();
                        return;
                    }

                    var documento = task.getResult();
                    Transaccion transaccion = documento.toObject(Transaccion.class);
                    if (transaccion == null || !documento.exists()) {
                        Toast.makeText(this, R.string.error_transaccion_no_encontrada, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    transaccion.setId(documento.getId());
                    editConcepto.setText(transaccion.getConcepto());
                    editMonto.setText(String.valueOf(transaccion.getMonto()));

                    if (transaccion.esIngreso()) {
                        radioIngreso.setChecked(true);
                    } else {
                        radioGasto.setChecked(true);
                    }
                });
    }

    private void guardarTransaccion() {
        if (guardando) {
            return;
        }

        String concepto = editConcepto.getText() != null
                ? editConcepto.getText().toString().trim()
                : "";
        String montoTexto = editMonto.getText() != null
                ? editMonto.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(concepto)) {
            tilConcepto.setError(getString(R.string.error_concepto_vacio));
            return;
        }

        if (!Transaccion.esConceptoValido(concepto)) {
            tilConcepto.setError(getString(
                    R.string.error_concepto_corto,
                    Transaccion.getConceptoMinLength()
            ));
            return;
        }

        if (TextUtils.isEmpty(montoTexto)) {
            tilMonto.setError(getString(R.string.error_monto_vacio));
            return;
        }

        double monto;
        try {
            monto = MontoParser.parse(montoTexto);
        } catch (NumberFormatException e) {
            tilMonto.setError(getString(R.string.error_monto_invalido));
            return;
        }

        if (!Transaccion.esMontoValido(monto)) {
            tilMonto.setError(getString(R.string.error_monto_cero));
            return;
        }

        int tipo = radioIngreso.isChecked()
                ? Transaccion.TIPO_INGRESO
                : Transaccion.TIPO_GASTO;

        Transaccion transaccion = new Transaccion(concepto, monto, tipo);
        setGuardando(true);
        iniciarTimeout();

        Task<?> operacion;
        if (!TextUtils.isEmpty(documentId)) {
            operacion = firestoreRepository.actualizar(documentId, transaccion);
        } else {
            operacion = firestoreRepository.crear(transaccion);
        }

        operacion.addOnCompleteListener(task -> {
            cancelarTimeout();

            if (task.isSuccessful()) {
                int mensaje = TextUtils.isEmpty(documentId)
                        ? R.string.mensaje_guardado
                        : R.string.mensaje_actualizado;
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                setGuardando(false);
                mostrarErrorFirebase(task.getException());
            }
        });
    }

    private void confirmarEliminacion() {
        if (guardando || TextUtils.isEmpty(documentId)) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_eliminar)
                .setMessage(R.string.mensaje_confirmar_eliminar)
                .setPositiveButton(R.string.accion_eliminar, (dialog, which) -> eliminarTransaccion())
                .setNegativeButton(R.string.accion_cancelar, null)
                .show();
    }

    private void eliminarTransaccion() {
        setGuardando(true);
        iniciarTimeout();

        firestoreRepository.eliminar(documentId)
                .addOnCompleteListener(task -> {
                    cancelarTimeout();

                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.mensaje_eliminado, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        setGuardando(false);
                        mostrarErrorFirebase(task.getException());
                    }
                });
    }

    private void mostrarErrorFirebase(Exception error) {
        String detalle = error != null && error.getMessage() != null
                ? error.getMessage()
                : getString(R.string.error_guardar_firestore);
        Toast.makeText(this, detalle, Toast.LENGTH_LONG).show();
    }

    private void iniciarTimeout() {
        cancelarTimeout();
        timeoutRunnable = () -> {
            if (guardando) {
                setGuardando(false);
                Toast.makeText(this, R.string.error_guardar_timeout, Toast.LENGTH_LONG).show();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MS);
    }

    private void cancelarTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private void setGuardando(boolean enProgreso) {
        guardando = enProgreso;
        btnGuardar.setEnabled(!enProgreso);
        btnEliminar.setEnabled(!enProgreso);
        editConcepto.setEnabled(!enProgreso);
        editMonto.setEnabled(!enProgreso);
        radioIngreso.setEnabled(!enProgreso);
        radioGasto.setEnabled(!enProgreso);
        btnGuardar.setText(enProgreso ? R.string.guardando : R.string.accion_guardar);
    }
}
