<script setup lang="ts">
import { inject, reactive, ref, watch } from 'vue';
import { Button, Checkbox, Tooltip } from 'ant-design-vue';
import { Icon, Input, notification, Select, SelectSchema, ParamInput } from '@xcan-angus/vue-ui';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { deconstruct } from '@/utils/swagger';

import { services, variable as variableApi } from 'src/api/tester';
import { ParamsItem, paramsTypeOpt } from './interface';
import { getDefaultParams } from '../interface';
import { deepDelAttrFromObj, getParamsByUri, getUriByParams, validateType } from '../utils';
import { API_EXTENSION_KEY, getModelDataByRef, variableNameReg } from '@/views/apis/utils';
import JsonContent from '../requestBody/json/index.vue';
import { itemTypes } from '../requestBody/json/util';
import SimpleEditableSelect from '@/components/apis/editableSelector/index.vue';

const valueKey = API_EXTENSION_KEY.valueKey;
const enabledKey = API_EXTENSION_KEY.enabledKey;
// const exportVariableFlagKey = API_EXTENSION_KEY.exportVariableFlagKey;

interface Props {
  value: ParamsItem[],
  apiUri?: string
}

// SwaggerUI.extension.sampleFromSchemaGeneric
const props = withDefaults(defineProps<Props>(), {
  apiUri: ''
});

const apiBaseInfo = inject('apiBaseInfo', ref());
const archivedId = inject('archivedId', ref());
const globalConfigs = inject('globalConfigs', { VITE_API_PARAMETER_NAME_LENGTH: 400, VITE_API_PARAMETER_VALUE_LENGTH: 4096 });

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', value: ParamsItem[]): void,
  (e: 'update:apiUri', value: string)
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
  emits('change', state.formData);
};

const enterHandle = (e: ChangeEvent): void => {
  if (e.key !== 'Enter') {
    return;
  }

  e.target.blur();
};

