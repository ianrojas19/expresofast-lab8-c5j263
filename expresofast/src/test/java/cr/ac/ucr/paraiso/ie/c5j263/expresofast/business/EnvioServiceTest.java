package cr.ac.ucr.paraiso.ie.c5j263.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.BitacoraEnvioRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.BitacoraResponseDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ConductorRepository conductorRepository;

    @Mock
    private BitacoraEnvioRepository bitacoraEnvioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EnvioService envioService;

    private Envio envioMock;
    private Vehiculo vehiculoMock;
    private Conductor conductorMock;

    @BeforeEach
    void setUp() {
        conductorMock = new Conductor();
        conductorMock.setId(1);
        conductorMock.setNombre("Juan");
        conductorMock.setApellidos("Perez");
        conductorMock.setActivo(true);

        vehiculoMock = new Vehiculo();
        vehiculoMock.setId(1);
        vehiculoMock.setPlaca("ABC-123");
        vehiculoMock.setCapacidadKg(new BigDecimal("5000.00"));
        vehiculoMock.setEstado("DISPONIBLE");

        envioMock = new Envio();
        envioMock.setId(1);
        envioMock.setCodigoRastreo("EXP-1234");
        envioMock.setDireccionDestino("San José Centro");
        envioMock.setPesoKg(new BigDecimal("10.00"));
        envioMock.setCosto(new BigDecimal("5000.00"));
        envioMock.setEstadoEnvio("PENDIENTE");
        envioMock.setVehiculo(vehiculoMock);
        envioMock.setConductor(conductorMock);
    }

    @Test
    @DisplayName("Crear envio con datos validos retorna DTO con estado PENDIENTE")
    void crearEnvio_DatosValidos_RetornaEnvioDTO() {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setCodigoRastreo("EXP-1234");
        dto.setDireccionDestino("San José Centro");
        dto.setPesoKg(new BigDecimal("10.00"));
        dto.setCosto(new BigDecimal("5000.00"));
        dto.setVehiculoId(1);
        dto.setConductorId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculoMock));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductorMock));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> {
            Envio e = inv.getArgument(0);
            e.setId(1);
            return e;
        });

        EnvioResponseDTO result = envioService.registrarEnvio(dto);

        assertNotNull(result);
        assertEquals("PENDIENTE", result.getEstadoEnvio());
        assertEquals("EXP-1234", result.getCodigoRastreo());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    @DisplayName("Crear envio con vehiculo sin capacidad lanza excepcion")
    void crearEnvio_VehiculoSinCapacidad_LanzaExcepcion() {
        vehiculoMock.setCapacidadKg(new BigDecimal("5.00")); // capacidad muy baja

        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setCodigoRastreo("EXP-5678");
        dto.setDireccionDestino("Alajuela");
        dto.setPesoKg(new BigDecimal("100.00")); // supera capacidad
        dto.setCosto(new BigDecimal("3000.00"));
        dto.setVehiculoId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculoMock));

        assertThrows(InvalidStateTransitionException.class, () ->
                envioService.registrarEnvio(dto));

        verify(envioRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({
        "ENTREGADO, EN_TRANSITO",
        "ENTREGADO, PENDIENTE",
        "CANCELADO, EN_TRANSITO",
        "CANCELADO, PENDIENTE"
    })
    @DisplayName("Transiciones de estado invalidas lanzan excepcion")
    void actualizarEstado_TransicionesInvalidas_LanzaExcepcion(String estadoAnterior, String estadoNuevo) {
        envioMock.setEstadoEnvio(estadoAnterior);
        when(envioRepository.findById(1)).thenReturn(Optional.of(envioMock));

        CambioEstadoDTO cambioDTO = new CambioEstadoDTO();
        cambioDTO.setNuevoEstado(estadoNuevo);
        cambioDTO.setObservaciones("Transición inválida");

        assertThrows(InvalidStateTransitionException.class, () ->
                envioService.actualizarEstado(1, cambioDTO));
    }

    @Test
    @DisplayName("Actualizar estado de forma exitosa guarda bitacora")
    void actualizarEstado_Exitoso() {
        envioMock.setEstadoEnvio("PENDIENTE");
        when(envioRepository.findById(1)).thenReturn(Optional.of(envioMock));

        CambioEstadoDTO cambioDTO = new CambioEstadoDTO();
        cambioDTO.setNuevoEstado("EN_TRANSITO");
        cambioDTO.setObservaciones("Comienza viaje");

        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setNombreCompleto("Administrador");

        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("admin");
        SecurityContextHolder.setContext(ctx);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioMock);

        EnvioResponseDTO result = envioService.actualizarEstado(1, cambioDTO);

        assertEquals("EN_TRANSITO", result.getEstadoEnvio());
        verify(bitacoraEnvioRepository, times(1)).save(any(BitacoraEnvio.class));
    }

    @Test
    @DisplayName("Obtener envios optimizados retorna lista")
    void obtenerEnviosOptimizados_RetornaLista() {
        when(envioRepository.findAllWithDetails()).thenReturn(List.of(envioMock));

        List<EnvioResponseDTO> result = envioService.obtenerEnviosOptimizados();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Obtener bitacora retorna lista")
    void obtenerBitacora_RetornaLista() {
        BitacoraEnvio b = new BitacoraEnvio();
        b.setId(1);
        b.setEstadoAnterior("PENDIENTE");
        b.setEstadoNuevo("EN_TRANSITO");
        b.setFechaCambio(LocalDateTime.now());
        Usuario u = new Usuario();
        u.setNombreCompleto("Admin");
        b.setUsuario(u);
        b.setObservaciones("test");

        when(bitacoraEnvioRepository.findByEnvioIdOrderByFechaCambioDesc(1)).thenReturn(List.of(b));

        List<BitacoraResponseDTO> result = envioService.obtenerBitacora(1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Cancelar envio pendiente cambia estado a CANCELADO")
    void cancelarEnvio_Exitoso() {
        envioMock.setEstadoEnvio("PENDIENTE");
        when(envioRepository.findById(1)).thenReturn(Optional.of(envioMock));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioMock);

        EnvioResponseDTO result = envioService.cancelarEnvio(1);

        assertEquals("CANCELADO", result.getEstadoEnvio());
        verify(envioRepository, times(1)).save(envioMock);
    }

    @Test
    @DisplayName("Cancelar envio entregado lanza excepcion")
    void cancelarEnvio_Entregado_LanzaExcepcion() {
        envioMock.setEstadoEnvio("ENTREGADO");
        when(envioRepository.findById(1)).thenReturn(Optional.of(envioMock));

        assertThrows(InvalidStateTransitionException.class, () ->
                envioService.cancelarEnvio(1));
    }

    @Test
    @DisplayName("Crear envio sin vehiculo ni conductor")
    void crearEnvio_SinVehiculoNiConductor_RetornaDTO() {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setCodigoRastreo("EXP-1234");
        dto.setDireccionDestino("San José Centro");
        dto.setPesoKg(new BigDecimal("10.00"));
        dto.setCosto(new BigDecimal("5000.00"));

        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> {
            Envio e = inv.getArgument(0);
            e.setId(1);
            return e;
        });

        EnvioResponseDTO result = envioService.registrarEnvio(dto);

        assertNotNull(result);
        assertNull(result.getPlacaVehiculo());
        assertNull(result.getNombreConductor());
    }

    @Test
    @DisplayName("Crear envio con vehiculo no existente")
    void crearEnvio_VehiculoNoExiste_LanzaExcepcion() {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setVehiculoId(99);
        when(vehiculoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> envioService.registrarEnvio(dto));
    }

    @Test
    @DisplayName("Crear envio con conductor no existente")
    void crearEnvio_ConductorNoExiste_LanzaExcepcion() {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setConductorId(99);
        when(conductorRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> envioService.registrarEnvio(dto));
    }

    @Test
    @DisplayName("Cancelar envio en transito lanza excepcion")
    void cancelarEnvio_EnvioEnTransito_LanzaExcepcion() {
        envioMock.setEstadoEnvio("EN_TRANSITO");
        when(envioRepository.findById(1)).thenReturn(Optional.of(envioMock));

        assertThrows(InvalidStateTransitionException.class, () ->
                envioService.cancelarEnvio(1));

        verify(envioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Obtener envio inexistente lanza ResourceNotFoundException")
    void obtenerEnvio_NoExistente_LanzaExcepcion() {
        when(envioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                envioService.obtenerEnvio(99));
    }

    @ParameterizedTest
    @CsvSource({
        "5.0, 10.0, 2500.0",
        "15.0, 50.0, 7500.0",
        "100.0, 2.5, 12000.0"
    })
    @DisplayName("Debe calcular la tarifa correcta segun peso y distancia")
    void calcularTarifa_CasosVariados_CalculaCorrectamente(
            double pesoKg, double distanciaKm, double tarifaEsperada) {
        double tarifaCalculada = envioService.calcularTarifa(pesoKg, distanciaKm);
        assertEquals(tarifaEsperada, tarifaCalculada, 0.01);
    }
}
