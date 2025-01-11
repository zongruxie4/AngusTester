package cloud.xcan.sdf.core.angustester.domain.apis.follow;

import cloud.xcan.sdf.spec.http.HttpMethod;

public interface ApisFollowP {

  Long getId();

  Long getApisId();

  String getApisName();

  HttpMethod getMethod();

  String getApisUri();

}
