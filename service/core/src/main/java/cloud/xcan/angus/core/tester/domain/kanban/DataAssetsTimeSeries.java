package cloud.xcan.angus.core.tester.domain.kanban;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class DataAssetsTimeSeries {

  private String timeSeries;

  private int value;

}
