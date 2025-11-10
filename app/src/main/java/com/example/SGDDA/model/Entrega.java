package com.example.SGDDA.model;

import com.google.firebase.firestore.Exclude;
import java.util.List;

// POJO (Plain Old Java Object) para o Firestore
public class Entrega {

    @Exclude
    private String documentId; // ID do documento no Firestore

    // Dados da Instituição
    private String instituicaoId;
    private String instituicaoNome;
    private String instituicaoEndereco;
    private String urgencia; // "Alta", "Média", "Normal"

    // Dados do Agendamento
    private String dataAgendada; // Ex: "15/05/2025"
    private String horarioEstimado; // Ex: "14:30"
    private String voluntarioNome;
    private String observacoes;
    private String status; // "Pendente" ou "Concluído"

    // Dados da Conclusão
    private String dataEntrega; // Preenchido quando conclui

    // Lista de Itens
    // Vamos salvar a lista de itens da doação dentro da entrega
    private List<DoacaoItem> itens;

    // Construtor vazio (Necessário para o Firestore)
    public Entrega() {}

    // Construtor principal (usado ao criar uma nova)
    public Entrega(String instituicaoId, String instituicaoNome, String instituicaoEndereco, String urgencia,
                   String dataAgendada, String horarioEstimado, String voluntarioNome,
                   String observacoes, String status, List<DoacaoItem> itens) {
        this.instituicaoId = instituicaoId;
        this.instituicaoNome = instituicaoNome;
        this.instituicaoEndereco = instituicaoEndereco;
        this.urgencia = urgencia;
        this.dataAgendada = dataAgendada;
        this.horarioEstimado = horarioEstimado;
        this.voluntarioNome = voluntarioNome;
        this.observacoes = observacoes;
        this.status = status;
        this.itens = itens;
        this.dataEntrega = null; // Começa nulo
    }

    // --- Getters e Setters ---
    // O Firestore usa os getters e setters para ler/escrever dados

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getInstituicaoId() { return instituicaoId; }
    public void setInstituicaoId(String instituicaoId) { this.instituicaoId = instituicaoId; }

    public String getInstituicaoNome() { return instituicaoNome; }
    public void setInstituicaoNome(String instituicaoNome) { this.instituicaoNome = instituicaoNome; }

    public String getInstituicaoEndereco() { return instituicaoEndereco; }
    public void setInstituicaoEndereco(String instituicaoEndereco) { this.instituicaoEndereco = instituicaoEndereco; }

    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }

    public String getDataAgendada() { return dataAgendada; }
    public void setDataAgendada(String dataAgendada) { this.dataAgendada = dataAgendada; }

    public String getHorarioEstimado() { return horarioEstimado; }
    public void setHorarioEstimado(String horarioEstimado) { this.horarioEstimado = horarioEstimado; }

    public String getVoluntarioNome() { return voluntarioNome; }
    public void setVoluntarioNome(String voluntarioNome) { this.voluntarioNome = voluntarioNome; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(String dataEntrega) { this.dataEntrega = dataEntrega; }

    public List<DoacaoItem> getItens() { return itens; }
    public void setItens(List<DoacaoItem> itens) { this.itens = itens; }
}

