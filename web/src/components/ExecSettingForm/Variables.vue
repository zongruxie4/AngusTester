<script setup lang='ts'>
import { ref, watch, onMounted, computed, defineAsyncComponent, inject } from 'vue';
import { Button, Collapse, CollapsePanel, RadioGroup, Radio, TabPane, Tabs, Textarea, Switch, InputGroup } from 'ant-design-vue';
import { EnumMessage, ExtractionMethod, utils, enumUtils, HttpExtractionLocation } from '@xcan-angus/infra';
import { RadioChangeEvent } from 'ant-design-vue/es/radio/interface';
import { Hints, Input, Icon, SelectEnum, IconRequired, Arrow } from '@xcan-angus/vue-ui';

import { useI18n } from 'vue-i18n';

import { VariableObj } from './PropsType';
const { t } = useI18n();
const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo.value?.id;
});

interface Props {
  variables: VariableObj[];
}

const VariableListModal = defineAsyncComponent(() => import('../Exec/Variables/VariableListModal/index.vue'));
const props = withDefaults(defineProps<Props>(), {
  variables: () => []
});

const keywords = ref();
const oldTableData = ref<VariableObj[]>([]);
// const tableData = ref<VariableObj[]>([]);
const tableData = computed(() => {
  if (keywords.value) {
    return oldTableData.value.filter(item => item.name.includes(keywords.value));
  }
  return oldTableData.value;
});

const selectedNames = computed(() => {
  return oldTableData.value.map(i => i.name);
});

watch(() => props.variables, (newValue) => {
  if (newValue?.length) {
    const list = JSON.parse(JSON.stringify(newValue));
    oldTableData.value = list.map(item => ({
      id: utils.uuid('variable'),
      enabled: true,
      ...item,
      description: item.description || undefined,
      isEdit: false,
      isAdd: false,
      isExpand: false,
      delLoading: false,
      saveloading: false,
      nameErr: false,
      valueErr: false,
      enableLoading: false,
      type: !!(item?.extraction && Object.keys(item.extraction)?.length)
    }));
    // oldTableData.value = JSON.parse(JSON.stringify(tableData.value));
  }
}, {
  deep: true,
  immediate: true
});

const methodOptions = ref<EnumMessage<ExtractionMethod>[]>();
const getMethodOptions = async () => {
  methodOptions.value = enumUtils.enumToMessages(ExtractionMethod);
};

const parameterNameChange = (value:string, variable:VariableObj) => {
  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(variable.extraction.location)) {
    variable.extraction.parameterNameErr = false;
    return;
  }

  variable.extraction.parameterNameErr = !value;
};

const locationChange = (value:string, variable:VariableObj) => {
  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(value)) {
    variable.extraction.parameterNameErr = false;
  }
};
const expressionChange = (value:string, variable:VariableObj) => {
  variable.extraction.expressionErr = !value;
};
const methodChange = (value, variable:VariableObj) => {
  variable.extraction.method.value = value;
  variable.extraction.parameterNameErr = false;
  variable.extraction.expressionErr = false;
};

// 变量名称校验
const variableNameChange = (value:string, variable:VariableObj):void => {
  variable.nameErr = !value;
};
// 变量值校验
const variableValueChange = (value:string, variable:VariableObj):void => {
  variable.valueErr = !value;
};

