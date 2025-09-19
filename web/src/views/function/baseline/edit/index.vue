<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Form, FormItem, TabPane, Tabs } from 'ant-design-vue';
import { AsyncComponent, Icon, Input, modal, notification, Select, Spin, Table } from '@xcan-angus/vue-ui';
import { EnumMessage, EvalWorkloadMethod, utils, TESTER, enumUtils, duration, SearchCriteria } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { debounce } from 'throttle-debounce';
import { func, project } from '@/api/tester';
import { BasicProps } from '@/types/types';

import { BaselineCaseInfo, BaselineDetail, BaselineEditState } from '@/views/function/baseline/types';

const { t } = useI18n();

// Props Definition
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Async Components
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));
const SelectCaseModal = defineAsyncComponent(() => import('./SelectCaseModal.vue'));

// Injected Dependencies
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

// Reactive Data
const projectMembers = ref([]);
const currentBaselineId = ref();
const formRef = ref();
const isSelectCaseModalVisible = ref(false);
const searchKeywords = ref();
const isLoading = ref(false);
const baselineDataSource = ref<BaselineDetail>();
const activeTabKey = ref('funcCase');
const evalWorkloadMethodOptions = ref<EnumMessage<EvalWorkloadMethod>[]>([]);
const isBaselineFlagVisible = ref(false);
const originalFormState = ref<BaselineEditState>();
const currentFormState = ref<BaselineEditState>({
  planId: '',
  description: '',
  name: '',
  caseIds: []
});

/**
 * Get form parameters for API calls
 * @returns Form parameters object
 */
const getFormParameters = () => {
  const params: BaselineEditState = { ...currentFormState.value, caseIds: baselineCaseList.value.map(i => i.id) };
  if (baselineDataSource.value?.id) {
    params.id = baselineDataSource.value.id;
  }

  if (!params.description && !params.id) {
    delete params.description;
  }

  if (isEditMode.value) {
    delete params.caseIds;
  }

  delete params.date;
  return params;
};

/**
 * Refresh baseline list in parent component
 */
const refreshBaselineList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'baselineList', notify: utils.uuid() });
  });
};

/**
 * Handle baseline edit confirmation
 */
const handleBaselineEditConfirm = async () => {
  const isFormUnchanged = isEqual(originalFormState.value, currentFormState.value);
  if (isFormUnchanged) {
    return;
  }

  const params = getFormParameters();
  delete params.planId;
  isLoading.value = true;
  const [error] = await func.updateBaseline(params);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('functionBaseline.editForm.modifySuccess'));

  const id = params.id;
  const name = params.name;
  updateTabPane({ _id: id, name });
  if (baselineDataSource.value) {
    baselineDataSource.value.name = name;
  }
};

/**
 * Handle baseline add confirmation
 */
const handleBaselineAddConfirm = async () => {
  const params = getFormParameters();
  isLoading.value = true;
  const [error, res] = await func.addBaseline(params);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('functionBaseline.editForm.addSuccess'));

  const _id = props.data?._id;
  const newId = res?.data?.id;
  const name = params.name;
  replaceTabPane(_id, { _id: newId, uiKey: newId, name, data: { _id: newId, id: newId } });
};

/**
 * Handle form submission
 */
const handleFormSubmit = async () => {
  formRef.value.validate().then(async () => {
    if (!isEditMode.value) {
      await handleBaselineAddConfirm();
    } else {
      await handleBaselineEditConfirm();
    }
    refreshBaselineList();
  });
};

/**
 * Handle baseline deletion
 */
const handleBaselineDelete = async () => {
  const data = baselineDataSource.value;
  if (!data) {
    return;
  }

  modal.confirm({
    content: t('functionBaseline.editForm.confirmDeleteBaseline', { name: data.name }),
    async onOk () {
      const id = data.id;
      isLoading.value = true;
      const [error] = await func.deleteBaseline([id]);
      isLoading.value = false;
      if (error) {
        return;
      }

      notification.success(t('functionBaseline.editForm.baselineDeletedSuccess'));
      deleteTabPane([id, id + '_detail']);
      refreshBaselineList();
    }
  });
};

/**
 * Handle form cancellation
 */
const handleFormCancel = () => {
  deleteTabPane([props.data._id]);
};

/**
 * Load baseline data by ID
 * @param id - Baseline ID
 */
const loadBaselineData = async (id: string) => {
  if (isLoading.value) {
    return;
  }

  isLoading.value = true;
  const [error, res] = await func.getBaselineDetail(id);
  isLoading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as BaselineDetail;
  if (!data) {
    return;
  }

  baselineDataSource.value = data;
  setFormDataFromBaseline(data);

  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id });
  }
};

/**
 * Parse JSON value for rich editor
 * @param value - Input value
 * @returns Parsed JSON string
 */
