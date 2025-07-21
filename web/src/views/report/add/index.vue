<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, ref, watch, Ref } from 'vue';
import { Icon, Input, Modal, notification } from '@xcan-angus/vue-ui';
import { Form, FormItem, Menu, MenuItem, MenuItemGroup, TabPane, Tabs, Textarea } from 'ant-design-vue';

import { reportMenus } from './config';
import dayjs from 'dayjs';
import { report as reportApi } from '@/api/tester';

interface Props {
  reportId?: string;
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  reportId: '',
  visible: false
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void}>();

// const router = useRouter();
// const route = useRoute();
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));
const projectInfo = inject('projectInfo', ref<{[key: string]: string}>({}));
const tenantInfo = inject('tenantInfo');
const projectId = ref();
const loading = ref(false);

const reportId = ref();
const createdDateRef = ref();
const basicDateRef = ref();
const contentRef = ref();
const activeTab = ref('createdDate');
const isChangeDescription = ref(false); // 是否手动修改描述

const CreatedDate = defineAsyncComponent(() => import('@/views/report/add/createdDate/index.vue'));
const Basic = defineAsyncComponent(() => import('@/views/report/add/basic/index.vue'));
const ProjectProcessContent = defineAsyncComponent(() => import('@/views/report/add/projectProcessContent/index.vue'));
const ServiceContent = defineAsyncComponent(() => import('@/views/report/add/servicesContent/index.vue'));
const ApisContent = defineAsyncComponent(() => import('@/views/report/add/apisContent/index.vue'));
const ScenarioContent = defineAsyncComponent(() => import('@/views/report/add/scenarioContent/index.vue'));
const SprintContent = defineAsyncComponent(() => import('@/views/report/add/sprintContent/index.vue'));
const TaskContent = defineAsyncComponent(() => import('@/views/report/add/taskContent/index.vue'));
const PlanContent = defineAsyncComponent(() => import('@/views/report/add/planContent/index.vue'));
const CasesContent = defineAsyncComponent(() => import('@/views/report/add/caseContent/index.vue'));
const ExecFuncContent = defineAsyncComponent(() => import('@/views/report/add/execFuncContent/index.vue'));
const ExecPerfStabilityCustomContent = defineAsyncComponent(() => import('@/views/report/add/execPerfContent/index.vue'));
const formState = ref({
  name: '',
  version: '1.0',
  description: ''
});

const autoSize = {
  minRows: 4,
  maxRows: 10
};

const showReportMenus = computed(() => {
  if (!proTypeShowMap.value.showTask) {
    return reportMenus.filter(item => !['TASK'].includes(item.key));
  }
  if (!proTypeShowMap.value.showSprint) {
    return reportMenus.map(item => {
      if (item.key === 'TASK') {
        return {
          ...item,
          children: item.children.filter(i => i.key !== 'TASK_SPRINT')
        };
      }
      return item;
    });
  }
  return reportMenus;
});

const descriptionChangeMark = () => {
  isChangeDescription.value = true;
};

const reportType = ref(['PROJECT_PROGRESS']);

const category = computed(() => {
  let type = reportType.value[0].split('_')[0];
  if (type === 'SERVICES') {
    type = 'APIS';
  }
  if (type === 'FUNC') {
    type = 'FUNCTIONAL';
  }

  if (type === 'EXEC') {
    type = 'EXECUTION';
  }
  return reportMenus.find(i => i.key === type);
});

const reportTypeName = computed(() => {
  return category.value?.label;
});

const report = computed(() => {
  const targetGroup = reportMenus.find(group => group.label === reportTypeName.value);
  return targetGroup?.children.find(menu => menu.key === reportType.value[0]);
});

const createTimeSetting = ref({
  createdAt: 'NOW'
});

const contentSetting = ref({
  filter: {}
});

const basicInfoSetting = ref({
  reportContacts: '',
  reportCopyright: '',
  otherInformation: '',
  watermark: ''
});

const createdByName = ref();

const isValid = ref(false);
const validate = async () => {
  isValid.value = true;
  const validCreatedDate = createdDateRef.value.validate();
  const validateContent = contentRef.value.validate();
  if (!validCreatedDate) {
    activeTab.value = 'createdDate';
  } else if (!validateContent) {
    activeTab.value = 'content';
  }
  if (!formState.value.name || !formState.value.version || !validCreatedDate || !validateContent) {
    // eslint-disable-next-line prefer-promise-reject-errors
    return Promise.reject();
  }
  return Promise.resolve();
};

