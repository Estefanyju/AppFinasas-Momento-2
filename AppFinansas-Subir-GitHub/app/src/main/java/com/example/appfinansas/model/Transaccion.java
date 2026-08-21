package com.example.appfinansas.model;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class Transaccion {

    public static final int TIPO_INGRESO = 1;
    public static final int TIPO_GASTO = 2;

    private static final int CONCEPTO_MIN_LENGTH = 3;

    @Exclude
    private String id;

    private String concepto;
    private double monto;
    private int tipo;

    private long fechaOrden;

    private Date fechaCreacion;

    public Transaccion() {
    }

    public Transaccion(String concepto, double monto, int tipo) {
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
    }

    public Transaccion(String id, String concepto, double monto, int tipo) {
        this.id = id;
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public long getFechaOrden() {
        return fechaOrden;
    }

    public void setFechaOrden(long fechaOrden) {
        this.fechaOrden = fechaOrden;
    }

    public boolean esIngreso() {
        return tipo == TIPO_INGRESO;
    }

    public static boolean esConceptoValido(String concepto) {
        return concepto != null && concepto.trim().length() >= CONCEPTO_MIN_LENGTH;
    }

    public static boolean esMontoValido(double monto) {
        return monto > 0;
    }

    public static int getConceptoMinLength() {
        return CONCEPTO_MIN_LENGTH;
    }
}
