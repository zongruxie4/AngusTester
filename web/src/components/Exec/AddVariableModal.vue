<script lang="ts" setup>
import { ref, computed, defineAsyncComponent, watch, inject } from 'vue';
import { Modal, RadioGroup, Radio, Textarea, InputGroup } from 'ant-design-vue';
import { Input, SelectEnum, IconRequired, Select, notification, Tooltip, Icon } from '@xcan-angus/vue-ui';
import { TESTER, http, VariableScope, HttpExtractionLocation } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

import { getRequestConfigs } from './getRequestConfigs';
import { ListVariableObj } from './PropsType';

interface Props {
  visible: boolean;
  currHasNameList:string[];
  methodOptions:{value: string, message: string}[];
  editData?:ListVariableObj;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  currHasNameList: () => [],
  methodOptions: () => [],
  editData: undefined
});

const emits = defineEmits<{(e: 'update:visible', value: boolean), (e: 'ok', value: any)}>();

const HTTPConfigs = defineAsyncComponent(() => import('./HTTPConfigs/index.vue'));

const projectInfo = inject('projectInfo', ref({ id: '' }));

const variable = ref<ListVariableObj>({
  name: '',
  value: '',
  description: '',
  type: false,
  enabled: true,
  scope: { value: 'GLOBAL', message: '全局' },
  source: 'EXECUTION',
  extraction: {
    parameterName: '',
    value: '',
    method: {
      value: 'EXACT_VALUE',
      message: '精确值'
    },
    expression: '',
    defaultValue: '',
    location: 'RESPONSE_BODY',
    finalValue: 'default'
  }
});

const serviceId = ref('');
const projectName = ref();
const projectIdChange = (value:string, names:string[]) => {
  projectName.value = value ? names[0] : undefined;
  serviceId.value = value;
};
const aipAction = computed(() => {
  return `${TESTER}/services/${serviceId.value}/apis?fullTextSearch=true`;
});

const apiId = ref();
const requestConfigs = ref();
const apiIdChange = async (value:string) => {
  apiId.value = value;
  if (!value) {
    requestConfigs.value = undefined;
    return;
  }

  const [error, { data }] = await http.get(`${TESTER}/apis/${value}?resolveRef=true`);
  if (error) {
    return;
  }

  requestConfigs.value = await getRequestConfigs(data);
};

// 变量名称校验
const nameErr = ref(false);
const variableNameChange = (value:string):void => {
  if (value) {
    if (props.currHasNameList.includes(value)) {
      nameErr.value = true;
      notification.error(t('xcan_exec.variableNameExists'));
    } else {
      nameErr.value = false;
    }
  } else {
    nameErr.value = true;
  }
};

// 变量值校验
const valueErr = ref(false);
const variableValueChange = (value:string):void => {
  valueErr.value = !value;
};

const parameterNameErr = ref(false);
const parameterNameChange = (value:string) => {
  parameterNameErr.value = !value;
};

const methodChange = (e) => {
  variable.value.extraction.method.value = e.target.value;
  variable.value.extraction.method.message = props.methodOptions.filter(item => item.value === e.target.value)[0].message;
};

