package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.follow;


import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseFollowDetailVo {

  private Long id;

  private Long caseId;

  private String caseName;

  private Long projectId;

  private Long planId;

  private String code;
}



