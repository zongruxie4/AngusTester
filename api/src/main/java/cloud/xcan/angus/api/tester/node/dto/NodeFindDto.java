package cloud.xcan.angus.api.tester.node.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.IPV4_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.commonlink.node.NodeSource;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.remote.PageQuery;
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

  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  private String name;

  @Length(max = IPV4_LENGTH)
  private String ip;

  private NodeSource source;

  private NodeRole role;

  private Boolean enabled;

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



