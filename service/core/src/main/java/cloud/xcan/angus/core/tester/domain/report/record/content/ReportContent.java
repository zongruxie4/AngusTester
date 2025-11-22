package cloud.xcan.angus.core.tester.domain.report.record.content;

import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.APIS_TESTING_RESULT;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.EXECUTION_SAMPLING_RESULT;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.FUNC_TESTING_CASE;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.FUNC_TESTING_PLAN;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.OBJECT;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.PROJECT_PROGRESS;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.SCENARIO_TESTING_RESULT;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.SERVICES_TESTING_RESULT;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.TASK;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.TASK_SPRINT;
import static cloud.xcan.angus.core.tester.domain.report.record.content.ReportContent.TEST_EVALUATION;

import cloud.xcan.angus.core.tester.domain.project.evaluation.TestEvaluationResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@JsonTypeInfo(use = Id.NAME, property = "template")
@JsonSubTypes({
    @Type(value = ProjectProgressContent.class, name = PROJECT_PROGRESS),
    @Type(value = TestEvaluationResult.class, name = TEST_EVALUATION),
    @Type(value = TaskSprintContent.class, name = TASK_SPRINT),
    @Type(value = TaskContent.class, name = TASK),
    @Type(value = FuncPlanContent.class, name = FUNC_TESTING_PLAN),
    @Type(value = FuncCaseContent.class, name = FUNC_TESTING_CASE),
    @Type(value = ServicesTestingContent.class, name = SERVICES_TESTING_RESULT),
    @Type(value = ApisTestingContent.class, name = APIS_TESTING_RESULT),
    @Type(value = ScenarioTestingContent.class, name = SCENARIO_TESTING_RESULT),
    @Type(value = ExecutionContent.class, name = EXECUTION_SAMPLING_RESULT),
    @Type(value = ObjectContent.class, name = OBJECT)
})
public interface ReportContent {

  String PROJECT_PROGRESS = "PROJECT_PROGRESS";
  String TEST_EVALUATION = "TEST_EVALUATION";
  String TASK_SPRINT = "TASK_SPRINT";
  String TASK = "TASK";
  String FUNC_TESTING_PLAN = "FUNC_TESTING_PLAN";
  String FUNC_TESTING_CASE = "FUNC_TESTING_CASE";
  String SERVICES_TESTING_RESULT = "SERVICES_TESTING_RESULT";
  String APIS_TESTING_RESULT = "APIS_TESTING_RESULT";
  String SCENARIO_TESTING_RESULT = "SCENARIO_TESTING_RESULT";
  String EXECUTION_SAMPLING_RESULT = "EXECUTION_SAMPLING_RESULT";
  String OBJECT = "OBJECT";

  @JsonIgnore
  String getTemplate();

}
