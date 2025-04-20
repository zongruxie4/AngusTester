package cloud.xcan.angus.core.tester.application.converter;

import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;

public class MockDatasourceConverter {

  public static void setReplaceInfo(Datasource datasource, Datasource datasourceDb) {
    datasourceDb.setName(datasource.getName())
        //.setDatabase(datasource.getDatabase()) -> Modification not allowed
        .setDriverClassName(datasource.getDriverClassName())
        .setUsername(datasource.getUsername())
        .setPassword(datasource.getPassword())
        .setJdbcUrl(datasource.getJdbcUrl());
  }

}
