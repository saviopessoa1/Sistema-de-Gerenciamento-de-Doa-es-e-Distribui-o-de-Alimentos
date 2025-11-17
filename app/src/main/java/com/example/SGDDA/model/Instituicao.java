package com.example.SGDDA.model;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable; 


public class Instituicao implements Serializable {

    @Exclude
    private String documentId; 

    private String nome;
    private String endereco;
    private String urgencia; 
    private String responsavel;
    private String telefone;

    
    public Instituicao() {}

    

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    
    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }


    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}


