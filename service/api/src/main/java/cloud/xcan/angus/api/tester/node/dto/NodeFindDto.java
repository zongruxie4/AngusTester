package cloud.xcan.angus.api.tester.node.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.IPV4_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.commonlink.node.NodeSource;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class NodeFindDto extends PageQuery {

  @Schema(description = "Node identifier for specific node lookup")
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Node display name for identification and search")
  private String name;

  @Length(max = IPV4_LENGTH)
  @Schema(description = "Node IP address for network connectivity filtering")
  private String ip;

  @Schema(description = "Node source type for infrastructure classification", example = "CLOUD")
  private NodeSource source;

  @Schema(description = "Node role for functional classification", example = "EXECUTOR")
  private NodeRole role;

  @Schema(description = "Node status filter for active/inactive node listing")
  private Boolean enabled;

  @Schema(description = "Tenant identifier for multi-tenant node filtering")
  private Long tenantId;

  @Schema(description = "Order identifier for purchase transaction tracking")
  private Long orderId;

  @Schema(description = "User identifier who created the node")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Node creation timestamp for timeline filtering")
  private LocalDateTime createdDate;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Node instance expiration date for lifecycle management")
  private LocalDateTime instanceExpiredDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