const typeChange = (value:RadioChangeEvent, variable:VariableObj) => {
  if (value && !variable?.extraction) {
    variable.extraction = {
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

  variable.valueErr = false;
  variable.extraction.parameterNameErr = false;
  variable.extraction.expressionErr = false;
};

onMounted(() => {
  getMethodOptions();
});

const activeKey = ref([]);

const collapseChange = (keys:string[]) => {
  activeKey.value = keys;
};

const arrowChange = (id:string) => {
  if (activeKey.value.includes(id)) {
    activeKey.value = activeKey.value.filter(item => item !== id);
  } else {
    activeKey.value.push(id);
  }
};

const variableVisible = ref(false);
const handleAdd = () => {
  variableVisible.value = true;
};

const handleAddVariable = (data: VariableObj[]) => {
  oldTableData.value.push(...data.map(i => {
    return {
      enabled: true,
      ...i,
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
  if (oldTableData.value.length > 200) {
    oldTableData.value = oldTableData.value.slice(200);
  }
};

const delVariable = (id:string) => {
  oldTableData.value = oldTableData.value.filter(item => item.id !== id);
};

defineExpose({
  tableData: oldTableData.value
});

const request = [
  {
    value: '1',
    message: t('xcan_execSettingForm.requestParameters')
  },
  {
    value: '2',
    message: t('xcan_execSettingForm.requestHeaders')
  },
  {
    value: '3',
    message: t('xcan_execSettingForm.cookie')
  },
  {
    value: '4',
    message: t('xcan_execSettingForm.authorization')
  },
  {
    value: '5',
    message: t('xcan_execSettingForm.requestBody')
  }
];
</script>
<template>
  <div>
    <div class="mb-2 -mt-2">
      <Hints :text="t('xcan_execSettingForm.variableManagement')" />
      <div class="flex flex-1 items-center justify-between mt-2">
        <Input
          v-model:value="keywords"
          class="w-60 mr-2"
          allowClear
          :placeholder="t('xcan_execSettingForm.searchVariables')" />
        <Button
          size="small"
          type="primary"
          :disabled="oldTableData.length >= 200"
          @click="handleAdd">
          <Icon
            icon="icon-jia"
            class="mr-1 -mt-0.5 text-3 leading-3" />
          {{ t('xcan_execSettingForm.addVariable') }}
        </Button>
      </div>
    </div>
    <Collapse
      :activeKey="activeKey"
      class="!bg-transparent"
      @change="collapseChange">
      <CollapsePanel
        v-for="variable in tableData"
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
                :open="activeKey.includes(variable.id)"
                @click="arrowChange(variable.id)" />
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
                @click.stop="delVariable(variable.id)" />
            </div>
          </div>
        </template>
        <div class="flex flex-1 max-w-150 mr-5 pl-3.5" style="min-width:360px">
          <div class="mr-2.5 leading-7 w-35 flex-none execut-form">
            <div class="h-7 whitespace-nowrap mb-5">
              <IconRequired />{{ t('xcan_execSettingForm.variableName') }}
            </div>
            <div class="h-7 whitespace-nowrap pl-1.75 mb-5">{{ t('xcan_execSettingForm.define') }}/{{ t('xcan_execSettingForm.extract') }}</div>
            <template v-if="variable.type">
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75">{{ t('xcan_execSettingForm.extractionMethod') }}</div>
              <div class="h-7 whitespace-nowrap mb-5"><IconRequired />{{ t('xcan_execSettingForm.parameterName') }}</div>
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75">{{ variable.extraction.method.value === 'REGEX' ? t('xcan_execSettingForm.regexExpression') : t('xcan_execSettingForm.extractionExpression') }}</div>
              <div class="h-7 whitespace-nowrap mb-5 pl-1.75">{{ t('xcan_execSettingForm.defaultValue') }}</div>
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
              @change="(event)=>variableNameChange(event.target.value,variable)" />
            <div class="h-7 mb-5">
              <RadioGroup v-model:value="variable.type" @change="(value)=>typeChange(value,variable)">
                <Radio :value="false">{{ t('xcan_execSettingForm.define') }}</Radio>
                <Radio :value="true">{{ t('xcan_execSettingForm.extract') }}</Radio>
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
                @change="(event)=>variableValueChange(event.target.value,variable)" />
            </template>
            <template v-else>
              <div class="h-7 mb-5">
                <RadioGroup v-model:value="variable.extraction.method.value" @change="methodChange($event,variable)">
                  <Radio
                    v-for="tab in methodOptions"
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
                  @change="(value)=>locationChange(value,variable)" />
                <Input
                  v-model:value="variable.extraction.parameterName"
                  :disabled="['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(variable.extraction.location)"
                  :error="variable.extraction.parameterNameErr"
                  :maxlength="400"
                  style="width: 60%;"
                  :placeholder="t('xcan_execSettingForm.enterParameterName')"
                  @change="(event)=>parameterNameChange(event.target.value,variable)" />
              </InputGroup>
              <template v-if="variable.extraction?.method?.value ==='REGEX'">
                <Input
                  v-model:value="variable.extraction.expression"
                  :maxlength="1024"
                  :error="variable.extraction.expressionErr"
                  :placeholder="t('xcan_execSettingForm.enterExpression')"
                  size="small"
                  class="mb-5"
                  @change="(event)=>expressionChange(event.target.value,variable)" />
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
                  @change="(event)=>expressionChange(event.target.value,variable)" />
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
              @change="methodChange($event,variable)">
              <TabPane
                v-for="tab in request"
                :key="tab.value"
                :tab="tab.message" />
            </Tabs>
          </div>
        </div>
      </CollapsePanel>
    </Collapse>
    <VariableListModal
      v-model:visible="variableVisible"
      :projectId="projectId"
      :selectedNames="selectedNames"
      @ok="handleAddVariable"></VariableListModal>
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
