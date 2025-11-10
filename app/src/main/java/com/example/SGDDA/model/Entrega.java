package com.example.SGDDA.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

// Implementa Serializable para poder passar entre Activities
public class Entrega implements Serializable {

    @Exclude
    private String documentId; // Para guardar o ID do Firestore

    // Informações da Instituição (para mostrar na lista)
    private String instituicaoId;
    private String instituicaoNome;
    private String instituicaoEndereco; // Adicionado
    private String instituicaoUrgencia; // Adicionado

    // Informações da Entrega
    private String voluntarioNome;
    private String dataEntrega;
    private String status; // Ex: "Pendente", "Em Coleta", "Concluída"
    private String observacoes;

    // Lista de Itens (a lista de DoacaoItem que foi selecionada)
    private List<DoacaoItem> itens;

    // Construtor vazio (obrigatório para o Firestore)
    public Entrega() {}

    // Construtor completo (usado ao criar a entrega)
    public Entrega(String instituicaoId, String instituicaoNome, String instituicaoEndereco, String instituicaoUrgencia,
                   String voluntarioNome, String dataEntrega, String status, List<DoacaoItem> itens) {
        this.instituicaoId = instituicaoId;
        this.instituicaoNome = instituicaoNome;
        this.instituicaoEndereco = instituicaoEndereco; // Salva o endereço
        this.instituicaoUrgencia = instituicaoUrgencia; // Salva a urgência
        this.voluntarioNome = voluntarioNome;
        this.dataEntrega = dataEntrega;
        this.status = status;
        this.itens = itens;
        this.observacoes = ""; // Inicia vazio
    }

    // --- Getters e Setters (Obrigatórios para o Firestore) ---

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getInstituicaoId() { return instituicaoId; }
    public void setInstituicaoId(String instituicaoId) { this.instituicaoId = instituicaoId; }

    public String getInstituicaoNome() { return instituicaoNome; }
    public void setInstituicaoNome(String instituicaoNome) { this.instituicaoNome = instituicaoNome; }

    public String getInstituicaoEndereco() { return instituicaoEndereco; } // Getter
    public void setInstituicaoEndereco(String instituicaoEndereco) { this.instituicaoEndereco = instituicaoEndereco; } // Setter

    public String getInstituicaoUrgencia() { return instituicaoUrgencia; } // Getter
    public void setInstituicaoUrgencia(String instituicaoUrgencia) { this.instituicaoUrgencia = instituicaoUrgencia; } // Setter

    public String getVoluntarioNome() { return voluntarioNome; }
    public void setVoluntarioNome(String voluntarioNome) { this.voluntarioNome = voluntarioNome; }

    public String getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(String dataEntrega) { this.dataEntrega = dataEntrega; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public List<DoacaoItem> getItens() { return itens; }
    public void setItens(List<DoacaoItem> itens) { this.itens = itens; }
}


