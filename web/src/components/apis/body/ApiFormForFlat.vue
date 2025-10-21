<script setup lang="ts">
// Vue core imports
import { watch, ref, inject } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Button, Checkbox } from 'ant-design-vue';
import { Icon, Input, Select, SimpleEditableSelect } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { http, TESTER } from '@xcan-angus/infra';

// Local component imports
import ApiUpload from './Upload.vue';

// Local imports
import { basicFlatParameterItemTypes, flatFormDataParameterTypes } from './interface3';
import { qsJsonToParamList } from '@/utils/apis';

// Utility imports
import { ParamsItem, API_EXTENSION_KEY, deepDelAttrFromObj, validateType } from '@/utils/apis';

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
const flatFormDataParameters = ref<ParamsItem[]>([]);

/**
 * Generate unique key for flat form items
 * @param index - Optional index for the key
 * @returns Unique symbol key
 */
const generateFlatFormItemKey = (index?: number): symbol => {
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
 * Handle value blur event for flat form parameters
 * @param target - HTML element that lost focus
 * @param index - Index of the parameter
 * @param data - Parameter data object
 */
const handleFlatParameterValueBlur = (target: HTMLElement, index: number, data: ParamsItem): void => {
  let value = target?.innerText || (target as any)?.target?.value;
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
 * Handle input blur event for flat form parameters
 * @param event - Blur event
 * @param index - Index of the parameter
 * @param data - Parameter data object
 * @param key - Field key being updated
 */
const handleFlatParameterFieldBlur = (event: Event, index: number, data: ParamsItem, key: string): void => {
  const value = (event.target as HTMLInputElement).value?.trim();
  if (value === data[key]) {
    return;
  }

  const updatedParameter = { ...data, [key]: value } as ParamsItem;
  emitParameterChange(index, updatedParameter);
};

// File upload size tracking
const fileUploadSizes = ref<number[]>([]);

/**
 * Handle file upload change event for flat forms
 * @param uploadData - Upload data containing file and size
 * @param index - Index of the parameter
 * @param data - Parameter data object
 */
const handleFlatFileUploadChange = (uploadData: { file: any; size: number }, index: number, data: ParamsItem): void => {
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
 * Handle parameter enable/disable checkbox change for flat forms
 * @param event - Checkbox change event
 * @param index - Index of the parameter
 * @param data - Parameter data object
 */
const handleFlatParameterEnabledChange = (event: any, index: number, data: ParamsItem) => {
  const isEnabled = event.target.checked;
  const updatedParameter = { ...data, [enabledKey]: isEnabled } as ParamsItem;
  emitParameterChange(index, updatedParameter);
  if (!isEnabled && isValidationEnabled.value) {
    jsonContentRefs.value[index] && jsonContentRefs.value[index].validate(false);
  }
};

/**
 * Handle parameter type change for flat forms
 * @param newType - New parameter type
 * @param index - Index of the parameter
 * @param data - Parameter data object
 */
const handleFlatParameterTypeChange = (newType: string, index: number, data: ParamsItem) => {
  let defaultValue;
  fileUploadSizes.value[index] = 0;
  if (newType !== 'file' && newType !== 'file(array)') {
    delete data.format;
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
 * Handle parameter deletion for flat forms
 * @param index - Index of the parameter to delete
 */
const handleFlatParameterDeletion = (index: number): void => {
  fileUploadSizes.value.splice(index, 1);
  emit('del', index);
};

/**
 * Emit parameter change event with proper type handling for flat forms
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

// Watch for prop value changes and update flat form data
watch(() => props.value, (newValue) => {
  flatFormDataParameters.value = [];
  newValue.forEach((item, index) => {
    let parameterType = item.type;
    if (item.format === 'binary') {
      parameterType = 'file';
      if (item.type === 'array') {
        parameterType = 'file(array)';
      }
      flatFormDataParameters.value.push({ ...item, key: item.key || generateFlatFormItemKey(index), type: parameterType });
      return;
    }
    if (props.hasFileType) {
      if (parameterType === 'object') {
        const jsonSchema = {
          [item.name as string]: item[valueKey]
        };
        if (typeof item[valueKey] === 'object' && JSON.stringify(item[valueKey]) !== '{}') {
          flatFormDataParameters.value.push(...qsJsonToParamList(jsonSchema).map(flatItem => ({ ...flatItem, [enabledKey]: item[enabledKey] })));
        } else {
          flatFormDataParameters.value.push({ ...item, key: item.key || generateFlatFormItemKey(index), type: 'string' });
        }
        return;
      }
      if (parameterType === 'array' && item.format !== 'binary') {
        if (item[valueKey].length) {
          const jsonSchema = {
            [item.name as string]: item[valueKey]
          };
          flatFormDataParameters.value.push(...qsJsonToParamList(jsonSchema).map(flatItem => ({ ...flatItem, [enabledKey]: item[enabledKey] })));
        } else {
          flatFormDataParameters.value.push({ ...item, key: item.key || generateFlatFormItemKey(index), type: 'string' });
        }
        return;
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
    flatFormDataParameters.value.push({ ...item, key: item.key || generateFlatFormItemKey(index), type: parameterType });
  });
}, {
  deep: true,
  immediate: true
});

/**
 * Get maximum file size for a specific flat form parameter
 * @param index - Index of the parameter
 * @returns Maximum file size allowed
 */
const getFlatParameterMaxFileSize = (index: number) => {
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
 * Validate flat form contents
 * @param shouldValidate - Whether to enable validation
 */
const validateFlatFormContents = async (shouldValidate = true) => {
  isValidationEnabled.value = shouldValidate;
  for (const index in jsonContentRefs.value) {
    if (flatFormDataParameters.value[index][enabledKey]) {
      jsonContentRefs.value[index].validate(shouldValidate);
    }
  }
};

/**
 * Get error state for a flat form parameter item
 * @param item - Parameter item to check
 * @returns True if item has validation errors
 */
const getFlatParameterErrorState = (item: ParamsItem) => {
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
 * Get model resolution for flat form parameters
 * @param models - Models object to populate
 */
const getFlatModelResolution = (models: any) => {
  flatFormDataParameters.value.forEach((item, index) => {
    if (item.$ref) {
      models[item.$ref] = JSON.parse(JSON.stringify(item.schema));
      delete models[item.$ref].schema.$ref;
    }
    jsonContentRefs.value[index] && jsonContentRefs.value[index].getModelResolve(models);
  });
};

/**
 * Update flat form component data
 */
const updateFlatComponentData = async () => {
  for (let i = 0; i < flatFormDataParameters.value.length; i++) {
    if (flatFormDataParameters.value[i].$ref) {
      await http.put(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/schema/${flatFormDataParameters.value[i].name}`, flatFormDataParameters.value[i].schema);
    }
    if (jsonContentRefs.value[i]) {
      await jsonContentRefs.value[i].updateComp();
    }
  }
};

// Expose component methods
defineExpose({
  getModelResolve: getFlatModelResolution,
  updateComp: updateFlatComponentData,
  validate: validateFlatFormContents
});

</script>
<template>
  <div class="space-y-3 relative" :class="{'pre-sign': props.useModel, 'not-button': props.viewType}">
    <div
      v-for="(item,index) in flatFormDataParameters"
      :key="item.key"
      class="space-y-3"
      :class="{'opacity-50': !item[enabledKey]}">
      <div class="flex flex-nowrap items-center mb-3 whitespace-nowrap space-x-2">
        <Checkbox
          :disabled="(!item.name && !item.value) || !!props.useModel"
          :checked="item[enabledKey] && (!!item.name || !!item.value)"
          @change="handleFlatParameterEnabledChange($event, index, item)" />
        <div class="max-w-100 flex flex-col flex-1">
          <Input
            :placeholder="t('xcan_apiBody.enterParameterName')"
            :value="item.name"
            :allowClear="false"
            :readonly="!!props.useModel || props.disabled"
            size="small"
            @blur="handleFlatParameterFieldBlur($event,index,item,'name')"
            @keypress="handleEnterKeyPress" />
        </div>
        <div class="flex flex-col w-25">
          <Select
            v-model:value="item.type"
            class="w-full"
            dropdownClassName="api-select-dropdown"
            :placeholder="t('xcan_apiBody.selectParameterType')"
            :readonly="!!item.$ref || !!props.useModel || props.disabled"
            :options="props.hasFileType ? flatFormDataParameterTypes : basicFlatParameterItemTypes"
            :allowClear="false"
            @change="handleFlatParameterTypeChange($event, index, item)" />
        </div>
        <div v-if="['file(array)', 'file'].includes(item.type)" class="flex flex-col flex-1 min-w-50">
          <!-- <span v-if="index === 0" class="mb-3 text-3 leading-3 text-theme-sub-content select-none">参数值</span> -->
          <div class="min-h-7 border rounded border-border-input file-wrapper flex bg-white">
            <ApiUpload
              :key="item.name"
              :value="item[valueKey]"
              :maxFileSize="getFlatParameterMaxFileSize(index)"
              :type="item.type"
              :sizes="fileUploadSizes"
              @change="handleFlatFileUploadChange($event,index,item)" />
          </div>
        </div>
        <div v-else class="flex flex-col flex-1">
          <SimpleEditableSelect
            v-if="item.enum"
            :placeholder="t('xcan_apiBody.enterParameterValue')"
            :options="item.enum"
            :value="item[valueKey] || item.schema?.[valueKey]"
            @blur="handleFlatParameterValueBlur($event,index,item )"
            @select="emitParameterChange(index, { ...item, [valueKey]: $event })" />
          <!-- <param-input
            v-else-if="!['array(json)', 'array(xml)', 'object(xml)', 'object(json)', 'array', 'object', 'file', 'file(array)'].includes(item.type)"
            placeholder="请输入调试值"
            :value="item[valueKey]"
            :error="getFlatParameterErrorState(item)"
            @blur="handleFlatParameterValueBlur($event,index,item )" /> -->
          <Input
            v-else
            v-model:value="item[valueKey]"
            :placeholder="t('xcan_apiBody.enterParameterValue')"
            :maxlength="4096"
            :error="getFlatParameterErrorState(item)"
            @blur="handleFlatParameterValueBlur($event,index,item )" />
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
            class="w-7 p-0"
            type="default"
            :disabled="!!props.useModel || (!item[valueKey] && !item.name)"
            @click="handleFlatParameterDeletion(index)">
            <Icon icon="icon-shanchuguanbi" />
          </Button>
        </template>
      </div>
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
