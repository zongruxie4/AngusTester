package cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.schema;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServiceSchemaDetailVo {

  private Long serviceId;

  private String openapi;

  private Info info;

  private ExternalDocumentation externalDocs;

  private List<Server> servers;

  private List<SecurityRequirement> security;

  private List<Tag> tags;

  private Map<String, Object> extensions;

  private SpecVersion specVersion;

  private Long lastModifiedBy;

  private LocalDateTime lastModifiedDate;

}
