<script setup lang='ts'>
import { ref, watch, onMounted, computed, defineAsyncComponent, inject, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  Button, Collapse, CollapsePanel, RadioGroup, Radio, TabPane, Tabs, Textarea, Switch, InputGroup
} from 'ant-design-vue';
import { Hints, Input, Icon, SelectEnum, IconRequired, Arrow } from '@xcan-angus/vue-ui';
import { EnumMessage, ExtractionMethod, utils, enumUtils, HttpExtractionLocation } from '@xcan-angus/infra';
import { RadioChangeEvent } from 'ant-design-vue/es/radio/interface';

// Local imports
import { VariableObj } from './PropsType';
import { ProjectInfo } from '@/layout/types';

const { t } = useI18n();

// Injected dependencies
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const currentProjectId = computed(() => {
  return projectInfo.value?.id;
});

/**
 * Component props interface
 */
interface Props {
  variables: VariableObj[];
}

// Async component imports
const VariableListModal = defineAsyncComponent(() => import('../Exec/Variables/VariableListModal/index.vue'));

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  variables: () => []
});

// Component state
const searchKeywords = ref();
const variableTableData = ref<VariableObj[]>([]);

// Computed properties
const filteredVariableData = computed(() => {
  if (searchKeywords.value) {
    return variableTableData.value.filter(item => item.name.includes(searchKeywords.value));
  }
  return variableTableData.value;
});

const selectedVariableNames = computed(() => {
  return variableTableData.value.map(i => i.name);
});

// Watchers
watch(() => props.variables, (newVariableList) => {
  if (newVariableList?.length) {
    const variableList = JSON.parse(JSON.stringify(newVariableList));
    variableTableData.value = variableList.map(variableItem => ({
      id: utils.uuid('variable'),
      enabled: true,
      ...variableItem,
      description: variableItem.description || undefined,
      isEdit: false,
      isAdd: false,
      isExpand: false,
      delLoading: false,
      saveloading: false,
      nameErr: false,
      valueErr: false,
      enableLoading: false,
      type: !!(variableItem?.extraction && Object.keys(variableItem.extraction)?.length)
    }));
  }
}, {
  deep: true,
  immediate: true
});

// Method options state
const extractionMethodOptions = ref<EnumMessage<ExtractionMethod>[]>();

/**
 * Load extraction method options
 */
const loadExtractionMethodOptions = async () => {
  extractionMethodOptions.value = enumUtils.enumToMessages(ExtractionMethod);
};

/**
 * Handle parameter name change and validation
 */
const handleParameterNameChange = (parameterNameValue: string, targetVariable: VariableObj) => {
  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(targetVariable.extraction.location)) {
    targetVariable.extraction.parameterNameErr = false;
    return;
  }

  targetVariable.extraction.parameterNameErr = !parameterNameValue;
};

/**
 * Handle extraction location change
 */
const handleExtractionLocationChange = (locationValue: string, targetVariable: VariableObj) => {
  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(locationValue)) {
    targetVariable.extraction.parameterNameErr = false;
  }
};

/**
 * Handle extraction expression change and validation
 */
const handleExtractionExpressionChange = (expressionValue: string, targetVariable: VariableObj) => {
  targetVariable.extraction.expressionErr = !expressionValue;
};

/**
 * Handle extraction method change
 */
const handleExtractionMethodChange = (methodValue, targetVariable: VariableObj) => {
  targetVariable.extraction.method.value = methodValue;
  targetVariable.extraction.parameterNameErr = false;
  targetVariable.extraction.expressionErr = false;
};

/**
 * Handle variable name change and validation
 */
const handleVariableNameChange = (variableNameValue: string, targetVariable: VariableObj): void => {
  targetVariable.nameErr = !variableNameValue;
};

/**
 * Handle variable value change and validation
 */
const handleVariableValueChange = (variableValueValue: string, targetVariable: VariableObj): void => {
  targetVariable.valueErr = !variableValueValue;
};

/**
 * Handle variable type change
 */
const handleVariableTypeChange = (typeChangeEvent: RadioChangeEvent, targetVariable: VariableObj) => {
  if (typeChangeEvent && !targetVariable?.extraction) {
    targetVariable.extraction = {
      defaultValue: '',
      expression: '',
      failureMessage: '',
      finalValue: '',
      location: 'PATH_PARAMETER',
      method: { value: 'EXACT_VALUE', message: '' },
      name: '',
      parameterName: '',
      parameterNameErr: false,
      expressionErr: false,
      value: ''
    };
  }

  targetVariable.valueErr = false;
  targetVariable.extraction.parameterNameErr = false;
  targetVariable.extraction.expressionErr = false;
};

// Component lifecycle
onMounted(() => {
  loadExtractionMethodOptions();
});

// Collapse state
const expandedCollapseKeys = ref([]);

/**
 * Handle collapse panel change
 */
const handleCollapsePanelChange = (collapseKeys: string[]) => {
  expandedCollapseKeys.value = collapseKeys;
};

/**
 * Handle collapse arrow click
 */
