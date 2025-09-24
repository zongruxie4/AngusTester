<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Input, Modal, notification } from '@xcan-angus/vue-ui';
import { Form, FormItem, Menu, MenuItem, MenuItemGroup, TabPane, Tabs, Textarea } from 'ant-design-vue';
import { appContext, CreatedAt, ScriptType } from '@xcan-angus/infra';
import { ReportTemplate, ReportCategory } from '@/enums/enums';

import { reportMenus } from './config';
import dayjs from 'dayjs';
import { report as reportApi } from '@/api/tester';

const { t } = useI18n();

// Component props definition
interface Props {
  reportId?: string;
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  reportId: '',
  visible: false
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok'):void}>();

// Injected dependencies
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));
const projectInfo = inject('projectInfo', ref<{[key: string]: string}>({}));
const tenantInfo = ref(appContext.getUser());

// Reactive variables
const projectId = ref();
const loading = ref(false);
const reportIdRef = ref(); // Renamed from reportId to avoid conflict with prop
const createdDateRef = ref();
const basicDateRef = ref();
const contentRef = ref();
const activeTab = ref('createdDate');
const isChangeDescription = ref(false); // Whether description was manually changed

// Async components
const CreatedDate = defineAsyncComponent(() => import('@/views/report/add/CreatedDate.vue'));
const Basic = defineAsyncComponent(() => import('@/views/report/add/Basic.vue'));
const ProjectProcessContent = defineAsyncComponent(() => import('@/views/report/add/ProjectProcessContent.vue'));
const ServiceContent = defineAsyncComponent(() => import('@/views/report/add/ServicesContent.vue'));
const ApisContent = defineAsyncComponent(() => import('@/views/report/add/ApisContent.vue'));
const ScenarioContent = defineAsyncComponent(() => import('@/views/report/add/ScenarioContent.vue'));
const SprintContent = defineAsyncComponent(() => import('@/views/report/add/SprintContent.vue'));
const TaskContent = defineAsyncComponent(() => import('@/views/report/add/TaskContent.vue'));
const PlanContent = defineAsyncComponent(() => import('@/views/report/add/PlanContent.vue'));
const CasesContent = defineAsyncComponent(() => import('@/views/report/add/CaseContent.vue'));
const ExecFuncContent = defineAsyncComponent(() => import('@/views/report/add/ExecFuncContent.vue'));
const ExecPerfStabilityCustomContent = defineAsyncComponent(() => import('@/views/report/add/ExecPerfContent.vue'));

// Form state
const formState = ref({
  name: '',
  version: '1.0',
  description: ''
});

// Textarea autosize configuration
const autoSize = {
  minRows: 4,
  maxRows: 10
};

/**
 * Compute report menus based on project type visibility settings
 * @returns Filtered report menus based on project type settings
 */
const showReportMenus = computed(() => {
  if (!proTypeShowMap.value.showTask) {
    return reportMenus.filter(item => ![ReportTemplate.TASK].includes(item.key));
  }
  if (!proTypeShowMap.value.showSprint) {
    return reportMenus.map(item => {
      if (item.key === ReportTemplate.TASK) {
        return {
          ...item,
          children: item.children.filter(i => i.key !== ReportTemplate.TASK_SPRINT)
        };
      }
      return item;
    });
  }
  return reportMenus;
});

/**
 * Mark description as manually changed
 */
const descriptionChangeMark = () => {
  isChangeDescription.value = true;
};

// Report type selection
const reportTemplate = ref([ReportTemplate.PROJECT_PROGRESS]);

/**
 * Compute report category based on selected report type
 * @returns Report category object
 */
const category = computed(() => {
  let type = reportTemplate.value[0].split('_')[0];
  if (type === 'SERVICES') {
    type = ReportCategory.APIS;
  }
  if (type === 'FUNC') {
    type = ReportCategory.FUNCTIONAL;
  }

  if (type === 'EXEC') {
    type = ReportCategory.EXECUTION;
  }
  return reportMenus.find(i => i.key === type);
});

