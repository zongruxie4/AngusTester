package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.favourite;


import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseFavouriteDetailVo {

  private Long id;

  private Long caseId;

  private String caseName;

  private Long projectId;

  private Long planId;

  private String code;

}