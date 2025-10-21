<script setup lang="ts">
// Vue core imports
import { reactive, watch, ref, inject, onMounted, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Button, Checkbox, Tooltip } from 'ant-design-vue';
import { Icon, Input, SelectSchema, Select, notification, SimpleEditableSelect } from '@xcan-angus/vue-ui';

// Infrastructure imports
import SwaggerUI from '@xcan-angus/swagger-ui';
import { http, TESTER } from '@xcan-angus/infra';

// Local component imports
import ParamsInput from '@/components/form/ParamInput.vue';
import JsonContent from '@/components/form/JsonContent.vue';

// Utility imports
import { ParamsItem, getDefaultParams, API_EXTENSION_KEY, deconstruct, validateType, deepDelAttrFromObj, getModelDataByRef, VARIABLE_NAME_REG } from '@/utils/apis';
import { variable as variableApi } from '@/api/tester';

// Local imports
import { cookieParameterItemTypes } from './interface';

// API extension key constants
const valueKey = API_EXTENSION_KEY.valueKey;
const enabledKey = API_EXTENSION_KEY.enabledKey;

const { t } = useI18n();

/**
 * Component props interface for cookie parameter management
 */
interface Props {
  value: ParamsItem[];
  hideImportBtn: boolean;
  viewType: boolean;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  hideImportBtn: false,
  viewType: false
});

// Component events
const emit = defineEmits<{
  (e: 'change', data: ParamsItem[]): void;
  (e: 'del', index: number): void;
  (e: 'rendered', value: true);
}>();

// Injected dependencies
const apiBaseInfo = inject('apiBaseInfo', ref());
const archivedId = inject('archivedId', ref());

// Component state
const state = reactive<{
  formData: ParamsItem[]
}>({
  formData: []
});

// JSON content component references
const jsonContentRefs = ref<any[]>([]);

/**
 * Generate unique key for form items
 * @param index - Optional index for the key
 * @returns Unique symbol key
 */
const generateFormItemKey = (index?: number): symbol => {
  return Symbol(index);
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
  let value = target.innerText.trim();
  if (['integer', 'number', 'boolean'].includes(data.schema?.type)) {
    try {
      if (value <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {}
  }
  if (value === data[valueKey]) {
    return;
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
    const value = SwaggerUI.extension.sampleFromSchemaGeneric(schema);
    if (option.readonly) {
      model.$ref = option.ref;
    }
    if (!schema.type) {
      let type: string = typeof value;
      if (type === 'object') {
        if (Object.prototype.toString.call(value) === '[object Array]') {
          type = 'array';
        }
      }
      schema.type = type;
    }
    const updatedParameter = { ...model, schema, [enabledKey]: true, [valueKey]: value };
    emitParameterChange(index, updatedParameter);
  }
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
  const checked = e.target.checked;
  const updatedParameter = { ...data, [enabledKey]: checked } as ParamsItem;
  emitParameterChange(index, updatedParameter);
  if (!checked && isValidationEnabled.value) {
    jsonContentRefs.value[index] && jsonContentRefs.value[index].validate(false);
  }
};

// Variable setting loading state
const variableSettingLoading = reactive({});

/**
 * Set parameter as variable
 * @param data - Parameter data to set as variable
 */
const setParameterAsVariable = async (data: ParamsItem): Promise<void> => {
  if (variableSettingLoading[data.name]) {
    return;
  }
  variableSettingLoading[data.name] = true;
  if (!VARIABLE_NAME_REG.test(data.name as string)) {
    notification.warning(t('xcan_apiCookie.nameNotMeetVariableRequirements'));
    return;
  }
  const value = typeof data[valueKey] === 'object' ? JSON.stringify(data[valueKey]) : data[valueKey];
  await variableApi.addVariables({ name: data.name, targetId: archivedId.value, scope: 'CURRENT', targetType: 'API', enabled: true, value });
  variableSettingLoading[data.name] = false;
};

/**
 * Emit change event to parent component
 */
const emitChangeToParent = () => {
  emit('change', state.formData.filter(item => !!item.name));
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
    updatedParameter.deepObject = true;
    updatedParameter[valueKey] = { '': '' };
  } else {
    updatedParameter[valueKey] = undefined;
    if (value === 'array') {
      updatedParameter[valueKey] = [''];
      updatedParameter.schema.item = {
        type: 'string'
      };
    }
    delete updatedParameter.deepObject;
    delete updatedParameter.explode;
  }
  emitParameterChange(index, updatedParameter);
};

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
 * Emit parameter change and add new empty parameter if needed
 * @param index - Parameter index
 * @param data - Updated parameter data
 */
const emitParameterChange = (index: number, data: ParamsItem): void => {
  state.formData[index] = data;
  if (state.formData.every(item => !!item.name)) {
    state.formData.push(getDefaultParams({ in: 'cookie' }) as ParamsItem);
  }
  emitChangeToParent();
};

/**
 * Filter models for cookie parameters
 * @param option - Model option
 * @returns True if model should be filtered out
 */
const filterCookieModels = (option: any) => {
  const model = option.model && JSON.parse(option.model);
  return !model || model.in !== 'cookie';
};

// Validation state
const isValidationEnabled = ref(false);

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
  const errors = validateType(item[valueKey], deepDelAttrFromObj(item.schema));
  return !!errors?.length;
};