const resetData = () => {
  isChangeDescription.value = false;
  reportId.value = undefined;
  createdByName.value = undefined;
  reportType.value = ['PROJECT_PROGRESS'];
  formState.value = {
    name: '',
    description: '',
    version: '1.0'
  };
  createTimeSetting.value = {
    createdAt: 'NOW'
  };
  basicInfoSetting.value = {
    reportContacts: tenantInfo?.value?.fullName + (tenantInfo?.value?.email ? `  ${tenantInfo?.value?.email}` : ''),
    reportCopyright: `©  ${tenantInfo?.value?.tenantName} ${dayjs().year()}。 保留一切权利。\n非经本公司书面许可，任何单位和个人不得擅自摘抄、复制本文档内容的部分或全部，并不得以任何形式传播。`,
    otherInformation: '',
    watermark: tenantInfo?.value?.tenantName
  };
  contentSetting.value = {
    filter: {}
  };
};

// 回显数据
const loadReportById = async () => {
  const [error, { data }] = await reportApi.getReportDetail(reportId.value);
  if (error) {
    return;
  }
  const { template, name, version, description } = data;
  projectId.value = data.projectId;
  reportType.value = [template.value];

  createTimeSetting.value = {
    ...data.createTimeSetting || {},
    createdAt: data.createTimeSetting?.createdAt?.value || 'NOW'
  };
  basicInfoSetting.value = {
    ...data.basicInfoSetting || {}
  };
  contentSetting.value = {
    ...data.contentSetting
  };
  formState.value.name = name;
  formState.value.version = version;
  formState.value.description = description;
  createdByName.value = data.createdByName;
};

// 取消
const cancel = () => {
  emits('update:visible', false);
};

// 确认
const ok = () => {
  validate().then(async () => {
    const params = reportId.value
      ? {
          basicInfoSetting: basicDateRef.value ? basicDateRef.value.getData() : basicInfoSetting.value,
          createTimeSetting: createdDateRef.value ? createdDateRef.value.getData() : createTimeSetting,
          name: formState.value.name,
          description: formState.value.description,
          version: formState.value.version,
          contentSetting: {
            filter: contentRef.value.getData()
          }
        }
      : {
          basicInfoSetting: basicDateRef.value ? basicDateRef.value.getData() : basicInfoSetting.value,
          category: category.value?.key,
          createTimeSetting: createdDateRef.value ? createdDateRef.value.getData() : createTimeSetting,
          name: formState.value.name,
          description: formState.value.description,
          projectId: projectId.value,
          version: formState.value.version,
          template: report.value?.key,
          contentSetting: {
            filter: contentRef.value.getData()
          }
        };
    loading.value = true;
    const [error] = reportId.value
      ? await reportApi.updateReport({
        ...params,
        id: reportId.value
      })
      : await reportApi.addReport(params);
    loading.value = false;
    if (error) {
      return;
    }
    if (reportId.value) {
      notification.success('更新报告成功');
    } else {
      notification.success('添加报告成功');
    }
    emits('ok');
    cancel();
    // router.push('/report');
  });
};

onMounted(() => {
  watch(() => props.visible, newValue => {
    if (newValue && props.reportId) {
      reportId.value = props.reportId;
      loadReportById();
    } else {
      resetData();
      if (!props.reportId) {
        watch(() => report.value, () => {
          if (!isChangeDescription.value) {
            formState.value.description = report.value?.description;
          }
        }, {
          immediate: true
        });
      }
      watch(() => projectInfo.value, newValue => {
        if (newValue?.id) {
          projectId.value = newValue.id;
        }
      }, {
        immediate: true
      });
    }
  }, {
    immediate: true
  });
});

