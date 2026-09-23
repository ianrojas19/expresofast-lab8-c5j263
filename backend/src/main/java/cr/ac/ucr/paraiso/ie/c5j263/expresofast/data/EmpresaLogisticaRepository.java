package cr.ac.ucr.paraiso.ie.c5j263.expresofast.data;

import cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.EmpresaLogistica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaLogisticaRepository extends JpaRepository<EmpresaLogistica, Integer> {
    boolean existsByNombre(String nombre);
    boolean existsByCedulaJuridica(String cedulaJuridica);
}
