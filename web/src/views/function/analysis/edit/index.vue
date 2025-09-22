<script lang="ts" setup>
import { computed, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, Form, FormItem, RadioButton, RadioGroup, Textarea } from 'ant-design-vue';
import { DatePicker, Icon, Input, notification, Select } from '@xcan-angus/vue-ui';
import { AuthObjectType, EnumMessage, enumOptionUtils, enumUtils, GM, TESTER } from '@xcan-angus/infra';
import {
  AnalysisCaseObject,
  AnalysisCaseTemplateDesc,
  AnalysisDataSource,
  AnalysisTaskObject,
  AnalysisTaskTemplate,
  AnalysisTimeRange
} from '@/enums/enums';
import { analysis } from '@/api/tester';
import { BasicProps } from '@/types/types';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { EditAnalysisState } from '@/views/function/analysis/types';

// Component setup
const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  data: undefined
});

const emits = defineEmits<{(e: 'ok')}>();

// Injected dependencies
const deleteTabPane = inject('deleteTabPane', (value) => value);
const formRef = ref();

/**
 * Organization type options for assignee-based analysis.
 */
const organizationTypeOptions = computed(() => enumOptionUtils.loadEnumAsOptions(AuthObjectType));

// Reactive state
const dataSourceOptions = ref<EnumMessage<AnalysisDataSource>[]>([]);
const templateDescriptionOptions = ref<EnumMessage<AnalysisCaseTemplateDesc>[]>([]);
const analysisCaseObjectOptions = ref<EnumMessage<AnalysisCaseObject>[]>([]);
const analysisTimeRangeOptions = ref<{value: string, message: string, label: string}[]>([]);
const isDescriptionManuallyChanged = ref(false);
const isSaving = ref(false);

const formData = ref<EditAnalysisState>({
  id: undefined,
  name: '',
  object: AnalysisTaskObject.CURRENT_PROJECT,
  timeRange: AnalysisTimeRange.ALL_TIME,
  resource: 'CASE',
  template: AnalysisTaskTemplate.PROGRESS,
  description: '',
  containsUserAnalysis: true,
  containsDataDetail: true,
  planId: '',
  datasource: AnalysisDataSource.REAL_TIME_DATA,
  orgType: AuthObjectType.DEPT,
  orgId: '',
  customRange: ['', '']
});

/**
 * Load template description options from enum.
 */
const loadTemplateDescriptionOptions = () => {
  templateDescriptionOptions.value = enumUtils.enumToMessages(AnalysisCaseTemplateDesc);
};

/**
 * Load analysis case object options from enum.
 */
const loadAnalysisCaseObjectOptions = () => {
  analysisCaseObjectOptions.value = enumUtils.enumToMessages(AnalysisCaseObject);
};

/**
 * Load analysis time range options from enum.
 */
const loadAnalysisTimeRangeOptions = () => {
  const data = enumUtils.enumToMessages(AnalysisTimeRange);
  analysisTimeRangeOptions.value = (data || []).map(item => ({ ...item, label: item.message }));
};

/**
 * Load Data source options for analysis configuration.
 */
const loadAnalysisDataSourceOptions = () => {
  dataSourceOptions.value = enumOptionUtils.loadEnumAsOptions(AnalysisDataSource);
};

/**
 * Load analysis detail data for editing existing analysis.
 * @param id - Analysis ID to load
 */
const loadAnalysisDetail = async (id) => {
  const [error, { data }] = await analysis.getAnalysisDetail(id);
  if (error) {
    return;
  }
  const {
    object, timeRange, name, resource, template, description,
    containsUserAnalysis, containsDataDetail, planId, datasource,
    orgType, orgId, startTime, endTime
  } = data;

  formData.value = {
    ...formData.value,
    id,
    object,
    name,
    resource,
    template,
    description,
    containsUserAnalysis,
    containsDataDetail,
    planId,
    orgType,
    orgId,
    timeRange: timeRange.value,
    datasource: datasource.value,
    customRange: [startTime, endTime].filter(Boolean)
  };
};

/**
 * Mark description as manually changed by user.
 */
const handleDescriptionChange = () => {
  isDescriptionManuallyChanged.value = true;
};

/**
 * Reset organization ID when organization type changes.
 */
