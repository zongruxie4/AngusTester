<script setup lang="ts">
import { watch, ref, computed, inject, onMounted, nextTick } from 'vue';
import { utils, http, TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { Input, Select, Icon, SelectSchema, SimpleEditableSelect } from '@xcan-angus/vue-ui';
import { Button, Checkbox } from 'ant-design-vue';
import ParamsInput from '@/components/form/ParamInput.vue';
import { API_EXTENSION_KEY, deconstruct, deepDelAttrFromObj, getModelDataByRef } from '@/utils/apis';
import SwaggerUI from '@xcan-angus/swagger-ui';
import Ajv from 'ajv';
import addFormats from 'ajv-formats';

import {
  inOptions,
  itemTypes
} from './util';
import { transJsonToList, transListToJson, transListToSchema } from '@/utils/apis';
const { t } = useI18n();

const { valueKey } = API_EXTENSION_KEY;

interface Item {
  type: string;
  [valueKey]: string;
  id: string;
  name?: string;
  pid?: string;
  level: number;
  idLine: string[];
  $ref?: string;
  enum?: string[];
}

interface Props {
  data: any,
  pType: 'array'|'object';
  disabled: boolean;
  schema: Record<string, any>;
  paramInType?: 'path'|'query';
  hideImportBtn: boolean;
  viewType?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  data: () => ([]),
  pType: 'array',
  disabled: false,
  paramInType: undefined,
  hideImportBtn: false,
  viewType: false
});


const emit = defineEmits<{
  (e: 'update:data', value: any):void;
  (e: 'change', value: any): void;
  (e: 'rendered', value: true);
}>();

const ajv = new Ajv();
addFormats(ajv);

const apiBaseInfo = inject('apiBaseInfo', ref());
const archivedId = inject('archivedId', ref());

const emitHandle = () => {
  const data = transListToJson(dataSource.value, props.pType);
  const schema = transListToSchema(dataSource.value, props.pType);
  emit('update:data', data);
  emit('change', schema);
};

const disabledValue = item => {
  return item.type === 'object' || item.type === 'array';
};

const disabledType = (item) => {
  if (props.disabled || item.$ref) {
    return true;
  }

  if (disabledItem(item)) {
    return true;
  }

  return false;
};

const disabledName = (item) => {
  if (props.disabled) {
    return true;
  }
  if (disabledItem(item)) {
    return true;
  }
  if (item.pid === -1) {
    return props.pType !== 'object';
  }
  return dataSourceObj.value[item.pid] && dataSourceObj.value[item.pid].type === 'array';
};

const hideDel = (item) => {
  // && item.type !== 'array'
  return disabledItem(item) && dataSourceObj.value[item.pid]?.type !== 'array' && item.pid !== -1;
};

const dataSourceObj = ref({});

const dataSource = ref<Item[]>([]);

const allArrFirstIds = computed(() => {
  const result = [];
  if (props.pType === 'array') {
    result.push(-1);
    // const first = dataSource.value[0];
    // if (first.type) {
    //   findAllFirstArr(dataSource.value.filter(i => i.idLine.includes(first.id)), result);
    // }
  }
  const other = dataSource.value.filter(i => {
    if (i.type === 'array') {
      if ((dataSourceObj.value[i.pid] === -1 && props.pType !== 'array') || (dataSourceObj.value[i.pid] && dataSourceObj.value[i.pid].type !== 'array')) {
        return true;
      }
      if (dataSource.value.find(t => t.pid === i.pid)?.id === i.id) {
        return true;
      }
    }
    return false;
  }).map(i => i.id);
  return [...result, ...other].map(pid => {
    const first = dataSource.value.find(i => i.pid === pid);
    return first?.id;
  }).filter(Boolean);
});

const disabledItem = (item) => {
  if (item.$ref) {
    return true;
  }
  if (item.idLine.some((id, idx) => dataSourceObj.value[id]?.$ref && idx < (item.idLine.length - 1))) {
    return true;
  }
  if (item.pid === -1 && props.pType === 'array' && !allArrFirstIds.value.includes(item.id)) {
    return true;
  }
  if (dataSourceObj.value[item.pid]?.type === 'array' && !allArrFirstIds.value.includes(item.id)) {
    return true;
  }
  return false;
};

const disabledAdd = (item) => {
  return item.type === 'object' && disabledItem(item);
};

// const selectModel = () => {
//   if (dataSource.value.every(i => i.name)) {
//     dataSource.value.push(getDefaultItem());
//   }
// };

