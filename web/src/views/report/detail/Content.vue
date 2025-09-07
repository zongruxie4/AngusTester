<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Hints } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { apis, exec, funcCase, funcPlan, scenario, services, task } from '@/api/tester';
import { dept, group, user } from '@/api/gm';
import { ReportTemplate } from '@/enums/enums';
import { AuthObjectType, enumOptionUtils } from '@xcan-angus/infra';

import { treeData } from './config';

const { t } = useI18n();

// Component props definition
interface Props {
  template?: ReportTemplate;
  projectId: string;
  contentSetting: {
    targetId: string;
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string;
    planOrSprintId?: string;
  };
  projectName: string;
}

const props = withDefaults(defineProps<Props>(), {
  template: undefined,
  projectId: '',
  contentSetting: () => ({
    targetId: ''
  })
});

// Checked keys for tree component
const checked = ref<string[]>([]);

// Creator type configuration mapping
const authObjectTypeOpt = enumOptionUtils.loadEnumAsOptions(AuthObjectType);

// Creator object name
const creatorObjectName = ref();

/**
 * Load creator object name based on object type and ID
 * @param creatorObjectId - ID of the creator object
 * @param creatorObjectType - Type of the creator object (USER, DEPT, GROUP)
 */
const loadCreatorObj = async (creatorObjectId, creatorObjectType) => {
  if (creatorObjectType === AuthObjectType.USER) {
    const [error, { data }] = await user.getUserList({ id: creatorObjectId });
    if (error) {
      return;
    }
    const target = data.list?.[0];
    creatorObjectName.value = target.fullName;
    return;
  }
  if (creatorObjectType === AuthObjectType.DEPT) {
    const [error, { data }] = await dept.getDeptList({ id: creatorObjectId });
    if (error) {
      return;
    }
    const target = data.list?.[0];
    creatorObjectName.value = target.name;
    return;
  }
  if (creatorObjectType === AuthObjectType.GROUP) {
    const [error, { data }] = await group.getGroupList({ id: creatorObjectId });
    if (error) {
      return;
    }
    const target = data.list?.[0];
    creatorObjectName.value = target.name;
  }
};

// Reactive variables for storing names of various entities
const sprintName = ref();
const planName = ref();
const taskName = ref();
const caseName = ref();
const serviceName = ref();
const apisName = ref();
const scenarioName = ref();
const execName = ref();

/**
 * Load sprint name by ID
 * @param sprintId - ID of the sprint
 */
const loadSprintName = async (sprintId) => {
  const [error, { data }] = await task.getSprintDetail(sprintId);
  if (error) {
    return;
  }
  sprintName.value = data.name;
};

/**
 * Load task name by ID
 * @param taskId - ID of the task
 */
const loadTaskName = async (taskId) => {
  const [error, { data }] = await task.getTaskDetail(taskId);
  if (error) {
    return;
  }
  taskName.value = data.name;
};

/**
 * Load plan name by ID
 * @param planId - ID of the plan
 */
const loadPlanName = async (planId) => {
  const [error, { data }] = await funcPlan.getPlanDetail(planId);
  if (error) {
    return;
  }
  planName.value = data.name;
};

/**
 * Load case name by ID
 * @param caseId - ID of the case
 */
const loadCaseName = async (caseId) => {
  const [error, { data }] = await funcCase.getCaseDetail(caseId);
  if (error) {
    return;
  }
  caseName.value = data.name;
};

/**
 * Load service and API names by API ID
 * @param apisId - ID of the API
 */
const loadServiceAndApisName = async (apisId) => {
  const [error, { data }] = await apis.getApiDetail(apisId);
  if (error) {
    return;
  }
  apisName.value = data.summary;
  serviceName.value = data.serviceName;
};

/**
 * Load service name by ID
 * @param serviceId - ID of the service
 */
const loadServiceName = async (serviceId) => {
  const [error, { data }] = await services.loadInfo(serviceId);
  if (error) {
    return;
  }
  serviceName.value = data.name;
};

/**
 * Load scenario name by ID
 * @param scenarioId - ID of the scenario
 */
const loadScenarioName = async (scenarioId) => {
  const [error, { data }] = await scenario.getScenarioDetail(scenarioId);
  if (error) {
    return;
  }
  scenarioName.value = data.name;
};

/**
 * Load execution name by ID
 * @param execId - ID of the execution
 */
const loadExecName = async (execId) => {
  const [error, { data }] = await exec.getExecDetail(execId);
  if (error) {
    return;
  }
  execName.value = data.name;
};

/**
 * Lifecycle hook - Initialize component
 * Watch for content setting changes and load corresponding data
 */
