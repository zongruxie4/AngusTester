package cloud.xcan.sdf.core.angustester.application.query.data;

import cloud.xcan.sdf.core.angustester.domain.data.datasource.Datasource;
import java.sql.SQLException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface DatasourceQuery {

  Datasource detail(Long id);

  Page<Datasource> find(Specification<Datasource> spec, Pageable pageable);

  Datasource checkAndFind(Long id);

  void checkNameExists(String name);

  void connDatabase(Datasource datasource) throws ClassNotFoundException, SQLException;

}