const parseJsonForEditor = (value) => {
  if (!value) {
    return undefined;
  }
  try {
    const result = JSON.parse(value);
    if (typeof result === 'object') {
      return value;
    }
    return JSON.stringify([{ insert: value }]);
  } catch {
    return JSON.stringify([{ insert: value }]);
  }
};

/**
 * Set form data from baseline information
 * @param data - Baseline detail data
 */
const setFormDataFromBaseline = (data: BaselineDetail) => {
  isBaselineFlagVisible.value = false;
  if (!data) {
    currentFormState.value = {
      planId: '',
      description: '',
      name: '',
      caseIds: []
    };
    return;
  }

  const {
    name = '',
    planId,
    description = '',
    caseIds
  } = data;

  currentFormState.value.name = name;
  currentFormState.value.planId = planId;
  currentFormState.value.description = parseJsonForEditor(description);
  currentFormState.value.caseIds = caseIds;
  originalFormState.value = JSON.parse(JSON.stringify(currentFormState.value));
};

/**
 * Load enumeration options
 */
const loadEnumerationOptions = async () => {
  evalWorkloadMethodOptions.value = enumUtils.enumToMessages(EvalWorkloadMethod);
};

/**
 * Load project members
 */
const loadProjectMembers = async () => {
  const [error, res] = await project.getProjectMember(props.projectId);
  if (error) {
    return;
  }

  const data = res?.data || [];
  projectMembers.value = (data || []).map(i => {
    return {
      ...i,
      label: i.fullName,
      value: i.id
    };
  });
};

/**
 * Handle plan ID change
 */
const handlePlanIdChange = () => {
  baselineCaseList.value = [];
};

/**
 * Load baseline case list
 */
const loadBaselineCaseList = async () => {
  const [error, { data }] = await func.getBaselineCaseList(currentBaselineId.value, {
    filters: searchKeywords.value ? [{ value: searchKeywords.value, key: 'caseName', op: SearchCriteria.OpEnum.Match }] : [],
    pageNo: paginationConfig.value.current,
    pageSize: paginationConfig.value.pageSize,
    projectId: props.projectId
  });

  if (error) {
    return;
  }
  baselineCaseList.value = data?.list || [];
  paginationConfig.value.total = +data.total || 0;
};

/**
 * Handle search keyword change with debounce
 */
const handleSearchKeywordChange = debounce(duration.search, () => {
  if (!currentBaselineId.value) {
    return;
  }
  paginationConfig.value.current = 1;
  loadBaselineCaseList();
});

/**
 * Open add baseline case modal
 */
const openAddBaselineCaseModal = () => {
  isSelectCaseModalVisible.value = true;
};

/**
 * Handle add case confirmation
 * @param caseIds - Array of case IDs
 * @param cases - Array of case information
 */
const handleAddCaseConfirm = async (caseIds: string[], cases: BaselineCaseInfo[]) => {
  if (currentBaselineId.value) {
    const [error] = await func.addBaselineCase(currentBaselineId.value, caseIds);
    if (error) {
      return;
    }
    isSelectCaseModalVisible.value = false;
    await loadBaselineCaseList();
  } else {
    isSelectCaseModalVisible.value = false;
    baselineCaseList.value.push(...cases);
  }
};

// Table Configuration
const paginationConfig = ref({
  current: 1,
  pageSize: 10,
  total: 0
});
const baselineCaseList = ref<BaselineCaseInfo[]>([]);
const tableColumns = [
  {
    title: t('functionBaseline.editForm.caseId'),
    dataIndex: 'id'
  },
  {
    title: t('functionBaseline.editForm.caseName'),
    dataIndex: 'name'
  },
  {
    title: t('functionBaseline.editForm.operation'),
    dataIndex: 'action'
  }
];

/**
 * Delete case from baseline
 * @param record - Case record to delete
 */
const deleteCaseFromBaseline = async (record: BaselineCaseInfo) => {
  if (currentBaselineId.value) {
    modal.confirm({
      title: t('functionBaseline.editForm.confirmDeleteCase', { name: record.name }),
      async onOk () {
        const [error] = await func.deleteBaselineCaseById([record.id]);
        if (error) {
          return;
        }
        if (paginationConfig.value.current !== 1 && baselineCaseList.value.length === 1) {
          paginationConfig.value.current -= 1;
        }
        loadBaselineCaseList();
      }
    });
  } else {
    baselineCaseList.value = baselineCaseList.value.filter(i => i.id !== record.id);
  }
};

// Form Validation
const richEditorRef = ref();

/**
 * Validate description field
 * @returns Promise with validation result
 */
const validateDescription = () => {
  if (!currentFormState.value.description) {
    return Promise.resolve();
  }
  if (richEditorRef.value && richEditorRef.value.getLength() > 2000) {
    return Promise.reject(t('functionBaseline.editForm.charactersCannotExceed2000'));
  }
  return Promise.resolve();
};

