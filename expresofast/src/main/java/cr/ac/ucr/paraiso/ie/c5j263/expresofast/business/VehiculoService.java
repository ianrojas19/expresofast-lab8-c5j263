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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final EmpresaLogisticaRepository empresaLogisticaRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                           ConductorRepository conductorRepository,
                           EmpresaLogisticaRepository empresaLogisticaRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.empresaLogisticaRepository = empresaLogisticaRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> obtenerVehiculos() {
        return vehiculoRepository.findAll();
    }

    public Vehiculo registrarVehiculo(String placa, BigDecimal capacidadKg, String estado, Integer empresaId) {
        if (vehiculoRepository.existsByPlaca(placa)) {
            throw new DuplicateResourceException("Ya existe un vehículo con la placa: " + placa);
        }

        EmpresaLogistica empresa = empresaLogisticaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa logística no encontrada con id: " + empresaId));

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(placa);
        vehiculo.setCapacidadKg(capacidadKg);
        vehiculo.setEstado(estado);
        vehiculo.setEmpresa(empresa);

        return vehiculoRepository.save(vehiculo);
    }

    public Vehiculo asignarConductor(Integer vehiculoId, Integer conductorId) {
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con id: " + vehiculoId));

        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado con id: " + conductorId));

        if (conductor.getActivo() == null || !conductor.getActivo()) {
            throw new InvalidStateTransitionException("No se puede asignar un conductor inactivo al vehículo.");
        }

        return vehiculo;
    }
}
