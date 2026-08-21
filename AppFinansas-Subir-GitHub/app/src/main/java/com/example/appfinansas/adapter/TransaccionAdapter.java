package com.example.appfinansas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appfinansas.R;
import com.example.appfinansas.model.Transaccion;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransaccionAdapter extends RecyclerView.Adapter<TransaccionAdapter.TransaccionViewHolder> {

    public interface OnTransaccionClickListener {
        void onTransaccionClick(Transaccion transaccion);
    }

    private final List<Transaccion> transacciones = new ArrayList<>();
    private final OnTransaccionClickListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

    public TransaccionAdapter(OnTransaccionClickListener listener) {
        this.listener = listener;
    }

    public void setTransacciones(List<Transaccion> nuevasTransacciones) {
        transacciones.clear();
        if (nuevasTransacciones != null) {
            transacciones.addAll(nuevasTransacciones);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransaccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaccion, parent, false);
        return new TransaccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaccionViewHolder holder, int position) {
        holder.bind(transacciones.get(position));
    }

    @Override
    public int getItemCount() {
        return transacciones.size();
    }

    class TransaccionViewHolder extends RecyclerView.ViewHolder {

        private final View itemRoot;
        private final View iconContainer;
        private final ImageView iconTipo;
        private final TextView textConcepto;
        private final TextView textMonto;
        private final TextView textTipo;

        TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            itemRoot = itemView.findViewById(R.id.itemRoot);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            iconTipo = itemView.findViewById(R.id.iconTipo);
            textConcepto = itemView.findViewById(R.id.textConcepto);
            textMonto = itemView.findViewById(R.id.textMonto);
            textTipo = itemView.findViewById(R.id.textTipo);
        }

        void bind(Transaccion transaccion) {
            boolean esIngreso = transaccion.esIngreso();

            textConcepto.setText(transaccion.getConcepto());

            if (esIngreso) {
                itemRoot.setBackgroundResource(R.drawable.bg_item_ingreso);
                iconContainer.setBackgroundResource(R.drawable.bg_icon_ingreso);
                iconTipo.setImageResource(R.drawable.ic_ingreso);
                textTipo.setText(R.string.tipo_ingreso);
                textTipo.setBackgroundResource(R.drawable.bg_badge_ingreso);
                textTipo.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.ingreso_dark));
                textMonto.setText("+ " + currencyFormat.format(transaccion.getMonto()));
                textMonto.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.ingreso));
            } else {
                itemRoot.setBackgroundResource(R.drawable.bg_item_gasto);
                iconContainer.setBackgroundResource(R.drawable.bg_icon_gasto);
                iconTipo.setImageResource(R.drawable.ic_gasto);
                textTipo.setText(R.string.tipo_gasto);
                textTipo.setBackgroundResource(R.drawable.bg_badge_gasto);
                textTipo.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gasto_dark));
                textMonto.setText("- " + currencyFormat.format(transaccion.getMonto()));
                textMonto.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gasto));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransaccionClick(transaccion);
                }
            });
        }
    }
}
