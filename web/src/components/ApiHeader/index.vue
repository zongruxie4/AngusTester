<script setup lang="ts">
import { reactive, watch, computed, ref, inject, onMounted, nextTick } from 'vue';
import { Button, Checkbox, Tooltip } from 'ant-design-vue';
import { Icon, Input, SelectSchema, Select, JsonContent, SimpleEditableSelect } from '@xcan-angus/vue-ui';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { http, TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
const { t }  = useI18n();

import ParamsInput from '@/components/ParamInput/index.vue';
import { ParamsItem, getDefaultParams, API_EXTENSION_KEY, deconstruct, validateType, deepDelAttrFromObj, getModelDataByRef } from '@/utils/ApiUtils';


import { itemTypes } from './interface';

const valueKey = API_EXTENSION_KEY.valueKey;
const enabledKey = API_EXTENSION_KEY.enabledKey;

interface Props {
  value: ParamsItem[];
  contentType?: string;
  authData: Record<string, string>;
  hideImportBtn: boolean;
  viewType: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  hideImportBtn: false,
  viewType: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', data: ParamsItem[]): void;
  (e: 'del', index: number): void;
  (e: 'update:authorizationData', value):void;
  (e: 'rendered', value: true);
}>();

const apiBaseInfo = inject('apiBaseInfo', ref());

const state = reactive<{
  formData:ParamsItem[]
}>({
  formData: []
});

const jsContentRef = ref<any[]>([]);
const showContentType = computed(() => {
  return !!props.contentType;
});
// 对参数名称限制
const nameInputProps = { dataType: 'mixin-en', includes: '-_', maxLength: 100 };

const getNameInputProps = (readonly) => {
  return {
    ...nameInputProps,
    readonly
  };
};

// const getText = (flag: boolean | undefined): string => {
//   return flag ? '取消变量' : '设为变量';
// };

const getKey = (index?: number): symbol => {
  return Symbol(index);
};

const enterHandle = (e): void => {
  if (e.key !== 'Enter') {
    return;
  }

  e.target.blur();
};

const handleValueBlur = (target:HTMLElement, index: number, data: ParamsItem):void => {
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

  const temp = { ...data, [valueKey]: value } as ParamsItem;
  changeEmit(index, temp);
};

const getModelData = async (ref) => {
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

const selectModels = async (_value, option, index) => {
  if (option) {
    // const model = JSON.parse(option.model);
    // const temp = { ...model, [enabledKey]: true };
    // if (option.readonly) {
    //   temp.$ref = option.ref;
    // }

    const model = await getModelData(option.ref);
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
    const temp = { ...model, schema, [enabledKey]: true, [valueKey]: value };
    changeEmit(index, temp);
  }
};

const handleBlur = (e, index: number, data: ParamsItem, key: string): void => {
  const value = e.target.value.trim();
  const temp = { ...data, [key]: value } as ParamsItem;
  changeEmit(index, temp);
};

// 启用 禁用
const handleChecked = (e, index:number, data: ParamsItem) => {
  const checked = e.target.checked;
  const temp = { ...data, [enabledKey]: checked } as ParamsItem;
  changeEmit(index, temp);
  if (!checked && validated.value) {
    jsContentRef.value[index] && jsContentRef.value[index].validate(false);
  }
};

// 设为变量
// const setVariableLoading = reactive({});
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
//   await variableApi.addVariables({ name: data.name, targetId: archivedId.value, scope: 'CURRENT', targetType: 'API', enabled: true, value });
//   // const temp = { ...data, [exportVariableKey]: !data[exportVariableKey] } as ParamsItem;
//   // changeEmit(index, temp);
//   setVariableLoading[data.name] = false;
// };

// 同步给父级
const emitChange = () => {
  emit('change', state.formData.filter(i => !!i.name));
};

const changeDataType = (value, index, item) => {
  const schema = item.schema || {};
  const temp = { ...item, schema: { ...schema, type: value } };
  if (value === 'object') {
    temp.deepObject = true;
    temp[valueKey] = { '': '' };
  } else {
    temp[valueKey] = undefined;
    if (value === 'array') {
      temp[valueKey] = [''];
      temp.schema.item = {
        type: 'string'
      };
    }
    delete temp.deepObject;
    delete temp.explode;
  }
  changeEmit(index, temp);
};

const handleDel = (index: number, data: ParamsItem): void => {
  const emptyList = state.formData.filter(item => !item.name);
  // 最少要有一条空数据
  if (!data.name && emptyList.length <= 1) {
    return;
  }
  state.formData.splice(index, 1);
  emitChange();
};

const addChild = (pItem, idx) => {
  jsContentRef.value[idx].addItem({ type: pItem.schema.type, id: -1, idLine: [-1], level: 0 });
};

const changeEmit = (index: number, data: ParamsItem): void => {
  state.formData[index] = data;
  if (state.formData.every(i => !!i.name)) {
    state.formData.push(getDefaultParams({ in: 'header' }) as ParamsItem);
  }
  emitChange();
};

const filterModels = (option) => {
  const model = option.model && JSON.parse(option.model);
  return !model || model.in !== 'header';
};

const validated = ref(false);
const validateContents = async (val = true) => {
  validated.value = val;
  for (const idx in jsContentRef.value) {
    if (state.formData[idx][enabledKey]) {
      jsContentRef.value[idx].validate(val);
    }
  }
};

const getErrValue = (item) => {
  if (!validated.value || !item.name || !item[enabledKey]) {
    return false;
  }
  const errors = validateType(item[valueKey], deepDelAttrFromObj(item.schema));
  return !!errors?.length;
};

const changeSchema = (schema, item, index) => {
  if (item.$ref) {
    return;
  }
  const temp = { ...item, schema };
  changeEmit(index, temp);
};

watch(() => props.value, (newValue) => {
  state.formData = (newValue || []).map((i, idx) => {
    return { ...i, key: i.key || getKey(idx) };
  });
  if (state.formData.every(i => !!i.name) && !props.viewType) {
    state.formData.push(getDefaultParams({ in: 'header', key: getKey() }) as ParamsItem);
  }
}, {
  deep: true,
  immediate: true
});

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});

