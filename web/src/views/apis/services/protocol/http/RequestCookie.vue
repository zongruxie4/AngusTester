<script setup lang="ts">
import { inject, reactive, ref, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, Tooltip } from 'ant-design-vue';
import { Icon, Input, Select, SelectSchema } from '@xcan-angus/vue-ui';
import { SchemaType } from '@/types/openapi-types';

import { schemaTypeToOption } from '@/utils/apis';
import { ParamsInfo } from '@/views/apis/services/protocol/http/types';
import { API_PARAMETER_NAME_LENGTH, API_PARAMETER_VALUE_LENGTH } from '@/utils/constant';
import { getDefaultParams } from './utils';
import { ParameterUtils, type ParameterComponentProps, type ParameterComponentEmits } from './RequestParameter';

import JsonContent from '@/views/apis/services/protocol/http/requestBody/Json.vue';
import SimpleEditableSelect from '@/components/form/EditableSelector.vue';
const ParamInput = defineAsyncComponent(() => import('@/components/form/ParamInput.vue'));

const { t } = useI18n();

const props = withDefaults(defineProps<ParameterComponentProps>(), {});

const apiBaseInfo = inject('apiBaseInfo', ref());

const emits = defineEmits<ParameterComponentEmits>();

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
 * Request parameter manager instance
 */
const parameterManager = ParameterUtils.createManager(componentState, jsonContentRefs.value, apiBaseInfo);

// Use parameter manager methods
const handleEnterKeyPress = parameterManager.handleEnterKeyPress.bind(parameterManager);
const handleParameterValueBlur = parameterManager.handleParameterValueBlur.bind(parameterManager);
const handleModelSelection = parameterManager.handleModelSelection.bind(parameterManager);
const handleFormFieldBlur = parameterManager.handleFormFieldBlur.bind(parameterManager);
const handleCheckboxChange = parameterManager.handleCheckboxChange.bind(parameterManager);
const copyParameterValue = parameterManager.copyParameterValue.bind(parameterManager);

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
 * Adds default parameter when all parameters are filled
 */
const addDefaultParameter = () => {
  componentState.formData.push(getDefaultParams({ in: 'cookie' }) as ParamsInfo);
};

// Use parameter manager methods
const changeParameterDataType = parameterManager.changeParameterDataType.bind(parameterManager);
const handleParameterDeletion = (index: number, parameterData: ParamsInfo) =>
  parameterManager.handleParameterDeletion(index, parameterData, emitChangeToParent);
const addChildParameter = parameterManager.addChildParameter.bind(parameterManager);
const updateParameterData = (index: number, parameterData: ParamsInfo) => {
  parameterManager.updateParameterData(index, parameterData);
  if (componentState.formData.every(item => !!item.name)) {
    addDefaultParameter();
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
  return ParameterUtils.filterModelOptions(option, ['cookie']);
};

// Use parameter manager methods
const validateFormContents = parameterManager.validateFormContents.bind(parameterManager);
const getParameterErrorState = parameterManager.getParameterErrorState.bind(parameterManager);
const updateParameterSchema = (newSchema: any, parameterItem: ParamsInfo, index: number) =>
  parameterManager.updateParameterSchema(newSchema, parameterItem, index);
const updateComponentData = parameterManager.updateComponentData.bind(parameterManager);
const getResolvedModelData = parameterManager.getResolvedModelData.bind(parameterManager);

/**
 * Watches for changes in props value
 * <p>
 * Updates form data when props value changes
 * </p>
 */
watch(() => props.value, (newValue) => {
  if (componentState.formData.length && componentState.formData.some(item =>
    !!item.name ||
    (!!item.schema?.type && item.schema?.type !== SchemaType.string) ||
    !!item[parameterManager.valueKey]
  )) {
    return;
  }
  componentState.formData = (newValue || []).map((parameterItem, index) => {
    return { ...parameterItem, key: parameterItem.key || parameterManager.generateUniqueKey(index) };
  });
  if (componentState.formData.every(item => !!item.name)) {
    componentState.formData.push(getDefaultParams({ in: 'cookie', key: parameterManager.generateUniqueKey() }) as ParamsInfo);
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
        :class="{'opacity-50': !item[parameterManager.enabledKey]}">
        <Checkbox
          :disabled="!item.name"
          :checked="item[parameterManager.enabledKey] && !!item.name"
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
            :options="item.schema.enum || item.schema?.[parameterManager.valueKey]"
            :value="item[parameterManager.valueKey]"
            @blur="handleParameterValueBlur($event,index,item )"
            @select="updateParameterData(index, { ...item, [parameterManager.valueKey]: $event, schema: {...item?.schema|| {}, [parameterManager.valueKey]: $event} })" />
          <ParamInput
            v-else-if="![SchemaType.array, SchemaType.object].includes(item.schema.type)"
            :placeholder="t('service.apiRequestCookie.form.valuePlaceholder', { maxLength: API_PARAMETER_VALUE_LENGTH })"
            :maxLength="0"
            :value="item[parameterManager.valueKey]"
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
          :disabled="![SchemaType.array, SchemaType.object].includes(item.schema.type) || (item.schema.type === SchemaType.object && item.$ref)"
          @click="addChildParameter(item, index)">
          <Icon icon="icon-jia" />
        </Button>
        <Button
          class="w-7 ml-3 p-0"
          type="default"
          size="small"
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
        @change="updateParameterSchema($event, item, index)" />
    </div>
  </div>
</template>
