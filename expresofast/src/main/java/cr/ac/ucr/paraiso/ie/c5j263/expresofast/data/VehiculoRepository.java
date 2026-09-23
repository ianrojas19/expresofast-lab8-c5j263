package cr.ac.ucr.paraiso.ie.c5j263.expresofast.data;

import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
    boolean existsByPlaca(String placa);
}
