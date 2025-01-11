package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.auth;


import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
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

  Boolean getCreatorFlag();

  void setCreatorFlag(Boolean creatorFlag);
}