const changeType = (value, item, idx) => {
  if (value === 'array' || value === 'object') {
    item[valueKey] = undefined;
  }
  const childs = value === 'array'
    ? dataSource.value.filter(i => (i.idLine.includes(item.pid) || item.pid === -1) && item.id !== i.id)
    : dataSource.value.filter(i => i.idLine.includes(item.id) && item.id !== i.id);
  dataSource.value.splice(idx + 1, childs.length);
  dataSourceObj.value[item.id].type = value;
  childs.forEach((i, idx) => idx > 0 && delete dataSourceObj.value[i.id]);
  emitHandle();
  addItem(item, idx);
};

const addItem = (pItem, idx = dataSource.value.length) => {
  const childs = pItem.level < 1
    ? dataSource.value.filter(item => item.level === 1)
    : dataSource.value.filter(item => item.idLine.includes(pItem.id) && item.id !== pItem.id);
  const type = 'string';

  const id = utils.uuid('api');
  if (pItem.type === 'array') {
    if (childs.length > 0) {
    // if ((childs.length > 0 && pItem.level < 1) || childs.length > 1) {
      // let allItems = [];
      const firstId = childs[0].id;
      let allItems = dataSource.value.filter(i => i.idLine.includes(firstId));
      allItems = JSON.parse(JSON.stringify(allItems));
      const newIds = allItems.map(() => utils.uuid('api'));
      const oldIds = allItems.map(i => i.id);
      allItems.forEach((i, idx) => {
        i[valueKey] = '';
        if (i.id === oldIds[idx]) {
          i.id = newIds[idx];
        }
        oldIds.forEach((oldId, index) => {
          const replaceIdx = i.idLine.findIndex(id => id === oldId);
          if (replaceIdx > -1) {
            i.idLine[replaceIdx] = newIds[index];
          }
          if (i.pid === oldId) {
            i.pid = newIds[index];
          }
        });
        dataSourceObj.value[i.id] = i;
      });
      dataSource.value.splice(idx + childs.length + 1, 0, ...allItems);
    } else {
      if (pItem.items) {
        const json = SwaggerUI.extension.sampleFromSchemaGeneric(pItem, { useValue: true });
        const childSchema = pItem.items;
        const data = transJsonToList(json, pItem.id, pItem.level + 1, [pItem], childSchema);
        dataSource.value.splice(idx + childs.length, 1, ...data);
        data.forEach(i => {
          dataSourceObj.value[i.id] = i;
        });
      } else {
        const item = {
          name: 'item',
          pid: pItem.id,
          type,
          id,
          idLine: [...pItem.idLine, id],
          checked: true,
          level: pItem.level + 1
        };
        dataSource.value.splice(idx + childs.length + 1, 0, item);
        dataSourceObj.value[id] = item;
      }
    }
  } else if (pItem.type === 'object') {
    const item = {
      name: '',
      pid: pItem.id,
      type,
      id,
      idLine: [...pItem.idLine, id],
      checked: true,
      level: pItem.level + 1
    };
    dataSource.value.splice(idx + childs.length + 1, 0, item);
    dataSourceObj.value[id] = item;
  }
};

const delItem = (item, idx) => {
  const delItems = dataSource.value.filter(i => i.idLine.includes(item.id));
  dataSource.value.splice(idx, delItems.length);
  delItems.forEach(i => {
    delete dataSourceObj.value[i.id];
  });
  emitHandle();
};