const handleOrganizationTypeChange = () => {
  formData.value.orgId = undefined;
};

/**
 * Close the current tab pane.
 */
const handleCancel = () => {
  const tabId = props.data?.id ? `analysisEdit_${props.data?.id}` : 'analysisEdit';
  deleteTabPane([tabId]);
};

/**
 * Build parameters object for API calls based on form data.
 * @returns Formatted parameters object
 */
const buildFormParameters = () => {
  const {
    object, timeRange, name, resource, template, description,
    containsUserAnalysis, containsDataDetail, planId, datasource,
    orgType, orgId, customRange, id
  } = formData.value;

  const parameters = {
    name,
    object,
    timeRange,
    resource,
    template,
    description,
    datasource,
    containsDataDetail,
    projectId: props.projectId,
    id
  };

  // Add conditional parameters based on analysis object type
  if (object === AnalysisCaseObject.CURRENT_PROJECT) {
    parameters.containsUserAnalysis = containsUserAnalysis;
  }
  if (object === AnalysisCaseObject.PLAN) {
    parameters.containsUserAnalysis = containsUserAnalysis;
    parameters.planId = planId;
  }
  if (object === AnalysisCaseObject.TESTER_ORG) {
    parameters.orgType = orgType;
    parameters.orgId = orgId;
  }
  if (timeRange === AnalysisTimeRange.CUSTOM_TIME) {
    parameters.startTime = customRange[0];
    parameters.endTime = customRange[1];
  }
  return parameters;
};

/**
 * Save analysis data by creating new or updating existing analysis.
 */
const handleSave = async () => {
  formRef.value.validate().then(async () => {
    const parameters = buildFormParameters();
    isSaving.value = true;

    const [error] = await (!parameters.id
      ? analysis.addAnalysis({ ...parameters })
      : analysis.updateAnalysis({ ...parameters }));

    isSaving.value = false;
    if (error) {
      return;
    }

    // Show success notification
    if (!parameters.id) {
      notification.success(t('functionAnalysis.editForm.addAnalysisSuccess'));
    } else {
      notification.success(t('functionAnalysis.editForm.updateAnalysisSuccess'));
    }

    emits('ok');
    handleCancel();
  });
};