</script>
<template>
  <Modal
    :visible="props.visible"
    :title="reportId ? '编辑报告' : '添加报告'"
    :width="1200"
    :okButtonProps="{
      loading: loading
    }"
    @ok="ok"
    @cancel="cancel">
    <div class="flex flex-col" style="height: 80vh;">
      <!-- <div  v-show="!reportId" class="h-12 leading-12 bg-gray-bg border-b font-semibold text-4 px-5"> 添加报告 </div> -->
      <div class="flex flex-1 min-h-0">
        <div v-show="!reportId" class="w-50 h-full border-r overflow-y-auto overflow-x-hidden py-5">
          <Menu
            v-model:selectedKeys="reportType"
            :items="showReportMenus"
            :disabled="!!reportId"
            class="border-r-0 h-full"
            mode="inline">
            <MenuItemGroup v-for="group in showReportMenus" :key="group.key">
              <template #title>
                <div class="flex items-center space-x-1.5 text-black-log-bg">
                  <Icon :icon="group.icon" class="text-3.5" />
                  <span class="font-medium text-3">{{ group.label }}</span>
                </div>
              </template>
              <MenuItem v-for="item in group.children" :key="item.key">
                <div class="flex justify-between items-center">
                  <span class="pl-3 flex-1 min-w-0 text-3">{{ item.label }}</span>
                  <Icon v-show="reportType[0] === item.key" icon="icon-duihaolv" />
                </div>
              </MenuItem>
            </MenuItemGroup>
          </Menu>
        </div>
        <div class="flex-1 min-w-0 overflow-y-auto p-4">
          <div class="border-blue-border border rounded bg-blue-bg4  px-6 py-4 flex items-center space-x-7">
            <img src="./image/default.png" class="rounded-full w-15 h-15" />
            <div class="space-y-2 flex-1 min-w-0">
              <div class="font-semibold text-3.5">{{ report?.label }}</div>
              <div class="flex justify-between">
                <div class="text-3 flex-1">分类： {{ reportTypeName }}</div>
                <div class="text-3 flex-1">报告人： {{ reportId ? createdByName : tenantInfo?.fullName }}</div>
              </div>
              <div class="text-3">描述： {{ report?.description }}</div>
            </div>
          </div>
          <Form :labelCol="{style: {width: '50px'}}">
            <div class="flex space-x-2 items-center mt-4">
              <FormItem
                class="flex-1 min-w-0 !mb-5"
                :class="{'ant-form-item-has-error': isValid && !formState.name}"
                required
                label="名称">
                <Input
                  v-model:value="formState.name"
                  :maxlength="100"
                  placeholder="报告名称，最长100个字符" />
              </FormItem>
              <FormItem
                class="flex-1 min-w-0 !mb-5"
                :class="{'ant-form-item-has-error': isValid && !formState.version }"
                required
                label="版本">
                <Input
                  v-model:value="formState.version"
                  :maxlength="20"
                  placeholder="报告版本，默认1.0.0，最长20个字符" />
              </FormItem>
            </div>
            <div class="flex space-x-2 items-center">
              <FormItem class="flex-1 min-w-0 !mb-5" label="描述">
                <Textarea
                  v-model:value="formState.description"
                  :maxlength="200"
                  placeholder="报告描述，使用描述解释此报告的内容和目的，或添加其他资源的链接，最长200个字符"
                  showCount
                  :autoSize="autoSize"
                  @change="descriptionChangeMark" />
              </FormItem>
            </div>
          </Form>
          <Tabs v-model:activeKey="activeTab" size="small">
            <TabPane key="createdDate" tab="生成时间">
              <CreatedDate
                ref="createdDateRef"
                :createTimeSetting="createTimeSetting"
                :showPeriodically="!reportType[0].includes('EXEC')" />
            </TabPane>
            <TabPane key="basic" tab="基本信息">
              <Basic ref="basicDateRef" :basicInfoSetting="basicInfoSetting" />
            </TabPane>
            <TabPane
              key="content"
              tab="内容"
              forceRender>
              <template v-if="reportType[0] === 'PROJECT_PROGRESS'">
                <ProjectProcessContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'SERVICES_TESTING_RESULT'">
                <ServiceContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'APIS_TESTING_RESULT'">
                <ApisContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'SCENARIO_TESTING_RESULT'">
                <ScenarioContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'TASK_SPRINT'">
                <SprintContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'TASK'">
                <TaskContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'FUNC_TESTING_PLAN'">
                <PlanContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'FUNC_TESTING_CASE'">
                <CasesContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'EXEC_FUNCTIONAL_RESULT'">
                <ExecFuncContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'EXEC_PERFORMANCE_RESULT'">
                <ExecPerfStabilityCustomContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  execType="TEST_PERFORMANCE"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'EXEC_STABILITY_RESULT'">
                <ExecPerfStabilityCustomContent
                  ref="contentRef"
                  execType="TEST_STABILITY"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportType[0] === 'EXEC_CUSTOMIZATION_RESULT'">
                <ExecPerfStabilityCustomContent
                  ref="contentRef"
                  execType="TEST_CUSTOMIZATION"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
            </TabPane>
          </Tabs>
        </div>
      </div>
      <!-- <div class="h-15 text-center border-t pt-4 space-x-2">
        <Button
          type="primary"
          size="small"
          @click="ok">
          确认
        </Button>
        <Button
          href="/report"
          size="small">
          取消
        </Button>
      </div> -->
    </div>
  </Modal>
</template>
<style scoped>
:deep(.ant-menu-inline .ant-menu-item) {
  height: 28px;
}

:deep(.ant-menu-inline .ant-menu-item-group-title) {
  @apply py-1;
}

:deep(.ant-form-item-label>label){
  height: 28px;
}
</style>
