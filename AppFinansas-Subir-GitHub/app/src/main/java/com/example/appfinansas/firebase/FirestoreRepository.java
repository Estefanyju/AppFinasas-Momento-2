package com.example.appfinansas.firebase;

import com.example.appfinansas.model.Transaccion;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirestoreRepository {

    public static final String COLECCION_TRANSACCIONES = "transacciones";

    private static boolean configuracionAplicada = false;

    private final FirebaseFirestore firestore;

    public FirestoreRepository() {
        firestore = FirebaseFirestore.getInstance();
        aplicarConfiguracionUnaVez();
    }

    private static synchronized void aplicarConfiguracionUnaVez() {
        if (configuracionAplicada) {
            return;
        }
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        configuracionAplicada = true;
    }

    public CollectionReference getTransaccionesRef() {
        return firestore.collection(COLECCION_TRANSACCIONES);
    }

    public Query getTransaccionesQuery() {
        return getTransaccionesRef();
    }

    public Task<DocumentReference> crear(Transaccion transaccion) {
        DocumentReference referencia = getTransaccionesRef().document();
        long ahora = System.currentTimeMillis();

        Map<String, Object> datos = new HashMap<>();
        datos.put("concepto", transaccion.getConcepto());
        datos.put("monto", transaccion.getMonto());
        datos.put("tipo", transaccion.getTipo());
        datos.put("fechaOrden", ahora);
        datos.put("fechaCreacion", new Date(ahora));

        return referencia.set(datos).continueWithTask(task -> {
            if (task.isSuccessful()) {
                return Tasks.forResult(referencia);
            }
            Exception error = task.getException();
            return Tasks.forException(error != null ? error : new Exception("Error desconocido al guardar"));
        });
    }

    public Task<Void> actualizar(String documentId, Transaccion transaccion) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("concepto", transaccion.getConcepto());
        datos.put("monto", transaccion.getMonto());
        datos.put("tipo", transaccion.getTipo());
        return getTransaccionesRef()
                .document(documentId)
                .update(datos);
    }

    public Task<Void> eliminar(String documentId) {
        return getTransaccionesRef()
                .document(documentId)
                .delete();
    }

    public Task<DocumentSnapshot> obtenerPorId(String documentId) {
        return getTransaccionesRef()
                .document(documentId)
                .get();
    }
}
