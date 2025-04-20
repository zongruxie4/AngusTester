package cloud.xcan.angus.core.tester.application.cmd.data;

import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface DatasourceCmd {

  IdKey<Long, Object> add(Datasource datasource);

  IdKey<Long, Object> replace(Datasource datasource);

  Datasource testByConfig(Datasource datasource);

  Datasource testById(Long id);

  void delete(Long id);

}




