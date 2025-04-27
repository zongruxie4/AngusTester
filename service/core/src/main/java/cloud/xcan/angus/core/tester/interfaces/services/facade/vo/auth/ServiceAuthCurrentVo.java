package cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServiceAuthCurrentVo {

  private boolean serviceAuth;

  private Set<ServicesPermission> permissions;
}
