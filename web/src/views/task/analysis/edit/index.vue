<script lang="ts" setup>
import { computed, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, Form, FormItem, RadioButton, RadioGroup, Textarea } from 'ant-design-vue';
import { DatePicker, Input, notification, Select, Icon } from '@xcan-angus/vue-ui';
import { AuthObjectType, EnumMessage, enumOptionUtils, enumUtils, GM, TESTER } from '@xcan-angus/infra';
import {
  AnalysisDataSource, AnalysisTaskObject, AnalysisTaskTemplate, AnalysisTaskTemplateDesc, AnalysisTimeRange
} from '@/enums/enums';
import { AnalysisEditState } from '../types';
import { analysis } from '@/api/tester';

import { BasicProps } from '@/types/types';

import SelectEnum from '@/components/enum/SelectEnum.vue';

// Component Props & Configuration
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: () => ({ id: '', fullName: '' }),
  data: undefined
});

const emits = defineEmits<{(e: 'ok')}>();

// Vue Composition API Setup
const { t } = useI18n();
const deleteTabPane = inject('deleteTabPane', (value) => value);

// Form References
const formRef = ref();

/**
 * Data source options for analysis configuration.
 */
const dataSourceOptions = ref<EnumMessage<AnalysisDataSource>[]>([]);

/**
 * Organization type options for assignee-based analysis.
 */
const organizationTypeOptions = computed(() => enumOptionUtils.loadEnumAsOptions(AuthObjectType));

// Enum Options Loading
const templateDescriptionOptions = ref<EnumMessage<AnalysisTaskTemplateDesc>[]>([]);

/**
 * Loads template description options from enum definitions.
 */
const loadTemplateDescriptionOptions = () => {
  templateDescriptionOptions.value = enumUtils.enumToMessages(AnalysisTaskTemplateDesc);
};

const analysisObjectOptions = ref<EnumMessage<AnalysisTaskObject>[]>([]);

/**
 * Loads analysis object options from enum definitions.
 */
const loadAnalysisObjectOptions = () => {
  analysisObjectOptions.value = enumUtils.enumToMessages(AnalysisTaskObject);
};

const timeRangeOptions = ref<{message: string, value:string}[]>([]);

/**
 * Loads time range options from enum definitions.
 */
const loadTimeRangeOptions = () => {
  const enumData = enumUtils.enumToMessages(AnalysisTimeRange);
  timeRangeOptions.value = enumData.map(item => ({ ...item, label: item.message }));
};


/**
 * Load Data source options for analysis configuration.
 */
 const loadAnalysisDataSourceOptions = () => {
  dataSourceOptions.value = enumOptionUtils.loadEnumAsOptions(AnalysisDataSource);
};

/**
 * Main form data state for analysis configuration.
 */
