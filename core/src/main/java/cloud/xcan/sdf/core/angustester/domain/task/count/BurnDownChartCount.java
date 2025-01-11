package cloud.xcan.sdf.core.angustester.domain.task.count;

import cloud.xcan.sdf.core.angustester.domain.kanban.DataTimeSeries;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class BurnDownChartCount {

  private Object total;

  private Object completed;

  private Object remained;

  private LocalDateTime startDate;

  private LocalDateTime endDate;

  private List<DataTimeSeries> remaining;

  private List<DataTimeSeries> expected;

  //private List<String> days;

}
