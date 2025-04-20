package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import java.io.Serializable;
import java.util.List;

/**
 * @author XiaoLong Liu
 */public interface ServiceAuthVo extends Serializable {

  Long getId();

  void setId(Long id);

  AuthObjectType getAuthObjectType();

  void setAuthObjectType(AuthObjectType authObjectType);

  Long getAuthObjectId();

  void setAuthObjectId(Long authObjectId);

  String getName();

  void setName(String name);

  List<MockServicePermission> getPermissions();

  void setPermissions(List<MockServicePermission> permissions);

  Boolean getCreator();

  void setCreator(Boolean creator);
}
