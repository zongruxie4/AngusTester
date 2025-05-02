package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample;

import cloud.xcan.angus.remote.OrderSort;
import cloud.xcan.angus.remote.PageQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleFindDto extends PageQuery {

  private Boolean finish;

  private String name;

  private Long nodeId;

  private Long timestamp;

  @Override
  public String getDefaultOrderBy() {
    return "timestamp";
  }

  @Override
  public OrderSort getDefaultOrderSort() {
    return OrderSort.ASC;
  }
}
