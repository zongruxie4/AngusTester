<script lang="ts" setup>
import { inject, ref, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Checkbox } from 'ant-design-vue';
import { Icon, Input, Select, SelectSchema } from '@xcan-angus/vue-ui';
import SwaggerUI from '@xcan-angus/swagger-ui';

import { API_EXTENSION_KEY } from '@/utils/apis';
import { deconstruct } from '@/utils/swagger';
import { services } from '@/api/tester';
import { FormData, getDefaultForm, itemTypes } from './PropsType';

import JsonContent from '@/views/apis/services/protocol/http/requestBody/Json.vue';

const ParamInput = defineAsyncComponent(() => import('@/components/ParamInput/index.vue'));

const { valueKey, enabledKey } = API_EXTENSION_KEY;
interface Props {
  data:FormData[];
  in: 'query'|'header'
}
const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  data: () => ([]),
  in: 'query'
});

const emits = defineEmits<{(e: 'change', value: FormData[])}>();
const apiBaseInfo = inject('apiBaseInfo', ref());

const formData = ref<FormData[]>([]);
const jsContentRef = ref<any[]>([]);

const handleEmit = () => {
  const result = formData.value.filter(data => data.name || data[valueKey] || data.description);
  emits('change', result);
  if (formData.value.every(i => !!i.name)) {
    formData.value.push(getDefaultForm({ in: props.in }));
  }
};

const handleBlur = () => {
  handleEmit();
};

const handleValueBlur = (target:HTMLElement, index: number, data):void => {
  let value = target?.innerText?.trim();
  if (['integer', 'number', 'boolean'].includes(data.schema?.type)) {
    try {
      if (value <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {}
  }
  const temp = { ...data, [valueKey]: value };
  formData.value[index] = temp;
  handleEmit();
};

const changeDataType = (value, index, item) => {
  const schema = item.schema || {};
  const temp = { ...item, schema: { ...schema, type: value } };
  if (value === 'object') {
    temp.deepObject = true;
    temp[valueKey] = { '': '' };
  } else {
    if (value === 'array') {
      temp[valueKey] = [''];
    }
    temp[valueKey] = undefined;
    delete temp.deepObject;
    delete temp.explode;
  }
  formData.value[index] = temp;
  const result = formData.value.filter(data => data.name || data[valueKey]);
  emits('change', result);
};

const handleDelete = (index, data) => {
  const emptyList = formData.value.filter(item => !item.name);
  if (!data.name && emptyList.length <= 1) {
    return;
  }
  formData.value.splice(index, 1);
  const result = formData.value.filter(data => data.name || data[valueKey]);
  emits('change', result);
};

const getModelData = async (ref) => {
  const [error, { data }] = await services.getComponentRef(apiBaseInfo.value.projectId, ref);
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
    const value = SwaggerUI.extension.sampleFromSchemaGeneric(schema);
    if (option.readonly) {
      model.$ref = option.ref;
    }
    const temp = { ...model, schema, [enabledKey]: true, [valueKey]: value };
    formData.value[index] = temp;
    handleEmit();
  } else {
    const temp = { ...formData.value[index], name: _value };
    formData.value[index] = temp;
    handleEmit();
  }
};

const addItem = (item: FormData) => {
  formData.value.unshift({ ...item, [enabledKey]: true, in: props.in });
  handleEmit();
};

const handleChecked = (e, data) => {
  const checked = e.target.checked;
  data.checked = checked;
};
const addChild = (pItem, idx) => {
  jsContentRef.value[idx].addItem({ type: pItem.schema.type, id: -1, idLine: [-1], level: 0 });
};
const changeSchema = (schema, item, index) => {
  const temp = { ...item, schema };
  formData.value[index] = temp;
  handleEmit();
};

watch(() => props.data, newValue => {
  if (formData.value.length < 2) {
    formData.value = newValue;
    if (formData.value.every(i => !!i.name)) {
      formData.value.push(getDefaultForm({ in: props.in }));
    }
  }
}, {
  immediate: true
});

const filterDataModel = (opt) => {
  const model = JSON.parse(opt.model);
  if (props.in === 'query') {
    return !(['query'].includes(model.in));
  }
  if (props.in === 'header') {
    return !(['header'].includes(model.in));
  }
};

const getModelResolve = () => {
  const models = {};
  formData.value.forEach((i, index) => {
    if (i.$ref) {
      models[i.$ref] = i;
    }
    jsContentRef.value[index] && jsContentRef.value[index].getModelResolve(models);
  });
  return models;
};

defineExpose({ getModelResolve, addItem });
</script>

<template>
  <ul class="min-h-30">
    <li
      v-for="(item, index) in formData"
      :key="index"
      class="space-y-2 mb-2"
      :class="{'opacity-50': !item[enabledKey]}">
      <div class="flex items-center space-x-2">
        <Checkbox v-model:checked="item[enabledKey]" @change="handleChecked($event, item)"></Checkbox>
        <div class="w-100">
          <SelectSchema
            v-if="apiBaseInfo.projectId"
            :id="apiBaseInfo.projectId"
            v-model:value="item.name"
            :disabled="!!item.$ref"
            :placeholder="t('service.webSocketForm.placeholder.parameterName')"
            mode="pure"
            :type="['parameters']"
            :params="{types: 'parameters', ignoreModel: false}"
            :excludes="opt => filterDataModel(opt)"
            @blur="handleBlur"
            @change="(_value, option) => selectModels(_value, option, index)" />
          <Input
            v-else
            v-model:value="item.name"
            size="small"
            :placeholder="t('service.webSocketForm.placeholder.inputParameterName')"
            class="flex-1"
            dataType="mixin-en"
            @blur="handleBlur" />
        </div>
        <Select
          v-model:value="item.schema.type"
          class="w-25"
          :disabled="item.$ref"
          :options="itemTypes"
          @change="changeDataType($event, index, item)" />
        <ParamInput
          v-if="!['array', 'object'].includes(item.schema.type)"
          v-model:value="item[valueKey]"
          size="small"
          class="flex-1 min-w-15"
          :placeholder="t('service.webSocketForm.placeholder.inputParameterValue')"
          @blur="handleValueBlur($event, index, item)" />
        <Input
          v-else
          disabled
          class="flex-1"
          size="small" />
        <!-- <Input
          v-model:value="item.description"
          size="small"
          class="w-100"
          :placeholder="t('service.webSocketForm.placeholder.inputParameterDescription')"
          @blur="handleBlur" /> -->
        <Button
          v-show="item.schema?.type === 'array' || item.schema?.type === 'object'"
          size="small"
          @click="addChild(item, index)">
          <Icon icon="icon-jia" />
        </Button>
        <Button
          size="small"
          @click="handleDelete(index, item)">
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
        @change="changeSchema($event, item, index)" />
    </li>
  </ul>
</template>
