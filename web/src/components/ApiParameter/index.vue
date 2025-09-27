<script setup lang="ts">
import { reactive, watch, ref, inject, onMounted, nextTick } from 'vue';
import { Button, Checkbox, Tooltip } from 'ant-design-vue';
import { Icon, Input, Select, SelectSchema } from '@xcan-angus/vue-ui';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { http, TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { deconstruct, getParamsByUri, getUriByParams, validateType, deepDelAttrFromObj, API_EXTENSION_KEY, getModelDataByRef, itemTypes } from 'src/utils/apis';
import { ParamsItem, paramsTypeOpt, getDefaultParams } from './interface';
import ParamInput from '@/components/ParamInput/index.vue';
import JsonContent from '@/components/JsonContent/index.vue';
import SimpleEditableSelect from '@/components/SimpleEditableSelect/index.vue';

const { t } = useI18n();
export interface Props {
  value: ParamsItem[];
  apiUri?: string;
  updateCompUrl: string;
  hideImportBtn: boolean;
  viewType: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  apiUri: '',
  value: () => ([]),
  hideImportBtn: false,
  viewType: false
});

const valueKey = API_EXTENSION_KEY.valueKey;
const enabledKey = API_EXTENSION_KEY.enabledKey;

const apiBaseInfo = inject('apiBaseInfo', ref());

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: ParamsItem[]): void;
  (e: 'update:apiUri', value: string);
  (e: 'rendered', value: true);
}>();

const state = reactive<{
  formData:ParamsItem[]
}>({
  formData: []
});
const jsContentRef = ref([]);
const validated = ref(false);

const getKey = (index?: number): symbol => {
  return Symbol(index);
};

const emitChange = () => {
  emit('change', state.formData);
};

const enterHandle = (e): void => {
  if (e.key !== 'Enter') {
    return;
  }

  e.target.blur();
};

const handleValueBlur = (target:HTMLElement, index: number, data: ParamsItem):void => {
  let value = target?.innerText?.trim();
  if (['integer', 'number', 'boolean'].includes(data.schema?.type)) {
    try {
      if (value <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {}
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
    const model = await getModelData(option.ref);
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
    const temp = { ...model, schema, [enabledKey]: true, [valueKey]: value };
    changeEmit(index, temp);
  }
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
  // if (!temp.name && !!temp.name[valueKey]) {
  //   return;
  // }
  changeEmit(index, temp);
};

// const addValueItem = (item, index) => {
//   const value = state.formData[index][valueKey];
//   value.push('');
//   const temp = { ...item, [valueKey]: value };
//   changeEmit(index, temp);
// };

// const delValueItem = (item, index, subIndex) => {
//   const value = state.formData[index][valueKey];
//   value.splice(subIndex, 1);
//   const temp = { ...item, [valueKey]: value };
//   changeEmit(index, temp);
// };

const handleBlur = (e, index: number, data: ParamsItem, key: string): void => {
  const value = e.target.value.trim();
  const temp = { ...data, [key]: value } as ParamsItem;
  changeEmit(index, temp);
};

// 启用禁用
const changeChecke = (e, index:number, data: ParamsItem) => {
  const checked = e?.target?.checked;
  const temp = { ...data, [enabledKey]: checked } as ParamsItem;
  changeEmit(index, temp);
  if (!checked && validated.value) {
    jsContentRef.value[index] && jsContentRef.value[index].validate(false);
  }
};

const selectChange = (value: string, index: number, data: ParamsItem, key: string): void => {
  const temp = { ...data, [key]: value } as ParamsItem;
  changeEmit(index, temp);
};

const changeSchema = (schema, item, index) => {
  const temp = { ...item, schema };
  changeEmit(index, temp);
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

// 删除
const handleDel = (index: number, data: ParamsItem): void => {
  const emptyList = state.formData.filter(item => !item.name);
  // 最少要有一条空数据
  if (!data.name && emptyList.length <= 1) {
    return;
  }
  state.formData.splice(index, 1);
  emitChange();
  if (data.in === 'path') {
    changeApiUriByparams();
  }
};

const changeEmit = (index: number, data: ParamsItem): void => {
  state.formData[index] = data;
  emitChange();
  changeApiUriByparams();
};

const addChild = (pItem, idx) => {
  jsContentRef.value[idx].addItem({ type: pItem.schema.type, id: -1, idLine: [-1], level: 0 });
};

// param 变更, 改变 apiUri
const changeApiUriByparams = () => {
  const pathList = state.formData.filter(item => item?.[enabledKey] && item.name && item.in === 'path');
  let apiUri = props.apiUri;
  if (pathList.length) {
    if (!apiUri) {
      apiUri = '/';
    }
  }
  apiUri = getUriByParams(apiUri || '', pathList);
  emit('update:apiUri', apiUri);
};

const setParamList = ():void => {
  const paramsList = getParamsByUri(props.apiUri || ''); // 已经存在的params直接取其值
  const tempIdx: number[] = []; // 存储需要删除的formData下标

  // 从 url拿到所有 path 的 list;
  const pathList = paramsList.filter(i => i.in === 'path');

  // let formValue = props.value.map(toRaw);

  // 正向对比, 找到 formData 中缺少的填入
  pathList?.forEach((current) => {
    const item = state.formData.find(ele => ele.name === current.name);
    if (!item) {
      state.formData.splice(-1, 0, {
        ...current,
        [enabledKey]: true
      });
    } else {
      item[enabledKey] = true;
    }
  });
  // 反向对比, 找到 formData 中多余的删除
  state.formData?.forEach((current, index) => {
    if (current.in === 'path') {
      const item = pathList.find(ele => ele.name === current.name);
      if (!item && current[enabledKey]) {
        tempIdx.push(index);
      }
    }
  });

  state.formData = state.formData.filter((_i, index) => !tempIdx.includes(index));
  emitChange();
};

watch(() => props.value, (newValue) => {
  // if (state.formData.filter(i => !!i.name || i[valueKey] || i.schema?.type !== 'string' || i.in !== 'query').length > 0) {
  //   return;
  // }
  state.formData = newValue.map((i) => {
    return { ...i, key: i.key || getKey() };
  });
}, {
  deep: true,
  immediate: true
});

watch(() => state.formData, () => {
  if (state.formData.every(i => !!i.name || !!i[valueKey]) && !props.viewType) {
    state.formData.push(getDefaultParams({ in: 'query', key: getKey() }) as ParamsItem);
  }
}, {
  deep: true,
  immediate: true
});

// 监听 apiUri 变更, 同步params
watch(() => props.apiUri, () => {
  setTimeout(() => {
    setParamList();
  });
});

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});

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
  return !!errors.length;
};