/**
 * Handle schema change for parameters
 * @param schema - New schema
 * @param item - Parameter item
 * @param index - Parameter index
 */
const handleSchemaChange = (schema: any, item: ParamsItem, index: number) => {
  if (item.$ref) {
    return;
  }
  const updatedParameter = { ...item, schema };
  emitParameterChange(index, updatedParameter);
};

// Watch for props value changes
watch(() => props.value, (newValue) => {
  state.formData = (newValue || []).map((item, idx) => {
    return { ...item, key: item.key || generateFormItemKey(idx) };
  });
  if (state.formData.every(item => !!item.name) && !props.viewType) {
    state.formData.push(getDefaultParams({ in: 'cookie', key: generateFormItemKey() }) as ParamsItem);
  }
}, {
  deep: true,
  immediate: true
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
  updateComp: updateComponentData,
  getModelResolve: getModelResolution,
  validate: validateParameterContents,
  getData: getProcessedParameterData
});
</script>
<template>
  <div class="space-y-3">
    <div
      v-for="(item, index) in state.formData"
      :key="item.key"
      class="space-y-2">
      <div
        class="flex flex-nowrap items-center mb-3 whitespace-nowrap space-x-2"
        :class="{'opacity-50': !item[enabledKey]}">
        <Checkbox
          :disabled="!item.name || props.viewType"
          :checked="item[enabledKey] && !!item.name"
          @change="handleParameterEnabledChange($event, index, item)" />
        <Tooltip placement="topLeft">
          <div class="flex flex-col max-w-100 flex-1" :class="{'border-l border-blue-1': item.$ref}">
            <SelectSchema
              v-if="apiBaseInfo?.serviceId"
              :id="apiBaseInfo?.serviceId"
              v-model:value="item.name"
              :hideImportBtn="props.hideImportBtn"
              :placeholder="t('xcan_apiCookie.parameterName')"
              mode="pure"
              :maxLength="400"
              :params="{ ignoreModel: false, types: 'parameters'}"
              :type="['parameters']"
              :inputProps="{readonly: item.$ref || props.viewType}"
              :excludes="opt => filterCookieModels(opt)"
              @blur="handleParameterFieldBlur($event, index, item, 'name')"
              @change="(...arg) => handleModelSelection(...arg, index)" />
            <Input
              v-else
              :placeholder="t('xcan_apiCookie.enterParameterName')"
              :maxLength="400"
              :value="item.name"
              :allowClear="false"
              :readonly="props.viewType"
              size="small"
              @blur="handleParameterFieldBlur($event, index, item, 'name')"
              @keypress="handleEnterKeyPress" />
          </div>
          <template v-if="item.$ref" #title>
            {{ t('xcan_apiCookie.componentReference', { ref: item.$ref }) }}
          </template>
        </Tooltip>
        <Select
          v-model:value="item.schema.type"
          :options="cookieParameterItemTypes"
          :readonly="props.viewType"
          class="w-25"
          @change="handleDataTypeChange($event, index, item)" />
        <div class="flex flex-col flex-1">
          <SimpleEditableSelect
            v-if="item.schema?.enum"
            :placeholder="t('xcan_apiCookie.enterDebugValue')"
            :options="item.schema.enum || item.schema?.[valueKey]"
            :value="item[valueKey]"
            :readonly="props.viewType"
            @blur="handleParameterValueBlur($event,index,item )"
            @select="emitParameterChange(index, { ...item, [valueKey]: $event })" />
          <ParamsInput
            v-else-if="!['array', 'object'].includes(item.schema.type)"
            :placeholder="t('xcan_apiCookie.enterDebugValue')"
            :value="item[valueKey]"
            :disabled="props.viewType"
            :error="getParameterErrorState(item)"
            @blur="handleParameterValueBlur($event, index, item)" />
          <Input v-else disabled />
        </div>
        <template v-if="!props.viewType">
          <Button
            v-if="archivedId"
            :title="t('xcan_apiCookie.setAsVariable')"
            type="primary"
            size="small"
            :disabled="!item.name || variableSettingLoading[item.name] || props.viewType"
            @click="setParameterAsVariable(item)">
            <Icon icon="icon-bianliang" />
          </Button>
          <Button
            size="small"
            :disabled="!['array', 'object'].includes(item.schema.type) || (item.schema.type === 'object' && item.$ref)"
            @click="addChildParameter(item, index)">
            <Icon icon="icon-jia" />
          </Button>
          <Button
            class="w-7 ml-3 p-0"
            type="default"
            size="small"
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
        :hideImportBtn="props.hideImportBtn"
        :viewType="props.viewType"
        @change="handleSchemaChange($event, item, index)" />
    </div>
  </div>
</template>
