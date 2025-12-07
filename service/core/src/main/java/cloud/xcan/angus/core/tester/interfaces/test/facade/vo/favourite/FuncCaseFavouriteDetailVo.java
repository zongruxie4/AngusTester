package cloud.xcan.angus.core.tester.interfaces.test.facade.vo.favourite;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


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
