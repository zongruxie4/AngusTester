<script setup lang="ts">
import { inject, reactive, ref, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, Tooltip } from 'ant-design-vue';
import { Icon, Input, notification, Select, SelectSchema } from '@xcan-angus/vue-ui';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { deconstruct } from '@/utils/swagger';
import { toClipboard } from '@xcan-angus/infra';
import { services } from '@/api/tester';

import { SchemaType } from '@/types/openapi-types';
import { API_EXTENSION_KEY, deepDelAttrFromObj, schemaTypeToOption } from '@/utils/apis';
import { ParamsInfo } from '@/views/apis/services/protocol/http/types';
import { API_PARAMETER_NAME_LENGTH, API_PARAMETER_VALUE_LENGTH } from '@/utils/constant';
import { getDefaultParams, validateType } from './utils';

import JsonContent from '@/views/apis/services/protocol/http/requestBody/Json.vue';
import SimpleEditableSelect from '@/components/apis/editableSelector/index.vue';
const ParamInput = defineAsyncComponent(() => import('@/components/ParamInput/index.vue'));

const { t } = useI18n();

const { valueKey, enabledKey } = API_EXTENSION_KEY;

interface Props {
  value: ParamsInfo[];
}

const props = withDefaults(defineProps<Props>(), {});

const apiBaseInfo = inject('apiBaseInfo', ref());

const emits = defineEmits<{
  (e: 'change', data: ParamsInfo[]): void;
  (e: 'del', index: number): void;
}>();

/**
 * Component state
 * <p>
 * Reactive state containing form data for cookie parameters
 * </p>
 */
const componentState = reactive<{
  formData: ParamsInfo[];
}>({
  formData: []
});

/**
 * JSON content component references
 */
const jsonContentRefs = ref<any[]>([]);
/**
 * Generates a unique key for form items
 * <p>
 * Creates a symbol-based key for tracking form items
 * </p>
 * @param index - Optional index for the key
 * @returns Unique symbol key
 */
const generateUniqueKey = (index?: number): symbol => {
  return Symbol(index);
};

/**
 * Handles Enter key press in input fields
 * <p>
 * Blurs the input field when Enter key is pressed
 * </p>
 * @param event - Keyboard event
 */
const handleEnterKeyPress = (event: KeyboardEvent): void => {
  if (event.key !== 'Enter') {
    return;
  }
  (event.target as HTMLElement).blur();
};

/**
 * Handles parameter value blur event
 * <p>
 * Processes and validates parameter values when input loses focus
 * </p>
 * @param target - HTML element that lost focus
 * @param index - Index of the parameter in the form
 * @param parameterData - Parameter data object
 */
const handleParameterValueBlur = (target: HTMLElement, index: number, parameterData: ParamsInfo): void => {
  let processedValue = target.innerText.trim().replace('\n', '');
  if ([SchemaType.integer, SchemaType.number, SchemaType.boolean].includes(parameterData.schema?.type)) {
    try {
      if (Number(processedValue) <= 9007199254740992) {
        processedValue = JSON.parse(processedValue);
      }
    } catch {}
  }
  if (processedValue === parameterData[valueKey]) {
    return;
  }

  const updatedParameter = { ...parameterData, [valueKey]: processedValue } as ParamsInfo;
  updateParameterData(index, updatedParameter);
};

/**
 * Fetches model data by reference
 * <p>
 * Retrieves component model data from the API service
 * </p>
 * @param reference - Model reference ID
 * @returns Deconstructed model data
 */