/**
 * Compute report type name
 * @returns Report type name
 */
const reportTypeName = computed(() => {
  return category.value?.label;
});

/**
 * Compute selected report details
 * @returns Selected report object
 */
const report = computed(() => {
  const targetGroup = reportMenus.find(group => group.label === reportTypeName.value);
  return targetGroup?.children.find(menu => menu.key === reportTemplate.value[0]);
});

// Creation time settings
const createTimeSetting = ref({
  createdAt: CreatedAt.NOW
});

// Content settings
const contentSetting = ref({
  filter: {}
});

// Basic info settings
const basicInfoSetting = ref({
  reportContacts: '',
  reportCopyright: '',
  otherInformation: '',
  watermark: ''
});

// Creator name
const createdByName = ref();

// Validation state
const isValid = ref(false);

/**
 * Validate form data
 * @returns Promise resolving if validation passes, rejecting if it fails
 */
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

/**
 * Reset form data to initial state
 */
const resetData = () => {
  isChangeDescription.value = false;
  reportIdRef.value = undefined;
  createdByName.value = undefined;
  reportTemplate.value = [ReportTemplate.PROJECT_PROGRESS];
  formState.value = {
    name: '',
    description: '',
    version: '1.0'
  };
  createTimeSetting.value = {
    createdAt: CreatedAt.NOW
  };
  basicInfoSetting.value = {
    reportContacts: tenantInfo?.value?.fullName + (tenantInfo?.value?.email ? `  ${tenantInfo?.value?.email}` : ''),
    reportCopyright: t('reportAdd.reportCopyright', {
      tenantName: tenantInfo?.value?.tenantName,
      year: dayjs().year()
    }),
    otherInformation: '',
    watermark: tenantInfo?.value?.tenantName
  };
  contentSetting.value = {
    filter: {}
  };
};

/**
 * Load report data by ID for editing
 * @returns Promise resolving when data is loaded
 */
