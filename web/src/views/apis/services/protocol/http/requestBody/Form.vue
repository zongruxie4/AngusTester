<script setup lang="ts">
import { defineAsyncComponent, inject, reactive, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox } from 'ant-design-vue';
import { Icon, Input, notification, Select, SelectSchema } from '@xcan-angus/vue-ui';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { deconstruct } from '@/utils/swagger';

import { services, variable as variableApi } from '@/api/tester';
import { validateType } from '@/views/apis/services/protocol/http/utils';
import { ParamsItem } from '@/views/apis/services/protocol/types';
import { SchemaType, SchemaFormat } from '@/types/openapi-types';
import { API_PARAMETER_NAME_LENGTH, API_PARAMETER_VALUE_LENGTH } from '@/utils/constant';

import {
  API_EXTENSION_KEY,
  deepDelAttrFromObj,
  getModelDataByRef,
  schemaTypeToOption,
  schemaTypeToWideOption,
  SchemaWideType,
  VARIABLE_NAME_REG
} from '@/utils/apis';

const ApiUpload = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/Upload.vue'));
const JsonContent = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/requestBody/Json.vue'));
const SimpleEditableSelect = defineAsyncComponent(() => import('@/components/form/EditableSelector.vue'));
const ParamInput = defineAsyncComponent(() => import('@/components/form/ParamInput/index.vue'));

// Define event types for better type safety
interface ChangeEvent extends Event {
  target: HTMLInputElement;
  key?: string;
}

interface CheckboxChangeEvent {
  target: {
    checked: boolean;
  };
}

interface Props {
  value: ParamsItem[],
  useModel: boolean;
  hasFileType?: boolean;
  formFileSize: number;
  maxFileSize: number;
}

const props = withDefaults(defineProps<Props>(), {
  useModel: false,
  hasFileType: false
});

const { t } = useI18n();
const { valueKey, enabledKey } = API_EXTENSION_KEY;
const apiBaseInfo = inject('apiBaseInfo', ref());
const archivedId = inject('archivedId', ref());
const projectId = inject<Ref<string>>('projectId', ref(''));

// Reference to JSON content components for validation and updates
const jsonContentRefs = ref<any[]>([]);

const emits = defineEmits<{
  (e: 'change', index:number, data:ParamsItem): void,
  (e: 'del', index:number): void,
  (e: 'update:formFileSize', value:number):void
}>();

// Form data array containing all parameter items
const formData = ref<ParamsItem[]>([]);

/**
 * Generate unique key for form items
 * @param index - Optional index for the key
 * @returns Unique symbol key
 */
const generateUniqueKey = (index?:number):symbol => {
  return Symbol(index);
};

/**
 * Handle Enter key press to blur the input field
 * @param e - Keyboard event
 */
const handleEnterKeyPress = (e: ChangeEvent): void => {
  if (e.key !== 'Enter') {
    return;
  }
  e.target.blur();
};

/**
 * Handle value blur event for parameter values
 * @param target - HTML element that lost focus
 * @param index - Index of the parameter item
 * @param data - Parameter item data
 */
