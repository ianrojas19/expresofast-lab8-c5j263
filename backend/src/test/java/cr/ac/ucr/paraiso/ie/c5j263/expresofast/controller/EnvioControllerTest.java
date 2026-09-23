package cr.ac.ucr.paraiso.ie.c5j263.expresofast.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.exception.GlobalExceptionHandler;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EnvioControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EnvioService envioService;

    @InjectMocks
    private EnvioController envioController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(envioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("GET /api/envios/{id} exitoso retorna HTTP 200 con datos del envio")
    void getEnvioById_Exitoso_Retorna200() throws Exception {
        EnvioResponseDTO dto = new EnvioResponseDTO();
        dto.setId(1);
        dto.setCodigoRastreo("EXP-1234");
        dto.setDireccionDestino("San José");
        dto.setPesoKg(new BigDecimal("10.00"));
        dto.setCosto(new BigDecimal("5000.00"));
        dto.setEstadoEnvio("PENDIENTE");

        when(envioService.obtenerEnvio(1)).thenReturn(dto);

        mockMvc.perform(get("/api/envios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"))
                .andExpect(jsonPath("$.estadoEnvio").value("PENDIENTE"));
    }

    @Test
    @DisplayName("GET /api/envios/{id} no encontrado retorna HTTP 404")
    void getEnvioById_NoEncontrado_Retorna404() throws Exception {
        when(envioService.obtenerEnvio(99))
                .thenThrow(new ResourceNotFoundException("Envío no encontrado con id: 99"));

        mockMvc.perform(get("/api/envios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("POST /api/envios con payload invalido retorna HTTP 400 con errores de validacion")
    void registrarEnvio_PayloadInvalido_Retorna400() throws Exception {
        EnvioRequestDTO payloadInvalido = new EnvioRequestDTO();
        // codigoRastreo y direccionDestino nulos: violación de @NotBlank
        payloadInvalido.setPesoKg(new BigDecimal("-1.00")); // viola @Positive
        payloadInvalido.setCosto(new BigDecimal("-100.00")); // viola @Positive

        mockMvc.perform(post("/api/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/envios con datos validos retorna HTTP 200")
    void registrarEnvio_DatosValidos_Retorna200() throws Exception {
        EnvioRequestDTO payload = new EnvioRequestDTO();
        payload.setCodigoRastreo("EXP-9999");
        payload.setDireccionDestino("Cartago");
        payload.setPesoKg(new BigDecimal("15.00"));
        payload.setCosto(new BigDecimal("8000.00"));
        payload.setVehiculoId(1);
        payload.setConductorId(1);

        EnvioResponseDTO responseDTO = new EnvioResponseDTO();
        responseDTO.setId(5);
        responseDTO.setCodigoRastreo("EXP-9999");
        responseDTO.setEstadoEnvio("PENDIENTE");

        when(envioService.registrarEnvio(any(EnvioRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-9999"));
    }
}