const fetchModelDataByReference = async (reference: string) => {
  const [error, { data }] = await services.getComponentRef(apiBaseInfo.value?.serviceId, reference);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

/**
 * Handles model selection
 * <p>
 * Processes selected model and updates parameter data
 * </p>
 * @param _selectedValue - Selected value
 * @param selectedOption - Selected option object
 * @param parameterIndex - Index of the parameter
 */
const handleModelSelection = async (_selectedValue: any, selectedOption: any, parameterIndex: number) => {
  if (selectedOption) {
    const modelData = await fetchModelDataByReference(selectedOption.ref);
    const parameterSchema = modelData.schema
      ? { ...modelData.schema, [valueKey]: modelData[valueKey] || modelData.schema?.[valueKey] }
      : {};
    const sampleValue = SwaggerUI.extension.sampleFromSchemaGeneric(parameterSchema);
    if (selectedOption.readonly) {
      modelData.$ref = selectedOption.ref;
    }
    if (!parameterSchema.type) {
      let valueType: string = typeof sampleValue;
      if (valueType === SchemaType.object) {
        if (Object.prototype.toString.call(sampleValue) === '[object Array]') {
          valueType = SchemaType.array;
        }
      }
      parameterSchema.type = valueType;
    }
    const updatedParameter = { ...modelData, schema: parameterSchema, [enabledKey]: true, [valueKey]: sampleValue };
    updateParameterData(parameterIndex, updatedParameter);
  }
};

/**
 * Handles form field blur event
 * <p>
 * Updates parameter data when form field loses focus
 * </p>
 * @param event - Change event from input field
 * @param index - Index of the parameter
 * @param parameterData - Parameter data object
 * @param fieldKey - Field key to update
 */
const handleFormFieldBlur = (event: ChangeEvent, index: number, parameterData: ParamsInfo, fieldKey: string): void => {
  const fieldValue = (event.target as HTMLInputElement).value?.trim() || '';
  const updatedParameter = { ...parameterData, [fieldKey]: fieldValue } as ParamsInfo;
  updateParameterData(index, updatedParameter);
};

/**
 * Handles checkbox change event
 * <p>
 * Enables or disables parameter based on checkbox state
 * </p>
 * @param event - Change event from checkbox
 * @param index - Index of the parameter
 * @param parameterData - Parameter data object
 */
const handleCheckboxChange = (event: any, index: number, parameterData: ParamsInfo) => {
  const isChecked = event.target.checked;
  const updatedParameter = { ...parameterData, [enabledKey]: isChecked } as ParamsInfo;
  updateParameterData(index, updatedParameter);
  if (!isChecked && isValidationEnabled.value) {
    jsonContentRefs.value[index] && jsonContentRefs.value[index].validate(false);
  }
};

/**
 * Copies parameter value to clipboard
 * <p>
 * Copies the parameter value to the system clipboard
 * </p>
 * @param parameterData - Parameter data object
 */
const copyParameterValue = async (parameterData: ParamsInfo) => {
  let textToCopy = parameterData[valueKey];
  if (typeof textToCopy !== 'string') {
    textToCopy = JSON.stringify(textToCopy);
  }

  toClipboard(textToCopy).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  });
};

/**
 * Emits change event to parent component
 * <p>
 * Sends filtered form data to parent component
 * </p>
 */
const emitChangeToParent = () => {
  emits('change', componentState.formData.filter(item => !!item.name));
};

/**
 * Changes parameter data type
 * <p>
 * Updates parameter schema and value based on selected data type
 * </p>
 * @param dataType - New data type
 * @param index - Index of the parameter
 * @param parameterItem - Parameter item object
 */
const changeParameterDataType = (dataType: string, index: number, parameterItem: ParamsInfo) => {
  const currentSchema = parameterItem.schema || {};
  const updatedParameter = { ...parameterItem, schema: { ...currentSchema, type: dataType } } as any;
  if (dataType === SchemaType.object) {
    updatedParameter.deepObject = true;
    updatedParameter[valueKey] = { '': '' };
  } else {
    updatedParameter[valueKey] = undefined;
    if (dataType === SchemaType.array) {
      updatedParameter[valueKey] = [''];
      updatedParameter.schema.item = {
        type: SchemaType.string
      };
    }
    delete updatedParameter.deepObject;
    delete updatedParameter.explode;
  }
  updateParameterData(index, updatedParameter);
};

/**
 * Handles parameter deletion
 * <p>
 * Removes parameter from the form data array
 * </p>
 * @param index - Index of the parameter to delete
 * @param parameterData - Parameter data object
 */
const handleParameterDeletion = (index: number, parameterData: ParamsInfo): void => {
  const emptyParameters = componentState.formData.filter(item => !item.name);
  // Keep at least one empty parameter
  if (!parameterData.name && emptyParameters.length <= 1) {
    return;
  }
  componentState.formData.splice(index, 1);
  emitChangeToParent();
};

/**
 * Adds child item to JSON content
 * <p>
 * Adds a new child item to the JSON content component
 * </p>
 * @param parentItem - Parent parameter item
 * @param index - Index of the parameter
 */
