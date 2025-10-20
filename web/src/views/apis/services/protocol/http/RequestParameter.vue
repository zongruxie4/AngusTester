<script setup lang="ts">
import { inject, reactive, ref, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, Tooltip } from 'ant-design-vue';
import { Icon, Input, Select, SelectSchema } from '@xcan-angus/vue-ui';
import { SchemaType } from '@/types/openapi-types';

import { getDefaultParams } from './utils';
import { API_PARAMETER_NAME_LENGTH, API_PARAMETER_VALUE_LENGTH } from '@/utils/constant';
import { ParamsInfo } from '@/views/apis/services/protocol/http/types';
import { ParameterUtils, type ParameterComponentProps, type ParameterComponentEmits } from './RequestParameter';

import {
  getUriByParams,
  getParamsByUri,
  QueryAndPathInOption, schemaTypeToOption
} from '@/utils/apis';

import JsonContent from '@/views/apis/services/protocol/http/requestBody/Json.vue';
import SimpleEditableSelect from '@/components/form/EditableSelector.vue';
const ParamInput = defineAsyncComponent(() => import('@/components/form/ParamInput/index.vue'));

interface Props extends ParameterComponentProps {
  apiUri?: string
}

const props = withDefaults(defineProps<Props>(), {
  apiUri: ''
});

const { t } = useI18n();

const apiBaseInfo = inject('apiBaseInfo', ref());

const emits = defineEmits<ParameterComponentEmits & {
  (e: 'update:apiUri', value: string)
}>();

/**
 * Component state
 * <p>
 * Reactive state containing form data for request parameters
 * </p>
 */
const componentState = reactive<{
  formData: ParamsInfo[]
}>({
  formData: []
});

/**
 * JSON content component references
 */
const jsonContentRefs = ref<any[]>([]);

/**
 * Request parameter manager instance
 */
const parameterManager = ParameterUtils.createManager(componentState, jsonContentRefs.value, apiBaseInfo);

/**
 * Emits change event to parent component
 * <p>
 * Sends form data to parent component
 * </p>
 */
const emitChangeToParent = () => {
  emits('change', componentState.formData);
};

// Use parameter manager methods
const handleEnterKeyPress = parameterManager.handleEnterKeyPress.bind(parameterManager);
const handleParameterValueBlur = parameterManager.handleParameterValueBlur.bind(parameterManager);
const handleModelSelection = parameterManager.handleModelSelection.bind(parameterManager);

// Use parameter manager methods
const changeParameterDataType = parameterManager.changeParameterDataType.bind(parameterManager);
const handleFormFieldBlur = parameterManager.handleFormFieldBlur.bind(parameterManager);
const handleCheckboxChange = parameterManager.handleCheckboxChange.bind(parameterManager);
const copyParameterValue = parameterManager.copyParameterValue.bind(parameterManager);

/**
 * Handles select change event
 * <p>
 * Updates parameter data when select value changes
 * </p>
 * @param value - Selected value
 * @param index - Index of the parameter
 * @param parameterData - Parameter data object
 * @param fieldKey - Field key to update
 */
const handleSelectChange = (value: string, index: number, parameterData: ParamsInfo, fieldKey: string): void => {
  const updatedParameter = { ...parameterData, [fieldKey]: value } as ParamsInfo;
  parameterManager.updateParameterData(index, updatedParameter);
  emitChangeToParent();
  updateApiUriByParameters();
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
  parameterManager.updateParameterSchema(newSchema, parameterItem, index);
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
  parameterManager.handleParameterDeletion(index, parameterData, emitChangeToParent);
  if (parameterData.in === 'path') {
    updateApiUriByParameters();
  }
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
  parameterManager.updateParameterData(index, parameterData);
  emitChangeToParent();
  updateApiUriByParameters();
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
  parameterManager.addChildParameter(parentItem, index);
};

/**
 * Updates API URI based on parameters
 * <p>
 * Updates the API URI when path parameters change
 * </p>
 */
const updateApiUriByParameters = () => {
  const pathParameters = componentState.formData.filter(item =>
    item?.[parameterManager.enabledKey] && item.name && item.in === 'path'
  );
  let currentApiUri = props.apiUri;
  if (pathParameters.length) {
    if (!currentApiUri) {
      currentApiUri = '/';
    }
  }
  currentApiUri = getUriByParams(currentApiUri || '', pathParameters);
  emits('update:apiUri', currentApiUri);
};