const filterDataModel = (opt) => {
  const model = JSON.parse(opt.model);
  return !(['query', 'path'].includes(model.in));
};

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

const addParam = (data) => {
  state.formData.splice(-1, 0, ...data);
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
  return state.formData.filter(i => i.name || i[valueKey]);
};

defineExpose({
  validate: validateContents,
  updateComp,
  getModelResolve,
  addParam,
  getData
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
          @change="changeChecke($event, index, item)" />
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
              :excludes="opt => filterDataModel(opt)"
              @blur="handleBlur($event, index, item, 'name')"
              @change="(_value, option) => selectModels(_value, option, index)" />
            <Input
              v-else
              v-model:value="item.name"
              :placeholder="t('xcan_apiParameter.enterParameterName')"
              :maxLength="400"
              :readonly="item.$ref || props.viewType"
              :allowClear="false"
              @blur="handleBlur($event, index, item, 'name')"
              @keypress="enterHandle" />
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
          @change="selectChange($event, index, item, 'in')" />
        <Select
          v-model:value="item.schema.type"
          class="w-25"
          :readonly="item.$ref || props.viewType"
          :options="itemTypes"
          @change="changeDataType($event, index, item)" />
        <div class="flex flex-col flex-1 ml-3 space-y-0.5">
          <SimpleEditableSelect
            v-if="item.schema?.enum"
            :placeholder="t('xcan_apiParameter.enterDebugValue')"
            :options="item.schema.enum"
            :readonly="props.viewType"
            :value="item[valueKey] || item.schema?.[valueKey]"
            @blur="handleValueBlur($event,index,item)"
            @select="changeEmit(index, { ...item, [valueKey]: $event })" />
          <param-input
            v-else-if="item.schema?.type !== 'array' && item.schema?.type !== 'object'"
            :placeholder="t('xcan_apiParameter.enterDebugValue')"
            :value="item[valueKey]"
            :disabled="props.viewType"
            :error="getErrValue(item)"
            @blur="handleValueBlur($event, index, item)" />
          <Input v-else disabled />
        </div>
        <template v-if="!props.viewType">
          <Button
            size="small"
            class="ml-2"
            :disabled="!['array', 'object'].includes(item.schema?.type) || (item.schema?.type === 'object' && item.$ref)"
            @click="addChild(item, index)">
            <Icon icon="icon-jia" />
          </Button>
          <Button
            size="small"
            class="ml-2"
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
        :paramInType="item.in"
        :hideImportBtn="props.hideImportBtn"
        :viewType="props.viewType"
        @change="changeSchema($event, item, index)" />
    </div>
  </div>
</template>
<style>
.api-select-dropdown .ant-select-item {
  @apply text-3 leading-7 py-0 min-h-full;
}
</style>
