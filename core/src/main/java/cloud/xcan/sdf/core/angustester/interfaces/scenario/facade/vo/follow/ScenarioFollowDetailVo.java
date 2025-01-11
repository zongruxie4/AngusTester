package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.follow;


import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ScenarioFollowDetailVo {

  private Long id;

  private Long scenarioId;

  private String scenarioName;

  private String plugin;

}



