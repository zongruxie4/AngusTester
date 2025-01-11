package cloud.xcan.sdf.core.angustester.application.converter;

import cloud.xcan.sdf.core.angustester.domain.data.datasource.Datasource;

public class MockDatasourceConverter {

  public static void setReplaceInfo(Datasource datasource, Datasource datasourceDb) {
    datasourceDb.setName(datasource.getName())
        //.setDatabase(datasource.getDatabase()) -> Modification not allowed
        .setDriverClassName(datasource.getDriverClassName())
        .setUsername(datasource.getUsername())
        .setPassd(datasource.getPassd())
        .setJdbcUrl(datasource.getJdbcUrl());
  }

}