const addChildParameter = (parentItem: ParamsInfo, index: number) => {
  jsonContentRefs.value[index].addItem({
    type: parentItem.schema.type,
    id: -1,
    idLine: [-1],
    level: 0
  });
};

/**
 * Updates parameter data and emits change
 * <p>
 * Updates parameter data at specified index and emits change event
 * </p>
 * @param index - Index of the parameter
 * @param parameterData - Updated parameter data
 */
const updateParameterData = (index: number, parameterData: ParamsInfo): void => {
  componentState.formData[index] = parameterData;
  if (componentState.formData.every(item => !!item.name)) {
    componentState.formData.push(getDefaultParams({ in: 'cookie' }) as ParamsInfo);
  }
  emitChangeToParent();
};

/**
 * Filters model options
 * <p>
 * Filters out models that are not cookie parameters
 * </p>
 * @param option - Model option to filter
 * @returns True if option should be excluded
 */
const filterModelOptions = (option: any) => {
  const model = option.model && JSON.parse(option.model);
  return !model || model.in !== 'cookie';
};

/**
 * Validation state
 */
const isValidationEnabled = ref(false);

/**
 * Validates form contents
 * <p>
 * Validates all enabled parameters in the form
 * </p>
 * @param enableValidation - Whether to enable validation
 */
const validateFormContents = async (enableValidation = true) => {
  isValidationEnabled.value = enableValidation;
  for (const index in jsonContentRefs.value) {
    if (componentState.formData[index][enabledKey]) {
      jsonContentRefs.value[index].validate(enableValidation);
    }
  }
};

/**
 * Gets error state for parameter
 * <p>
 * Determines if parameter has validation errors
 * </p>
 * @param parameterItem - Parameter item to check
 * @returns True if parameter has errors
 */
const getParameterErrorState = (parameterItem: ParamsInfo) => {
  if (!isValidationEnabled.value || !parameterItem.name || !parameterItem[enabledKey]) {
    return false;
  }
  const validationErrors = validateType(parameterItem[valueKey], deepDelAttrFromObj(parameterItem.schema, []));
  return !!validationErrors?.length;
};

/**
 * Updates parameter schema
 * <p>
 * Updates the schema for a parameter item
 * </p>
 * @param newSchema - New schema object
 * @param parameterItem - Parameter item to update
 * @param index - Index of the parameter
 */
const updateParameterSchema = (newSchema: any, parameterItem: ParamsInfo, index: number) => {
  if (parameterItem.$ref) {
    return;
  }
  const updatedParameter = { ...parameterItem, schema: newSchema };
  updateParameterData(index, updatedParameter);
};

/**
 * Updates component data
 * <p>
 * Updates component data in the API service
 * </p>
 */
const updateComponentData = async () => {
  if (!apiBaseInfo.value?.serviceId) {
    return;
  }
  for (let i = 0; i < componentState.formData.length; i++) {
    if (componentState.formData[i].$ref) {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { $ref, ...content } = componentState.formData[i];
      await services.addComponent(apiBaseInfo.value?.serviceId, 'parameters', componentState.formData[i].name,
        { ...content, schema: { ...(content.schema || {}), [valueKey]: content[valueKey] } });
    }
    if (jsonContentRefs.value[i]) {
      await jsonContentRefs.value[i].updateComp();
    }
  }
};

/**
 * Gets resolved model data
 * <p>
 * Returns resolved model data for all parameters with references
 * </p>
 * @returns Resolved model data object
 */
const getResolvedModelData = () => {
  const resolvedModels = {};
  componentState.formData.forEach((parameterItem, index) => {
    if (parameterItem.$ref) {
      resolvedModels[parameterItem.$ref] = JSON.parse(JSON.stringify(parameterItem));
      delete resolvedModels[parameterItem.$ref].schema.$ref;
      jsonContentRefs.value[index] && jsonContentRefs.value[index].getModelResolve(resolvedModels);
    }
  });
  return resolvedModels;
};

/**
 * Watches for changes in props value
 * <p>
 * Updates form data when props value changes
 * </p>
 */
watch(() => props.value, (newValue) => {
  if (componentState.formData.length && componentState.formData.some(item =>
    !!item.name ||
    (!!item.schema?.type && item.schema?.type !== 'string') ||
    !!item[valueKey]
  )) {
    return;
  }
  componentState.formData = (newValue || []).map((parameterItem, index) => {
    return { ...parameterItem, key: parameterItem.key || generateUniqueKey(index) };
  });
  if (componentState.formData.every(item => !!item.name)) {
    componentState.formData.push(getDefaultParams({ in: 'cookie', key: generateUniqueKey() }) as ParamsInfo);
  }
}, {
  deep: true,
  immediate: true
});

