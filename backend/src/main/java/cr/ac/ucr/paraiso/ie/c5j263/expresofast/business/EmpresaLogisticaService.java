package cr.ac.ucr.paraiso.ie.c5j263.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5j263.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5j263.expresofast.exception.DuplicateResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EmpresaLogisticaService {

    private final EmpresaLogisticaRepository empresaLogisticaRepository;

    public EmpresaLogisticaService(EmpresaLogisticaRepository empresaLogisticaRepository) {
        this.empresaLogisticaRepository = empresaLogisticaRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpresaLogistica> obtenerEmpresas() {
        return empresaLogisticaRepository.findAll();
    }

    public EmpresaLogistica registrarEmpresa(String nombre, String cedulaJuridica, String telefono) {
        if (empresaLogisticaRepository.existsByNombre(nombre)) {
            throw new DuplicateResourceException("Ya existe una empresa con el nombre: " + nombre);
        }
        if (empresaLogisticaRepository.existsByCedulaJuridica(cedulaJuridica)) {
            throw new DuplicateResourceException("Ya existe una empresa con la cédula jurídica: " + cedulaJuridica);
        }

        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setNombre(nombre);
        empresa.setCedulaJuridica(cedulaJuridica);
        empresa.setTelefono(telefono);
        empresa.setFechaRegistro(LocalDateTime.now());

        return empresaLogisticaRepository.save(empresa);
    }
}
