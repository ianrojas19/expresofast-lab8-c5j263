package cr.ac.ucr.paraiso.ie.c5j263.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ConductorRepository conductorRepository;

    @Mock
    private EmpresaLogisticaRepository empresaLogisticaRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    private EmpresaLogistica empresaMock;
    private Vehiculo vehiculoMock;
    private Conductor conductorActivoMock;
    private Conductor conductorInactivoMock;

    @BeforeEach
    void setUp() {
        empresaMock = new EmpresaLogistica();
        empresaMock.setId(1);
        empresaMock.setNombre("Logistica Express");
        empresaMock.setCedulaJuridica("3-101-123456");

        vehiculoMock = new Vehiculo();
        vehiculoMock.setId(1);
        vehiculoMock.setPlaca("NEW-999");
        vehiculoMock.setCapacidadKg(new BigDecimal("3000.00"));
        vehiculoMock.setEstado("DISPONIBLE");
        vehiculoMock.setEmpresa(empresaMock);

        conductorActivoMock = new Conductor();
        conductorActivoMock.setId(1);
        conductorActivoMock.setNombre("Pedro");
        conductorActivoMock.setApellidos("Jimenez");
        conductorActivoMock.setActivo(true);

        conductorInactivoMock = new Conductor();
        conductorInactivoMock.setId(2);
        conductorInactivoMock.setNombre("Luis");
        conductorInactivoMock.setApellidos("Mora");
        conductorInactivoMock.setActivo(false);
    }

    @Test
    @DisplayName("Registrar vehiculo con placa duplicada lanza DuplicateResourceException")
    void registrarVehiculo_PlacaDuplicada_LanzaExcepcion() {
        when(vehiculoRepository.existsByPlaca("ABC-123")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                vehiculoService.registrarVehiculo("ABC-123", new BigDecimal("3000"), "DISPONIBLE", 1));

        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar vehiculo con datos validos retorna vehiculo guardado")
    void registrarVehiculo_DatosValidos_RetornaVehiculo() {
        when(vehiculoRepository.existsByPlaca("NEW-999")).thenReturn(false);
        when(empresaLogisticaRepository.findById(1)).thenReturn(Optional.of(empresaMock));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenReturn(vehiculoMock);

        Vehiculo result = vehiculoService.registrarVehiculo("NEW-999", new BigDecimal("3000"), "DISPONIBLE", 1);

        assertNotNull(result);
        assertEquals("NEW-999", result.getPlaca());
        verify(vehiculoRepository, times(1)).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("Asignar conductor inactivo lanza excepcion")
    void asignarConductor_ConductorInactivo_LanzaExcepcion() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculoMock));
        when(conductorRepository.findById(2)).thenReturn(Optional.of(conductorInactivoMock));

        assertThrows(InvalidStateTransitionException.class, () ->
                vehiculoService.asignarConductor(1, 2));
    }

    @Test
    @DisplayName("Asignar conductor activo retorna vehiculo")
    void asignarConductor_ConductorActivo_Exitoso() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculoMock));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductorActivoMock));

        Vehiculo result = vehiculoService.asignarConductor(1, 1);

        assertNotNull(result);
        assertEquals("NEW-999", result.getPlaca());
    }

    @Test
    @DisplayName("Registrar vehiculo con empresa inexistente lanza ResourceNotFoundException")
    void registrarVehiculo_EmpresaNoExiste_LanzaExcepcion() {
        when(vehiculoRepository.existsByPlaca("ZZZ-999")).thenReturn(false);
        when(empresaLogisticaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                vehiculoService.registrarVehiculo("ZZZ-999", new BigDecimal("1000"), "DISPONIBLE", 99));
    }
}
