package cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.services.schema.ServiceServer;
import cloud.xcan.angus.core.tester.domain.services.schema.ServicesSchema;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.schema.ServiceSchemaDetailVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.schema.ServiceServerVo;
import java.util.Objects;

public class ServicesSchemaAssembler {

  public static ServiceServerVo toServiceServerVo(ServiceServer server) {
    return new ServiceServerVo().setServiceId(server.getServiceId()).setServer(server.getServer());
  }

  public static ServiceSchemaDetailVo toDetailVo(ServicesSchema schema) {
    return Objects.isNull(schema) ? null :
        new ServiceSchemaDetailVo().setServiceId(schema.getServiceId())
            .setOpenapi(schema.getOpenapi())
            .setInfo(schema.getInfo())
            .setExternalDocs(schema.getExternalDocs())
            .setServers(schema.getServers())
            .setSecurity(schema.getSecurity())
            .setTags(schema.getTags())
            .setExtensions(schema.getExtensions())
            .setSpecVersion(schema.getSpecVersion())
            .setModifiedBy(schema.getModifiedBy())
            .setModifiedDate(schema.getModifiedDate());
  }

}
