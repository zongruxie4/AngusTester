package cloud.xcan.sdf.api.angustester.node.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.IPV4_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.commonlink.node.NodeSource;
import cloud.xcan.sdf.api.enums.NodeRole;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodeFindDto extends PageQuery {

  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  @Length(max = IPV4_LENGTH)
  private String ip;

  private NodeSource source;

  private NodeRole role;

  private Boolean enabledFlag;

  private Long tenantId;

  private Long orderId;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime instanceExpiredDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



