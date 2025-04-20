package cloud.xcan.angus.core.tester.domain.apis.follow;

import cloud.xcan.angus.spec.http.HttpMethod;

public interface ApisFollowP {

  Long getId();

  Long getApisId();

  String getApisName();

  HttpMethod getMethod();

  String getApisUri();

}
