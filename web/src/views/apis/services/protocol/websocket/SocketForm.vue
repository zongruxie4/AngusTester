<script lang="ts" setup>
import { inject, ref, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox } from 'ant-design-vue';
import { Icon, Input, Select, SelectSchema } from '@xcan-angus/vue-ui';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { SchemaType } from '@/types/openapi-types';
import { API_EXTENSION_KEY, schemaTypeToOption } from '@/utils/apis';
import { deconstruct } from '@/utils/swagger';
import { services } from '@/api/tester';
import { FormData, getDefaultForm } from './types';

import JsonContent from '@/views/apis/services/protocol/http/requestBody/Json.vue';
const ParamInput = defineAsyncComponent(() => import('@/components/form/ParamInput.vue'));

const { valueKey, enabledKey } = API_EXTENSION_KEY;

interface Props {
  data: FormData[];
  in: 'query' | 'header';
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  data: () => ([]),
  in: 'query'
});

const emits = defineEmits<{(e: 'change', value: FormData[])}>();

const apiBaseInfo = inject('apiBaseInfo', ref());
const parameterFormData = ref<FormData[]>([]);
const jsonContentRefs = ref<any[]>([]);

/**
 * Emits filtered form data to parent component
 * <p>
 * Filters out empty entries and adds a new empty form if all current forms have names
 * </p>
 */
const emitFormDataChanges = () => {
  const filteredData = parameterFormData.value.filter(data =>
    data.name || data[valueKey] || data.description
  );
  emits('change', filteredData);

  // Add new empty form if all current forms have names
  if (parameterFormData.value.every(item => !!item.name)) {
    parameterFormData.value.push(getDefaultForm({ in: props.in }));
  }
};

/**
 * Handles form field blur event
 * <p>
 * Triggers form data emission when user leaves a field
 * </p>
 */
const handleFormFieldBlur = () => {
  emitFormDataChanges();
};

/**
 * Handles parameter value blur event with type conversion
 * <p>
 * Processes the input value based on the parameter's schema type and updates the form data
 * </p>
 * @param target - The HTML element that triggered the blur event
 * @param index - The index of the parameter in the form data array
 * @param parameterData - The parameter data object
 */
const handleParameterValueBlur = (target: HTMLElement, index: number, parameterData: FormData): void => {
  let processedValue: any = target?.innerText?.trim();

  // Convert value based on schema type
  if ([SchemaType.integer, SchemaType.number, SchemaType.boolean].includes(parameterData.schema?.type)) {
    try {
      const numericValue = Number(processedValue);
      if (!isNaN(numericValue) && numericValue <= 9007199254740992) {
        processedValue = JSON.parse(processedValue);
      }
    } catch (error) {
      console.warn('Failed to parse parameter value:', error);
    }
  }

  parameterFormData.value[index] = { ...parameterData, [valueKey]: processedValue };
  emitFormDataChanges();
};

/**
 * Changes the data type of a parameter
 * <p>
 * Updates the parameter's schema type and resets the value based on the new type
 * </p>
 * @param newType - The new data type for the parameter
 * @param index - The index of the parameter in the form data array
 * @param parameterItem - The parameter item to update
 */
const changeParameterDataType = (newType: string, index: number, parameterItem: FormData) => {
  const currentSchema = parameterItem.schema || {};
  const updatedParameter: any = {
    ...parameterItem,
    schema: { ...currentSchema, type: newType }
  };

  if (newType === SchemaType.object) {
    updatedParameter.deepObject = true;
    updatedParameter[valueKey] = { '': '' };
  } else {
    if (newType === SchemaType.array) {
      updatedParameter[valueKey] = [''];
    } else {
      updatedParameter[valueKey] = undefined;
    }
    delete updatedParameter.deepObject;
    delete updatedParameter.explode;
  }

  parameterFormData.value[index] = updatedParameter;
  const filteredResult = parameterFormData.value.filter(data => data.name || data[valueKey]);
  emits('change', filteredResult);
};