/**
 * Sets parameter list from API URI
 * <p>
 * Extracts parameters from API URI and updates form data
 * </p>
 */
const setParameterListFromUri = (): void => {
  const uriParameters = getParamsByUri(props.apiUri || ''); // Extract existing params from URI
  const indicesToRemove: number[] = []; // Store indices of parameters to remove

  // Get all path parameters from URI
  const pathParameters = uriParameters.filter(param => param.in === 'path');

  // Forward comparison: find missing parameters in formData and add them
  pathParameters?.forEach((currentParameter) => {
    const existingItem = componentState.formData.find(item => item.name === currentParameter.name);
    if (!existingItem) {
      componentState.formData.splice(-1, 0, {
        ...currentParameter,
        [parameterManager.enabledKey]: true
      } as any);
    } else {
      existingItem[parameterManager.enabledKey] = true;
    }
  });

  // Reverse comparison: find extra parameters in formData and remove them
  componentState.formData?.forEach((currentParameter, index) => {
    if (currentParameter.in === 'path') {
      const uriItem = pathParameters.find(param => param.name === currentParameter.name);
      if (!uriItem && currentParameter[parameterManager.enabledKey]) {
        indicesToRemove.push(index);
      }
    }
  });

  componentState.formData = componentState.formData.filter((_item, index) => !indicesToRemove.includes(index));
  emitChangeToParent();
};

// Use parameter manager methods
const validateFormContents = parameterManager.validateFormContents.bind(parameterManager);
const getParameterErrorState = parameterManager.getParameterErrorState.bind(parameterManager);
const updateComponentData = parameterManager.updateComponentData.bind(parameterManager);
const getResolvedModelData = parameterManager.getResolvedModelData.bind(parameterManager);

/**
 * Filters data model options
 * <p>
 * Filters out models that are not query or path parameters
 * </p>
 * @param option - Model option to filter
 * @returns True if option should be excluded
 */
const filterDataModelOptions = (option: any) => {
  return ParameterUtils.filterModelOptions(option, ['query', 'path']);
};

/**
 * Adds parameters to the form
 * <p>
 * Adds new parameters to the form data array
 * </p>
 * @param parameterData - Array of parameters to add
 */
const addParameters = (parameterData: any[]) => {
  componentState.formData.splice(-1, 0, ...parameterData);
};

/**
 * Watches for changes in props value
 * <p>
 * Updates form data when props value changes
 * </p>
 */
watch(() => props.value, (newValue) => {
  if (componentState.formData.filter(item =>
    !!item.name ||
    item[parameterManager.valueKey] ||
    item.schema?.type !== SchemaType.string ||
    item.in !== 'query'
  ).length > 0) {
    return;
  }
  componentState.formData = newValue.map((parameterItem) => {
    return { ...parameterItem, key: parameterItem.key || parameterManager.generateUniqueKey() };
  });
}, {
  deep: true,
  immediate: true
});

/**
 * Watches for changes in form data
 * <p>
 * Adds default parameter when all parameters are filled
 * </p>
 */
watch(() => componentState.formData, () => {
  if (componentState.formData.every(item => !!item.name || !!item[parameterManager.valueKey])) {
    componentState.formData.push(getDefaultParams({ in: 'query', key: parameterManager.generateUniqueKey() }) as any);
  }
}, {
  deep: true,
  immediate: true
});

/**
 * Watches for changes in API URI
 * <p>
 * Synchronizes parameters when API URI changes
 * </p>
 */
watch(() => props.apiUri, () => {
  setTimeout(() => {
    setParameterListFromUri();
  });
});

