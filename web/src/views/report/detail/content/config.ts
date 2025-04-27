import { contentTreeData as apisContentTreeData } from '@/views/report/add/apisContent/config';
import { contentTreeData as caseContentTreeData } from '@/views/report/add/caseContent/config';
import { contentTreeData as execFuncContentTreeData } from '@/views/report/add/execFuncContent/config';
import { contentTreeData as execPerfStabilityCustomContentTreeData } from '@/views/report/add/execPerfContent/config';
import { contentTreeData as planContentTreeData } from '@/views/report/add/planContent/config';
import { contentTreeData as projectContentTreeData } from '@/views/report/add/projectProcessContent/config';
import { contentTreeData as scenarioContentTreeData } from '@/views/report/add/scenarioContent/config';
import { contentTreeData as serviceContentTreeData } from '@/views/report/add/servicesContent/config';
import { contentTreeData as sprintContentTreeData } from '@/views/report/add/sprintContent/config';
import { contentTreeData as taskContentTreeData } from '@/views/report/add/taskContent/config';

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
  TASK: taskContentTreeData
};
