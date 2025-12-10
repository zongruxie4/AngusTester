import { contentTreeData as apisContentTreeData } from '@/views/report/add/ApisContentConfig';
import { contentTreeData as caseContentTreeData } from '@/views/report/add/CaseContentConfig';
import { contentTreeData as execFuncContentTreeData } from '@/views/report/add/ExecFuncContentConfig';
import { contentTreeData as execPerfStabilityCustomContentTreeData } from '@/views/report/add/ExecPerfContentConfig';
import { contentTreeData as planContentTreeData } from '@/views/report/add/PlanContentConfig';
import { contentTreeData as projectContentTreeData } from '@/views/report/add/ProjectProcessContentConfig';
import { contentTreeData as scenarioContentTreeData } from '@/views/report/add/ScenarioContentConfig';
import { contentTreeData as serviceContentTreeData } from '@/views/report/add/ServicesContentConfig';
import { contentTreeData as sprintContentTreeData } from '@/views/report/add/SprintContentConfig';
import { contentTreeData as taskContentTreeData } from '@/views/report/add/TaskContentConfig';
import { contentTreeData as testEvaluationContentTreeData } from '@/views/report/add/TestEvaluationContentConfig';
export const treeData = {
  APIS_TESTING_RESULT: apisContentTreeData,
  FUNC_TESTING_CASE: caseContentTreeData,
  EXEC_FUNCTIONAL_RESULT: execFuncContentTreeData,
  EXEC_PERFORMANCE_RESULT: execPerfStabilityCustomContentTreeData,
  EXEC_STABILITY_RESULT: execPerfStabilityCustomContentTreeData,
  EXEC_CUSTOMIZATION_RESULT: execPerfStabilityCustomContentTreeData,
  FUNC_TESTING_PLAN: planContentTreeData,
  PROJECT_PROGRESS: projectContentTreeData,
  SERVICES_TESTING_RESULT: serviceContentTreeData,
  SCENARIO_TESTING_RESULT: scenarioContentTreeData,
  TASK_SPRINT: sprintContentTreeData,
  TASK: taskContentTreeData,
  TEST_EVALUATION: testEvaluationContentTreeData
};