const getModelData = async (ref) => {
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

const selectModels = async (value, option, index, item) => {
  if (option) {
    // const model = JSON.parse(option.model);
    const model = await getModelData(option.ref);
    const value = SwaggerUI.extension.sampleFromSchemaGeneric(model, { useValue: true });
    dataSource.value[index].type = model.type;
    dataSource.value[index] = {
      ...dataSource.value[index],
      ...model
    };
    if (option.readonly) {
      dataSource.value[index].$ref = option.ref;
    }
    const childSchema = model.type === 'object' ? model.properties : model.type === 'array' ? model.items : {};
    const data = transJsonToList(value, item.id, item.level + 1, [dataSource.value[index]], childSchema);
    dataSource.value.splice(index, 1, ...data);
    data.forEach(i => {
      dataSourceObj.value[i.id] = i;
    });
    emitHandle();
  } else {
    dataSource.value[index].name = value;
  }
};

const handleValueBlur = (dom, item) => {
  let value = dom.innerText;
  if (['integer', 'number', 'boolean'].includes(item.schema?.type)) {
    try {
      if (value <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {}
  }
  dataSourceObj.value[item.id][valueKey] = value;
  emitHandle();
};

watch(() => props.pType, () => {
  const childrenSchema = props.pType === 'array' ? (props.schema || {}) : (props.schema.properties || {});
  dataSource.value = transJsonToList(props.data, -1, 1, [], childrenSchema, props.schema);
  dataSource.value.forEach(i => {
    dataSourceObj.value[i.id] = i;
  });
}, {
  immediate: true
});

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});

const startValidate = ref(false);
const validate = (val = true) => {
  startValidate.value = val;
};

const validateValue = (value, schema) => {
  if (!startValidate.value) {
    return false;
  }
  let _schema = JSON.parse(JSON.stringify(schema));
  _schema = deepDelAttrFromObj(_schema, ['name', 'id', 'level', 'pid', 'idLine', 'checked']);
  const valida = ajv.compile(_schema);
  const valid = valida(value);
  return !valid;
};

const getModelResolve = (models) => {
  dataSource.value.forEach(i => {
    if (i.$ref) {
      const datas = dataSource.value.filter(i => i.idLine.includes(i.id));
      models[i.$ref] = transListToSchema(datas, i.type, i.pid);
      delete models[i.$ref].schema?.$ref;
    }
  });
};

const updateComp = async () => {
  const data = dataSource.value.filter(i => i.$ref);
  for (const i of data) {
    const schema = transListToSchema(i, i.type, i.pid);
    await http.put(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/schema/${i.name}`, schema);
  }
};

defineExpose({ addItem, validate, getModelResolve, updateComp });
</script>
<template>
  <div
    v-for="(item, idx) in dataSource"
    :key="item.id"
    :data-id="item.id"
    :p-id="item.pid"
    class="flex items-center space-x-2">
    <Checkbox
      v-model:checked="item.checked"
      class="invisible" />
    <div class="flex-1 flex justify-end max-w-100">
      <div :style="{width: `calc(100% - ${item.level * 10}px)`, marginLeft: `${item.level * 10}px`}">
        <SelectSchema
          v-if="apiBaseInfo?.serviceId"
          :id="apiBaseInfo?.serviceId"
          v-model:value="item.name"
          mode="pure"
          :placeholder="t('xcan_jsonContent.enterParameterName')"
          :maxLength="400"
          :hideImportBtn="props.hideImportBtn"
          :type="['schemas']"
          :inputProps="{readonly: !!item.$ref || props.viewType || disabledName(item)}"
          @blur="emitHandle"
          @change="(_value, option) => selectModels(_value, option, idx, item)" />
        <Input
          v-else
          v-model:value="item.name"
          :maxLength="400"
          :placeholder="t('xcan_jsonContent.enterParameterName')"
          :readonly="disabledName(item) || props.viewType"
          @blur="emitHandle" />
      </div>
    </div>
    <Select
      v-if="props.paramInType"
      :value="props.paramInType"
      class="w-25"
      :options="inOptions"
      :readonly="true" />
    <Select
      v-model:value="item.type"
      class="w-25"
      :options="itemTypes"
      :readonly="props.viewType || disabledType(item)"
      @change="changeType($event, item, idx)" />
    <div v-if="disabledValue(item)" class="flex-1">
      <Input
        readonly />
    </div>
    <SimpleEditableSelect
      v-else-if="item.enum?.length"
      v-model:value="item[valueKey]"
      class="flex-1"
      :maxLength="4096"
      :readonly="props.viewType"
      :options="item.enum"
      @change="emitHandle"
      @select="emitHandle" />
    <ParamsInput
      v-else
      class="flex-1"
      :maxLength="4096"
      :value="item[valueKey]"
      :disabled="props.viewType"
      :error="validateValue(item[valueKey], item)"
      @blur="handleValueBlur($event, item)" />
    <template v-if="!props.viewType">
      <Button
        v-if="archivedId"
        size="small"
        class="invisible">
        <Icon icon="icon-bianliang" />
      </Button>
      <Button
        size="small"
        :disabled="(item.type !== 'array' && item.type !== 'object' || disabledAdd(item))"
        @click="addItem(item, idx)">
        <Icon icon="icon-jia" />
      </Button>
      <Button
        size="small"
        :disabled="hideDel(item)"
        @click="delItem(item, idx)">
        <Icon icon="icon-shanchuguanbi" />
      </Button>
    </template>
  </div>
</template>
