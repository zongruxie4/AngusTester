package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth;


import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import java.io.Serializable;
import java.util.List;

public interface ApiAuthVo extends Serializable {

  Long getId();

  void setId(Long id);

  AuthObjectType getAuthObjectType();

  void setAuthObjectType(AuthObjectType authObjectType);

  Long getAuthObjectId();

  void setAuthObjectId(Long authObjectId);

  String getName();

  void setName(String name);

  List<ApiPermission> getPermissions();

  void setPermissions(List<ApiPermission> permissions);

  Long getApisId();

  void setApisId(Long apisId);

  Boolean getCreator();

  void setCreator(Boolean creator);
}



