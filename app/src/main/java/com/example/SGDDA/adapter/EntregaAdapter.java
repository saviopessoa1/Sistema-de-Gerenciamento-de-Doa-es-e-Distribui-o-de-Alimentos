package com.example.SGDDA.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.SGDDA.model.Entrega;
import com.example.SGDDA.ui.DetalhesEntregaActivity;
import com.example.SGDDA.ui.DetalhesEntregaConcluidaActivity;

import java.util.List;

public class EntregaAdapter extends RecyclerView.Adapter<EntregaAdapter.ViewHolder> {

    private List<Entrega> entregaList;
    private Context context;

    public EntregaAdapter(Context context, List<Entrega> entregaList) {
        this.context = context;
        this.entregaList = entregaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_entrega, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrega entrega = entregaList.get(position);
        if (entrega == null) return;

        holder.titleInstituicao.setText(entrega.getInstituicaoNome());
        holder.textEndereco.setText(entrega.getInstituicaoEndereco());

        // Lógica Simplificada de Visibilidade
        if ("Pendente".equals(entrega.getStatus()) || "Em Coleta".equals(entrega.getStatus())) {
            // --- MODO PENDENTE ---
            holder.textUrgencia.setText("Urgência: " + entrega.getInstituicaoUrgencia());
            holder.textAgendado.setText("Agendado: " + entrega.getDataEntrega());

            holder.textUrgencia.setVisibility(View.VISIBLE);
            holder.textAgendado.setVisibility(View.VISIBLE);

            holder.textEntregue.setVisibility(View.GONE); // Some, puxando o endereço pra cima
            holder.iconConcluido.setVisibility(View.GONE);

            // Cores do Botão
            holder.btnDetalhes.setText("Detalhes");
            if ("Em Coleta".equals(entrega.getStatus())) {
                holder.btnDetalhes.setBackgroundColor(ContextCompat.getColor(context, R.color.app_accent_yellow));
            } else {
                holder.btnDetalhes.setBackgroundColor(ContextCompat.getColor(context, R.color.app_accent_green));
            }

        } else if ("Concluída".equals(entrega.getStatus())) {
            // --- MODO CONCLUÍDO ---
            holder.textEntregue.setText("Entregue: " + entrega.getDataEntrega());

            holder.textUrgencia.setVisibility(View.GONE); // Some
            holder.textAgendado.setVisibility(View.GONE); // Some

            holder.textEntregue.setVisibility(View.VISIBLE); // Aparece
            holder.iconConcluido.setVisibility(View.VISIBLE);

            holder.btnDetalhes.setText("Detalhes");
            holder.btnDetalhes.setBackgroundColor(ContextCompat.getColor(context, R.color.app_accent_blue));
        }

        // Clique no botão
        holder.btnDetalhes.setOnClickListener(v -> {
            if ("Concluída".equals(entrega.getStatus())) {
                Intent intent = new Intent(context, DetalhesEntregaConcluidaActivity.class);
                intent.putExtra("ENTREGA_OBJETO", entrega);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, DetalhesEntregaActivity.class);
                intent.putExtra("ENTREGA_ID", entrega.getDocumentId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entregaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleInstituicao, textUrgencia, textAgendado, textEndereco, textEntregue;
        ImageView iconConcluido;
        Button btnDetalhes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleInstituicao = itemView.findViewById(R.id.titleInstituicao);
            textUrgencia = itemView.findViewById(R.id.textUrgencia);
            textAgendado = itemView.findViewById(R.id.textAgendado);
            textEndereco = itemView.findViewById(R.id.textEndereco);
            textEntregue = itemView.findViewById(R.id.textEntregue);
            iconConcluido = itemView.findViewById(R.id.iconConcluido);
            btnDetalhes = itemView.findViewById(R.id.btnDetalhes);
        }
    }
}