defineExpose({
  validate: validateFormContents,
  updateComp: updateComponentData,
  getModelResolve: getResolvedModelData,
  addParam: addParameters
});
</script>
<template>
  <div class="space-y-3 min-w-220">
    <div
      v-for="(item, index) in componentState.formData"
      :key="item.key"
      class="space-y-2">
      <div
        class="flex flex-nowrap whitespace-nowrap items-center space-x-2"
        :class="{'opacity-50': !item[parameterManager.enabledKey]}">
        <Checkbox
          :disabled="!item.name && !item[parameterManager.valueKey]"
          :checked="item[parameterManager.enabledKey] && (!!item.name || !!item[parameterManager.valueKey]) "
          @change="handleCheckboxChange($event, index, item)" />
        <Tooltip placement="topLeft">
          <div class="flex flex-col w-100 flex-shrink-0">
            <SelectSchema
              v-if="apiBaseInfo?.serviceId"
              :id="apiBaseInfo?.serviceId"
              v-model:value="item.name"
              :disabled="item.$ref"
              :placeholder="t('common.placeholders.searchKeyword')"
              mode="pure"
              :maxLength="API_PARAMETER_NAME_LENGTH"
              :type="['parameters']"
              :params="{types: 'parameters', ignoreModel: false}"
              :excludes="opt => filterDataModelOptions(opt)"
              @blur="handleFormFieldBlur($event, index, componentState.formData[index], 'name')"
              @change="(_value, option) => handleModelSelection(_value, option, index)" />
            <Input
              v-else
              v-model:value="item.name"
              :placeholder="t('service.apiRequestParams.form.inputNamePlaceholder')"
              :maxLength="API_PARAMETER_NAME_LENGTH"
              :allowClear="false"
              @blur="handleFormFieldBlur($event, index, item, 'name')"
              @keypress="handleEnterKeyPress" />
          </div>
          <template v-if="item.$ref" #title>
            {{ t('service.apiRequestParams.tips.componentReference', { ref: item.$ref }) }}
          </template>
        </Tooltip>
        <Select
          v-model:value="item.in"
          dropdownClassName="api-select-dropdown"
          :placeholder="t('service.apiRequestParams.form.typePlaceholder')"
          :disabled="item.$ref"
          :allowClear="false"
          :options="QueryAndPathInOption"
          class="w-25 flex-shrink-0"
          @change="handleSelectChange($event, index, item, 'in')" />
        <Select
          v-model:value="item.schema.type"
          class="w-25 flex-shrink-0"
          :disabled="item.$ref"
          :options="schemaTypeToOption"
          @change="changeParameterDataType($event, index, item)" />
        <div class="flex flex-col flex-25 ml-3 space-y-0.5">
          <SimpleEditableSelect
            v-if="item.schema?.enum"
            :placeholder="t('service.apiRequestParams.form.valuePlaceholder', { maxLength: API_PARAMETER_VALUE_LENGTH })"
            :maxLength="0"
            :options="item.schema.enum"
            :value="item[parameterManager.valueKey] || item.schema?.[parameterManager.valueKey]"
            @blur="handleParameterValueBlur($event,index,item)"
            @select="updateParameterData(index, { ...item, [parameterManager.valueKey]: $event,schema: {...item?.schema|| {}, [parameterManager.valueKey]: $event} } )" />
          <ParamInput
            v-else-if="item.schema?.type !== SchemaType.array && item.schema?.type !== SchemaType.object"
            :placeholder="t('service.apiRequestParams.form.valuePlaceholder', { maxLength: API_PARAMETER_VALUE_LENGTH })"
            :maxLength="0"
            :value="item[parameterManager.valueKey]"
            :error="getParameterErrorState(item)"
            @blur="handleParameterValueBlur($event, index, item)" />
          <Input v-else disabled />
        </div>
        <Button
          type="primary"
          size="small"
          :title="t('service.apiRequestParams.actions.copyValue')"
          class="ml-2"
          @click="copyParameterValue(item)">
          <Icon icon="icon-fuzhi" />
        </Button>
        <Button
          size="small"
          class="ml-2"
          :disabled="![SchemaType.array, SchemaType.object].includes(item.schema?.type) || (item.schema?.type === SchemaType.object && item.$ref)"
          @click="addChildParameter(item, index)">
          <Icon icon="icon-jia" />
        </Button>
        <Button
          size="small"
          class="ml-2"
          :disabled="!item.name && !item[parameterManager.valueKey]"
          @click="handleParameterDeletion(index, item)">
          <Icon icon="icon-shanchuguanbi" />
        </Button>
      </div>
      <JsonContent
        v-if="item.schema?.type === SchemaType.array || item.schema?.type === SchemaType.object"
        :ref="dom => jsonContentRefs[index] = dom"
        v-model:data="item[parameterManager.valueKey]"
        :schema="item.schema || {}"
        :disabled="!!item.$ref"
        :pType="item.schema?.type"
        :paramInType="item.in"
        @change="updateParameterSchema($event, item, index)" />
    </div>
  </div>
</template>
<style>
.api-select-dropdown .ant-select-item {
  @apply text-3 leading-7 py-0 min-h-full;
}
</style>
