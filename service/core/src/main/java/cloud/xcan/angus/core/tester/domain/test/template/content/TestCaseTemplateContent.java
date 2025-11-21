package cloud.xcan.angus.core.tester.domain.test.template.content;

import cloud.xcan.angus.core.tester.domain.test.TestLayer;
import cloud.xcan.angus.core.tester.domain.test.TestPurpose;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseStepView;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestStep;
import cloud.xcan.angus.core.tester.domain.test.template.TestTemplateType;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestCaseTemplateContent implements TestTemplateContent{

  private TestTemplateType templateType = TestTemplateType.TEST_CASE;

  private TestLayer testLayer;

  private TestPurpose testPurpose;

  private String precondition;

  private CaseStepView stepView;

  private List<CaseTestStep> steps;

  private String description;

}
