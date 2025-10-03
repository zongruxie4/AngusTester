<script setup lang="ts">
// Vue core imports
import { reactive, watch, ref, inject, onMounted, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Button, Checkbox, Tooltip } from 'ant-design-vue';
import { Icon, Input, Select, SelectSchema } from '@xcan-angus/vue-ui';

// Infrastructure imports
import SwaggerUI from '@xcan-angus/swagger-ui';
import { http, TESTER } from '@xcan-angus/infra';

// Local component imports
import ParamInput from '@/components/ParamInput/index.vue';
import JsonContent from '@/components/JsonContent/index.vue';
import SimpleEditableSelect from '@/components/SimpleEditableSelect/index.vue';

// Utility imports
import { deconstruct, getParamsByUri, getUriByParams, API_EXTENSION_KEY, getModelDataByRef, itemTypes, validateType, deepDelAttrFromObj } from 'src/utils/apis';

// Local imports
import { ParamsItem, paramsTypeOpt, getDefaultParams } from './interface';

const { t } = useI18n();

// API extension key constants
const valueKey = API_EXTENSION_KEY.valueKey;
const enabledKey = API_EXTENSION_KEY.enabledKey;

/**
 * Component props interface for API parameter management
 */
export interface Props {
  value: ParamsItem[];
  apiUri?: string;
  updateCompUrl: string;
  hideImportBtn: boolean;
  viewType: boolean;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  apiUri: '',
  value: () => ([]),
  hideImportBtn: false,
  viewType: false
});

// Component events
const emit = defineEmits<{
  (e: 'change', value: ParamsItem[]): void;
  (e: 'update:apiUri', value: string): void;
  (e: 'rendered', value: true): void;
}>();

// Injected dependencies
const apiBaseInfo = inject('apiBaseInfo', ref());

// Component state
const state = reactive<{
  formData: ParamsItem[]
}>({
  formData: []
});

// JSON content component references
const jsonContentRefs = ref<any[]>([]);

// Validation state
const isValidationEnabled = ref(false);

/**
 * Generate unique key for form items
 * @param index - Optional index for the key
 * @returns Unique symbol key
 */
const generateFormItemKey = (index?: number): symbol => {
  return Symbol(index);
};

/**
 * Emit change event to parent component
 */
const emitChangeToParent = () => {
  emit('change', state.formData);
};

/**
 * Handle enter key press in input fields
 * @param e - Keyboard event
 */
const handleEnterKeyPress = (e: KeyboardEvent): void => {
  if (e.key !== 'Enter') {
    return;
  }
  (e.target as HTMLElement).blur();
};

/**
 * Handle parameter value blur event with type conversion
 * @param target - Target HTML element
 * @param index - Parameter index
 * @param data - Parameter data
 */