const handleValueBlur = (target:HTMLElement, index: number, data: ParamsItem):void => {
  let value = target?.innerText?.trim().replace('\n', '');
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
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value?.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

const selectModels = async (_value, option, index) => {
  if (option) {
    // const model = JSON.parse(option.model);
    const model = await getModelData(option.ref);

    const schema = model.schema ? { ...model.schema, [valueKey]: model[valueKey] || model.schema?.[valueKey] } : {};
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

const handleBlur = (e: ChangeEvent, index: number, data: ParamsItem, key: string): void => {
  const value = e.target.value.trim();
  const temp = { ...data, [key]: value } as ParamsItem;
  changeEmit(index, temp);
};

// 启用禁用
const changeChecke = (e:ChangeEvent, index:number, data: ParamsItem) => {
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
const setVariableLoading = reactive({});
// 设为变量
const setToVariable = async (data: ParamsItem): void => {
  if (setVariableLoading[data.name]) {
    return;
  }
  setVariableLoading[data.name] = true;
  if (!variableNameReg.test(data.name as string)) {
    notification.warning('名称不符合变量要求,允许数字字母!@$%^&*()_-+=./等');
    return;
  }
  const value = typeof data[valueKey] === 'object' ? JSON.stringify(data[valueKey]) : data[valueKey];
  const [error] = await variableApi.addVariables({ targetId: archivedId.value, name: data.name, scope: 'CURRENT', targetType: 'API', enabled: true, value });
  setVariableLoading[data.name] = false;
  if (!error) {
    notification.success('设置变量成功');
  }
};

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

  // emits('del', index);
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
  emits('update:apiUri', apiUri);
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

  // query list 逻辑同上
  // const queryList = paramsList.filter(i => i.in === 'query');
  // formValue.forEach((item, index) => {
  //   if (item.in === 'query' && item[enabledKey] && queryList.length) {
  //     item.name = queryList[0].name;
  //     item[valueKey] = queryList[0]?.[valueKey];
  //     item[enabledKey] = true;
  //     queryList.shift();
  //   } else if (item.in === 'query' && (!!item.name || !!item[valueKey]) && item[enabledKey]) {
  //     tempIdx.push(index);
  //   }
  // });
  state.formData = state.formData.filter((_i, index) => !tempIdx.includes(index));
  // formValue.push(...queryList);
  // emits('change', state.formData);
  emitChange();
};

watch(() => props.value, (newValue) => {
  if (state.formData.filter(i => !!i.name || i[valueKey] || i.schema?.type !== 'string' || i.in !== 'query').length > 0) {
    return;
  }
  state.formData = newValue.map((i) => {
    return { ...i, key: i.key || getKey() };
  });
}, {
  deep: true,
  immediate: true
});

watch(() => state.formData, () => {
  if (state.formData.every(i => !!i.name || !!i[valueKey])) {
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

// const validateParams = ():boolean => {
//   const samplingSummary = state.formData.filter(i => i[enabledKey] && i.in === 'path' && i.name).every(i => i[valueKey]);
//   validated.value = true;
//   return samplingSummary;
// };

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
  const errors = validateType(item[valueKey], deepDelAttrFromObj(item.schema, []));
  return !!errors.length;
};

const filterDataModel = (opt) => {
  const model = JSON.parse(opt.model);
  return !(['query', 'path'].includes(model.in));
};

const updateComp = async () => {
  if (!apiBaseInfo.value?.serviceId) {
    return;
  }
  for (let i = 0; i < state.formData.length; i++) {
    if (state.formData[i].$ref) {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { $ref, ...content } = state.formData[i];
      await services.addComponent(apiBaseInfo.value?.serviceId, 'parameters', state.formData[i].name, { ...content, schema: { ...(content.schema || {}), [valueKey]: content[valueKey] } });
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

defineExpose({
  validate: validateContents,
  updateComp,
  getModelResolve,
  addParam
});
</script>
<template>
  <div class="space-y-3 min-w-220">
    <div
      v-for="(item, index) in state.formData"
      :key="item.key"
      class="space-y-2">
      <div
        class="flex flex-nowrap whitespace-nowrap items-center space-x-2"
        :class="{'opacity-50': !item[enabledKey]}">
        <Checkbox
          :disabled="!item.name && !item[valueKey]"
          :checked="item[enabledKey] && (!!item.name || !!item[valueKey]) "
          @change="changeChecke($event, index, item)" />
        <Tooltip placement="topLeft">
          <div class="flex flex-col w-100 flex-shrink-0">
            <SelectSchema
              v-if="apiBaseInfo?.serviceId"
              :id="apiBaseInfo?.serviceId"
              v-model:value="item.name"
              :disabled="item.$ref"
              placeholder="参数名称"
              mode="pure"
              :maxLength="globalConfigs.VITE_API_PARAMETER_NAME_LENGTH"
              :type="['parameters']"
              :params="{types: 'parameters', ignoreModel: false}"
              :excludes="opt => filterDataModel(opt)"
              @blur="handleBlur($event, index, state.formData[index], 'name')"
              @change="(_value, option) => selectModels(_value, option, index)" />
            <Input
              v-else
              v-model:value="item.name"
              placeholder="请输入参数名称"
              :maxLength="globalConfigs.VITE_API_PARAMETER_NAME_LENGTH"
              :allowClear="false"
              @blur="handleBlur($event, index, item, 'name')"
              @keypress="enterHandle" />
          </div>
          <template v-if="item.$ref" #title>
            组件引用：{{ item.$ref }}
          </template>
        </Tooltip>
        <Select
          v-model:value="item.in"
          dropdownClassName="api-select-dropdown"
          placeholder="请选择参数类型"
          :disabled="item.$ref"
          :allowClear="false"
          :options="paramsTypeOpt"
          class="w-25 flex-shrink-0"
          @change="selectChange($event, index, item, 'in')" />
        <Select
          v-model:value="item.schema.type"
          class="w-25 flex-shrink-0"
          :disabled="item.$ref"
          :options="itemTypes"
          @change="changeDataType($event, index, item)" />
        <div class="flex flex-col flex-25 ml-3 space-y-0.5">
          <SimpleEditableSelect
            v-if="item.schema?.enum"
            :placeholder="`请输入调试值，最大支持${globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH}个字符`"
            :maxLength="globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH"
            :options="item.schema.enum"
            :value="item[valueKey] || item.schema?.[valueKey]"
            @blur="handleValueBlur($event,index,item)"
            @select="changeEmit(index, { ...item, [valueKey]: $event,schema: {...item?.schema|| {}, [valueKey]: $event} } )" />
          <ParamInput
            v-else-if="item.schema?.type !== 'array' && item.schema?.type !== 'object'"
            :placeholder="`请输入调试值，最大支持${globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH}个字符`"
            :maxLength="globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH"
            :value="item[valueKey]"
            :error="getErrValue(item)"
            @blur="handleValueBlur($event, index, item)" />
          <Input v-else disabled />
        </div>
        <!-- <Button
          v-if="archivedId"
          type="primary"
          size="small"
          title="设为变量"
          class="ml-2"
          :disabled="!item.name || setVariableLoading[item.name]"
          @click="setToVariable(item)">
          <Icon icon="icon-bianliang" />
        </Button> -->
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
          :disabled="!item.name && !item[valueKey]"
          @click="handleDel(index, item)">
          <Icon icon="icon-shanchuguanbi" />
        </Button>
      </div>
      <JsonContent
        v-if="item.schema?.type === 'array' || item.schema?.type === 'object'"
        :ref="dom => jsContentRef[index] = dom"
        v-model:data="item[valueKey]"
        :schema="item.schema || {}"
        :disabled="!!item.$ref"
        :pType="item.schema?.type"
        :paramInType="item.in"
        @change="changeSchema($event, item, index)" />
    </div>
  </div>
</template>
<style>
.api-select-dropdown .ant-select-item {
  @apply text-3 leading-7 py-0 min-h-full;
}
</style>
../RequestBody/JsonContent/util