// LIFECYCLE HOOKS
onMounted(async () => {
  // Load all enum options
  await loadTemplateDescriptionOptions();
  loadAnalysisCaseObjectOptions();
  loadAnalysisTimeRangeOptions();
  loadAnalysisDataSourceOptions();

  // Initialize form data if editing existing analysis
  if (props.data) {
    if (props.data.template) {
      formData.value.template = props.data.template;
    }
    if (props.data.id) {
      await loadAnalysisDetail(props.data.id);
    }
  }

  // Watch template changes to auto-update description
  watch(() => formData.value.template, () => {
    if (!isDescriptionManuallyChanged.value) {
      formData.value.description = templateDescriptionOptions.value
        .find(item => item.value === formData.value.template)?.message;
    }
  }, {
    immediate: true
  });
});
</script>
<template>
  <div class="p-5">
    <div class="mb-2 flex space-x-2">
      <Button
        type="primary"
        size="small"
        :loading="isSaving"
        @click="handleSave">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5 mr-1" />
        {{ t('functionAnalysis.editForm.save') }}
      </Button>

      <Button size="small" @click="handleCancel">
        <Icon icon="icon-zhongzhi2" class="text-3.5 mr-1" />
        {{ t('functionAnalysis.editForm.cancel') }}
      </Button>
    </div>

    <Form
      ref="formRef"
      :colon="false"
      :labelCol="{style: {'min-width': '90px'}}"
      :model="formData"
      class="w-200 mt-5">
      <FormItem
        name="template"
        :label="t('functionAnalysis.editForm.analysisTemplate')"
        required>
        <SelectEnum
          v-model:value="formData.template"
          :lazy="false"
          :disabled="!!props.data?.id"
          defaultActiveFirstOption
          enumKey="AnalysisCaseTemplate">
        </SelectEnum>
      </FormItem>

      <FormItem
        name="name"
        :label="t('functionAnalysis.editForm.analysisName')"
        required>
        <Input
          v-model:value="formData.name"
          :maxlength="100"
          :placeholder="t('functionAnalysis.editForm.inputAnalysisName')" />
      </FormItem>

      <FormItem
        name="description"
        :label="t('functionAnalysis.editForm.analysisDescription')">
        <Textarea
          v-model:value="formData.description"
          :maxlength="200"
          :placeholder="t('functionAnalysis.editForm.inputAnalysisDescription')"
          @change="handleDescriptionChange" />
      </FormItem>

      <FormItem
        name="object"
        :label="t('functionAnalysis.editForm.analysisObject')"
        required>
        <RadioGroup
          v-model:value="formData.object"
          buttonStyle="solid"
          size="small">
          <RadioButton
            v-for="item in analysisCaseObjectOptions"
            :value="item.value">
            {{ item.message }}
          </RadioButton>
        </RadioGroup>
      </FormItem>

      <template v-if="formData.object === AnalysisCaseObject.PLAN">
        <FormItem
          name="planId"
          :label="t('functionAnalysis.editForm.selectPlan')"
          required
          class="ml-16">
          <Select
            v-model:value="formData.planId"
            :action="`${TESTER}/func/plan?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ value: 'id', label: 'name' }"
            :lazy="false"
            class="!w-50"
            defaultActiveFirstOption
            showSearch
            internal
            :placeholder="t('functionAnalysis.editForm.selectOrSearchPlan')">
            <template #option="record">
              <div class="flex items-center" :title="record.name">
                <Icon icon="icon-jihua" class="mr-1 text-4" />
                <div style="max-width: 220px;" class="truncate">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </FormItem>
      </template>

      <template v-if="formData.object === AnalysisCaseObject.TESTER_ORG">
        <FormItem
          name="orgId"
          :label="t('functionAnalysis.editForm.selectOrganization')"
          required
          class="ml-16">
          <Select
            v-model:value="formData.orgType"
            :options="organizationTypeOptions"
            class="!w-30 mr-2"
            @change="handleOrganizationTypeChange" />

          <Select
            v-show="formData.orgType === AuthObjectType.USER"
            v-model:value="formData.orgId"
            class="!w-50"
            :showSearch="true"
            :placeholder="t('functionAnalysis.editForm.selectUser')"
            :action="`${GM}/user?fullTextSearch=true`"
            :fieldNames="{ label: 'fullName', value: 'id' }">
          </Select>

          <Select
            v-show="formData.orgType === AuthObjectType.DEPT"
            v-model:value="formData.orgId"
            class="!w-50"
            :placeholder="t('functionAnalysis.editForm.selectDepartment')"
            :showSearch="true"
            :action="`${GM}/dept?fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }">
          </Select>

          <Select
            v-show="formData.orgType === AuthObjectType.GROUP"
            v-model:value="formData.orgId"
            class="!w-50"
            :placeholder="t('functionAnalysis.editForm.selectGroup')"
            :showSearch="true"
            :action="`${GM}/group?fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }">
          </Select>
        </FormItem>
      </template>

      <FormItem label=" ">
        <Checkbox
          v-show="formData.object !== AnalysisCaseObject.TESTER_ORG || formData.orgType !== AuthObjectType.USER"
          v-model:checked="formData.containsUserAnalysis">
          {{ t('functionAnalysis.editForm.includeAssigneeAnalysis') }}
        </Checkbox>

        <Checkbox v-model:checked="formData.containsDataDetail">
          {{ t('functionAnalysis.editForm.includeDataDetail') }}
        </Checkbox>
      </FormItem>

      <FormItem
        name="timeRange"
        :label="t('functionAnalysis.editForm.analysisPeriod')"
        required>
        <RadioGroup v-model:value="formData.timeRange" :options="analysisTimeRangeOptions"></RadioGroup>
        <DatePicker
          v-if="formData.timeRange === AnalysisTimeRange.CUSTOM_TIME"
          v-model:value="formData.customRange"
          type="date-range"
          class="mt-2" />
      </FormItem>

      <FormItem
        :label="t('functionAnalysis.editForm.dataDisplay')"
        name="datasource"
        required>
        <RadioGroup v-model:value="formData.datasource" :options="dataSourceOptions" />
      </FormItem>
    </Form>
  </div>
</template>
<style scoped>
.ant-form :deep(.ant-form-item) label {
  height: 28px;
}
</style>