/**
 * Handles parameter deletion
 * <p>
 * Removes a parameter from the form data, preventing deletion if it's the only empty parameter
 * </p>
 * @param index - The index of the parameter to delete
 * @param parameterData - The parameter data to delete
 */
const handleParameterDeletion = (index: number, parameterData: FormData) => {
  const emptyParameters = parameterFormData.value.filter(item => !item.name);
  if (!parameterData.name && emptyParameters.length <= 1) {
    return;
  }

  parameterFormData.value.splice(index, 1);
  const filteredResult = parameterFormData.value.filter(data => data.name || data[valueKey]);
  emits('change', filteredResult);
};

/**
 * Retrieves model data from API
 * <p>
 * Fetches component reference data and deconstructs it for use in the form
 * </p>
 * @param componentRef - The component reference to fetch
 * @returns The deconstructed model data
 */
const getModelDataFromAPI = async (componentRef: string) => {
  const [error, { data }] = await services.getComponentRef(apiBaseInfo.value.projectId, componentRef);
  if (error) {
    console.warn('Failed to fetch model data:', error);
    return {};
  }
  return deconstruct(data || {});
};

/**
 * Handles model selection from schema dropdown
 * <p>
 * Updates the parameter with selected model data or simple name input
 * </p>
 * @param inputValue - The input value (name or model reference)
 * @param selectedOption - The selected option from dropdown
 * @param index - The index of the parameter being updated
 */
const handleModelSelection = async (inputValue: string, selectedOption: any, index: number) => {
  if (selectedOption) {
    const modelData = await getModelDataFromAPI(selectedOption.ref);
    const schema = modelData.schema || {};
    const sampleValue = SwaggerUI.extension.sampleFromSchemaGeneric(schema);

    if (selectedOption.readonly) {
      modelData.$ref = selectedOption.ref;
    }

    const updatedParameter = {
      ...modelData,
      schema,
      [enabledKey]: true,
      [valueKey]: sampleValue
    };
    parameterFormData.value[index] = updatedParameter;
    emitFormDataChanges();
  } else {
    const updatedParameter = { ...parameterFormData.value[index], name: inputValue };
    parameterFormData.value[index] = updatedParameter;
    emitFormDataChanges();
  }
};

/**
 * Adds a new parameter item to the form
 * <p>
 * Inserts a new parameter at the beginning of the form data array
 * </p>
 * @param parameterItem - The parameter item to add
 */
const addParameterItem = (parameterItem: FormData) => {
  parameterFormData.value.unshift({
    ...parameterItem,
    [enabledKey]: true,
    in: props.in
  });
  emitFormDataChanges();
};

/**
 * Handles checkbox state changes
 * <p>
 * Updates the checked state of a parameter
 * </p>
 * @param event - The checkbox change event
 * @param parameterData - The parameter data to update
 */
const handleCheckboxChange = (event: any, parameterData: FormData) => {
  const isChecked = event.target.checked;
  parameterData.checked = isChecked;
};

/**
 * Adds a child item to a complex parameter
 * <p>
 * Adds a new child item to array or object type parameters
 * </p>
 * @param parentItem - The parent parameter item
 * @param index - The index of the parent parameter
 */
const addChildParameter = (parentItem: FormData, index: number) => {
  jsonContentRefs.value[index].addItem({
    type: parentItem.schema.type,
    id: -1,
    idLine: [-1],
    level: 0
  });
};

/**
 * Updates parameter schema
 * <p>
 * Updates the schema of a parameter and emits the changes
 * </p>
 * @param newSchema - The new schema to apply
 * @param parameterItem - The parameter item to update
 * @param index - The index of the parameter
 */
const updateParameterSchema = (newSchema: any, parameterItem: FormData, index: number) => {
  parameterFormData.value[index] = { ...parameterItem, schema: newSchema };
  emitFormDataChanges();
};

/**
 * Filters data models based on parameter type
 * <p>
 * Determines which models should be excluded based on the current parameter context
 * </p>
 * @param option - The model option to filter
 * @returns True if the model should be excluded, false otherwise
 */
