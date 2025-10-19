<script setup lang="ts">
import { inject, reactive, ref, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox, Tooltip } from 'ant-design-vue';
import { Icon, Input, Select, SelectSchema, notification } from '@xcan-angus/vue-ui';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { deconstruct } from '@/utils/swagger';

import { services } from '@/api/tester';
import { getDefaultParams, schemaTypeToOptions, validateType } from './utils';
import {
  API_EXTENSION_KEY,
  deepDelAttrFromObj,
  getModelDataByRef,
  getUriByParams,
  getParamsByUri,
  QueryAndPathInOption
} from '@/utils/apis';
import JsonContent from '@/views/apis/services/protocol/http/requestBody/Json.vue';
import SimpleEditableSelect from '@/components/apis/editableSelector/index.vue';
import { toClipboard } from '@xcan-angus/infra';
import { ParamsInfo } from '@/views/apis/services/protocol/http/types';

const ParamInput = defineAsyncComponent(() => import('@/components/ParamInput/index.vue'));

interface Props {
  value: ParamsInfo[],
  apiUri?: string
}

// SwaggerUI.extension.sampleFromSchemaGeneric
const props = withDefaults(defineProps<Props>(), {
  apiUri: ''
});

const { t } = useI18n();

const valueKey = API_EXTENSION_KEY.valueKey;
const enabledKey = API_EXTENSION_KEY.enabledKey;

const apiBaseInfo = inject('apiBaseInfo', ref());
const globalConfigs = inject('globalConfigs', { VITE_API_PARAMETER_NAME_LENGTH: 400, VITE_API_PARAMETER_VALUE_LENGTH: 4096 });

const emits = defineEmits<{
  (e: 'change', value: ParamsInfo[]): void,
  (e: 'update:apiUri', value: string)
}>();

const state = reactive<{
  formData:ParamsInfo[]
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

const handleValueBlur = (target:HTMLElement, index: number, data: ParamsInfo):void => {
  let value = target?.innerText?.trim().replace('\n', '');
  if (['integer', 'number', 'boolean'].includes(data.schema?.type)) {
    try {
      if (value <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {}
  }
  const temp = { ...data, [valueKey]: value } as ParamsInfo;
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
  changeEmit(index, temp);
};

const handleBlur = (e: ChangeEvent, index: number, data: ParamsInfo, key: string): void => {
  const value = e.target.value.trim();
  const temp = { ...data, [key]: value } as ParamsInfo;
  changeEmit(index, temp);
};

// 启用禁用
const changeChecke = (e:ChangeEvent, index:number, data: ParamsInfo) => {
  const checked = e?.target?.checked;
  const temp = { ...data, [enabledKey]: checked } as ParamsInfo;

  changeEmit(index, temp);
  if (!checked && validated.value) {
    jsContentRef.value[index] && jsContentRef.value[index].validate(false);
  }
};

const selectChange = (value: string, index: number, data: ParamsInfo, key: string): void => {
  const temp = { ...data, [key]: value } as ParamsInfo;
  changeEmit(index, temp);
};

const changeSchema = (schema, item, index) => {
  const temp = { ...item, schema };
  changeEmit(index, temp);
};

const copyValue = async (data: ParamsInfo) => {
  let text = data[valueKey];
  if (typeof text !== 'string') {
    text = JSON.stringify(text);
  }

  toClipboard(text).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  });
};

// 删除
const handleDel = (index: number, data: ParamsInfo): void => {
  const emptyList = state.formData.filter(item => !item.name);
  // 最少要有一条空数据
  if (!data.name && emptyList.length <= 1) {
    return;
  }
  state.formData.splice(index, 1);
  emitChange();
  if (data.in === 'path') {
    changeApiUriByParams();
  }

  // emits('del', index);
};

const changeEmit = (index: number, data: ParamsInfo): void => {
  state.formData[index] = data;
  emitChange();
  changeApiUriByParams();
};

const addChild = (pItem, idx) => {
  jsContentRef.value[idx].addItem({ type: pItem.schema.type, id: -1, idLine: [-1], level: 0 });
};

// param 变更, 改变 apiUri
const changeApiUriByParams = () => {
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
      await services.addComponent(apiBaseInfo.value?.serviceId, 'parameters', state.formData[i].name,
        { ...content, schema: { ...(content.schema || {}), [valueKey]: content[valueKey] } });
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
    state.formData.push(getDefaultParams({ in: 'query', key: getKey() }) as ParamsInfo);
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
              :placeholder="t('common.placeholders.searchKeyword')"
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
              :placeholder="t('service.apiRequestParams.form.inputNamePlaceholder')"
              :maxLength="globalConfigs.VITE_API_PARAMETER_NAME_LENGTH"
              :allowClear="false"
              @blur="handleBlur($event, index, item, 'name')"
              @keypress="enterHandle" />
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
          @change="selectChange($event, index, item, 'in')" />
        <Select
          v-model:value="item.schema.type"
          class="w-25 flex-shrink-0"
          :disabled="item.$ref"
          :options="schemaTypeToOptions"
          @change="changeDataType($event, index, item)" />
        <div class="flex flex-col flex-25 ml-3 space-y-0.5">
          <SimpleEditableSelect
            v-if="item.schema?.enum"
            :placeholder="t('service.apiRequestParams.form.valuePlaceholder', { maxLength: globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH })"
            :maxLength="globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH"
            :options="item.schema.enum"
            :value="item[valueKey] || item.schema?.[valueKey]"
            @blur="handleValueBlur($event,index,item)"
            @select="changeEmit(index, { ...item, [valueKey]: $event,schema: {...item?.schema|| {}, [valueKey]: $event} } )" />
          <ParamInput
            v-else-if="item.schema?.type !== 'array' && item.schema?.type !== 'object'"
            :placeholder="t('service.apiRequestParams.form.valuePlaceholder', { maxLength: globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH })"
            :maxLength="globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH"
            :value="item[valueKey]"
            :error="getErrValue(item)"
            @blur="handleValueBlur($event, index, item)" />
          <Input v-else disabled />
        </div>
        <Button
          type="primary"
          size="small"
          :title="t('service.apiRequestParams.actions.copyValue')"
          class="ml-2"
          @click="copyValue(item)">
          <Icon icon="icon-fuzhi" />
        </Button>
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
