package cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.favorite;



import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ScenarioFavouriteDetailVo {

  private Long id;

  private Long scenarioId;

  private String scenarioName;

  private String plugin;

}



