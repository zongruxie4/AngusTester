package cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.NormalStatus;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
public class NodeDomainFindDto extends PageQuery {

  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String name;

  private NormalStatus status;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