const httpConfigsRef = ref();
const handleOk = () => {
  let isErr = false;
  if (!variable.value.name) {
    nameErr.value = true;
    isErr = true;
  }
  if (!variable.value.type) {
    if (!variable.value.value) {
      valueErr.value = true;
      isErr = true;
    }
  } else {
    if (!['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(variable.value.extraction.location) && !variable.value.extraction.parameterName) {
      parameterNameErr.value = true;
      isErr = true;
    }

    if (apiId.value && httpConfigsRef.value) {
      isErr = !httpConfigsRef.value.isValid();
    }
  }

  if (isErr) {
    return;
  }

  const { type, extraction, value, ...others } = variable.value;

  let _variable:any = others;
  if (type) {
    if (apiId.value && httpConfigsRef.value) {
      _variable = {
        ..._variable,
        type,
        extraction: {
          ...extraction,
          serviceId: serviceId.value,
          apisId: apiId.value,
          requestErr: 0,
          expressionErr: false,
          parameterNameErr: false,
          apiAction: aipAction.value,
          request: httpConfigsRef.value.getData()
        }
      };
    } else {
      _variable = {
        ..._variable,
        type,
        extraction: {
          ...extraction,
          expressionErr: false,
          parameterNameErr: false
        }
      };
    }
  } else {
    _variable = {
      ..._variable,
      type,
      value
    };
  }

  emits('ok', _variable);
  cancel();
};

const cancel = () => {
  reset();
  emits('update:visible', false);
};

const reset = () => {
  variable.value = {
    name: '',
    value: '',
    description: '',
    type: false,
    enabled: true,
    scope: { value: 'GLOBAL', message: '全局' },
    source: 'EXECUTION',
    extraction: {
      parameterName: '',
      value: '',
      method: {
        value: 'EXACT_VALUE',
        message: '精确值'
      },
      expression: '',
      defaultValue: '',
      location: 'QUERY_PARAMETER',
      finalValue: 'default'
    }
  };
  requestConfigs.value = undefined;
  apiId.value = undefined;
  serviceId.value = undefined;
  nameErr.value = false;
  valueErr.value = false;
  parameterNameErr.value = false;
};

watch(() => props.visible, (newValue) => {
  if (!newValue) {
    return;
  }

  if (props.editData) {
    const _editData = JSON.parse(JSON.stringify(props.editData));
    variable.value = _editData;
    if (!variable.value.extraction) {
      variable.value.extraction = {
        parameterName: '',
        value: '',
        method: {
          value: 'EXACT_VALUE',
          message: '精确值'
        },
        expression: '',
        defaultValue: '',
        location: 'QUERY_PARAMETER',
        finalValue: 'default'
      };
    }

    if (_editData?.type) {
      serviceId.value = _editData?.extraction?.serviceId;
      apiId.value = _editData?.extraction?.apisId;
      requestConfigs.value = _editData?.extraction?.request;
    }
  } else {
    reset();
  }
}, {
  immediate: true
});

const typeChange = () => {
  valueErr.value = false;
  parameterNameErr.value = false;
};

</script>
<template>
  <Modal
    :title="t('xcan_exec.addVariable')"
    :visible="visible"
    :width="800"
    centered
    @cancel="cancel"
    @ok="handleOk">
    <div class="flex overflow-y-auto -mr-5 pr-3.5" style="max-height: 70vh;">
      <div class="mr-2.5 leading-7 flex-none" style="min-width: 68px;">
        <div class="h-7 whitespace-nowrap mb-5">
          <IconRequired />{{ t('xcan_exec.name') }}
        </div>
                  <div class="h-7 whitespace-nowrap mb-5"><IconRequired />{{ t('xcan_exec.scope') }}</div>
                  <div class="h-7 whitespace-nowrap pl-1.75" style="margin-bottom: 34px;">{{ t('xcan_exec.description') }}</div>
        <div class="h-7 whitespace-nowrap pl-1.75" style="margin-bottom: 20px;">定义/提取</div>
        <template v-if="variable.type">
          <div class="h-7 whitespace-nowrap pl-1.75" style="margin-bottom: 24px;">提取方式</div>
          <div class="h-7 whitespace-nowrap mb-5"><IconRequired />提取参数名</div>
          <template v-if="variable.extraction?.method?.value !=='EXACT_VALUE'">
            <div class="h-7 whitespace-nowrap mb-5 pl-1.75">{{ variable.extraction?.method?.value === 'REGEX'?'正则表达式':'提取表达式' }}</div>
          </template>
          <div class="h-7 whitespace-nowrap mb-5 pl-1.75">缺省值</div>
          <div class="h-7 whitespace-nowrap mb-5 pl-1.75">服务</div>
          <div class="h-7 whitespace-nowrap mb-5 pl-1.75">接口</div>
        </template>
        <template v-else>
          <div class="h-7 whitespace-nowrap" style="margin-top: 22px;"><IconRequired />值</div>
        </template>
      </div>
      <div v-if="visible" class="flex-1 flex flex-col space-y-5">
        <Input
          v-model:value="variable.name"
          :error="nameErr"
          :maxlength="100"
          trimAll
          dataType="mixin-en"
          includes="!@$%^&*()_-+="
          placeholder="输入变量名称，最多可输入100字符"
          size="small"
          @change="(event)=>variableNameChange(event.target.value)" />
        <SelectEnum
          :value="variable.scope.value"
          disabled
          :enumKey="VariableScope"
          class="w-full" />
        <Textarea
          v-model:value="variable.description"
          placeholder="变量描述"
          size="small"
          :maxlength="400"
          :autoSize="{ minRows: 2, maxRows: 2 }" />
        <div class="h-7  flex-none">
          <RadioGroup v-model:value="variable.type" @change="typeChange">
            <Radio :value="false">定义</Radio>
            <Radio :value="true">
              <span class="flex items-center">提取
                <Tooltip
                  title="定义提取变量接口不支持依赖其他变量。"
                  placement="topLeft"
                  arrowPointAtCenter>
                  <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
                </Tooltip>
              </span>
            </Radio>
          </RadioGroup>
        </div>
        <template v-if="!variable.type">
          <Input
            v-model:value="variable.value"
            :error="valueErr"
            :maxlength="4096"
            placeholder="输入变量值，支持Mock函数，最多可输入4096字符"
            size="small"
            @change="(event)=>variableValueChange(event.target.value)" />
        </template>
        <template v-else>
          <div class="h-7 flex-none">
            <RadioGroup :value="variable.extraction?.method?.value" @change="methodChange">
              <Radio
                v-for="tab in props.methodOptions"
                :key="tab.value"
                :value="tab.value">
                {{ tab.message }}
              </Radio>
            </RadioGroup>
          </div>
          <InputGroup compact class="w-full">
            <SelectEnum
              v-model:value="variable.extraction.location"
              style="width: 20%;"
              :enumKey="HttpExtractionLocation" />
            <Input
              v-model:value="variable.extraction.parameterName"
              :disabled="['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(variable.extraction.location)"
              :error="parameterNameErr"
              :maxlength="400"
              style="width: 80%;"
              placeholder="输入提取参数名称"
              @change="(event)=>parameterNameChange(event.target.value)" />
          </InputGroup>
          <template v-if="variable.extraction?.method?.value !=='EXACT_VALUE'">
            <template v-if="variable.extraction?.method?.value ==='REGEX'">
              <Input
                v-model:value="variable.extraction.expression"
                :maxlength="1024"
                placeholder="输入正则表达式"
                size="small" />
            </template>
            <template v-else>
              <Input
                v-model:value="variable.extraction.expression"
                :maxlength="1024"
                :error="variable.extraction.expressionErr"
                placeholder="输入提取表达式"
                class="mb-5"
                size="small" />
            </template>
          </template>
          <Input
            v-model:value="variable.extraction.defaultValue"
            :maxlength="4096"
            placeholder="输入缺省值"
            size="small" />
          <Select
            :value="serviceId"
            class="w-full"
            showSearch
            allowClear
            placeholder="选择或查询服务"
            :action="`${TESTER}/services?projectId=${projectInfo?.id}`"
            :fieldNames="{label:'name',value:'id'}"
            @change="projectIdChange">
            <template #title="item">
              <div class="text-3 leading-3 flex items-center" style="height: 28px;">
                <label class="w-4 h-4 leading-4 rounded-full text-white text-center mr-1 bg-blue-badge-s">S</label>
                <div :title="item.name" class="truncate">{{ item.name }}</div>
              </div>
            </template>
          </Select>
          <Select
            :value="apiId"
            :disabled="!serviceId"
            class="w-full"
            showSearch
            :action="aipAction"
            :fieldNames="{ label: 'summary', value: 'id' } "
            :allowClear="true"
            placeholder="选中或查询接口"
            @change="apiIdChange" />
          <template v-if="apiId && requestConfigs">
            <div class=" flex-1">
              <HTTPConfigs
                ref="httpConfigsRef"
                :value="requestConfigs"
                class="w-full h-full" />
            </div>
          </template>
        </template>
      </div>
    </div>
  </Modal>
</template>
