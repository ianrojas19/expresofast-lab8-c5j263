package cr.ac.ucr.paraiso.ie.c5j263.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5j263.expresofast.business.AuthService;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto.AuthResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequest) {
        AuthResponseDTO response = authService.login(authRequest);
        return ResponseEntity.ok(response);
    }
}