onMounted(() => {
  watch(() => props.contentSetting, () => {
    const { creatorObjectType, creatorObjectId, planOrSprintId, targetId } = props.contentSetting;
    if (creatorObjectId && creatorObjectType) {
      loadCreatorObj(creatorObjectId, creatorObjectType);
    }
    if (props.template === ReportTemplate.TASK_SPRINT) {
      loadSprintName(targetId);
      return;
    }
    if (props.template === ReportTemplate.TASK) {
      loadSprintName(planOrSprintId);
      loadTaskName(targetId);
      return;
    }

    if (props.template === ReportTemplate.FUNC_TESTING_PLAN) {
      loadPlanName(targetId);
      return;
    }
    if (props.template === ReportTemplate.FUNC_TESTING_CASE) {
      loadPlanName(planOrSprintId);
      loadCaseName(targetId);
      return;
    }
    if (props.template === ReportTemplate.APIS_TESTING_RESULT) {
      loadServiceAndApisName(targetId);
      return;
    }
    if (props.template === ReportTemplate.SERVICES_TESTING_RESULT) {
      loadServiceName(targetId);
      return;
    }
    if (props.template === ReportTemplate.SCENARIO_TESTING_RESULT) {
      loadScenarioName(targetId);
      return;
    }

    loadExecName(targetId);
  }, {
    deep: true,
    immediate: true
  });

  /**
   * Watch for template changes and update checked tree nodes
   */
  watch(() => props.template, newValue => {
    if (newValue) {
      (treeData[newValue] || []).forEach(item => {
        checked.value.push(item.key);
        if (item.children) {
          checked.value.push(...item.children.map(i => i.key));
        }
      });
    }
  }, {
    immediate: true
  });
});
</script>
<template>
  <div>
    <div
      v-if="props.template && [ReportTemplate.PROJECT_PROGRESS, ReportTemplate.TASK_SPRINT, ReportTemplate.FUNC_TESTING_PLAN].includes(props.template)"
      class="spacey-2">
      <div class="leading-7 flex items-center space-x-2">
        <span class="w-12">{{ t('reportHome.reportDetail.content.organizationPersonnel') }}</span>
        <span v-if="props.contentSetting.creatorObjectType">（{{
          authObjectTypeOpt[props.contentSetting.creatorObjectType]
        }}）</span>
        <Colon />
        <span>{{ creatorObjectName || '--' }}</span>
      </div>
      <div class="leading-7 flex items-center space-x-2">
        <span class="w-12">{{ t('reportHome.reportDetail.content.time') }}</span>
        <Colon />
        <div v-if="props.contentSetting.createdDateStart && props.contentSetting.createdDateEnd">
          {{ props.contentSetting.createdDateStart }} - {{ props.contentSetting.createdDateEnd }}
        </div>
        <div v-else>
          --
        </div>
      </div>
    </div>

    <div v-if="props.template === ReportTemplate.PROJECT_PROGRESS" class="leading-7 flex items-center space-x-2">
      <span class="w-12">{{ t('reportHome.reportDetail.content.project') }}</span>
      <Colon />
      <div>
        {{ props.projectName }}
      </div>
    </div>

    <div
      v-if="props.template && [ReportTemplate.TASK_SPRINT, ReportTemplate.TASK].includes(props.template)"
      class="leading-7 flex items-center space-x-2">
      <span class="w-12">{{ t('reportHome.reportDetail.content.sprint') }}</span>
      <Colon />
      <div>
        {{ sprintName }}
      </div>
    </div>

    <div
      v-if="props.template && [ReportTemplate.TASK].includes(props.template)"
      class="leading-7 flex items-center space-x-2">
      <span class="w-12">{{ t('reportHome.reportDetail.content.task') }}</span>
      <Colon />
      <div>
        {{ taskName }}
      </div>
    </div>

    <div
      v-if="props.template && [ReportTemplate.FUNC_TESTING_PLAN, ReportTemplate.FUNC_TESTING_CASE].includes(props.template)"
      class="leading-7 flex items-center space-x-2">
      <span class="w-12">{{ t('reportHome.reportDetail.content.plan') }}</span>
      <Colon />
      <div>
        {{ planName }}
      </div>
    </div>

    <div
      v-if="props.template && [ReportTemplate.FUNC_TESTING_CASE].includes(props.template)"
      class="leading-7 flex items-center space-x-2">
      <span class="w-12">{{ t('reportHome.reportDetail.content.case') }}</span>
      <Colon />
      <div>
        {{ caseName }}
      </div>
    </div>

    <div
      v-if="props.template && [ReportTemplate.SERVICES_TESTING_RESULT, ReportTemplate.APIS_TESTING_RESULT].includes(props.template)"
      class="leading-7 flex items-center space-x-2">
      <span class="w-12">{{ t('reportHome.reportDetail.content.service') }}</span>
      <Colon />
      <div>
        {{ serviceName }}
      </div>
    </div>

    <div
      v-if="props.template && [ReportTemplate.APIS_TESTING_RESULT].includes(props.template)"
      class="leading-7 flex items-center space-x-2">
      <span class="w-12">{{ t('reportHome.reportDetail.content.api') }}</span>
      <Colon />
      <div>
        {{ apisName }}
      </div>
    </div>

    <div
      v-if="props.template && [ReportTemplate.SCENARIO_TESTING_RESULT].includes(props.template)"
      class="leading-7 flex items-center space-x-2">
      <span class="w-12">{{ t('reportHome.reportDetail.content.scenario') }}</span>
      <Colon />
      <div>
        {{ scenarioName }}
      </div>
    </div>

    <div
      v-if="props.template && props.template.includes('EXEC')"
      class="leading-7 flex items-center space-x-2">
      <span class="w-12">{{ t('reportHome.reportDetail.content.execution') }}</span>
      <Colon />
      <div>
        {{ execName }}
      </div>
    </div>

    <div class="flex items-center space-x-1 mt-4">
      <span class="h-4 w-1.5 bg-blue-border1"></span>
      <span>{{ t('reportHome.reportDetail.content.content') }}</span>
      <Hints :text="t('reportHome.reportDetail.content.contentHint')" />
    </div>
    <Tree
      v-model:checkedKeys="checked"
      class="mt-2 text-3"
      disabled
      :treeData="treeData[props.template || '']|| []"
      :defaultExpandAll="true"
      :selectable="false"
      :checkable="true">
      <template #title="{title}">
        <span style="color: rgb(82, 90, 101);">{{ title }}</span>
      </template>
    </Tree>
  </div>
</template>
