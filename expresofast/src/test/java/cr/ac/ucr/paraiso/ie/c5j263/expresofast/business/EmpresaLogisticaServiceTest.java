package cr.ac.ucr.paraiso.ie.c5j263.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaLogisticaServiceTest {

    @Mock
    private EmpresaLogisticaRepository empresaLogisticaRepository;

    @InjectMocks
    private EmpresaLogisticaService empresaLogisticaService;

    private EmpresaLogistica empresaMock;

    @BeforeEach
    void setUp() {
        empresaMock = new EmpresaLogistica();
        empresaMock.setId(1);
        empresaMock.setNombre("Logistica Express");
        empresaMock.setCedulaJuridica("3-101-123456");
        empresaMock.setTelefono("2555-1000");
    }

    @Test
    @DisplayName("Registrar empresa con nombre duplicado lanza DuplicateResourceException")
    void registrarEmpresa_NombreDuplicado_LanzaExcepcion() {
        when(empresaLogisticaRepository.existsByNombre("Logistica Express")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                empresaLogisticaService.registrarEmpresa("Logistica Express", "3-101-999999", "2555-9999"));

        verify(empresaLogisticaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar empresa con cedula juridica duplicada lanza DuplicateResourceException")
    void registrarEmpresa_CedulaDuplicada_LanzaExcepcion() {
        when(empresaLogisticaRepository.existsByNombre("Nueva Empresa")).thenReturn(false);
        when(empresaLogisticaRepository.existsByCedulaJuridica("3-101-123456")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                empresaLogisticaService.registrarEmpresa("Nueva Empresa", "3-101-123456", "2555-0000"));

        verify(empresaLogisticaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar empresa con datos validos retorna empresa guardada")
    void registrarEmpresa_DatosValidos_RetornaEmpresa() {
        when(empresaLogisticaRepository.existsByNombre("Trans CR")).thenReturn(false);
        when(empresaLogisticaRepository.existsByCedulaJuridica("3-101-777777")).thenReturn(false);
        when(empresaLogisticaRepository.save(any(EmpresaLogistica.class))).thenReturn(empresaMock);

        EmpresaLogistica result = empresaLogisticaService.registrarEmpresa("Trans CR", "3-101-777777", "2500-0000");

        assertNotNull(result);
        verify(empresaLogisticaRepository, times(1)).save(any(EmpresaLogistica.class));
    }

    @Test
    @DisplayName("Obtener empresas retorna lista")
    void obtenerEmpresas_RetornaLista() {
        when(empresaLogisticaRepository.findAll()).thenReturn(List.of(empresaMock));

        List<EmpresaLogistica> result = empresaLogisticaService.obtenerEmpresas();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