/**
 * Handle pagination change
 * @param paginationParams - Pagination parameters
 */
const handlePaginationChange = ({ current, pageSize }) => {
  paginationConfig.value.current = current;
  paginationConfig.value.pageSize = pageSize;
  loadBaselineCaseList();
};

/**
 * Check if component is in edit mode
 */
const isEditMode = computed(() => {
  return !!props.data?.id;
});

/**
 * Initialize component data on mount
 */
onMounted(() => {
  loadEnumerationOptions();
  loadProjectMembers();

  watch(() => props.data, async (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }

    currentBaselineId.value = id;

    await loadBaselineData(id);
    await loadBaselineCaseList();
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="isLoading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="handleFormSubmit">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>{{ t('functionBaseline.editForm.save') }}</span>
      </Button>

      <template v-if="isEditMode">
        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="handleBaselineDelete">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>{{ t('functionBaseline.editForm.delete') }}</span>
        </Button>
      </template>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="handleFormCancel">
        <Icon icon="icon-zhongzhi2" class="text-3.5" />
        <span>{{ t('functionBaseline.editForm.cancel') }}</span>
      </Button>
    </div>

    <Form
      ref="formRef"
      :model="currentFormState"
      :labelCol="{ style: { width: '75px' } }"
      class="max-w-242.5"
      size="small"
      layout="horizontal">
      <FormItem
        :label="t('functionBaseline.editForm.name')"
        name="name"
        :rules="{ required: true, message: t('functionBaseline.editForm.pleaseEnterBaselineName') }">
        <Input
          v-model:value="currentFormState.name"
          size="small"
          :maxlength="200"
          :placeholder="t('functionBaseline.editForm.baselineBriefOverview')" />
      </FormItem>
      <FormItem
        :label="t('functionBaseline.editForm.testPlan')"
        name="planId"
        :rules="{ required: true, message: t('functionBaseline.editForm.pleaseSelectTestPlan') }">
        <Select
          v-model:value="currentFormState.planId"
          size="small"
          :disabled="!!currentBaselineId"
          :action="`${TESTER}/func/plan?projectId=${props.projectId}&fullTextSearch=true`"
          :fieldNames="{value: 'id', label: 'name'}"
          :placeholder="t('functionBaseline.editForm.selectTestPlan')"
          @change="handlePlanIdChange" />
      </FormItem>

      <FormItem
        :label="t('functionBaseline.editForm.description')"
        name="description"
        :rules="[{validator: validateDescription}]">
        <RichEditor
          ref="richEditorRef"
          v-model:value="currentFormState.description"
          :placeholder="t('functionBaseline.editForm.baselineBriefOverview2000')"
          :height="100" />
      </FormItem>

      <Tabs
        v-model:activeKey="activeTabKey"
        size="small"
        class="pl-5 baselineEditTab">
        <TabPane
          key="funcCase"
          forceRender
          :tab="t('functionBaseline.editForm.baselineCases')">
          <div class="flex justify-between mb-3">
            <Input
              v-model:value="searchKeywords"
              :disabled="!currentBaselineId"
              :placeholder="t('functionBaseline.editForm.enterQueryName')"
              class="w-50"
              @change="handleSearchKeywordChange" />
            <Button
              :disabled="!currentFormState.planId || baselineDataSource?.established"
              size="small"
              type="primary"
              @click="openAddBaselineCaseModal">
              <Icon icon="icon-jia" class="mr-1" />
              {{ t('functionBaseline.editForm.addBaselineCase') }}
            </Button>
          </div>
          <Table
            :columns="tableColumns"
            :dataSource="baselineCaseList"
            :pagination="currentBaselineId ? paginationConfig : false"
            size="small"
            noDataSize="small"
            @change="handlePaginationChange">
            <template #bodyCell="{record, column}">
              <template v-if="column.dataIndex === 'action'">
                <Button
                  type="text"
                  size="small"
                  @click="deleteCaseFromBaseline(record)">
                  <Icon icon="icon-qingchu" />
                  {{ t('functionBaseline.editForm.delete') }}
                </Button>
              </template>
            </template>
          </Table>
        </TabPane>
      </Tabs>
    </Form>
    <AsyncComponent :visible="selectModalVisible">
      <SelectCaseModal
        v-model:visible="isSelectCaseModalVisible"
        :planId="currentFormState.planId"
        :baselineId="currentBaselineId"
        :projectId="props.projectId"
        @ok="handleAddCaseConfirm" />
    </AsyncComponent>
  </Spin>
</template>

<style scoped>
:deep(.ant-form-item-label>label::after) {
  margin-right: 10px;
}

.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  padding-top: 0;
}
</style>
