package cr.ac.ucr.paraiso.ie.c5j263.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5j263.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.BitacoraResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/optimizados")
    public ResponseEntity<List<EnvioResponseDTO>> getEnviosOptimizados() {
        return ResponseEntity.ok(envioService.obtenerEnviosOptimizados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDTO> getEnvioById(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerEnvio(id));
    }

    @PostMapping
    public ResponseEntity<EnvioResponseDTO> registrarEnvio(@Valid @RequestBody EnvioRequestDTO envioRequestDTO) {
        return ResponseEntity.ok(envioService.registrarEnvio(envioRequestDTO));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstado(@PathVariable Integer id, @RequestBody CambioEstadoDTO cambioEstadoDTO) {
        return ResponseEntity.ok(envioService.actualizarEstado(id, cambioEstadoDTO));
    }

    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerBitacora(id));
    }
}