const handleValueBlur = (target: HTMLElement, index:number, data:ParamsItem):void => {
  let value = target?.innerText;

  // Parse numeric and boolean values from string
  if ([SchemaType.integer, SchemaType.number, SchemaType.boolean].includes(data?.type)) {
    try {
      const numericValue = Number(value);
      if (!isNaN(numericValue) && numericValue <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {
      // Keep original value if parsing fails
    }
  }

  const updatedData = { ...data, [valueKey]: value } as ParamsItem;
  emitChange(index, updatedData);
};

/**
 * Handle blur event for input fields
 * @param e - Change event
 * @param index - Index of the parameter item
 * @param data - Parameter item data
 * @param key - Property key to update
 */
const handleInputBlur = (e:ChangeEvent, index:number, data:ParamsItem, key:string):void => {
  const value = e.target.value?.trim();
  if (value === data[key]) {
    return;
  }

  const updatedData = { ...data, [key]: value } as ParamsItem;
  emitChange(index, updatedData);
};
/**
 * Fetch model data by reference
 * @param ref - Model reference
 * @returns Deconstructed model data
 */
const fetchModelData = async (ref) => {
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

/**
 * Handle model selection from schema dropdown
 * @param _value - Selected value
 * @param option - Selected option with model data
 * @param index - Index of the parameter item
 * @param item - Parameter item data
 */
const handleModelSelection = async (_value, option, index, item) => {
  if (option) {
    const model = await fetchModelData(option.ref);
    const sampleValue = SwaggerUI.extension.sampleFromSchemaGeneric(model, { useValue: true });
    const updatedItem = {
      ...item,
      ...model,
      [valueKey]: sampleValue
    };

    // Set reference if option is readonly
    if (option.readonly) {
      updatedItem.$ref = option.ref;
    }
    emitChange(index, updatedItem);
  }
};

// Track file sizes for each parameter item
const fileSizes = ref<number[]>([]);

/**
 * Handle file upload changes
 * @param uploadData - Upload parameter containing file and size
 * @param index - Index of the parameter item
 * @param data - Parameter item data
 */
const handleFileUploadChange = (uploadData: { file?: File | File[] | any; size: number } | undefined, index:number, data:ParamsItem):void => {
  // Handle case when uploadData is undefined
  if (!uploadData) {
    uploadData = { file: undefined, size: 0 };
  }
  const { file, size } = uploadData;
  fileSizes.value[index] = size;

  if (file) {
    const fileType = Array.isArray(file) ? SchemaType.array : SchemaType.string;
    const updatedData = {
      ...data,
      [valueKey]: file,
      format: SchemaFormat.binary,
      type: fileType
    };

    // Set items property for array type files
    if (fileType === SchemaType.array) {
      updatedData.items = {
        type: SchemaType.string,
        format: SchemaFormat.binary
      };
    } else {
      delete updatedData.items;
    }

    emitChange(index, updatedData);
  } else {
    // Clear file data when file is removed
    const clearedData = {
      ...data,
      [valueKey]: undefined,
      format: SchemaFormat.binary,
      type: SchemaType.string
    };
    delete clearedData.items;
    emitChange(index, clearedData);
  }
};

/**
 * Handle checkbox change for enabling/disabling parameter items
 * @param e - Change event
 * @param index - Index of the parameter item
 * @param data - Parameter item data
 */
const handleCheckboxChange = (e:CheckboxChangeEvent, index: number, data:ParamsItem) => {
  const isChecked = e.target.checked;
  const updatedData = { ...data, [enabledKey]: isChecked } as ParamsItem;
  emitChange(index, updatedData);

  // Disable validation if item is unchecked
  if (!isChecked && isValidationEnabled.value) {
    jsonContentRefs.value[index] && jsonContentRefs.value[index].validate(false);
  }
};

/**
 * Handle data type change for parameter items
 * @param newType - New data type
 * @param index - Index of the parameter item
 * @param data - Parameter item data
 */
const handleDataTypeChange = (newType: string, index: number, data:ParamsItem) => {
  fileSizes.value[index] = 0;
  let defaultValue;
  if (newType !== SchemaWideType.file && newType !== SchemaWideType.file_array) {
    delete data.format;

    // Handle object types
    if (newType.includes(SchemaWideType.object)) {
      defaultValue = { '': '' };
      newType = SchemaWideType.object;
    }

    // Handle array types
    if (newType.includes(SchemaWideType.array)) {
      defaultValue = [''];
      newType = SchemaWideType.array;
    }

    // Set format based on type
    if (newType.includes(SchemaWideType.xml) || newType.includes(SchemaWideType.object_xml)) {
      data.format = 'xml';
    }
    if (newType.includes(SchemaWideType.json) || newType.includes(SchemaWideType.object_json)) {
      data.format = 'json';
    }
  } else {
    // Handle file types
    data.format = SchemaFormat.binary;
    if (newType === SchemaWideType.file_array) {
      newType = SchemaWideType.array;
      defaultValue = [];
    } else {
      newType = SchemaWideType.string;
      defaultValue = {};
    }
  }

  const updatedData = { ...data, [valueKey]: defaultValue, type: newType };
  emitChange(index, updatedData);
};

// Track loading state for variable creation
const variableCreationLoading = reactive({});

/**
 * Convert parameter to variable
 * @param data - Parameter item data
 */
const convertToVariable = async (data:ParamsItem): Promise<void> => {
  if (variableCreationLoading[data.name as string]) {
    return;
  }

  variableCreationLoading[data.name as string] = true;

  // Validate variable name format
  if (!VARIABLE_NAME_REG.test(data.name as string)) {
    notification.warning(t('service.apiRequestBody.messages.variableNameInvalid'));
    variableCreationLoading[data.name as string] = false;
    return;
  }

  // Convert value to string if it's an object
  const variableValue = typeof data[valueKey] === 'object' ? JSON.stringify(data[valueKey]) : data[valueKey];

  const [error] = await variableApi.addVariables({
    projectId: projectId.value,
    name: data.name,
    value: variableValue,
    passwordValue: false
  });

  variableCreationLoading[data.name as string] = false;

  if (!error) {
    notification.success(t('service.apiRequestBody.messages.setVariableSuccess'));
  }
};

/**
 * Handle parameter deletion
 * @param index - Index of the parameter to delete
 */
const handleParameterDeletion = (index:number):void => {
  fileSizes.value.splice(index, 1);
  emits('del', index);
};

/**
 * Emit change event with updated parameter data
 * @param index - Index of the parameter
 * @param data - Updated parameter data
 */
const emitChange = (index:number, data:ParamsItem):void => {
  if (props.hasFileType) {
    if (data.format === SchemaFormat.binary) {
      if (data.type === SchemaWideType.file) {
        data.type = SchemaWideType.string;
      }
      if (data.type === SchemaWideType.file_array) {
        data.type = SchemaWideType.array;
      }
    } else {
      if (data.type === SchemaWideType.object_json) {
        data.format = 'json';
        data.type = SchemaWideType.object;
      }
      if (data.type === SchemaWideType.object_xml) {
        data.format = 'xml';
        data.type = SchemaWideType.object;
      }
      if (data.type === SchemaWideType.array_json) {
        data.format = 'json';
        data.type = SchemaWideType.array;
      }
      if (data.type === SchemaWideType.array_xml) {
        data.format = 'xml';
        data.type = SchemaWideType.array;
      }
    }
  }
  emits('change', index, data);
};

/**
 * Handle schema change for parameter items
 * @param schema - Updated schema
 * @param item - Parameter item
 * @param index - Index of the parameter
 */
const handleSchemaChange = (schema, item, index) => {
  emits('change', index, { ...item, ...schema });
};

/**
 * Get clean schema for parameter item
 * @param item - Parameter item
 * @returns Clean schema without type modifiers
 */
const getCleanItemSchema = (item) => {
  const baseType = item.type.split('(')[0];
  return {
    ...item,
    type: baseType
  };
};

/**
 * Add child item to JSON content
 * @param parentItem - Parent parameter item
 * @param index - Index of the parent item
 */
const addChildItem = (parentItem, index) => {
  jsonContentRefs.value[index].addItem({
    type: parentItem.type.split('(')[0],
    id: -1,
    idLine: [-1],
    level: 0
  });
};

// Track validation state
const isValidationEnabled = ref(false);

/**
 * Validate all form contents
 * @param enable - Whether to enable validation
 */
const validateFormContents = async (enable = true) => {
  isValidationEnabled.value = enable;
  for (const idx in jsonContentRefs.value) {
    if (formData.value[idx][enabledKey]) {
      jsonContentRefs.value[idx].validate(enable);
    }
  }
};

/**
 * Check if parameter item has validation errors
 * @param item - Parameter item to validate
 * @returns True if item has validation errors
 */
const hasValidationError = (item) => {
  if (!isValidationEnabled.value || !item.name || !item[enabledKey]) {
    return false;
  }

  let itemType = item.type;
  if (itemType.includes('(')) {
    itemType = itemType.split('(')[0];
  }

  const cleanItem = deepDelAttrFromObj({ ...item, type: itemType }, ['name', enabledKey, 'key']);
  const errors = validateType(item[valueKey], cleanItem);
  return !!errors?.length;
};

/**
 * Resolve model references for all form items
 * @param models - Models object to populate
 */
const resolveModelReferences = (models) => {
  formData.value.forEach((item, index) => {
    if (item.$ref) {
      models[item.$ref] = JSON.parse(JSON.stringify(item.schema));
      delete models[item.$ref].schema.$ref;
    }
    jsonContentRefs.value[index] && jsonContentRefs.value[index].getModelResolve(models);
  });
};

/**
 * Update components with current form data
 */
const updateComponents = async () => {
  for (let i = 0; i < formData.value.length; i++) {
    if (formData.value[i].$ref) {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { $ref, ...content } = formData.value[i];
      await services.addComponent(apiBaseInfo.value.serviceId, 'schema', formData.value[i].name || '', {
        ...content,
        [valueKey]: content[valueKey]
      });
    }
    if (jsonContentRefs.value[i]) {
      await jsonContentRefs.value[i].updateComp();
    }
  }
};

/**
 * Calculate maximum file size for a specific item
 * @param index - Index of the parameter item
 * @returns Maximum allowed file size
 */
const calculateItemMaxFileSize = (index) => {
  const currentItemSize = fileSizes.value[index] || 0;
  return props.maxFileSize - (props.formFileSize - currentItemSize);
};

watch(() => props.value, (newValue) => {
  formData.value = newValue.map((i, idx) => {
    let type = i.type;
    if (i.format === SchemaFormat.binary) {
      type = SchemaWideType.file;
      if (i.type === SchemaType.array) {
        type = SchemaWideType.file_array;
      }
    }
    if (props.hasFileType) {
      if (type === SchemaType.object) {
        type = SchemaWideType.object_json;
        if (i.format === SchemaFormat.xml) {
          type = SchemaWideType.object_xml;
        }
      }
      if (type === SchemaType.array && i.format !== SchemaFormat.binary) {
        type = SchemaWideType.array_json;
        if (i.format === SchemaFormat.xml) {
          type = SchemaWideType.array_xml;
        }
      }
    }
    if (!i.type) {
      if (i.properties) {
        type = SchemaWideType.object_json;
      } else if (i.items) {
        type = SchemaWideType.array_json;
      } else {
        type = SchemaWideType.string;
      }
    }
    return { ...i, key: i.key || generateUniqueKey(idx), type };
  });
}, {
  deep: true,
  immediate: true
});

// Watch file sizes and update total form file size
watch(() => fileSizes.value, () => {
  if (!props.hasFileType) {
    return;
  }
  const totalFileSize = fileSizes.value.reduce((total, current) => {
    return total + current;
  }, 0);
  emits('update:formFileSize', totalFileSize);
}, {
  deep: true
});

// Expose methods to parent component
defineExpose({
  getModelResolve: resolveModelReferences,
  updateComp: updateComponents,
  validate: validateFormContents
});
</script>
<template>
  <div class="space-y-3 relative min-w-220" :class="{'pre-sign': props.useModel}">
    <div
      v-for="(item,index) in formData "
      :key="item.key"
      class="space-y-3"
      :class="{'opacity-50': !item[enabledKey]}">
      <div class="flex flex-nowrap items-center mb-3 whitespace-nowrap space-x-2">
        <Checkbox
          :disabled="(!item.name && !item.value) || props.useModel"
          :checked="item[enabledKey] && (!!item.name || !!item.value)"
          @change="handleCheckboxChange($event, index, item)" />
        <div class="w-100 flex flex-col flex-shrink-0">
          <SelectSchema
            v-if="apiBaseInfo?.serviceId"
            :id="apiBaseInfo?.serviceId"
            v-model:value="item.name"
            :placeholder="t('common.placeholders.searchKeyword')"
            mode="pure"
            :type="['schemas']"
            :maxLength="API_PARAMETER_NAME_LENGTH"
            :disabled="!!item.$ref || props.useModel"
            @blur="handleInputBlur($event,index,item,'name')"
            @change="(_value, option) => handleModelSelection(_value, option, index, item)" />
          <Input
            v-else
            :placeholder="t('common.placeholders.searchKeyword')"
            :value="item.name"
            :allowClear="false"
            :disabled="props.useModel"
            :maxLength="API_PARAMETER_NAME_LENGTH"
            size="small"
            @blur="handleInputBlur($event,index,item,'name')"
            @keypress="handleEnterKeyPress" />
        </div>
        <div class="flex flex-col w-25 flex-shrink-0">
          <Select
            v-model:value="item.type"
            class="w-full"
            dropdownClassName="api-select-dropdown"
            :placeholder="t('service.apiRequestBody.form.typePlaceholder')"
            :disabled="!!item.$ref || props.useModel"
            :options="props.hasFileType ? schemaTypeToWideOption : schemaTypeToOption"
            :allowClear="false"
            @change="handleDataTypeChange($event, index, item)" />
        </div>
        <div v-if="[SchemaWideType.file_array, SchemaWideType.file].includes(item.type)" class="flex flex-col flex-1 min-w-50">
          <div class="min-h-7 border rounded border-border-input file-wrapper flex">
            <ApiUpload
              :key="item.name"
              :value="item[valueKey]"
              :maxFileSize="calculateItemMaxFileSize(index)"
              :type="item.type"
              :sizes="fileSizes"
              @change="(uploadData) => handleFileUploadChange(uploadData,index,item)" />
          </div>
        </div>
        <div v-else class="flex flex-col flex-1">
          <SimpleEditableSelect
            v-if="item.enum"
            :placeholder="t('service.apiRequestBody.form.valuePlaceholder', { maxLength: API_PARAMETER_VALUE_LENGTH })"
            :maxLength="API_PARAMETER_VALUE_LENGTH"
            :options="item.enum"
            :value="item[valueKey] || item.schema?.[valueKey]"
            @blur="handleValueBlur($event,index,item )"
            @select="emitChange(index, { ...item, [valueKey]: $event, schema: {...item?.schema|| {}, [valueKey]: $event} })" />
          <ParamInput
            v-else-if="![SchemaWideType.array_json, SchemaWideType.array_xml, SchemaWideType.object_xml, SchemaWideType.object_json, SchemaWideType.array, SchemaWideType.object, SchemaWideType.file, SchemaWideType.file_array].includes(item.type)"
            :placeholder="t('service.apiRequestBody.form.valuePlaceholder', { maxLength: API_PARAMETER_VALUE_LENGTH })"
            :maxLength="API_PARAMETER_VALUE_LENGTH"
            :value="item[valueKey]"
            :error="hasValidationError(item)"
            @blur="handleValueBlur($event,index,item )" />
          <Input v-else disabled />
        </div>
        <Button
          v-if="archivedId"
          type="primary"
          size="small"
          :disabled="props.useModel || !item.name || variableCreationLoading[item.name]"
          @click="convertToVariable(item)">
          <Icon icon="icon-bianliang" :title="t('service.apiRequestBody.form.setVariable')" />
        </Button>
        <Button
          size="small"
          :disabled="![SchemaWideType.array_json, SchemaWideType.array_xml, SchemaWideType.object_xml, SchemaWideType.object_json, SchemaWideType.array, SchemaWideType.object].includes(item.type) || ([SchemaWideType.object_xml, SchemaWideType.object_json, SchemaWideType.object].includes(item.type) && item.$ref)"
          @click="addChildItem(item, index)">
          <Icon icon="icon-jia" />
        </Button>
        <Button
          size="small"
          class="w-7 p-0"
          type="default"
          :disabled="props.useModel || (!item[valueKey] && !item.name)"
          @click="handleParameterDeletion(index)">
          <Icon icon="icon-shanchuguanbi" />
        </Button>
      </div>
      <JsonContent
        v-if="[SchemaWideType.array_json, SchemaWideType.array_xml, SchemaWideType.object_xml, SchemaWideType.object_json, SchemaWideType.array, SchemaWideType.object].includes(item.type)"
        :ref="dom => jsonContentRefs[index] = dom"
        v-model:data="item[valueKey]"
        :schema="getCleanItemSchema(item)"
        :disabled="!!item.$ref || useModel"
        :pType="item.type.split('(')[0]"
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
  right: 106px;
  width: 1px;
  height: 100%;
  border-left: 1px solid #1890ff;
}
</style>
<style>
.ant-select-dropdown-sm .ant-select-item.ant-select-item-option .ant-select-item-option-content {
  padding-right: 10px;
}
</style>