const formData = ref<AnalysisEditState>({
  id: undefined,
  name: '',
  object: AnalysisTaskObject.CURRENT_PROJECT,
  timeRange: AnalysisTimeRange.ALL_TIME,
  resource: 'TASK',
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

const isSaving = ref(false);
const isDescriptionManuallyChanged = ref(false);

/**
 * Loads existing analysis data for editing.
 *
 * @param analysisId - The ID of the analysis to load
 */
const loadExistingAnalysisData = async (analysisId: string) => {
  const [error, { data }] = await analysis.getAnalysisDetail(analysisId);
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
    id: analysisId,
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
    customRange: [startTime, endTime].filter(Boolean) as [string, string]
  };
};

/**
 * Handles manual description changes by the user.
 */
const handleDescriptionChange = () => {
  isDescriptionManuallyChanged.value = true;
};

/**
 * Handles organization type changes.
 */
const handleOrganizationTypeChange = () => {
  formData.value.orgId = undefined;
};

/**
 * Cancels the current edit operation and closes the tab.
 */
const handleCancel = () => {
  const tabId = props.data?.id ? `analysisEdit_${props.data?.id}` : 'analysisEdit';
  deleteTabPane([tabId]);
};

/**
 * Builds the parameters object for API submission.
 *
 * @returns Formatted parameters object for API call
 */
const buildSubmissionParameters = () => {
  const {
    object, timeRange, name, resource, template, description,
    containsUserAnalysis, containsDataDetail, planId, datasource,
    orgType, orgId, customRange, id
  } = formData.value;

  const baseParameters: any = {
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

  // Add object-specific parameters
  if (object === AnalysisTaskObject.CURRENT_PROJECT) {
    baseParameters.containsUserAnalysis = containsUserAnalysis;
  }

  if (object === AnalysisTaskObject.SPRINT) {
    baseParameters.containsUserAnalysis = containsUserAnalysis;
    baseParameters.planId = planId;
  }

  if (object === AnalysisTaskObject.ASSIGNEE_ORG) {
    baseParameters.orgType = orgType;
    baseParameters.orgId = orgId;
  }

  // Add custom time range parameters
  if (timeRange === AnalysisTimeRange.CUSTOM_TIME && customRange) {
    baseParameters.startTime = customRange[0];
    baseParameters.endTime = customRange[1];
  }
  return baseParameters;
};

/**
 * Saves the analysis configuration.
 * <p>
 * Validates the form, builds parameters, and submits to the appropriate API endpoint.
 * </p>
 */
const handleSave = async () => {
  formRef.value.validate().then(async () => {
    const submissionParams = buildSubmissionParameters();
    isSaving.value = true;

    const [error] = await (!submissionParams.id
      ? analysis.addAnalysis({ ...submissionParams })
      : analysis.updateAnalysis({ ...submissionParams }));

    isSaving.value = false;
    if (error) {
      return;
    }

    // Show success notification
    if (!submissionParams.id) {
      notification.success(t('actions.tips.addSuccess'));
    } else {
      notification.success(t('actions.tips.editSuccess'));
    }

    emits('ok');
    handleCancel();
  });
};

// Lifecycle Hooks
onMounted(async () => {
  // Load all enum options
  loadTemplateDescriptionOptions();
  loadAnalysisObjectOptions();
  loadTimeRangeOptions();
  loadAnalysisDataSourceOptions();

  // Initialize form data if editing existing analysis
  if (props.data) {
    if (props.data.template) {
      formData.value.template = props.data.template as AnalysisTaskTemplate;
    }
    if (props.data.id) {
      await loadExistingAnalysisData(props.data.id);
    }
  }

  // Watch for template changes to auto-update description
  watch(() => formData.value.template, () => {
    // Only set default description from template when creating (no id)
    if (!props.data?.id && !formData.value.id && !isDescriptionManuallyChanged.value) {
      formData.value.description = templateDescriptionOptions.value
        .find(item => item.value === formData.value.template as any)?.message || '';
    }
  }, {
    immediate: true
  });
});

</script>
<template>
  <div class="p-5">
    <div class="mb-4 flex space-x-2">
      <Button
        type="primary"
        size="small"
        :loading="isSaving"
        @click="handleSave">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5 mr-1" />
        {{ t('actions.save') }}
      </Button>
      <Button size="small" @click="handleCancel">
        <Icon icon="icon-zhongzhi2" class="text-3.5 mr-1" />
        {{ t('common.cancel') }}
      </Button>
    </div>

    <Form
      ref="formRef"
      :colon="false"
      :labelCol="{style: {width: '100px'}}"
      :model="formData"
      class="w-200 mt-5">
      <FormItem
        name="name"
        :label="t('common.name')"
        class="input-item"
        required>
        <Input
          v-model:value="formData.name"
          :maxlength="100"
          :placeholder="t('taskAnalysis.placeholder.inputAnalysisName')" />
      </FormItem>

      <FormItem
        name="template"
        :label="t('taskAnalysis.form.template')"
        class="input-item"
        required>
        <SelectEnum
          v-model:value="formData.template"
          :lazy="false"
          :disabled="!!props.data?.id"
          defaultActiveFirstOption
          enumKey="AnalysisTaskTemplate">
        </SelectEnum>
      </FormItem>

      <FormItem
        name="description"
        :label="t('common.description')">
        <Textarea
          v-model:value="formData.description"
          :maxlength="200"
          :placeholder="t('taskAnalysis.placeholder.inputAnalysisDesc')"
          @change="handleDescriptionChange" />
      </FormItem>

      <FormItem
        name="object"
        :label="t('taskAnalysis.form.object')"
        class="input-item"
        required>
        <RadioGroup
          v-model:value="formData.object"
          buttonStyle="solid"
          size="small">
          <RadioButton
            v-for="item in analysisObjectOptions"
            :value="item.value">
            {{ item.message }}
          </RadioButton>
        </RadioGroup>
      </FormItem>

      <template v-if="formData.object === AnalysisTaskObject.SPRINT">
        <FormItem
          name="planId"
          :label="t('taskAnalysis.form.selectSprint')"
          required
          class="ml-16 input-item">
          <Select
            v-model:value="formData.planId"
            :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
            :fieldNames="{ value: 'id', label: 'name' }"
            :lazy="false"
            class="!w-50"
            defaultActiveFirstOption
            showSearch
            internal
            :placeholder="t('taskAnalysis.placeholder.selectSprint')">
            <template #option="record">
              <div class="flex items-center" :title="record.name">
                <Icon icon="icon-jihua" class="mr-1 text-4" />
                <div style="max-width: 220px;" class="truncate">{{ record.name }}</div>
              </div>
            </template>
          </Select>
        </FormItem>
      </template>

      <template v-if="formData.object === AnalysisTaskObject.ASSIGNEE_ORG">
        <FormItem
          name="orgId"
          :label="t('taskAnalysis.form.selectOrg')"
          required
          class="ml-16 input-item">
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
            :placeholder="t('organization.placeholders.selectUser')"
            :action="`${GM}/user?fullTextSearch=true`"
            :fieldNames="{ label: 'fullName', value: 'id' }">
          </Select>
          <Select
            v-show="formData.orgType === AuthObjectType.DEPT"
            v-model:value="formData.orgId"
            class="!w-50"
            :placeholder="t('organization.placeholders.selectDept')"
            :showSearch="true"
            :action="`${GM}/dept?fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }">
          </Select>
          <Select
            v-show="formData.orgType === AuthObjectType.GROUP"
            v-model:value="formData.orgId"
            class="!w-50"
            :placeholder="t('organization.placeholders.selectGroup')"
            :showSearch="true"
            :action="`${GM}/group?fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }">
          </Select>
        </FormItem>
      </template>
      <FormItem label=" ">
        <Checkbox
          v-show="formData.object !== AnalysisTaskObject.ASSIGNEE_ORG || formData.orgType !== AuthObjectType.USER"
          v-model:checked="formData.containsUserAnalysis">
          {{ t('taskAnalysis.form.containsUserAnalysis') }}
        </Checkbox>
        <Checkbox v-model:checked="formData.containsDataDetail">
          {{ t('taskAnalysis.form.containsDataDetail') }}
        </Checkbox>
      </FormItem>

      <FormItem
        name="timeRange"
        :label="t('taskAnalysis.form.timeRange')"
        required>
        <RadioGroup
          v-model:value="formData.timeRange"
          :options="timeRangeOptions" />
        <DatePicker
          v-if="formData.timeRange === AnalysisTimeRange.CUSTOM_TIME"
          v-model:value="formData.customRange"
          type="date-range"
          class="mt-2" />
      </FormItem>

      <FormItem
        :label="t('taskAnalysis.form.dataSource')"
        name="datasource"
        required>
        <RadioGroup
          v-model:value="formData.datasource"
          :options="dataSourceOptions" />
      </FormItem>
    </Form>
  </div>
</template>
<style scoped>
.ant-form :deep(.ant-form-item) label {
  height: 28px;
  font-weight: 500;
}
</style>