const handleParameterValueBlur = (target: HTMLElement, index: number, data: ParamsItem): void => {
  let value = target?.innerText?.trim();
  if (['integer', 'number', 'boolean'].includes(data.schema?.type)) {
    try {
      if (typeof value === 'string' && parseFloat(value) <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {}
  }
  const updatedParameter = { ...data, [valueKey]: value } as ParamsItem;
  emitParameterChange(index, updatedParameter);
};

/**
 * Get model data by reference
 * @param ref - Reference path
 * @returns Model data or empty object on error
 */
const getModelDataByReference = async (ref: string) => {
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

/**
 * Handle model selection for parameters
 * @param _value - Selected value
 * @param option - Selected option
 * @param index - Parameter index
 */
const handleModelSelection = async (_value: any, option: any, index: number) => {
  if (option) {
    const model = await getModelDataByReference(option.ref);
    const schema = model.schema || {};
    const value = SwaggerUI.extension.sampleFromSchemaGeneric(schema, { useValue: true });
    if (!schema.type) {
      let type: string = typeof value;
      if (type === 'object') {
        if (Object.prototype.toString.call(value) === '[object Array]') {
          type = 'array';
        }
      }
      schema.type = type;
    }
    if (option.readonly) {
      model.$ref = option.ref;
    }
    const updatedParameter = { ...model, schema, [enabledKey]: true, [valueKey]: value };
    emitParameterChange(index, updatedParameter);
  }
};

/**
 * Handle data type change for parameters
 * @param value - New data type
 * @param index - Parameter index
 * @param item - Parameter item
 */
const handleDataTypeChange = (value: string, index: number, item: ParamsItem) => {
  const schema = item.schema || {};
  const updatedParameter = { ...item, schema: { ...schema, type: value } };
  if (value === 'object') {
    (updatedParameter as any).deepObject = true;
    updatedParameter[valueKey] = { '': '' };
  } else {
    updatedParameter[valueKey] = undefined;
    if (value === 'array') {
      updatedParameter[valueKey] = [''];
      updatedParameter.schema.item = {
        type: 'string'
      };
    }
    delete (updatedParameter as any).deepObject;
    delete (updatedParameter as any).explode;
  }
  emitParameterChange(index, updatedParameter);
};

/**
 * Handle input blur event for parameter fields
 * @param e - Blur event
 * @param index - Parameter index
 * @param data - Parameter data
 * @param key - Field key to update
 */
const handleParameterFieldBlur = (e: Event, index: number, data: ParamsItem, key: string): void => {
  const value = (e.target as HTMLInputElement).value.trim();
  const updatedParameter = { ...data, [key]: value } as ParamsItem;
  emitParameterChange(index, updatedParameter);
};

/**
 * Handle parameter enabled/disabled checkbox change
 * @param e - Checkbox change event
 * @param index - Parameter index
 * @param data - Parameter data
 */
const handleParameterEnabledChange = (e: any, index: number, data: ParamsItem) => {
  const checked = e?.target?.checked;
  const updatedParameter = { ...data, [enabledKey]: checked } as ParamsItem;
  emitParameterChange(index, updatedParameter);
  if (!checked && isValidationEnabled.value) {
    jsonContentRefs.value[index] && jsonContentRefs.value[index].validate(false);
  }
};

/**
 * Handle select change for parameter fields
 * @param value - Selected value
 * @param index - Parameter index
 * @param data - Parameter data
 * @param key - Field key to update
 */
const handleSelectChange = (value: string, index: number, data: ParamsItem, key: string): void => {
  const updatedParameter = { ...data, [key]: value } as ParamsItem;
  emitParameterChange(index, updatedParameter);
};

/**
 * Handle schema change for parameters
 * @param schema - New schema
 * @param item - Parameter item
 * @param index - Parameter index
 */
const handleSchemaChange = (schema: any, item: ParamsItem, index: number) => {
  const updatedParameter = { ...item, schema };
  emitParameterChange(index, updatedParameter);
};

// 记录当前变量 loading 数据；
// const setVariableLoading = reactive({});
// 设为变量
// const setToVariable = async (data: ParamsItem): void => {
//   if (setVariableLoading[data.name]) {
//     return;
//   }
//   setVariableLoading[data.name] = true;
//   if (!variableNameReg.test(data.name as string)) {
//     notification.warning('名称不符合变量要求,允许数字字母!@$%^&*()_-+=./等');
//     return;
//   }
//   const value = typeof data[valueKey] === 'object' ? JSON.stringify(data[valueKey]) : data[valueKey];
//   await variableApi.addVariables({ targetId: archivedId.value, name: data.name, scope: 'CURRENT', targetType: 'API', enabled: true, value });
//   setVariableLoading[data.name] = false;
// };

/**
 * Handle parameter deletion
 * @param index - Parameter index to delete
 * @param data - Parameter data
 */
const handleParameterDeletion = (index: number, data: ParamsItem): void => {
  const emptyParameters = state.formData.filter(item => !item.name);
  // Keep at least one empty parameter
  if (!data.name && emptyParameters.length <= 1) {
    return;
  }
  state.formData.splice(index, 1);
  emitChangeToParent();
  if (data.in === 'path') {
    updateApiUriFromParameters();
  }
};

/**
 * Emit parameter change and update API URI
 * @param index - Parameter index
 * @param data - Updated parameter data
 */
const emitParameterChange = (index: number, data: ParamsItem): void => {
  state.formData[index] = data;
  emitChangeToParent();
  updateApiUriFromParameters();
};

/**
 * Add child item to JSON content
 * @param parentItem - Parent parameter item
 * @param index - Parameter index
 */
const addChildParameter = (parentItem: ParamsItem, index: number) => {
  jsonContentRefs.value[index].addItem({ type: parentItem.schema.type, id: -1, idLine: [-1], level: 0 });
};

/**
 * Update API URI based on current parameters
 */
const updateApiUriFromParameters = () => {
  const pathParameters = state.formData.filter(item => item?.[enabledKey] && item.name && item.in === 'path');
  let apiUri = props.apiUri;
  if (pathParameters.length) {
    if (!apiUri) {
      apiUri = '/';
    }
  }
  apiUri = getUriByParams(apiUri || '', pathParameters);
  emit('update:apiUri', apiUri);
};

/**
 * Synchronize parameter list with URI parameters
 */
const synchronizeParameterList = (): void => {
  const uriParameters = getParamsByUri(props.apiUri || '');
  const indicesToRemove: number[] = [];

  // Get all path parameters from URI
  const pathParameters = uriParameters.filter(param => param.in === 'path');

  // Forward comparison: find missing parameters in formData and add them
  pathParameters?.forEach((currentParam) => {
    const existingItem = state.formData.find(item => item.name === currentParam.name);
    if (!existingItem) {
      state.formData.splice(-1, 0, {
        ...currentParam,
        [enabledKey]: true
      } as ParamsItem);
    } else {
      existingItem[enabledKey] = true;
    }
  });

  // Reverse comparison: find extra parameters in formData and mark for removal
  state.formData?.forEach((currentParam, index) => {
    if (currentParam.in === 'path') {
      const uriItem = pathParameters.find(param => param.name === currentParam.name);
      if (!uriItem && currentParam[enabledKey]) {
        indicesToRemove.push(index);
      }
    }
  });

  state.formData = state.formData.filter((_item, index) => !indicesToRemove.includes(index));
  emitChangeToParent();
};

// Watch for props value changes
watch(() => props.value, (newValue) => {
  state.formData = newValue.map((item) => {
    return { ...item, key: item.key || generateFormItemKey() };
  });
}, {
  deep: true,
  immediate: true
});

// Watch for form data changes
watch(() => state.formData, () => {
  if (state.formData.every(item => !!item.name || !!item[valueKey]) && !props.viewType) {
    state.formData.push(getDefaultParams({ in: 'query', key: generateFormItemKey() }) as ParamsItem);
  }
}, {
  deep: true,
  immediate: true
});

// Watch for API URI changes and synchronize parameters
watch(() => props.apiUri, () => {
  setTimeout(() => {
    synchronizeParameterList();
  });
});

/**
 * Component mounted lifecycle hook
 */
onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});

/**
 * Validate all parameter contents
 * @param val - Validation flag
 */
const validateParameterContents = async (val = true) => {
  isValidationEnabled.value = val;
  for (const idx in jsonContentRefs.value) {
    if (state.formData[idx][enabledKey]) {
      jsonContentRefs.value[idx].validate(val);
    }
  }
};

/**
 * Get error state for parameter item
 * @param item - Parameter item
 * @returns True if item has validation errors
 */
const getParameterErrorState = (item: ParamsItem) => {
  if (!isValidationEnabled.value || !item.name || !item[enabledKey]) {
    return false;
  }
  const errors = validateType(item[valueKey], deepDelAttrFromObj(item.schema, []));
  return !!errors.length;
  // return false; // Simplified validation for now
};

/**
 * Filter data models for parameters
 * @param option - Model option
 * @returns True if model should be filtered out
 */
const filterParameterModels = (option: any) => {
  const model = JSON.parse(option.model);
  return !(['query', 'path'].includes(model.in));
};

/**
 * Update component data
 */
const updateComponentData = async () => {
  if (!apiBaseInfo.value.serviceId) {
    return;
  }
  for (let i = 0; i < state.formData.length; i++) {
    if (state.formData[i].$ref) {
      await http.put(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/parameters/${state.formData[i].name}`, state.formData[i]);
    }
    if (jsonContentRefs.value[i]) {
      await jsonContentRefs.value[i].updateComp();
    }
  }
};

/**
 * Add parameters to the form data
 * @param data - Parameters to add
 */
const addParameters = (data: ParamsItem[]) => {
  state.formData.splice(-1, 0, ...data);
};

/**
 * Get model resolution data
 * @returns Model resolution object
 */
const getModelResolution = () => {
  const models = {};
  state.formData.forEach((item, index) => {
    if (item.$ref) {
      models[item.$ref] = JSON.parse(JSON.stringify(item));
      delete models[item.$ref].schema.$ref;
      jsonContentRefs.value[index] && jsonContentRefs.value[index].getModelResolve(models);
    }
  });
  return models;
};

/**
 * Get processed parameter data
 * @returns Filtered parameter data
 */
const getProcessedParameterData = () => {
  return state.formData.filter(item => item.name || item[valueKey]);
};

// Expose component methods
defineExpose({
  validate: validateParameterContents,
  updateComp: updateComponentData,
  getModelResolve: getModelResolution,
  addParam: addParameters,
  getData: getProcessedParameterData
});
</script>
<template>
  <div class="space-y-3">
    <div
      v-for="(item, index) in state.formData"
      :key="item.key"
      class="space-y-2 w-full">
      <div
        class="flex flex-nowrap whitespace-nowrap items-center space-x-2  w-full"
        :class="{'opacity-50': !item[enabledKey]}">
        <Checkbox
          :disabled="!item.name && !item[valueKey] || props.viewType"
          :checked="item[enabledKey] && (!!item.name || !!item[valueKey]) "
          @change="handleParameterEnabledChange($event, index, item)" />
        <Tooltip placement="topLeft">
          <div class="flex flex-col max-w-100 flex-1">
            <SelectSchema
              v-if="apiBaseInfo?.serviceId"
              :id="apiBaseInfo?.serviceId"
              v-model:value="item.name"
              :inputProps="{readonly: item.$ref || props.viewType}"
              :disabled="item.$ref || props.viewType"
              :placeholder="t('xcan_apiParameter.parameterName')"
              mode="pure"
              :maxLength="400"
              :hideImportBtn="props.hideImportBtn"
              :type="['parameters']"
              :params="{types: 'parameters', ignoreModel: false}"
              :excludes="opt => filterParameterModels(opt)"
              @blur="handleParameterFieldBlur($event, index, item, 'name')"
              @change="(_value, option) => handleModelSelection(_value, option, index)" />
            <Input
              v-else
              v-model:value="item.name"
              :placeholder="t('xcan_apiParameter.enterParameterName')"
              :maxLength="400"
              :readonly="item.$ref || props.viewType"
              :allowClear="false"
              @blur="handleParameterFieldBlur($event, index, item, 'name')"
              @keypress="handleEnterKeyPress" />
          </div>
          <template v-if="item.$ref" #title>
            {{ t('xcan_apiParameter.componentReference', { ref: item.$ref }) }}
          </template>
        </Tooltip>
        <Select
          v-model:value="item.in"
          dropdownClassName="api-select-dropdown"
          :placeholder="t('xcan_apiParameter.selectParameterType')"
          :readonly="item.$ref || props.viewType"
          :allowClear="false"
          :options="paramsTypeOpt"
          class="w-25"
          @change="handleSelectChange($event, index, item, 'in')" />
        <Select
          v-model:value="item.schema.type"
          class="w-25"
          :readonly="item.$ref || props.viewType"
          :options="itemTypes"
          @change="handleDataTypeChange($event, index, item)" />
        <div class="flex flex-col flex-1 ml-3 space-y-0.5">
          <SimpleEditableSelect
            v-if="item.schema?.enum"
            :placeholder="t('xcan_apiParameter.enterDebugValue')"
            :options="item.schema.enum"
            :readonly="props.viewType"
            :value="item[valueKey] || item.schema?.[valueKey]"
            @blur="handleParameterValueBlur($event,index,item)"
            @select="emitParameterChange(index, { ...item, [valueKey]: $event })" />
          <param-input
            v-else-if="item.schema?.type !== 'array' && item.schema?.type !== 'object'"
            :placeholder="t('xcan_apiParameter.enterDebugValue')"
            :value="item[valueKey]"
            :disabled="props.viewType"
            :error="getParameterErrorState(item)"
            :maxLength="2000 as any"
            @blur="handleParameterValueBlur($event, index, item)" />
          <Input v-else disabled />
        </div>
        <template v-if="!props.viewType">
          <Button
            size="small"
            class="ml-2"
            :disabled="!['array', 'object'].includes(item.schema?.type) || (item.schema?.type === 'object' && item.$ref)"
            @click="addChildParameter(item, index)">
            <Icon icon="icon-jia" />
          </Button>
          <Button
            size="small"
            class="ml-2"
            :disabled="!item.name && !item[valueKey] || props.viewType"
            @click="handleParameterDeletion(index, item)">
            <Icon icon="icon-shanchuguanbi" />
          </Button>
        </template>
      </div>
      <JsonContent
        v-if="item.schema?.type === 'array' || item.schema?.type === 'object'"
        :ref="dom => jsonContentRefs[index] = dom"
        v-model:data="item[valueKey]"
        :schema="item.schema || {}"
        :disabled="!!item.$ref"
        :pType="item.schema?.type"
        :paramInType="item.in"
        :hideImportBtn="props.hideImportBtn"
        :viewType="props.viewType"
        @change="handleSchemaChange($event, item, index)" />
    </div>
  </div>
</template>
<style>
.api-select-dropdown .ant-select-item {
  @apply text-3 leading-7 py-0 min-h-full;
}
</style>
