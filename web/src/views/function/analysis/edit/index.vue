<script lang="ts" setup>
import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, Form, FormItem, RadioButton, RadioGroup, Textarea } from 'ant-design-vue';
import { DatePicker, Input, notification, Select } from '@xcan-angus/vue-ui';
import {TESTER, GM, enumUtils, EnumMessage} from '@xcan-angus/infra';
import { AnalysisCaseTemplateDesc, AnalysisCaseObject, AnalysisTimeRange } from '@/enums/enums';
import { analysis } from '@/api/tester';
import SelectEnum from '@/components/SelectEnum/index.vue'

const { t } = useI18n();
interface Props {
  projectId: string;
  userInfo: {id: string};
  data?: Record<string, string>;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: () => ({ id: '' }),
  data: undefined
});

const emits = defineEmits<{(e: 'ok')}>();

const deleteTabPane = inject('deleteTabPane', (value) => value);
const formRef = ref();
const showContentOpt = [
  {
    value: 'REAL_TIME_DATA',
    label: t('functionAnalysis.editForm.realTimeData')
  },
  {
    value: 'SNAPSHOT_DATA',
    label: t('functionAnalysis.editForm.snapshotData')
  }
];

const orgOpt = [
  {
    value: 'DEPT',
    label: t('functionAnalysis.editForm.department')
  },
  {
    value: 'GROUP',
    label: t('functionAnalysis.editForm.group')
  },
  {
    value: 'USER',
    label: t('functionAnalysis.editForm.user')
  }
];

const templateDescOpt = ref<EnumMessage<AnalysisCaseTemplateDesc>[]>([]);
const loadDescOpt = () => {
  templateDescOpt.value = enumUtils.enumToMessages(AnalysisCaseTemplateDesc);
};

const analysisCaseObjectOpt = ref<EnumMessage<AnalysisCaseObject>[]>([]);
const loadAnalysisCaseObject = () => {
  analysisCaseObjectOpt.value = enumUtils.enumToMessages(AnalysisCaseObject);
};

const analysisTimeRangeOpt = ref<{value: string, message: string, label: string}[]>([]);
const loadAnalysisTimeRange = () => {
  const data = enumUtils.enumToMessages(AnalysisTimeRange);
  analysisTimeRangeOpt.value = (data || []).map(item => ({ ...item, label: item.message }));
};

const formData = ref({
  object: 'CURRENT_PROJECT',
  timeRange: 'ALL_TIME',
  resource: 'CASE',
  template: '',
  description: '',
  containsUserAnalysis: true,
  containsDataDetail: true,
  planId: '',
  datasource: 'REAL_TIME_DATA',
  orgType: 'DEPT',
  orgId: '',
  customRange: [],
  id: undefined,
  name: undefined
});

const saving = ref(false);

