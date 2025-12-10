<script lang="ts" setup>
import { onMounted, ref, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Hints, IconRequired, Select, Icon, DatePicker, notification } from '@xcan-angus/vue-ui';
import { RadioGroup, Tree, Radio } from 'ant-design-vue';
import { enumUtils, TESTER } from '@xcan-angus/infra';
import { contentTreeData } from './TestEvaluationContentConfig';
import { CombinedTargetType, EvaluationScope, EvaluationPurpose } from '@/enums/enums';
import { modules } from '@/api/tester';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { travelTreeData } from '@/utils/utils';
import dayjs from 'dayjs';

const { t } = useI18n();

// Component props definition
interface Props {
  projectId: string;
  contentSetting: {
    targetId: string;
  };
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

// Enum options - Load from enumUtils to get internationalized messages
const scopeOptions = computed(() => {
  return enumUtils.enumToMessages(EvaluationScope).map(item => ({
    label: item.message,
    value: item.value
  }));
});

const purposeOptions = computed(() => {
  return enumUtils.enumToMessages(EvaluationPurpose).map(item => ({
    label: item.message,
    value: item.value
  }));
});
const defaultStartDate = dayjs().format(DATE_TIME_FORMAT);
const defaultDeadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);

// Reactive variable for execution ID
const evaluationId = ref<string|undefined>(props.projectId);
const evaluationScope = ref(EvaluationScope.PROJECT);
const purposes = ref<EvaluationPurpose[]>([]);
const date = ref<[string, string]>([defaultStartDate, defaultDeadlineDate]);


// Checked keys for tree component
const checked = ref<string[]>([]);
contentTreeData.forEach(item => {
  checked.value.push(item.key);
  if (item.children) {
    checked.value.push(...item.children.map(i => i.key));
  }
});

/**
 * Check if a purpose is selected
 */
 const isPurposeSelected = (purpose: EvaluationPurpose): boolean => {
  return purposes.value.includes(purpose) || false;
};

/**
 * Toggle purpose selection
 */
 const togglePurpose = (purpose: string) => {

  const currentPurposes = purposes.value || [];
  const purposeValue = purpose as EvaluationPurpose;
  const index = currentPurposes.indexOf(purposeValue);
  if (index > -1) {
    // Remove if already selected
    purposes.value = currentPurposes.filter(p => p !== purposeValue);
  } else {
    // Add if not selected
    purposes.value = [...currentPurposes, purposeValue];
  }
};

const moduleTreeData = ref<{name: string; id: string;}[]>([]);
const loadModuleTree = async () => {
  if (!props.projectId) {
    return;
  }
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  moduleTreeData.value = [{ name: t('common.noModule'), id: '-1' }, ...travelTreeData(data || [])];
};


/**
 * Handle module selection change
 */
 const handleModuleChange = (selectKeys: any[]) => {
  if (!selectKeys.length) {
    evaluationId.value = undefined;
    return;
  }
  const moduleId = selectKeys[0];
  if (moduleId === '-1' || moduleId === undefined) {
    evaluationId.value = undefined;
  } else {
    evaluationId.value = moduleId;
  }
};


/**
 * Lifecycle hook - Initialize component
 * Watch for content setting changes and update execution ID
 */
onMounted(async() => {
  await loadModuleTree();
  watch(() => props.contentSetting, newValue => {
    if (newValue?.targetId) {
        evaluationId.value = newValue.targetId;
        purposes.value = (newValue?.evaluationPurposes || []).filter(Boolean).map(i=> i.value);
        evaluationScope.value = newValue?.targetType?.value;
        if (newValue.createdDateEnd && newValue.createdDateStart) {
          date.value = [newValue.createdDateStart, newValue.createdDateEnd];  
        } else {
          date.value = undefined;
        }
    }
  }, {
    immediate: true
  });
});

const handleScopeChange = (value: any) => {
  evaluationId.value = undefined;
  if (value === EvaluationScope.PROJECT) {
    evaluationId.value = props.projectId;
  }
};

// Validation state
const valid = ref(false);

/**
 * Validate if execution ID is selected
 * @returns Boolean indicating if validation passes
 */
const validate = () => {
  valid.value = true;
  if (!evaluationId.value) {
    notification.warning(t('reportAdd.execFuncContent.selectObject'));
    valid.value = false;
    return false;
  }
  if (purposes.value.length === 0) {
    notification.warning(t('reportAdd.execFuncContent.selectPurpose'));
    valid.value = false;
    return false;
  }
  return !!evaluationId.value && purposes.value.length > 0;
};

/**
 * Get execution data for report
 * @returns Object containing execution target ID and type
 */
const getData = () => {
  valid.value = false;
  return {
    targetId: evaluationId.value,
    targetType: evaluationScope.value,
    evaluationPurposes: purposes.value,
    createdDateStart: date.value[0] || undefined,
    createdDateEnd: date.value[1] || undefined
  };
};

