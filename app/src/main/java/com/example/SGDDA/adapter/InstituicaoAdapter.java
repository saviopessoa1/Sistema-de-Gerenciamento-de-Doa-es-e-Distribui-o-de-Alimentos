package com.example.SGDDA.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SGDDA.R;
import com.example.SGDDA.model.Instituicao;

import java.util.List;
import java.util.Locale;

public class InstituicaoAdapter extends RecyclerView.Adapter<InstituicaoAdapter.ViewHolder> {

    private List<Instituicao> instituicaoList;
    private Context context;

    public InstituicaoAdapter(Context context, List<Instituicao> instituicaoList) {
        this.context = context;
        this.instituicaoList = instituicaoList;
    }

    // Método para atualizar a lista do adapter quando filtrarmos
    public void updateList(List<Instituicao> newList) {
        this.instituicaoList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usa o layout item_instituicao.xml que já criamos
        View view = LayoutInflater.from(context).inflate(R.layout.item_instituicao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Instituicao instituicao = instituicaoList.get(position);
        if (instituicao == null) return;

        holder.titleInstituicao.setText(instituicao.getNome());
        holder.textEndereco.setText(instituicao.getEndereco());

        String urgencia = instituicao.getUrgencia();
        if (urgencia == null) urgencia = "Normal";

        holder.textUrgencia.setText("Urgência: " + urgencia);

        // Lógica de cores e ícone de urgência
        switch (urgencia.toLowerCase(Locale.ROOT)) {
            case "alta":
                holder.iconUrgencia.setVisibility(View.VISIBLE);
                holder.iconUrgencia.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.app_accent_red)));
                holder.textUrgencia.setTextColor(ContextCompat.getColor(context, R.color.app_accent_red));
                break;
            case "média":
                holder.iconUrgencia.setVisibility(View.VISIBLE);
                holder.iconUrgencia.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.app_accent_yellow)));
                holder.textUrgencia.setTextColor(ContextCompat.getColor(context, R.color.app_accent_yellow));
                break;
            default: // "Normal" ou qualquer outro
                holder.iconUrgencia.setVisibility(View.GONE);
                holder.textUrgencia.setTextColor(ContextCompat.getColor(context, R.color.white_60));
                break;
        }

        // Lógica do botão "Acessar" (apenas um exemplo)
        holder.btnAcessar.setOnClickListener(v -> {
            Toast.makeText(context, "Acessando " + instituicao.getNome(), Toast.LENGTH_SHORT).show();
            // Aqui você poderia abrir uma tela de "Detalhes da Instituição"
        });
    }

    @Override
    public int getItemCount() {
        return instituicaoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleInstituicao, textUrgencia, textEndereco;
        ImageView iconUrgencia;
        Button btnAcessar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Encontra os IDs do layout item_instituicao.xml
            titleInstituicao = itemView.findViewById(R.id.titleInstituicao);
            textUrgencia = itemView.findViewById(R.id.textUrgencia);
            textEndereco = itemView.findViewById(R.id.textEndereco);
            iconUrgencia = itemView.findViewById(R.id.iconUrgencia);
            btnAcessar = itemView.findViewById(R.id.btnAcessar);
        }
    }
}