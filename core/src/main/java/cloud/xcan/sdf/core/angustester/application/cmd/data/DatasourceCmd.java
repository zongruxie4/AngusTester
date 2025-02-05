package cloud.xcan.sdf.core.angustester.application.cmd.data;

import cloud.xcan.sdf.core.angustester.domain.data.datasource.Datasource;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface DatasourceCmd {

  IdKey<Long, Object> add(Datasource datasource);

  IdKey<Long, Object> replace(Datasource datasource);

  Datasource testByConfig(Datasource datasource);

  Datasource testById(Long id);

  void delete(Long id);

}