// Expose methods to parent component
defineExpose({
  validate,
  getData
});
</script>
<template>
  <div class="flex items-center space-x-1">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.execFuncContent.filter') }}</span>
  </div>
  <div class="mt-2 pl-2 space-y-2">

    <div class="flex flex-1 items-center space-x-2">
      <div class="w-16 text-right"><IconRequired class="mr-1" />测评对象</div>
      <Colon />
      <RadioGroup
        v-model:value="evaluationScope"
        :disabled="!props.projectId || props.disabled"
        class="mt-0.5"
        @change="handleScopeChange">
        <Radio
          v-for="item in scopeOptions"
          :key="item.value"
          :value="item.value">
          {{ item.label }}
        </Radio>
      </RadioGroup>
    </div>

    <div v-if="evaluationScope === EvaluationScope.FUNC_PLAN" class="inline-flex flex-1 items-center space-x-2">
      <div class="w-16 text-right"><IconRequired class="mr-1" />计划</div>
      <Colon />
      <Select
        v-model:value="evaluationId"
        :disabled="!props.projectId || props.disabled"
        size="small"
        class="w-50"
        :lazy="false"
        showSearch
        :defaultActiveFirstOption="true"
        :action="`${TESTER}/func/plan?projectId=${props.projectId}&fullTextSearch=true`"
        :fieldNames="{ value: 'id', label: 'name' }"
        :placeholder="t('common.placeholders.selectOrSearchPlan')">
        <template #option="item">
          <div class="flex items-center" :title="item.name">
            <Icon icon="icon-jihua" class="mr-1 text-3.5" />
            <div class="truncate" style="max-width: 300px;">{{ item.name }}</div>
          </div>
        </template>
      </Select>
    </div>

    <div v-if="evaluationScope === EvaluationScope.MODULE" class="inline-flex flex-1 items-start space-x-2">
      <div class="w-16 text-right"><IconRequired class="mr-1" />模块</div>
      <Colon />
      <div class="border border-gray-300 rounded p-2 min-w-80" style="max-height: 300px; overflow-y: auto;">
        <div
          :class="{'bg-blue-50': evaluationId === undefined}"
          class="flex items-center space-x-2 h-9 leading-9 pl-2 cursor-pointer hover:bg-gray-100 rounded"
          @click="handleModuleChange([undefined])">
          <Icon icon="icon-liebiaoshitu" class="text-3.5" />
          <span class="flex-1">{{ t('common.all') }}</span>
        </div>
        <Tree
          v-if="moduleTreeData.length > 0"
          :treeData="moduleTreeData"
          :disabled="!props.projectId || props.disabled"
          :selectedKeys="evaluationId ? [evaluationId] : []"
          blockNode
          defaultExpandAll
          :fieldNames="{
            children: 'children',
            title: 'name',
            key: 'id'
          }"
          @select="handleModuleChange">
          <template #title="{ name, id }">
            <div class="flex items-center space-x-2">
              <Icon v-if="id !== '-1'" icon="icon-mokuai" />
              <Icon v-else icon="icon-liebiaoshitu" class="text-3.5" />
              <span class="flex-1">{{ name }}</span>
            </div>
          </template>
        </Tree>
      </div>
    </div>

    <div class="flex flex-1 items-start space-x-2">
      <div class="w-16 text-right"><IconRequired class="mr-1" />测评目的</div>
      <Colon />
      <div class="mt-0.5">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          <div
            v-for="item in purposeOptions"
            :key="item.value"
            :class="[
              'p-4 rounded-lg border-2 cursor-pointer transition-all duration-200',
              'hover:shadow-md active:scale-98',
              isPurposeSelected(item.value as EvaluationPurpose)
                ? 'border-blue-500 bg-blue-50 shadow-sm'
                : 'border-gray-200 bg-white hover:border-blue-300 hover:bg-gray-50'
            ]"
            @click="togglePurpose(item.value)">
            <div class="flex items-center">
              <div
                :class="[
                  'w-5 h-5 rounded border-2 mr-3 flex items-center justify-center flex-shrink-0 transition-all',
                  isPurposeSelected(item.value as EvaluationPurpose)
                    ? 'border-blue-500 bg-blue-500 shadow-sm'
                    : 'border-gray-300 bg-white'
                ]">
                <Icon
                  v-if="isPurposeSelected(item.value as EvaluationPurpose)"
                  icon="icon-duigou"
                  class="text-white text-3" />
              </div>
              <span
                :class="[
                  'text-3.5 leading-5 flex-1',
                  isPurposeSelected(item.value as EvaluationPurpose)
                    ? 'text-blue-700 font-semibold'
                    : 'text-gray-700'
                ]">
                {{ item.label }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="flex flex-1 items-center space-x-2">
      <div class="w-16 text-right">{{ t('evaluation.columns.resourceCreationTime') }}</div>
      <Colon />
      <DatePicker
        v-model:value="date"
        format="YYYY-MM-DD HH:mm:ss"
        class="w-1/2"
        :allowClear="true"
        :showNow="false"
        :showTime="{ format: TIME_FORMAT }"
        :placeholder="[t('common.startDate'), t('common.deadlineDate')]"
        type="date-range"
        size="small" />
    </div>
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.execFuncContent.content') }}</span>
    <Hints :text="t('reportAdd.execFuncContent.contentHints')" />
  </div>
  <Tree
    v-model:checkedKeys="checked"
    class="mt-2 text-3"
    disabled
    :treeData="contentTreeData"
    :defaultExpandAll="true"
    :selectable="false"
    :checkable="true">
    <template #title="{title, tips}">
      <div class="flex items-start space-x-2">
        <span style="color: rgb(82, 90, 101);">{{ title }}</span>
        <Hints
          v-if="tips"
          :text="tips"
          class="leading-6 items-center" />
      </div>
    </template>
  </Tree>
</template>
