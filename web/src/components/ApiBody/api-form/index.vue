<script setup lang="ts">
// Vue core imports
import { watch, ref, inject } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Button, Checkbox } from 'ant-design-vue';
import { Icon, Input, Select, SelectSchema, SimpleEditableSelect } from '@xcan-angus/vue-ui';

// Infrastructure imports
import SwaggerUI from '@xcan-angus/swagger-ui';
import { http, TESTER } from '@xcan-angus/infra';

// Local component imports
import ApiUpload from '../upload/index.vue';
import ParamInput from '../../ParamInput/index.vue';
import JsonContent from '../../JsonContent/index.vue';

// Local imports
import { basicParameterItemTypes, formDataParameterTypes } from './interface';

// Utility imports
import { ParamsItem, API_EXTENSION_KEY, deconstruct, deepDelAttrFromObj, validateType, getModelDataByRef } from '@/utils/apis/index';

const { t } = useI18n();

// Component props interface
export interface Props {
  value: ParamsItem[];
  useModel: boolean;
  hasFileType?: boolean;
  formFileSize: number;
  maxFileSize: number;
  hideImportBtn: boolean;
  disabled: boolean;
  viewType?: boolean;
  fieldNames?: { valueKey: string; enabledKey: string; fileNameKey: string };
}

// Injected dependencies
const apiBaseInfo = inject('apiBaseInfo', ref());

// Component refs
const jsonContentRefs = ref<any[]>([]);

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  useModel: false,
  hasFileType: false,
  hideImportBtn: false,
  disabled: false,
  viewType: false,
  fieldNames: () => ({
    valueKey: API_EXTENSION_KEY.valueKey,
    enabledKey: API_EXTENSION_KEY.enabledKey,
    fileNameKey: API_EXTENSION_KEY.fileNameKey
  })
});

// Field name constants
const valueKey = props.fieldNames.valueKey;
const enabledKey = props.fieldNames.enabledKey;

// Component events
const emit = defineEmits<{
  (e: 'change', index: number, data: ParamsItem): void;
  (e: 'del', index: number): void;
  (e: 'update:formFileSize', value: number): void;
}>();

// Form data state management
const formDataParameters = ref<ParamsItem[]>([]);

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
 * @param event - Keyboard event
 */
const handleEnterKeyPress = (event: KeyboardEvent): void => {
  if (event.key !== 'Enter') {
    return;
  }
  (event.target as HTMLElement).blur();
};

/**
 * Handle value blur event for form parameters
 * @param target - HTML element that lost focus
 * @param index - Index of the parameter
 * @param data - Parameter data object
 */
