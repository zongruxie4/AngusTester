package cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.enums.NormalStatus;
import cloud.xcan.angus.remote.PageQuery;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
public class NodeDomainFindDto extends PageQuery {

  private Long id;

  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  private NormalStatus status;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



