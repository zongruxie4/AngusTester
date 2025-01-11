package cloud.xcan.sdf.core.angustester.domain.apis.favourite;

import cloud.xcan.sdf.spec.http.HttpMethod;

public interface ApisFavouriteP {

  Long getId();

  Long getApisId();

  String getApisName();

  HttpMethod getMethod();

  String getApisUri();

}