const handleParameterValueBlur = (target: HTMLElement, index: number, data: ParamsItem): void => {
  let value = target?.innerText;
  if (['integer', 'number', 'boolean'].includes(data?.type)) {
    try {
      if (typeof value === 'string' && parseFloat(value) <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {}
  }
  const updatedParameter = { ...data, [valueKey]: value } as ParamsItem;
  emitParameterChange(index, updatedParameter);
};

// const handleValueChange = (value: string, index:number, data: ParamsItem):void => {
//   const temp = { ...data, value } as ParamsItem;
//   changeEmit(index, temp);
// };

/**
 * Handle input blur event for form parameters
 * @param event - Blur event
 * @param index - Index of the parameter
 * @param data - Parameter data object
 * @param key - Field key being updated
 */
const handleParameterFieldBlur = (event: Event, index: number, data: ParamsItem, key: string): void => {
  const value = (event.target as HTMLInputElement).value?.trim();
  if (value === data[key]) {
    return;
  }

  const updatedParameter = { ...data, [key]: value } as ParamsItem;
  emitParameterChange(index, updatedParameter);
};

/**
 * Get model data by reference
 * @param ref - Model reference
 * @returns Deconstructed model data
 */
const getModelDataByReference = async (ref: string) => {
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

/**
 * Handle model selection for form parameters
 * @param value - Selected value
 * @param option - Selected option object
 * @param index - Index of the parameter
 * @param item - Parameter item
 */
const handleModelSelection = async (_value: string, option: any, index: number, item: ParamsItem) => {
  if (option) {
    const modelData = await getModelDataByReference(option.ref);
    const sampleValue = SwaggerUI.extension.sampleFromSchemaGeneric(modelData, { useValue: true });
    const updatedParameter = {
      ...item,
      ...modelData,
      [valueKey]: sampleValue
    };
    if (option.readonly) {
      updatedParameter.$ref = option.ref;
    }
    emitParameterChange(index, updatedParameter);
  }
};

// File upload size tracking
const fileUploadSizes = ref<number[]>([]);

/**
 * Handle file upload change event
 * @param uploadData - Upload data containing file and size
 * @param index - Index of the parameter
 * @param data - Parameter data object
 */
const handleFileUploadChange = (uploadData: { file: any; size: number }, index: number, data: ParamsItem): void => {
  const { file, size } = uploadData;
  fileUploadSizes.value[index] = size;
  if (file) {
    const fileType = Array.isArray(file) ? 'array' : 'string';
    const updatedParameter = {
      ...data,
      [valueKey]: file,
      format: 'binary',
      type: fileType
    };
    if (fileType === 'array') {
      updatedParameter.items = {
        type: 'string',
        format: 'binary'
      };
    } else {
      delete updatedParameter.items;
    }
    emitParameterChange(index, updatedParameter);
  } else {
    const updatedParameter = {
      ...data,
      [valueKey]: undefined,
      format: 'binary',
      type: 'string'
    };
    delete updatedParameter.items;
    emitParameterChange(index, updatedParameter);
  }
};

/**
 * Handle parameter enable/disable checkbox change
 * @param event - Checkbox change event
 * @param index - Index of the parameter
 * @param data - Parameter data object
 */
const handleParameterEnabledChange = (event: any, index: number, data: ParamsItem) => {
  const isEnabled = event.target.checked;
  const updatedParameter = { ...data, [enabledKey]: isEnabled } as ParamsItem;
  emitParameterChange(index, updatedParameter);
  if (!isEnabled && isValidationEnabled.value) {
    jsonContentRefs.value[index] && jsonContentRefs.value[index].validate(false);
  }
};

/**
 * Handle parameter type change
 * @param newType - New parameter type
 * @param index - Index of the parameter
 * @param data - Parameter data object
 */
const handleParameterTypeChange = (newType: string, index: number, data: ParamsItem) => {
  let defaultValue;
  fileUploadSizes.value[index] = 0;
  if (newType !== 'file' && newType !== 'file(array)') {
    delete data.format;
    if (newType.includes('object')) {
      defaultValue = { '': '' };
      newType = 'object';
    }
    if (newType.includes('array')) {
      defaultValue = [''];
      newType = 'array';
    }
    if (newType.includes('xml')) {
      data.format = 'xml';
    }
    if (newType.includes('json')) {
      data.format = 'json';
    }
  } else {
    data.format = 'binary';
    if (newType === 'file(array)') {
      newType = 'array';
      defaultValue = [];
    } else {
      newType = 'string';
      defaultValue = {};
    }
  }
  const updatedParameter = { ...data, [valueKey]: defaultValue, type: newType };
  emitParameterChange(index, updatedParameter);
};

// const setVariableLoading = reactive({});
// // 设为变量
// const setToVariable = async (data:ParamsItem):void => {
//   if (setVariableLoading[data.name as string]) {
//     return;
//   }
//   setVariableLoading[data.name as string] = true;
//   if (!variableNameReg.test(data.name as string)) {
//     notification.warning('名称不符合变量要求,允许数字字母!@$%^&*()_-+=./等');
//     return;
//   }

//   const value = typeof data[valueKey] === 'object' ? JSON.stringify(data[valueKey]) : data[valueKey];
//   await variableApi.addVariables({ name: data.name, targetId: archivedId.value, scope: 'CURRENT', targetType: 'API', enabled: true, value });
//   // const temp = { ...data, [exportVariableKey]: !data[exportVariableKey] } as ParamsItem;
//   // changeEmit(index, temp);
//   setVariableLoading[data.name as string] = false;
// };

/**
 * Handle parameter deletion
 * @param index - Index of the parameter to delete
 */
const handleParameterDeletion = (index: number): void => {
  fileUploadSizes.value.splice(index, 1);
  emit('del', index);
};

/**
 * Emit parameter change event with proper type handling
 * @param index - Index of the parameter
 * @param data - Updated parameter data
 */
const emitParameterChange = (index: number, data: ParamsItem): void => {
  if (props.hasFileType) {
    if (data.format === 'binary') {
      if (data.type === 'file') {
        data.type = 'string';
      }
      if (data.type === 'file(array)') {
        data.type = 'array';
      }
    } else {
      if (data.type === 'object(json)') {
        data.format = 'json';
        data.type = 'object';
      }
      if (data.type === 'object(xml)') {
        data.format = 'xml';
        data.type = 'object';
      }
      if (data.type === 'array(json)') {
        data.format = 'json';
        data.type = 'array';
      }
      if (data.type === 'array(xml)') {
        data.format = 'xml';
        data.type = 'array';
      }
    }
  }
  emit('change', index, data);
};

/**
 * Handle schema change for parameters
 * @param schema - New schema data
 * @param item - Parameter item
 * @param index - Index of the parameter
 */
const handleSchemaChange = (schema: any, item: ParamsItem, index: number) => {
  emit('change', index, { ...item, ...schema });
};

/**
 * Get item schema with proper type handling
 * @param item - Parameter item
 * @returns Schema object with corrected type
 */
const getParameterItemSchema = (item: ParamsItem) => {
  const baseType = item.type.split('(')[0];
  return {
    ...item,
    type: baseType
  };
};

/**
 * Add child item to parameter
 * @param parentItem - Parent parameter item
 * @param index - Index of the parameter
 */
const addChildParameter = (parentItem: ParamsItem, index: number) => {
  jsonContentRefs.value[index].addItem({
    type: parentItem.type.split('(')[0],
    id: -1,
    idLine: [-1],
    level: 0
  });
};

// Watch for prop value changes and update form data
watch(() => props.value, (newValue) => {
  formDataParameters.value = newValue.map((item, index) => {
    let parameterType = item.type;
    if (item.format === 'binary') {
      parameterType = 'file';
      if (item.type === 'array') {
        parameterType = 'file(array)';
      }
    }
    if (props.hasFileType) {
      if (parameterType === 'object') {
        parameterType = 'object(json)';
        if (item.format === 'xml') {
          parameterType = 'object(xml)';
        }
      }
      if (parameterType === 'array' && item.format !== 'binary') {
        parameterType = 'array(json)';
        if (item.format === 'xml') {
          parameterType = 'array(xml)';
        }
      }
    }
    if (!item.type) {
      if (item.properties) {
        parameterType = 'object(json)';
      } else if (item.items) {
        parameterType = 'array(json)';
      } else {
        parameterType = 'string';
      }
    }
    return { ...item, key: item.key || generateFormItemKey(index), type: parameterType };
  });
}, {
  deep: true,
  immediate: true
});

/**
 * Get maximum file size for a specific parameter
 * @param index - Index of the parameter
 * @returns Maximum file size allowed
 */
const getParameterMaxFileSize = (index: number) => {
  const currentSize = fileUploadSizes.value[index] || 0;
  return props.maxFileSize - (props.formFileSize - currentSize);
};

// Watch for file size changes and update form file size
watch(() => fileUploadSizes.value, () => {
  if (!props.hasFileType) {
    return;
  }
  const totalSize = fileUploadSizes.value.reduce((previous, current) => {
    return previous + current;
  }, 0);
  emit('update:formFileSize', totalSize);
}, {
  deep: true
});

// Validation state management
const isValidationEnabled = ref(false);

/**
 * Validate form contents
 * @param shouldValidate - Whether to enable validation
 */
const validateFormContents = async (shouldValidate = true) => {
  isValidationEnabled.value = shouldValidate;
  for (const index in jsonContentRefs.value) {
    if (formDataParameters.value[index][enabledKey]) {
      jsonContentRefs.value[index].validate(shouldValidate);
    }
  }
};

/**
 * Get error state for a parameter item
 * @param item - Parameter item to check
 * @returns True if item has validation errors
 */
const getParameterErrorState = (item: ParamsItem) => {
  if (!isValidationEnabled.value || !item.name || !item[enabledKey]) {
    return false;
  }
  let parameterType = item.type;
  if (parameterType.includes('(')) {
    parameterType = parameterType.split('(')[0];
  }
  const validationErrors = validateType(item[valueKey], deepDelAttrFromObj({ ...item, type: parameterType }, ['name', enabledKey, 'key']));
  return !!validationErrors?.length;
};

/**
 * Get model resolution for form parameters
 * @param models - Models object to populate
 */
const getModelResolution = (models: any) => {
  formDataParameters.value.forEach((item, index) => {
    if (item.$ref) {
      models[item.$ref] = JSON.parse(JSON.stringify(item.schema));
      delete models[item.$ref].schema.$ref;
    }
    jsonContentRefs.value[index] && jsonContentRefs.value[index].getModelResolve(models);
  });
};

/**
 * Update component data
 */
const updateComponentData = async () => {
  for (let i = 0; i < formDataParameters.value.length; i++) {
    if (formDataParameters.value[i].$ref) {
      await http.put(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/schema/${formDataParameters.value[i].name}`, formDataParameters.value[i].schema);
    }
    if (jsonContentRefs.value[i]) {
      await jsonContentRefs.value[i].updateComp();
    }
  }
};

// Expose component methods
defineExpose({
  getModelResolve: getModelResolution,
  updateComp: updateComponentData,
  validate: validateFormContents
});

</script>
<template>
  <div class="space-y-3 relative" :class="{'pre-sign': props.useModel, 'not-button': props.viewType}">
    <div
      v-for="(item,index) in formDataParameters"
      :key="item.key"
      class="space-y-3"
      :class="{'opacity-50': !item[enabledKey]}">
      <div class="flex flex-nowrap items-center mb-3 whitespace-nowrap space-x-2">
        <Checkbox
          :disabled="(!item.name && !item.value) || !!props.useModel || props.viewType"
          :checked="item[enabledKey] && (!!item.name || !!item.value)"
          @change="handleParameterEnabledChange($event, index, item)" />
        <div class="max-w-100 flex flex-col flex-1">
          <SelectSchema
            v-if="apiBaseInfo?.serviceId"
            :id="apiBaseInfo?.serviceId"
            v-model:value="item.name"
            :hideImportBtn="props.hideImportBtn"
            :placeholder="t('xcan_apiBody.enterParameterName')"
            mode="pure"
            :type="['schemas']"
            :inputProps="{readonly: !!item.$ref || !!props.useModel || props.disabled || props.viewType}"
            @blur="handleParameterFieldBlur($event,index,item,'name')"
            @change="(_value, option) => handleModelSelection(_value, option, index, item)" />
          <Input
            v-else
            :placeholder="t('xcan_apiBody.enterParameterName')"
            :value="item.name"
            :allowClear="false"
            :readonly="!!item.$ref || !!props.useModel || props.disabled || props.viewType"
            size="small"
            @blur="handleParameterFieldBlur($event,index,item,'name')"
            @keypress="handleEnterKeyPress" />
        </div>
        <div class="flex flex-col w-25">
          <Select
            v-model:value="item.type"
            class="w-full"
            dropdownClassName="api-select-dropdown"
            :placeholder="t('xcan_apiBody.selectParameterType')"
            :readonly="!!item.$ref || !!props.useModel || props.disabled || props.viewType"
            :options="props.hasFileType ? formDataParameterTypes : basicParameterItemTypes"
            :allowClear="false"
            @change="handleParameterTypeChange($event, index, item)" />
        </div>
        <div v-if="['file(array)', 'file'].includes(item.type)" class="flex flex-col flex-1 min-w-50">
          <!-- <span v-if="index === 0" class="mb-3 text-3 leading-3 text-theme-sub-content select-none">参数值</span> -->
          <div class="min-h-7 border rounded border-border-input file-wrapper flex">
            <ApiUpload
              :key="item.name"
              :value="item[valueKey]"
              :maxFileSize="getParameterMaxFileSize(index)"
              :type="item.type"
              :sizes="fileUploadSizes"
              @change="handleFileUploadChange($event,index,item)" />
          </div>
        </div>
        <div v-else class="flex flex-col flex-1">
          <SimpleEditableSelect
            v-if="item.enum"
            :placeholder="t('xcan_apiBody.enterDebugValue')"
            :readonly="props.viewType"
            :options="item.enum"
            :value="item[valueKey] || item.schema?.[valueKey]"
            @blur="handleParameterValueBlur($event,index,item )"
            @select="emitParameterChange(index, { ...item, [valueKey]: $event })" />
          <param-input
            v-else-if="!['array(json)', 'array(xml)', 'object(xml)', 'object(json)', 'array', 'object', 'file', 'file(array)'].includes(item.type)"
            :placeholder="t('xcan_apiBody.enterDebugValue')"
            :disabled="props.viewType"
            :value="item[valueKey]"
            :error="getParameterErrorState(item)"
            :maxLength="2000"
            @blur="handleParameterValueBlur($event,index,item )" />
          <Input v-else disabled />
        </div>
        <!-- <Button
          v-if="archivedId"
          type="primary"
          size="small"
          :disabled="!!props.useModel || !item.name || setVariableLoading[item.name]"
          @click="setToVariable(item)">
          <Icon icon="icon-bianliang" :title="t('xcan_apiBody.setAsVariable')" />
        </Button> -->
        <template v-if="!props.viewType">
          <Button
            size="small"
            :disabled="!['array(json)', 'array(xml)', 'object(xml)', 'object(json)', 'array', 'object'].includes(item.type) || (['object(xml)', 'object(json), object'].includes(item.type) && item.$ref)"
            @click="addChildParameter(item, index)">
            <Icon icon="icon-jia" />
          </Button>
          <Button
            size="small"
            class="w-7 p-0"
            type="default"
            :disabled="!!props.useModel || (!item[valueKey] && !item.name) || props.viewType"
            @click="handleParameterDeletion(index)">
            <Icon icon="icon-shanchuguanbi" />
          </Button>
        </template>
      </div>
      <JsonContent
        v-if="['array(json)', 'array(xml)', 'object(xml)', 'object(json)', 'array', 'object'].includes(item.type)"
        :ref="dom => jsonContentRefs[index] = dom"
        v-model:data="item[valueKey]"
        :schema="getParameterItemSchema(item)"
        :disabled="!!item.$ref || !!useModel"
        :pType="item.type.split('(')[0]"
        :hideImportBtn="props.hideImportBtn"
        :viewType="props.viewType"
        @change="handleSchemaChange($event, item, index)" />
    </div>
  </div>
</template>
<style scoped>
:deep(.file-wrapper) .ant-upload-list {
  @apply flex flex-wrap;
}

.pre-sign::before {
  content: "";
  display: inline-block;
  position: absolute;
  z-index: 0;
  top: 0;
  left: 22px;
  width: 1px;
  height: 100%;
  border-left: 1px solid #1890ff;
}

.pre-sign::after {
  content: "";
  display: inline-block;
  position: absolute;
  z-index: 0;
  top: 0;
  right: 70px;
  width: 1px;
  height: 100%;
  border-left: 1px solid #1890ff;
}

.not-button::after {
  right: 0;
}
</style>
<style>
.ant-select-dropdown-sm .ant-select-item.ant-select-item-option .ant-select-item-option-content {
  padding-right: 10px;
}
</style>
