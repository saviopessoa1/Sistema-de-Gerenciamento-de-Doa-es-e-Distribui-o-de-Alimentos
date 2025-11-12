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
    private OnInstituicaoClickListener clickListener;

    // Variáveis para controlar a aparência
    private String buttonTextDefault;
    private String selectedId = null; // Guarda o ID da instituição selecionada

    public InstituicaoAdapter(Context context, List<Instituicao> instituicaoList, OnInstituicaoClickListener listener, String buttonTextDefault) {
        this.context = context;
        this.instituicaoList = instituicaoList;
        this.clickListener = listener;
        this.buttonTextDefault = buttonTextDefault;
    }

    public interface OnInstituicaoClickListener {
        void onInstituicaoClick(Instituicao instituicao);
    }

    // Método para definir qual está selecionada
    public void setSelectedId(String id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    public void updateList(List<Instituicao> newList) {
        this.instituicaoList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        if (urgencia == null || urgencia.isEmpty()) {
            urgencia = "Normal";
        }
        holder.textUrgencia.setText("Urgência: " + urgencia);

        // Cores da Urgência
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
            default:
                holder.iconUrgencia.setVisibility(View.GONE);
                holder.textUrgencia.setTextColor(ContextCompat.getColor(context, R.color.white_60));
                break;
        }

        // Lógica do Botão (Seleção)
        boolean isSelected = instituicao.getDocumentId() != null && instituicao.getDocumentId().equals(selectedId);

        if (isSelected) {
            holder.btnAcessar.setText("Selecionado");
            holder.btnAcessar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.app_primary_light)));
            holder.btnAcessar.setTextColor(ContextCompat.getColor(context, R.color.white_60));
        } else {
            holder.btnAcessar.setText(buttonTextDefault);
            holder.btnAcessar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.app_accent_green))); // Verde padrão
            holder.btnAcessar.setTextColor(ContextCompat.getColor(context, R.color.app_primary_dark));
        }

        // Cliques
        View.OnClickListener listener = v -> {
            if (clickListener != null) {
                clickListener.onInstituicaoClick(instituicao);
            }
        };
        holder.btnAcessar.setOnClickListener(listener);
        holder.itemView.setOnClickListener(listener);
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
            titleInstituicao = itemView.findViewById(R.id.titleInstituicao);
            textUrgencia = itemView.findViewById(R.id.textUrgencia);
            textEndereco = itemView.findViewById(R.id.textEndereco);
            iconUrgencia = itemView.findViewById(R.id.iconUrgencia);
            btnAcessar = itemView.findViewById(R.id.btnAcessar);
        }
    }
}


