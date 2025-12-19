package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;

import static cloud.xcan.angus.spec.experimental.BizConstant.IPV4_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceSource;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@Accessors(chain = true)
public class MockServiceFindDto extends PageQuery {

  @Schema(description = "Mock service identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for scope filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Mock service name for filtering")
  private String name;

  @Length(max = IPV4_LENGTH)
  @Schema(description = "Node IP address for network-based filtering")
  private String nodeIp;

  @Schema(description = "Node identifier for deployment-based filtering")
  private Long nodeId;

  @Schema(description = "Mock service source type for origin-based filtering")
  private MockServiceSource source;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}

