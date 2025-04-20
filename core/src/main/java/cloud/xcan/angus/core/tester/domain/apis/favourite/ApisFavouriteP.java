package cloud.xcan.angus.core.tester.domain.apis.favourite;

import cloud.xcan.angus.spec.http.HttpMethod;

public interface ApisFavouriteP {

  Long getId();

  Long getApisId();

  String getApisName();

  HttpMethod getMethod();

  String getApisUri();

}