const filterDataModel = (option: any) => {
  const model = JSON.parse(option.model);
  if (props.in === 'query') {
    return !(['query'].includes(model.in));
  }
  if (props.in === 'header') {
    return !(['header'].includes(model.in));
  }
  return false;
};

/**
 * Gets resolved model references
 * <p>
 * Collects all model references from form data and JSON content components
 * </p>
 * @returns Object containing resolved model references
 */
const getResolvedModelReferences = () => {
  const resolvedModels = {};
  parameterFormData.value.forEach((parameter, index) => {
    if (parameter.$ref) {
      resolvedModels[parameter.$ref] = parameter;
    }
    jsonContentRefs.value[index] && jsonContentRefs.value[index].getModelResolve(resolvedModels);
  });
  return resolvedModels;
};

/**
 * Watches for changes in props data
 * <p>
 * Initializes form data when props change, ensuring at least one empty form exists
 * </p>
 */
watch(() => props.data, newValue => {
  if (parameterFormData.value.length < 2) {
    parameterFormData.value = newValue;
    if (parameterFormData.value.every(item => !!item.name)) {
      parameterFormData.value.push(getDefaultForm({ in: props.in }));
    }
  }
}, {
  immediate: true
});

/**
 * Exposes methods for parent component access
 */
defineExpose({
  getModelResolve: getResolvedModelReferences,
  addItem: addParameterItem
});
</script>

<template>
  <ul class="min-h-30">
    <li
      v-for="(parameter, index) in parameterFormData"
      :key="index"
      class="space-y-2 mb-2"
      :class="{'opacity-50': !parameter[enabledKey]}">
      <div class="flex items-center space-x-2">
        <Checkbox
          :checked="!!parameter[enabledKey]"
          @change="handleCheckboxChange($event, parameter)">
        </Checkbox>
        <div class="w-100">
          <SelectSchema
            v-if="apiBaseInfo.projectId"
            :id="apiBaseInfo.projectId"
            v-model:value="parameter.name"
            :disabled="!!parameter.$ref"
            :placeholder="t('service.webSocketForm.placeholder.parameterName')"
            mode="pure"
            :type="['parameters']"
            :params="{types: 'parameters', ignoreModel: false}"
            :excludes="opt => filterDataModel(opt)"
            @blur="handleFormFieldBlur"
            @change="(value, option) => handleModelSelection(value, option, index)" />
          <Input
            v-else
            v-model:value="parameter.name"
            size="small"
            :placeholder="t('service.webSocketForm.placeholder.inputParameterName')"
            class="flex-1"
            dataType="mixin-en"
            @blur="handleFormFieldBlur" />
        </div>
        <Select
          v-model:value="parameter.schema.type"
          class="w-25"
          :disabled="!!parameter.$ref"
          :options="schemaTypeToOption"
          @change="changeParameterDataType($event, index, parameter)" />
        <ParamInput
          v-if="![SchemaType.array, SchemaType.object].includes(parameter.schema.type)"
          :value="String(parameter[valueKey] || '')"
          size="small"
          class="flex-1 min-w-15"
          :placeholder="t('service.webSocketForm.placeholder.inputParameterValue')"
          :maxLength="0"
          @blur="handleParameterValueBlur($event, index, parameter)" />
        <Input
          v-else
          disabled
          class="flex-1"
          size="small" />
        <Button
          v-show="parameter.schema?.type === SchemaType.array || parameter.schema?.type === SchemaType.object"
          size="small"
          @click="addChildParameter(parameter, index)">
          <Icon icon="icon-jia" />
        </Button>
        <Button
          size="small"
          @click="handleParameterDeletion(index, parameter)">
          <Icon icon="icon-shanchuguanbi" />
        </Button>
      </div>
      <JsonContent
        v-if="parameter.schema?.type === SchemaType.array || parameter.schema?.type === SchemaType.object"
        :ref="dom => jsonContentRefs[index] = dom"
        v-model:data="parameter[valueKey]"
        :schema="parameter.schema || {}"
        :disabled="!!parameter.$ref"
        :pType="parameter.schema?.type"
        @change="updateParameterSchema($event, parameter, index)" />
    </li>
  </ul>
</template>
