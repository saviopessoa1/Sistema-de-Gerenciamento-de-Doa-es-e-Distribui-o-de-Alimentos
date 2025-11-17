package com.example.SGDDA.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public class Entrega implements Serializable {

    @Exclude
    private String documentId; 

    
    private String instituicaoId;
    private String instituicaoNome;
    private String instituicaoEndereco; 
    private String instituicaoUrgencia; 

    
    private String voluntarioNome;
    private String dataEntrega;
    private String status; 
    private String observacoes;

    
    private List<DoacaoItem> itens;

    
    public Entrega() {}

    
    public Entrega(String instituicaoId, String instituicaoNome, String instituicaoEndereco, String instituicaoUrgencia,
                   String voluntarioNome, String dataEntrega, String status, List<DoacaoItem> itens) {
        this.instituicaoId = instituicaoId;
        this.instituicaoNome = instituicaoNome;
        this.instituicaoEndereco = instituicaoEndereco; 
        this.instituicaoUrgencia = instituicaoUrgencia; 
        this.voluntarioNome = voluntarioNome;
        this.dataEntrega = dataEntrega;
        this.status = status;
        this.itens = itens;
        this.observacoes = ""; 
    }

    

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getInstituicaoId() { return instituicaoId; }
    public void setInstituicaoId(String instituicaoId) { this.instituicaoId = instituicaoId; }

    public String getInstituicaoNome() { return instituicaoNome; }
    public void setInstituicaoNome(String instituicaoNome) { this.instituicaoNome = instituicaoNome; }

    public String getInstituicaoEndereco() { return instituicaoEndereco; } 
    public void setInstituicaoEndereco(String instituicaoEndereco) { this.instituicaoEndereco = instituicaoEndereco; } 

    public String getInstituicaoUrgencia() { return instituicaoUrgencia; } 
    public void setInstituicaoUrgencia(String instituicaoUrgencia) { this.instituicaoUrgencia = instituicaoUrgencia; } 

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