const loadAnalysisInfo = async (id) => {
  const [error, { data }] = await analysis.getAnalysisDetail(id);
  if (error) {
    return;
  }
  const { object, timeRange, name, resource, template, description, containsUserAnalysis, containsDataDetail, planId, datasource, orgType, orgId, startTime, endTime } = data;

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

const descriptionChanged = ref(false);
const descChanged = () => {
  descriptionChanged.value = true;
};

const handleChangeOrgType = () => {
  formData.value.orgId = undefined;
};

const cancel = () => {
  const tabId = props.data?.id ? `analysisEdit_${props.data?.id}` : 'analysisEdit';
  deleteTabPane([tabId]);
};

const getParams = () => {
  const { object, timeRange, name, resource, template, description, containsUserAnalysis, containsDataDetail, planId, datasource, orgType, orgId, customRange, id } = formData.value;
  const res = {
    name, object, timeRange, resource, template, description, datasource, containsDataDetail, projectId: props.projectId, id
  };
  if (object === 'CURRENT_PROJECT') {
    res.containsUserAnalysis = containsUserAnalysis;
  }
  if (object === 'PLAN') {
    res.containsUserAnalysis = containsUserAnalysis;
    res.planId = planId;
  }
  if (object === 'TESTER_ORG') {
    res.orgType = orgType;
    res.orgId = orgId;
  }
  if (timeRange === 'CUSTOM_TIME') {
    res.startTime = customRange[0];
    res.endTime = customRange[1];
  }
  return res;
};

const save = async () => {
  formRef.value.validate().then(async () => {
    const params = getParams();
    saving.value = true;
    const [error] = await (!params.id
      ? analysis.addAnalysis({ ...params })
      : analysis.updateAnalysis({
        ...params
      }));
    saving.value = false;
    if (error) {
      return;
    }
    if (!params.id) {
      notification.success(t('functionAnalysis.editForm.addAnalysisSuccess'));
    } else {
      notification.success(t('functionAnalysis.editForm.updateAnalysisSuccess'));
    }

    emits('ok');
    cancel();
  });
};

onMounted(async () => {
  await loadDescOpt();
  loadAnalysisCaseObject();
  loadAnalysisTimeRange();

  if (props.data) {
    if (props.data.template) {
      formData.value.template = props.data.template;
    }
    if (props.data.id) {
      loadAnalysisInfo(props.data.id);
    }
  }
  watch(() => formData.value.template, () => {
    if (!descriptionChanged.value) {
      formData.value.description = templateDescOpt.value.find(item => item.value === formData.value.template)?.message;
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
        :loading="saving"
        @click="save">
        {{ t('functionAnalysis.editForm.save') }}
      </Button>
      <Button size="small" @click="cancel">
        {{ t('functionAnalysis.editForm.cancel') }}
      </Button>
    </div>

    <Form
      ref="formRef"
      :colon="false"
      :labelCol="{style: {width: '90px'}}"
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

      <FormItem name="description" :label="t('functionAnalysis.editForm.analysisDescription')">
        <Textarea
          v-model:value="formData.description"
          :maxlength="200"
          :placeholder="t('functionAnalysis.editForm.inputAnalysisDescription')"
          @change="descChanged" />
      </FormItem>

      <FormItem
        name="object"
        :label="t('functionAnalysis.editForm.analysisObject')"
        required>
        <RadioGroup
          v-model:value="formData.object"
          buttonStyle="solid"
          size="small">
          <RadioButton v-for="item in analysisCaseObjectOpt" :value="item.value">{{ item.message }}</RadioButton>
        </RadioGroup>
      </FormItem>

      <template v-if="formData.object === 'PLAN'">
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

      <template v-if="formData.object === 'TESTER_ORG'">
        <FormItem
          name="orgId"
          :label="t('functionAnalysis.editForm.selectOrganization')"
          required
          class="ml-16">
          <Select
            v-model:value="formData.orgType"
            :options="orgOpt"
            class="!w-30 mr-2"
            @change="handleChangeOrgType" />
          <!-- <span>{{ formData.orgType === 'USER' ? '选择用户' : formData.orgType === 'GROUP' ? '选择组' :  formData.orgType === 'DEPT' ? '选择部门' : ''}}</span> -->
          <Select
            v-show="formData.orgType === 'USER'"
            v-model:value="formData.orgId"
            class="!w-50"
            :showSearch="true"
            :placeholder="t('functionAnalysis.editForm.selectUser')"
            :action="`${GM}/user?fullTextSearch=true`"
            :fieldNames="{ label: 'fullName', value: 'id' }">
          </Select>
          <Select
            v-show="formData.orgType === 'DEPT'"
            v-model:value="formData.orgId"
            class="!w-50"
            :placeholder="t('functionAnalysis.editForm.selectDepartment')"
            :showSearch="true"
            :action="`${GM}/dept?fullTextSearch=true`"
            :fieldNames="{ label: 'name', value: 'id' }">
          </Select>
          <Select
            v-show="formData.orgType === 'GROUP'"
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
        <Checkbox v-show="formData.object !== 'TESTER_ORG' || formData.orgType !== 'USER'" v-model:checked="formData.containsUserAnalysis">{{ t('functionAnalysis.editForm.includeAssigneeAnalysis') }}</Checkbox>
        <Checkbox v-model:checked="formData.containsDataDetail">{{ t('functionAnalysis.editForm.includeDataDetail') }}</Checkbox>
      </FormItem>

      <FormItem
        name="timeRange"
        :label="t('functionAnalysis.editForm.analysisPeriod')"
        required>
        <RadioGroup v-model:value="formData.timeRange" :options="analysisTimeRangeOpt"></RadioGroup>
        <DatePicker
          v-if="formData.timeRange === 'CUSTOM_TIME'"
          v-model:value="formData.customRange"
          type="date-range"
          class="mt-2" />
      </FormItem>

      <FormItem
        :label="t('functionAnalysis.editForm.dataDisplay')"
        name="datasource"
        required>
        <RadioGroup v-model:value="formData.datasource" :options="showContentOpt" />
      </FormItem>
    </Form>
  </div>
</template>
<style scoped>
.ant-form :deep(.ant-form-item) label {
  height: 28px;
}
</style>