const loadReportById = async () => {
  const [error, { data }] = await reportApi.getReportDetail(reportIdRef.value);
  if (error) {
    return;
  }
  const { template, name, version, description } = data;
  projectId.value = data.projectId;
  reportTemplate.value = [template.value];

  createTimeSetting.value = {
    ...data.createTimeSetting || {},
    createdAt: data.createTimeSetting?.createdAt?.value || CreatedAt.NOW
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

/**
 * Cancel report creation/editing
 */
const cancel = () => {
  emits('update:visible', false);
};

/**
 * Confirm and save report
 * Handles both creation and updating of reports
 */
const ok = () => {
  validate().then(async () => {
    const params = reportIdRef.value
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
    const [error] = reportIdRef.value
      ? await reportApi.updateReport({
        ...params,
        id: reportIdRef.value
      })
      : await reportApi.addReport(params);
    loading.value = false;
    if (error) {
      return;
    }
    if (reportIdRef.value) {
      notification.success(t('reportAdd.notification.updateSuccess'));
    } else {
      notification.success(t('reportAdd.notification.addSuccess'));
    }
    emits('ok');
    cancel();
    // router.push('/report');
  });
};

/**
 * Lifecycle hook - Initialize component
 * Watch for visibility changes and handle report loading/reset
 */
onMounted(() => {
  watch(() => props.visible, newValue => {
    if (newValue && props.reportId) {
      reportIdRef.value = props.reportId;
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
    :title="reportId ? t('actions.edit') : t('actions.add')"
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
            v-model:selectedKeys="reportTemplate"
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
                  <Icon v-show="reportTemplate[0] === item.key" icon="icon-duihaolv" />
                </div>
              </MenuItem>
            </MenuItemGroup>
          </Menu>
        </div>
        <div class="flex-1 min-w-0 overflow-y-auto p-4">
          <div class="border-blue-border border rounded bg-blue-bg4  px-6 py-4 flex items-center space-x-7">
            <img src="../../../assets/images/default.png" class="rounded-full w-15 h-15" />
            <div class="space-y-2 flex-1 min-w-0">
              <div class="font-semibold text-3.5">{{ report?.label }}</div>
              <div class="flex justify-between">
                <div class="text-3 flex-1">{{ t('reportAdd.reportInfo.category') }}： {{ reportTypeName }}</div>
                <div class="text-3 flex-1">{{ t('reportAdd.reportInfo.reporter') }}： {{ reportId ? createdByName : tenantInfo?.fullName }}</div>
              </div>
              <div class="text-3">{{ t('reportAdd.reportInfo.description') }}： {{ report?.description }}</div>
            </div>
          </div>
          <Form :labelCol="{style: {width: '50px'}}">
            <div class="flex space-x-2 items-center mt-4">
              <FormItem
                class="flex-1 min-w-0 !mb-5"
                :class="{'ant-form-item-has-error': isValid && !formState.name}"
                required
                :label="t('reportAdd.form.name')">
                <Input
                  v-model:value="formState.name"
                  :maxlength="100"
                  :placeholder="t('reportAdd.form.namePlaceholder')" />
              </FormItem>
              <FormItem
                class="flex-1 min-w-0 !mb-5"
                :class="{'ant-form-item-has-error': isValid && !formState.version }"
                required
                :label="t('reportAdd.form.version')">
                <Input
                  v-model:value="formState.version"
                  :maxlength="20"
                  :placeholder="t('reportAdd.form.versionPlaceholder')" />
              </FormItem>
            </div>
            <div class="flex space-x-2 items-center">
              <FormItem class="flex-1 min-w-0 !mb-5" :label="t('reportAdd.form.description')">
                <Textarea
                  v-model:value="formState.description"
                  :maxlength="200"
                  :placeholder="t('reportAdd.form.descriptionPlaceholder')"
                  showCount
                  :autoSize="autoSize"
                  @change="descriptionChangeMark" />
              </FormItem>
            </div>
          </Form>
          <Tabs v-model:activeKey="activeTab" size="small">
            <TabPane key="createdDate" :tab="t('reportAdd.tabs.createdDate')">
              <CreatedDate
                ref="createdDateRef"
                :createTimeSetting="createTimeSetting"
                :showPeriodically="!reportTemplate[0].includes('EXEC')" />
            </TabPane>
            <TabPane key="basic" :tab="t('reportAdd.tabs.basic')">
              <Basic ref="basicDateRef" :basicInfoSetting="basicInfoSetting" />
            </TabPane>
            <TabPane
              key="content"
              :tab="t('reportAdd.tabs.content')"
              forceRender>
              <template v-if="reportTemplate[0] === ReportTemplate.PROJECT_PROGRESS">
                <ProjectProcessContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.SERVICES_TESTING_RESULT">
                <ServiceContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.APIS_TESTING_RESULT">
                <ApisContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.SCENARIO_TESTING_RESULT">
                <ScenarioContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.TASK_SPRINT">
                <SprintContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.TASK">
                <TaskContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.FUNC_TESTING_PLAN">
                <PlanContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.FUNC_TESTING_CASE">
                <CasesContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.EXEC_FUNCTIONAL_RESULT">
                <ExecFuncContent
                  ref="contentRef"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.EXEC_PERFORMANCE_RESULT">
                <ExecPerfStabilityCustomContent
                  ref="contentRef"
                  :execType="ScriptType.TEST_PERFORMANCE"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.EXEC_STABILITY_RESULT">
                <ExecPerfStabilityCustomContent
                  ref="contentRef"
                  :execType="ScriptType.TEST_STABILITY"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
              <template v-if="reportTemplate[0] === ReportTemplate.EXEC_CUSTOMIZATION_RESULT">
                <ExecPerfStabilityCustomContent
                  ref="contentRef"
                  :execType="ScriptType.TEST_CUSTOMIZATION"
                  :disabled="!!reportId"
                  :projectId="projectId"
                  :contentSetting="contentSetting.filter" />
              </template>
            </TabPane>
          </Tabs>
        </div>
      </div>
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