const handleCollapseArrowClick = (collapseId: string) => {
  if (expandedCollapseKeys.value.includes(collapseId)) {
    expandedCollapseKeys.value = expandedCollapseKeys.value.filter(key => key !== collapseId);
  } else {
    expandedCollapseKeys.value.push(collapseId);
  }
};

// Modal state
const isVariableModalVisible = ref(false);

/**
 * Handle add variable button click
 */
const handleAddVariableClick = () => {
  isVariableModalVisible.value = true;
};

/**
 * Handle adding new variables
 */
const handleAddNewVariables = (newVariableData: VariableObj[]) => {
  variableTableData.value.push(...newVariableData.map(variableItem => {
    return {
      enabled: true,
      ...variableItem,
      id: utils.uuid('variable'),
      isEdit: false,
      isAdd: true,
      isExpand: false,
      delLoading: false,
      saveloading: false,
      nameErr: false,
      valueErr: false,
      enableLoading: false
    };
  }));
  if (variableTableData.value.length > 200) {
    variableTableData.value = variableTableData.value.slice(200);
  }
};

/**
 * Handle variable deletion
 */
const handleVariableDeletion = (variableId: string) => {
  variableTableData.value = variableTableData.value.filter(item => item.id !== variableId);
};

// Component exposure
defineExpose({
  tableData: variableTableData.value
});

