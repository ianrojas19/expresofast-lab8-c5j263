package cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto;

public class CambioEstadoDTO {
    private String nuevoEstado;
    private String observaciones;

    public CambioEstadoDTO() {}

    public String getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(String nuevoEstado) { this.nuevoEstado = nuevoEstado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
