<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Colon, Hints } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { exec, task, funcPlan, funcCase, apis, services, scenario } from '@/api/tester';
import { user, dept, group } from 'src/api/gm';

import { treeData } from './config';

interface Props {
  template?: 'PROJECT_PROGRESS'|'TASK_SPRINT'|'TASK'|'FUNC_TESTING_PLAN'|'FUNC_TESTING_CASE'|'SERVICES_TESTING_RESULT'|'APIS_TESTING_RESULT'|'SCENARIO_TESTING_RESULT'|'EXEC_FUNCTIONAL_RESULT'|'EXEC_PERFORMANCE_RESULT'|'EXEC_STABILITY_RESULT'|'EXEC_CUSTOMIZATION_RESULT';
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

const checked = ref<string[]>([]);
const creatorTypeConfig = {
  USER: '用户',
  DEPT: '部门',
  GROUP: '组'
};
const creatorObjectName = ref();
const loadCreatorObj = async (creatorObjectId, creatorObjectType) => {
  if (creatorObjectType === 'USER') {
    const [error, { data }] = await user.getUserList({ id: creatorObjectId });
    if (error) {
      return;
    }
    const target = data.list?.[0];
    creatorObjectName.value = target.fullName;
    return;
  }
  if (creatorObjectType === 'dept') {
    const [error, { data }] = await dept.getDeptList({ id: creatorObjectId });
    if (error) {
      return;
    }
    const target = data.list?.[0];
    creatorObjectName.value = target.name;
    return;
  }
  if (creatorObjectType === 'GROUP') {
    const [error, { data }] = await group.getGroupList({ id: creatorObjectId });
    if (error) {
      return;
    }
    const target = data.list?.[0];
    creatorObjectName.value = target.name;
  }
};

const sprintName = ref();
const planName = ref();
const taskName = ref();
const caseName = ref();
const serviceName = ref();
const apisName = ref();
const scenarioName = ref();
const execName = ref();

const loadSprintName = async (sprintId) => {
  const [error, { data }] = await task.getSprintInfo(sprintId);
  if (error) {
    return;
  }
  sprintName.value = data.name;
};

const loadTaskName = async (taskId) => {
  const [error, { data }] = await task.loadTaskInfo(taskId);
  if (error) {
    return;
  }
  taskName.value = data.name;
};

const loadPlanName = async (planId) => {
  const [error, { data }] = await funcPlan.getPlanDetail(planId);
  if (error) {
    return;
  }
  planName.value = data.name;
};

const loadCaseName = async (caseId) => {
  const [error, { data }] = await funcCase.getCaseDetail(caseId);
  if (error) {
    return;
  }
  caseName.value = data.name;
};

const loadServiceAndApisName = async (apisId) => {
  const [error, { data }] = await apis.getApiDetail(apisId);
  if (error) {
    return;
  }
  apisName.value = data.summary;
  serviceName.value = data.serviceName;
};

const loadServiceName = async (serviceId) => {
  const [error, { data }] = await services.loadInfo(serviceId);
  if (error) {
    return;
  }
  serviceName.value = data.name;
};

const loadScenarioName = async (scenarioId) => {
  const [error, { data }] = await scenario.getScenarioDetail(scenarioId);
  if (error) {
    return;
  }
  scenarioName.value = data.name;
};

const loadExecName = async (execId) => {
  const [error, { data }] = await exec.getExecDetail(execId);
  if (error) {
    return;
  }
  execName.value = data.name;
};

onMounted(() => {
  watch(() => props.contentSetting, () => {
    const { creatorObjectType, creatorObjectId, planOrSprintId, targetId } = props.contentSetting;
    if (creatorObjectId && creatorObjectType) {
      loadCreatorObj(creatorObjectId, creatorObjectType);
    }
    if (props.template === 'TASK_SPRINT') {
      loadSprintName(targetId);
      return;
    }
    if (props.template === 'TASK') {
      loadSprintName(planOrSprintId);
      loadTaskName(targetId);
      return;
    }

    if (props.template === 'FUNC_TESTING_PLAN') {
      loadPlanName(targetId);
      return;
    }
    if (props.template === 'FUNC_TESTING_CASE') {
      loadPlanName(planOrSprintId);
      loadCaseName(targetId);
      return;
    }
    if (props.template === 'APIS_TESTING_RESULT') {
      loadServiceAndApisName(targetId);
      return;
    }
    if (props.template === 'SERVICES_TESTING_RESULT') {
      loadServiceName(targetId);
      return;
    }
    if (props.template === 'SCENARIO_TESTING_RESULT') {
      loadScenarioName(targetId);
      return;
    }

    loadExecName(targetId);
  }, {
    deep: true,
    immediate: true
  });
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
      v-if="props.template && ['PROJECT_PROGRESS', 'TASK_SPRINT', 'FUNC_TESTING_PLAN'].includes(props.template)"
      class="spacey-2">
      <div class="leading-7 flex items-center space-x-2">
        <span class="w-12">组织人员</span>
        <span v-if="props.contentSetting.creatorObjectType">（{{ creatorTypeConfig[props.contentSetting.creatorObjectType] }}）</span>
        <Colon />
        <span>{{ creatorObjectName || '--' }}</span>
      </div>
      <div class="leading-7 flex items-center space-x-2">
        <span class="w-12">时间</span>
        <Colon />
        <div v-if="props.contentSetting.createdDateStart && props.contentSetting.createdDateEnd">
          {{ props.contentSetting.createdDateStart }} - {{ props.contentSetting.createdDateEnd }}
        </div>
        <div v-else>
          --
        </div>
      </div>
    </div>

    <div v-if="props.template === 'PROJECT_PROGRESS'" class="leading-7 flex items-center space-x-2">
      <span class="w-12">项目</span>
      <Colon />
      <div>
        {{ props.projectName }}
      </div>
    </div>

    <div v-if="props.template && ['TASK_SPRINT', 'TASK'].includes(props.template)" class="leading-7 flex items-center space-x-2">
      <span class="w-12">迭代</span>
      <Colon />
      <div>
        {{ sprintName }}
      </div>
    </div>

    <div v-if="props.template && ['TASK'].includes(props.template)" class="leading-7 flex items-center space-x-2">
      <span class="w-12">任务</span>
      <Colon />
      <div>
        {{ taskName }}
      </div>
    </div>

    <div v-if="props.template && ['FUNC_TESTING_PLAN', 'FUNC_TESTING_CASE'].includes(props.template)" class="leading-7 flex items-center space-x-2">
      <span class="w-12">计划</span>
      <Colon />
      <div>
        {{ planName }}
      </div>
    </div>

    <div v-if="props.template && ['FUNC_TESTING_CASE'].includes(props.template)" class="leading-7 flex items-center space-x-2">
      <span class="w-12">用例</span>
      <Colon />
      <div>
        {{ caseName }}
      </div>
    </div>

    <div v-if="props.template && ['SERVICES_TESTING_RESULT', 'APIS_TESTING_RESULT'].includes(props.template)" class="leading-7 flex items-center space-x-2">
      <span class="w-12">服务</span>
      <Colon />
      <div>
        {{ serviceName }}
      </div>
    </div>

    <div v-if="props.template && ['APIS_TESTING_RESULT'].includes(props.template)" class="leading-7 flex items-center space-x-2">
      <span class="w-12">接口</span>
      <Colon />
      <div>
        {{ apisName }}
      </div>
    </div>

    <div v-if="props.template && ['SCENARIO_TESTING_RESULT'].includes(props.template)" class="leading-7 flex items-center space-x-2">
      <span class="w-12">场景</span>
      <Colon />
      <div>
        {{ scenarioName }}
      </div>
    </div>

    <div v-if="props.template && props.template.includes('EXEC')" class="leading-7 flex items-center space-x-2">
      <span class="w-12">执行</span>
      <Colon />
      <div>
        {{ execName }}
      </div>
    </div>

    <div class="flex items-center space-x-1 mt-4">
      <span class="h-4 w-1.5 bg-blue-border1"></span>
      <span>内容</span>
      <Hints text="以下是报告输出内容目录信息。" />
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