defineExpose({
  updateComp: updateComponentData,
  getModelResolve: getResolvedModelData,
  validate: validateFormContents
});
</script>
<template>
  <div class="space-y-3 min-w-220">
    <div
      v-for="(item, index) in componentState.formData"
      :key="item.key"
      class="space-y-2">
      <div
        class="flex flex-nowrap items-center mb-3 whitespace-nowrap space-x-2"
        :class="{'opacity-50': !item[enabledKey]}">
        <Checkbox
          :disabled="!item.name"
          :checked="item[enabledKey] && !!item.name"
          @change="handleCheckboxChange($event, index, item)" />
        <Tooltip placement="topLeft">
          <div class="flex flex-col w-100 flex-shrink-0" :class="{'border-l border-blue-1': item.$ref}">
            <SelectSchema
              v-if="apiBaseInfo?.serviceId"
              :id="apiBaseInfo?.serviceId"
              v-model:value="item.name"
              :placeholder="t('common.placeholders.searchKeyword')"
              mode="pure"
              :maxLength="API_PARAMETER_NAME_LENGTH"
              :params="{ ignoreModel: false, types: 'parameters'}"
              :type="['parameters']"
              :disabled="item.$ref"
              :excludes="opt => filterModelOptions(opt)"
              @blur="handleFormFieldBlur($event, index, componentState.formData[index], 'name')"
              @change="(value, option) => handleModelSelection(value, option, index)" />
            <Input
              v-else
              :placeholder="t('service.apiRequestCookie.form.inputNamePlaceholder')"
              :value="item.name"
              :allowClear="false"
              :maxLength="API_PARAMETER_NAME_LENGTH"
              size="small"
              @blur="handleFormFieldBlur($event, index, item, 'name')"
              @keypress="handleEnterKeyPress" />
          </div>
          <template v-if="item.$ref" #title>
            {{ t('service.apiRequestCookie.tips.componentReference', { ref: item.$ref }) }}
          </template>
        </Tooltip>
        <Select
          v-model:value="item.schema.type"
          :options="schemaTypeToOption"
          class="w-25 flex-shrink-0"
          @change="changeParameterDataType($event, index, item)" />
        <div class="flex flex-col flex-1 flex-shrink-0">
          <SimpleEditableSelect
            v-if="item.schema?.enum"
            :placeholder="t('service.apiRequestCookie.form.valuePlaceholder', { maxLength: API_PARAMETER_VALUE_LENGTH })"
            :maxLength="API_PARAMETER_VALUE_LENGTH"
            :options="item.schema.enum || item.schema?.[valueKey]"
            :value="item[valueKey]"
            @blur="handleParameterValueBlur($event,index,item )"
            @select="updateParameterData(index, { ...item, [valueKey]: $event, schema: {...item?.schema|| {}, [valueKey]: $event} })" />
          <ParamInput
            v-else-if="!['array', 'object'].includes(item.schema.type)"
            :placeholder="t('service.apiRequestCookie.form.valuePlaceholder', { maxLength: API_PARAMETER_VALUE_LENGTH })"
            :maxLength="0"
            :value="item[valueKey]"
            :error="getParameterErrorState(item)"
            @blur="handleParameterValueBlur($event, index, item)" />
          <Input v-else disabled />
        </div>
        <Button
          type="primary"
          size="small"
          :title="t('service.apiRequestCookie.actions.copyValue')"
          class="ml-2"
          @click="copyParameterValue(item)">
          <Icon icon="icon-fuzhi" />
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
          :disabled="!item.name && !item[valueKey]"
          @click="handleParameterDeletion(index, item)">
          <Icon icon="icon-shanchuguanbi" />
        </Button>
      </div>
      <JsonContent
        v-if="item.schema?.type === 'array' || item.schema?.type === 'object'"
        :ref="dom => jsonContentRefs[index] = dom"
        v-model:data="item[valueKey]"
        :schema="item.schema || {}"
        :disabled="!!item.$ref"
        :pType="item.schema?.type"
        @change="updateParameterSchema($event, item, index)" />
    </div>
  </div>
</template>