// Request location options
const requestLocationOptions = [
  {
    value: '1',
    message: t('protocol.requestParameter')
  },
  {
    value: '2',
    message: t('protocol.requestHeader')
  },
  {
    value: '3',
    message: t('protocol.cookie')
  },
  {
    value: '4',
    message: t('protocol.authorization')
  },
  {
    value: '5',
    message: t('protocol.requestBody')
  }
];
</script>
<template>
  <div>
    <div class="mb-2 -mt-2">
      <Hints :text="t('xcan_execSettingForm.variableManagement')" />
      <div class="flex flex-1 items-center justify-between mt-2">
        <Input
          v-model:value="searchKeywords"
          class="w-60 mr-2"
          allowClear
          :placeholder="t('xcan_execSettingForm.searchVariables')" />
        <Button
          size="small"
          type="primary"
          :disabled="variableTableData.length >= 200"
          @click="handleAddVariableClick">
          <Icon
            icon="icon-jia"
            class="mr-1 -mt-0.5 text-3 leading-3" />
          {{ t('xcan_execSettingForm.addVariable') }}
        </Button>
      </div>
    </div>
    <Collapse
      :activeKey="expandedCollapseKeys"
      class="!bg-transparent"
      @change="handleCollapsePanelChange">
      <CollapsePanel
        v-for="variable in filteredVariableData"
        :key="variable.id"
        :showArrow="false"
        class="text-3"
        ghost>
        <template #header>
          <div class="flex text-text-content leading-5.5 w-full items-center pr-5">
            <div class="flex items-center flex-1">
              <div class="py-0.5 px-2 ml-2.5 truncate mr-3.5" :title="variable.name">{{ variable.name }}</div>
              <Arrow
                class="mr-10"
                :open="expandedCollapseKeys.includes(variable.id)"
                @click="handleCollapseArrowClick(variable.id)" />
              <Switch
                v-model:checked="variable.enabled"
                :checkedChildren="t('status.enabled')"
                :unCheckedChildren="t('status.disabled')"
                size="small" />
            </div>
            <div>
              <Icon
                icon="icon-qingchu"
                class="text-4"
                @click.stop="handleVariableDeletion(variable.id)" />
            </div>
          </div>
        </template>
        <div class="flex flex-1 max-w-150 mr-5 pl-3.5" style="min-width:360px">
          <div class="mr-2.5 leading-7 w-35 flex-none execut-form">
            <div class="h-7 whitespace-nowrap mb-5">
              <IconRequired />{{ t('xcan_execSettingForm.variableName') }}
            </div>
            <div class="h-7 whitespace-nowrap pl-1.75 mb-5">{{ t('common.extract') }}</div>
            <template v-if="variable.type">
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75">{{ t('xcan_execSettingForm.extractionMethod') }}</div>
              <div class="h-7 whitespace-nowrap mb-5"><IconRequired />{{ t('xcan_execSettingForm.parameterName') }}</div>
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75">{{ variable.extraction.method.value === 'REGEX' ? t('common.regexExpression') : t('xcan_execSettingForm.extractionExpression') }}</div>
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75">{{ t('common.defaultValue') }}</div>
            </template>
            <template v-else>
              <div class="h-7 whitespace-nowrap mb-5"><IconRequired />{{ t('xcan_execSettingForm.variableValue') }}</div>
            </template>
            <div class="h-7 whitespace-nowrap mb-5"><IconRequired />{{ t('xcan_execSettingForm.scope') }}</div>
            <div class="h-7 whitespace-nowrap mb-5 pl-1.75">{{ t('xcan_execSettingForm.variableDescription') }}</div>
          </div>
          <div class="flex-1">
            <Input
              v-model:value="variable.name"
              :error="variable.nameErr"
              :maxlength="400"
              dataType="mixin-en"
              includes="!@$%^&*()_-+="
              :placeholder="t('xcan_execSettingForm.enterVariableName')"
              size="small"
              class="mb-5"
              @change="(event)=>handleVariableNameChange(event.target.value,variable)" />
            <div class="h-7 mb-5">
              <RadioGroup v-model:value="variable.type" @change="(value)=>handleVariableTypeChange(value,variable)">
                <Radio :value="false">{{ t('xcan_execSettingForm.define') }}</Radio>
                <Radio :value="true">{{ t('common.extract') }}</Radio>
              </RadioGroup>
            </div>
            <template v-if="!variable.type">
              <Input
                v-model:value="variable.value"
                :error="variable.valueErr"
                :maxlength="400"
                :placeholder="t('xcan_execSettingForm.enterVariableValue')"
                size="small"
                class="mb-5"
                @change="(event)=>handleVariableValueChange(event.target.value,variable)" />
            </template>
            <template v-else>
              <div class="h-7 mb-5">
                <RadioGroup v-model:value="variable.extraction.method.value" @change="handleExtractionMethodChange($event,variable)">
                  <Radio
                    v-for="tab in extractionMethodOptions"
                    :key="tab.value"
                    :value="tab.value">
                    {{ tab.message }}
                  </Radio>
                </RadioGroup>
              </div>
              <InputGroup compact class="w-full mb-5">
                <SelectEnum
                  v-model:value="variable.extraction.location"
                  style="width: 40%;"
                  :enumKey="HttpExtractionLocation"
                  @change="(value)=>handleExtractionLocationChange(value,variable)" />
                <Input
                  v-model:value="variable.extraction.parameterName"
                  :disabled="['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(variable.extraction.location)"
                  :error="variable.extraction.parameterNameErr"
                  :maxlength="400"
                  style="width: 60%;"
                  :placeholder="t('xcan_execSettingForm.enterParameterName')"
                  @change="(event)=>handleParameterNameChange(event.target.value,variable)" />
              </InputGroup>
              <template v-if="variable.extraction?.method?.value ==='REGEX'">
                <Input
                  v-model:value="variable.extraction.expression"
                  :maxlength="1024"
                  :error="variable.extraction.expressionErr"
                  :placeholder="t('xcan_execSettingForm.enterExpression')"
                  size="small"
                  class="mb-5"
                  @change="(event)=>handleExtractionExpressionChange(event.target.value,variable)" />
              </template>
              <template v-else-if="variable.extraction?.method?.value ==='EXACT_VALUE'">
                <Input
                  v-model:value="variable.extraction.value"
                  :maxlength="400"
                  :disabled="!variable.isEdit"
                  class="mb-5"
                  :placeholder="t('xcan_execSettingForm.enterExtractionValue')"
                  size="small" />
              </template>
              <template v-else>
                <Input
                  v-model:value="variable.extraction.expression"
                  :maxlength="1024"
                  :error="variable.extraction.expressionErr"
                  :placeholder="t('xcan_execSettingForm.enterExpression')"
                  class="mb-5"
                  size="small"
                  @change="(event)=>handleExtractionExpressionChange(event.target.value,variable)" />
              </template>
              <Input
                v-model:value="variable.extraction.defaultValue"
                :maxlength="4096"
                :placeholder="t('xcan_execSettingForm.enterDefaultValue')"
                size="small"
                class="mb-5" />
            </template>
            <Textarea
              v-model:value="variable.description"
              :placeholder="t('xcan_execSettingForm.variableDescription')"
              size="small"
              :autoSize="{ minRows: 2, maxRows: 6 }"
              class="mb-5" />
            <Tabs
              v-model:value="variable.extraction.method.value"
              class="variable-method-tab"
              size="small"
              @change="handleExtractionMethodChange($event,variable)">
              <TabPane
                v-for="tab in requestLocationOptions"
                :key="tab.value"
                :tab="tab.message" />
            </Tabs>
          </div>
        </div>
      </CollapsePanel>
    </Collapse>
    <VariableListModal
      v-model:visible="isVariableModalVisible"
      :projectId="currentProjectId"
      :selectedNames="selectedVariableNames"
      @ok="handleAddNewVariables"></VariableListModal>
  </div>
</template>
<style scoped>
:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header){
  padding: 6px 4px;
}

:deep(.ant-collapse-content) {
  border: 0;
  background-color: transparent;
}

:deep(.ant-collapse-content > .ant-collapse-content-box) {
  padding: 14px 8px;
}

:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header .ant-collapse-arrow svg){
  margin-top: -5px;
}

.variable-method-tab :deep(.ant-tabs-tab) {
  padding: 0;
}

.variable-method-tab.ant-tabs > :deep(.ant-tabs-nav) {
  margin: 0 0 4px;
}

.variable-method-tab :deep(.ant-tabs-tab+.ant-tabs-tab) {
  margin: 0;
}

:deep(.variable-method-tab .ant-tabs-nav-list) {
  @apply justify-between flex-1;
}

:deep(.variable-method-tab .ant-tabs-nav::before) {
  border: 0;
}

:deep(.variable-method-tab .ant-tabs-ink-bar) {
  display: none;
}
</style>
