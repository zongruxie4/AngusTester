package cloud.xcan.angus.core.tester.application.query.data;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;
import java.sql.SQLException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface DatasourceQuery {

  Datasource detail(Long id);

  Page<Datasource> find(GenericSpecification<Datasource> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  Datasource checkAndFind(Long id);

  void checkNameExists(String name);

  void connDatabase(Datasource datasource) throws ClassNotFoundException, SQLException;

}




