package com.example.SGDDA.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.example.SGDDA.model.Entrega;
import com.example.SGDDA.ui.DetalhesEntregaActivity; // Importa a tela de Detalhes

import java.util.List;

public class EntregaAdapter extends RecyclerView.Adapter<EntregaAdapter.ViewHolder> {

    private List<Entrega> entregaList;
    private Context context;
    private static final String TAG = "EntregaAdapter";

    public EntregaAdapter(Context context, List<Entrega> entregaList) {
        this.context = context;
        this.entregaList = entregaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Usa o layout item_entrega.xml que você já criou
        View view = LayoutInflater.from(context).inflate(R.layout.item_entrega, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrega entrega = entregaList.get(position);
        if (entrega == null) {
            Log.e(TAG, "Entrega na posição " + position + " é nula.");
            return;
        }

        holder.titleInstituicao.setText(entrega.getInstituicaoNome());
        holder.textEndereco.setText(entrega.getInstituicaoEndereco());

        // Controla a visibilidade dos campos com base no status
        if ("Pendente".equals(entrega.getStatus()) || "Em Coleta".equals(entrega.getStatus())) {
            holder.textUrgencia.setText("Urgência: " + entrega.getInstituicaoUrgencia());
            holder.textAgendado.setText("Agendado: " + entrega.getDataEntrega());
            holder.textUrgencia.setVisibility(View.VISIBLE);
            holder.textAgendado.setVisibility(View.VISIBLE);
            holder.textEntregue.setVisibility(View.GONE);
            holder.iconConcluido.setVisibility(View.GONE);
            holder.btnDetalhes.setText("Detalhes");
            // Mudar a cor do botão com base no status "Em Coleta"
            if ("Em Coleta".equals(entrega.getStatus())) {
                holder.btnDetalhes.setBackgroundColor(ContextCompat.getColor(context, R.color.app_accent_yellow));
            } else {
                holder.btnDetalhes.setBackgroundColor(ContextCompat.getColor(context, R.color.app_accent_green));
            }

        } else if ("Concluída".equals(entrega.getStatus())) {
            holder.textEntregue.setText("Entregue: " + entrega.getDataEntrega());
            holder.textUrgencia.setVisibility(View.GONE);
            holder.textAgendado.setVisibility(View.GONE);
            holder.textEntregue.setVisibility(View.VISIBLE);
            holder.iconConcluido.setVisibility(View.VISIBLE);
            holder.btnDetalhes.setText("Ver Obs."); // Muda o texto do botão
            holder.btnDetalhes.setBackgroundColor(ContextCompat.getColor(context, R.color.app_accent_blue));
        }

        // Listener para o botão "Detalhes" ou "Ver Obs."
        holder.btnDetalhes.setOnClickListener(v -> {
            if ("Concluída".equals(entrega.getStatus())) {
                // TODO: Abrir a tela DetalhesEntregaConcluidaActivity
                // Intent intent = new Intent(context, DetalhesEntregaConcluidaActivity.class);
                // intent.putExtra("ENTREGA_ID", entrega.getDocumentId());
                // context.startActivity(intent);
                Toast.makeText(context, "Abrindo Observações...", Toast.LENGTH_SHORT).show();
            } else {
                // Abre a tela de Detalhes da Entrega Pendente
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
            // Encontra os IDs do layout item_entrega.xml
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


