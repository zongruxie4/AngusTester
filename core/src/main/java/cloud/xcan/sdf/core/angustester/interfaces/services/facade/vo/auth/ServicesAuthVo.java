package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth;


import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import java.io.Serializable;
import java.util.List;

public interface ServicesAuthVo extends Serializable {

  Long getId();

  void setId(Long id);

  AuthObjectType getAuthObjectType();

  void setAuthObjectType(AuthObjectType authObjectType);

  Long getAuthObjectId();

  void setAuthObjectId(Long authObjectId);

  String getName();

  void setName(String name);

  List<ServicesPermission> getPermissions();

  void setPermissions(List<ServicesPermission> permissions);

  Long getServiceId();

  void setServiceId(Long serviceId);

  Boolean getCreatorFlag();

  void setCreatorFlag(Boolean creatorFlag);
}



