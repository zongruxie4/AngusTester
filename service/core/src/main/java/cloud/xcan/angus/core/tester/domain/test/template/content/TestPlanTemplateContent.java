package cloud.xcan.angus.core.tester.domain.test.template.content;

import cloud.xcan.angus.core.tester.domain.test.template.TestTemplateType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestPlanTemplateContent implements TestTemplateContent{

  private TestTemplateType templateType = TestTemplateType.TEST_PLAN;

  private String testingScope;

  private String testingObjectives;

  private String acceptanceCriteria;

  private String otherInformation;

}
