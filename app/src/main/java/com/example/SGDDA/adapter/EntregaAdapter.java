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
import androidx.recyclerview.widget.RecyclerView;
import com.example.SGDDA.R;
import com.example.SGDDA.model.Entrega;
import com.example.SGDDA.ui.DetalhesEntregaActivity; // Tela de Detalhes Pendente
import com.example.SGDDA.ui.DetalhesEntregaConcluidaActivity; // Tela de Detalhes Concluída
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
        // Usa o layout item_entrega.xml que você já criou
        View view = LayoutInflater.from(context).inflate(R.layout.item_entrega, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrega entrega = entregaList.get(position);

        holder.titleInstituicao.setText(entrega.getInstituicaoNome());
        holder.textEndereco.setText(entrega.getInstituicaoEndereco());

        if (entrega.getStatus() != null && entrega.getStatus().equals("Concluído")) {
            // Se estiver CONCLUÍDO
            holder.textUrgencia.setVisibility(View.GONE);
            holder.textAgendado.setVisibility(View.GONE);
            holder.iconConcluido.setVisibility(View.VISIBLE);
            holder.textEntregue.setVisibility(View.VISIBLE);
            holder.textEntregue.setText("Entregue: " + entrega.getDataEntrega());
        } else {
            // Se estiver PENDENTE
            holder.textUrgencia.setVisibility(View.VISIBLE);
            holder.textAgendado.setVisibility(View.VISIBLE);
            holder.iconConcluido.setVisibility(View.GONE);
            holder.textEntregue.setVisibility(View.GONE);
            holder.textUrgencia.setText("Urgência: " + entrega.getUrgencia());
            holder.textAgendado.setText("Agendado: " + entrega.getDataAgendada());
        }

        // Adiciona o clique no botão "Detalhes"
        holder.btnDetalhes.setOnClickListener(v -> {
            Intent intent;
            if (entrega.getStatus() != null && entrega.getStatus().equals("Concluído")) {
                intent = new Intent(context, DetalhesEntregaConcluidaActivity.class);
            } else {
                intent = new Intent(context, DetalhesEntregaActivity.class);
            }

            // TODO: Passar o ID da entrega para a próxima tela
            // intent.putExtra("ENTREGA_ID", entrega.getDocumentId());
            context.startActivity(intent);
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