const updateComp = async () => {
  if (!apiBaseInfo.value.serviceId) {
    return;
  }
  for (let i = 0; i < state.formData.length; i++) {
    if (state.formData[i].$ref) {
      await http.put(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/parameters/${state.formData[i].name}`, state.formData[i]);
    }
    if (jsContentRef.value[i]) {
      await jsContentRef.value[i].updateComp();
    }
  }
};

const getModelResolve = () => {
  const models = {};
  state.formData.forEach((i, index) => {
    if (i.$ref) {
      models[i.$ref] = JSON.parse(JSON.stringify(i));
      delete models[i.$ref].schema.$ref;
      jsContentRef.value[index] && jsContentRef.value[index].getModelResolve(models);
    }
  });
  return models;
};

const getData = () => {
  const formData = state.formData.filter(i => i.name || i[valueKey]);
  // return state.formData.filter(i => i.name || i[valueKey]);
  if (showContentType.value) {
    formData.push({ name: 'Content-Type', schema: { type: 'string' }, [valueKey]: props.contentType, in: 'header' });
  }
  return formData;
};

defineExpose({ updateComp, getModelResolve, validate: validateContents, getData });
</script>
<template>
  <div class="space-y-3">
    <div v-if="showContentType" class="flex flex-nowrap items-center whitespace-nowrap">
      <Checkbox :checked="true" :disabled="props.viewType" />
      <div class="flex flex-col flex-1 max-w-100 ml-2">
        <Input
          :placeholder="t('xcan_apiHeader.enterParameterName')"
          value="Content-Type"
          size="small"
          :allowClear="false"
          readonly />
      </div>
      <Select
        value="string"
        :options="itemTypes"
        readonly
        class="w-25 ml-2" />
      <div class="flex flex-col flex-1 ml-2">
        <Input
          :placeholder="t('xcan_apiHeader.enterDebugValue')"
          :value="props.contentType"
          :allowClear="false"
          size="small"
          readonly />
      </div>
      <template v-if="!props.viewType">
        <div class="flex ml-2">
          <div class="w-16.25"></div>
        </div>
      </template>
    </div>
    <div
      v-for="(value, key) in props.authData"
      :key="key"
      class="flex flex-nowrap items-center whitespace-nowrap">
      <Checkbox :checked="true" :disabled="props.viewType" />
      <div class="flex flex-col max-w-100 flex-1 ml-2">
        <Input
          :placeholder="t('xcan_apiHeader.enterParameterName')"
          :value="key"
          size="small"
          :allowClear="false"
          readonly />
      </div>
      <Select
        value="string"
        :options="itemTypes"
        readonly
        class="w-25 ml-2" />
      <div class="flex flex-col flex-1 ml-2">
        <Input
          :placeholder="t('xcan_apiHeader.enterDebugValue')"
          :value="value"
          :allowClear="false"
          size="small"
          readonly />
      </div>
      <template v-if="!props.viewType">
        <div class="flex ml-2">
          <div class="w-16.25"></div>
        </div>
      </template>
    </div>
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
          @change="handleChecked($event, index, item)" />
        <Tooltip placement="topLeft">
          <div class="flex flex-col max-w-100 flex-1" :class="{'border-l border-blue-1': item.$ref}">
            <SelectSchema
              v-if="apiBaseInfo?.serviceId"
              :id="apiBaseInfo?.serviceId"
              v-model:value="item.name"
              :placeholder="t('xcan_apiHeader.parameterName')"
              mode="pure"
              :maxLength="400"
              :hideImportBtn="props.hideImportBtn"
              :params="{ ignoreModel: false, types: 'parameters'}"
              :type="['parameters']"
              :excludes="opt => filterModels(opt)"
              :inputProps="getNameInputProps(item.$ref || props.viewType)"
              @blur="handleBlur($event, index, item, 'name')"
              @change="(...arg) => selectModels(...arg, index)" />
            <Input
              v-else
              :placeholder="t('xcan_apiHeader.enterParameterName')"
              :value="item.name"
              :allowClear="false"
              :maxLength="400"
              :readonly="item.$ref || props.viewType"
              dataType="mixin-en"
              :inputProps="nameInputProps"
              @blur="handleBlur($event, index, item, 'name')"
              @keypress="enterHandle" />
          </div>
          <template v-if="item.$ref" #title>
            {{ t('xcan_apiHeader.componentReference', { ref: item.$ref }) }}
          </template>
        </Tooltip>
        <Select
          v-model:value="item.schema.type"
          :options="itemTypes"
          :readonly="item.$ref || props.viewType"
          class="w-25"
          @change="changeDataType($event, index, item)" />
        <div class="flex flex-col flex-1">
          <SimpleEditableSelect
            v-if="item.schema?.enum"
            :placeholder="t('xcan_apiHeader.enterDebugValue')"
            :readonly="props.viewType"
            :options="item.schema.enum || item.schema?.[valueKey]"
            :value="item[valueKey]"
            @blur="handleValueBlur($event,index,item )"
            @select="changeEmit(index, { ...item, [valueKey]: $event })" />
          <ParamsInput
            v-else-if="!['array', 'object'].includes(item.schema.type)"
            :placeholder="t('xcan_apiHeader.enterDebugValue')"
            :disabled="props.viewType"
            :value="item[valueKey]"
            :error="getErrValue(item)"
            @blur="handleValueBlur($event, index, item)" />
          <Input v-else disabled />
        </div>
        <!-- <Button
          v-if="archivedId"
          :title="t('xcan_apiHeader.setAsVariable')"
          type="primary"
          size="small"
          :disabled="!item.name || setVariableLoading[item.name]"
          @click="setToVariable(item)">
          <Icon icon="icon-bianliang" />
        </Button> -->
        <template v-if="!props.viewType">
          <Button
            size="small"
            :disabled="!['array', 'object'].includes(item.schema.type) || (item.schema.type === 'object' && item.$ref)"
            @click="addChild(item, index)">
            <Icon icon="icon-jia" />
          </Button>
          <Button
            class="w-7 ml-3 p-0"
            type="default"
            size="small"
            :disabled="!item.name && !item[valueKey] || props.viewType"
            @click="handleDel(index, item)">
            <Icon icon="icon-shanchuguanbi" />
          </Button>
        </template>
      </div>
      <JsonContent
        v-if="item.schema?.type === 'array' || item.schema?.type === 'object'"
        :ref="dom => jsContentRef[index] = dom"
        v-model:data="item[valueKey]"
        :schema="item.schema || {}"
        :disabled="!!item.$ref"
        :pType="item.schema?.type"
        :hideImportBtn="props.hideImportBtn"
        :viewType="props.viewType"
        @change="changeSchema($event, item, index)" />
    </div>
  </div>
</template>
