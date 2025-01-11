package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.schema;

import cloud.xcan.sdf.api.NameJoinField;
import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServiceServerVo {

  private Long serviceId;

  @NameJoinField(id = "serviceId", repository = "servicesRepo")
  private String serviceName;

  private Server server;

}
