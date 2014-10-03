/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.dbsti.importaXml.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author Franciscato
 */
@Entity
public class Nota implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String chaveAcesso;
    private String numeroProtocolo;
    private Integer numeroNota;
    private String Serie;
    private String Modelo;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dataHoraEmissao;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dataHoraSaida;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date DataRecebimento;

    private String situacao;
    private String mensagem;
    private Integer codigoAmbiente;

    @ManyToOne
    private Emitente nfeEmitente;

    @ManyToOne
    private Destinatario destinatario;

    @ManyToOne
    private Transportador transportador;

    private String camihhoXml;
    private String caminhoPdf;   

    public Transportador getTransportador() {
        return transportador;
    }

    public void setTransportador(Transportador transportador) {
        this.transportador = transportador;
    }

    public Destinatario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Destinatario destinatario) {
        this.destinatario = destinatario;
    }

    public String getCamihhoXml() {
        return camihhoXml;
    }

    public void setCamihhoXml(String camihhoXml) {
        this.camihhoXml = camihhoXml;
    }

    public String getCaminhoPdf() {
        return caminhoPdf;
    }

    public void setCaminhoPdf(String caminhoPdf) {
        this.caminhoPdf = caminhoPdf;
    }

    public Emitente getNfeEmitente() {
        return nfeEmitente;
    }

    public void setNfeEmitente(Emitente nfeEmitente) {
        this.nfeEmitente = nfeEmitente;
    }

    public Date getDataHoraEmissao() {
        return dataHoraEmissao;
    }

    public void setDataHoraEmissao(Date dataHoraEmissao) {
        this.dataHoraEmissao = dataHoraEmissao;
    }

    public Date getDataHoraSaida() {
        return dataHoraSaida;
    }

    public void setDataHoraSaida(Date dataHoraSaida) {
        this.dataHoraSaida = dataHoraSaida;
    }

    public String getModelo() {
        return Modelo;
    }

    public void setModelo(String Modelo) {
        this.Modelo = Modelo;
    }

    public String getSerie() {
        return Serie;
    }

    public void setSerie(String Serie) {
        this.Serie = Serie;
    }

    public Integer getNumeroNota() {
        return numeroNota;
    }

    public void setNumeroNota(Integer numeroNota) {
        this.numeroNota = numeroNota;
    }

    public Integer getCodigoAmbiente() {
        return codigoAmbiente;
    }

    public void setCodigoAmbiente(Integer codigoAmbiente) {
        this.codigoAmbiente = codigoAmbiente;
    }

    public String getChaveAcesso() {
        return chaveAcesso;
    }

    public void setChaveAcesso(String chaveAcesso) {
        this.chaveAcesso = chaveAcesso;
    }

    public Date getDataRecebimento() {
        return DataRecebimento;
    }

    public void setDataRecebimento(Date DataRecebimento) {
        this.DataRecebimento = DataRecebimento;
    }

    public String getNumeroProtocolo() {
        return numeroProtocolo;
    }

    public void setNumeroProtocolo(String numeroProtocolo) {
        this.numeroProtocolo = numeroProtocolo;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Nota)) {
            return false;
        }
        Nota other = (Nota) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.com.dbsti.importaXml.model.NFeMestre[ id=" + id + " ]";
    }

}
