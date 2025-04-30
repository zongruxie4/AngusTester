<script setup lang="ts">
import { inject, reactive, ref, watch } from 'vue';
import { Button, Checkbox } from 'ant-design-vue';
import { Icon, Input, notification, Select, SelectSchema } from '@xcan-angus/vue-ui';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { ParamInput } from '@xcan-angus/vue-ui';

import ApiUpload from '@/views/apis/services/apiHttp/upload/index.vue';
import { ParamsItem } from '../../interface';
import { formDataTypes, itemTypes } from './interface';
import { API_EXTENSION_KEY, getModelDataByRef, variableNameReg } from '@/views/apis/utils';
import JsonContent from '@/views/apis/services/apiHttp/requestBody/json/index.vue';
import { services, variable as variableApi } from '@/api/altester';
import { deconstruct } from '@/utils/swagger';
import { deepDelAttrFromObj, validateType } from '@/views/apis/services/apiHttp/utils';
import SimpleEditableSelect from '@/components/apis/editableSelector/index.vue';

const { valueKey, enabledKey } = API_EXTENSION_KEY;
const apiBaseInfo = inject('apiBaseInfo', ref());
const archivedId = inject('archivedId', ref());
const globalConfigs = inject('globalConfigs', { VITE_API_PARAMETER_NAME_LENGTH: 400, VITE_API_PARAMETER_VALUE_LENGTH: 4096 });
const jsContentRef = ref<any[]>([]);
interface Props {
  value: ParamsItem[],
  useModel: boolean;
  hasFileType?: boolean;
  formFileSize: number;
  maxFileSize: number;
}

const props = withDefaults(defineProps<Props>(), {
  useModel: false,
  hasFileType: false
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'change', index:number, data:ParamsItem): void,
  (e: 'del', index:number): void,
  (e: 'update:formFileSize', value:number):void
}>();

// const state = reactive<State>({
//   formData: []
// });

const formData = ref<ParamsItem[]>([]);

// const getText = (flag: boolean | undefined): string => {
//   return flag ? '取消变量' : '设为变量';
// };

const getKey = (index?:number):symbol => {
  return Symbol(index);
};

const enterHandle = (e: ChangeEvent): void => {
  if (e.key !== 'Enter') {
    return;
  }

  e.target.blur();
};

const handleValueBlur = (target: HTMLElement, index:number, data:ParamsItem):void => {
  let value = target?.innerText;
  if (['integer', 'number', 'boolean'].includes(data?.type)) {
    try {
      if (value <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {}
  }
  const temp = { ...data, [valueKey]: value } as ParamsItem;
  changeEmit(index, temp);
};

// const handleValueChange = (value: string, index:number, data: ParamsItem):void => {
//   const temp = { ...data, value } as ParamsItem;
//   changeEmit(index, temp);
// };

const handleBlur = (e:ChangeEvent, index:number, data:ParamsItem, key:string):void => {
  const value = e.target.value?.trim();
  if (value === data[key]) {
    return;
  }

  const temp = { ...data, [key]: value } as ParamsItem;
  changeEmit(index, temp);
};
const getModelData = async (ref) => {
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

const selectModels = async (_value, option, index, item) => {
  if (option) {
    // const model = JSON.parse(option.model);
    const model = await getModelData(option.ref);
    const value = SwaggerUI.extension.sampleFromSchemaGeneric(model, { useValue: true });
    const temp = {
      ...item,
      ...model,
      [valueKey]: value
    };
    if (option.readonly) {
      temp.$ref = option.ref;
    }
    changeEmit(index, temp);
    // dataSource.value[index].type = model.type;
    // dataSource.value[index] = {
    //   ...dataSource.value[index],
    //   ...model
    // };

    // const childSchema = model.type === 'object' ? model.properties : model.type === 'array' ? model.items : {};
    // const data = transJsonToList(value, item.id, item.level + 1, [dataSource.value[index]], childSchema);
    // dataSource.value.splice(index, 1, ...data);
    // data.forEach(i => {
    //   dataSourceObj.value[i.id] = i;
    // });
    // emitHandle();
  } else {
    // dataSource.value[index].name = value;
  }
};

const sizes = ref<number[]>([]);
const uploadChange = ({ file, size }, index:number, data:ParamsItem):void => {
  sizes.value[index] = size;
  if (file) {
    const type = Array.isArray(file) ? 'array' : 'string';
    const temp = {
      ...data,
      [valueKey]: file,
      format: 'binary',
      type
    };
    if (type === 'array') {
      temp.items = {
        type: 'string',
        format: 'binary'
      };
      // temp[formContentTypeKey] = file.map(i => i[formContentTypeKey]).join(';');
    } else {
      delete temp.items;
      // temp[formContentTypeKey] = file[formContentTypeKey];
    }
    changeEmit(index, temp);
  } else {
    const temp = {
      ...data,
      [valueKey]: undefined,
      format: 'binary',
      type: 'string'
    };
    delete temp.items;
    // temp[formContentTypeKey] = undefined;
    changeEmit(index, temp);
  }
};

// 启用禁用
const handleChecked = (e:ChangeEvent, index: number, data:ParamsItem) => {
  const checked = e.target.checked;
  const temp = { ...data, [enabledKey]: checked } as ParamsItem;
  changeEmit(index, temp);
  if (!checked && validated.value) {
    jsContentRef.value[index] && jsContentRef.value[index].validate(false);
  }
};

// 数据类型变更
const typeChange = (type: string, index: number, data:ParamsItem) => {
  let value;
  sizes.value[index] = 0;
  if (type !== 'file' && type !== 'file(array)') {
    delete data.format;
    if (type.includes('object')) {
      value = { '': '' };
      type = 'object';
    }
    if (type.includes('array')) {
      value = [''];
      type = 'array';
    }
    if (type.includes('xml')) {
      data.format = 'xml';
    }
    if (type.includes('json')) {
      data.format = 'json';
    }
  } else {
    data.format = 'binary';
    if (type === 'file(array)') {
      type = 'array';
      value = [];
    } else {
      type = 'string';
      value = {};
    }
  }
  const temp = { ...data, [valueKey]: value, type };
  changeEmit(index, temp);
};

const setVariableLoading = reactive({});
// 设为变量
const setToVariable = async (data:ParamsItem):void => {
  if (setVariableLoading[data.name as string]) {
    return;
  }
  setVariableLoading[data.name as string] = true;
  if (!variableNameReg.test(data.name as string)) {
    notification.warning('名称不符合变量要求,允许数字字母!@$%^&*()_-+=./等');
    return;
  }

  const value = typeof data[valueKey] === 'object' ? JSON.stringify(data[valueKey]) : data[valueKey];
  const [error] = await variableApi.addVariables({ name: data.name, targetId: archivedId.value, scope: 'CURRENT', targetType: 'API', enabled: true, value });
  // const temp = { ...data, [exportVariableFlagKey]: !data[exportVariableFlagKey] } as ParamsItem;
  // changeEmit(index, temp);
  setVariableLoading[data.name as string] = false;
  if (!error) {
    notification.success('设置变量成功');
  }
};

const handleDel = (index:number):void => {
  sizes.value.splice(index, 1);
  emits('del', index);
};

const changeEmit = (index:number, data:ParamsItem):void => {
  if (props.hasFileType) {
    if (data.format === 'binary') {
      if (data.type === 'file') {
        data.type = 'string';
      }
      if (data.type === 'file(array)') {
        data.type = 'array';
      }
    } else {
      if (data.type === 'object(json)') {
        data.format = 'json';
        data.type = 'object';
      }
      if (data.type === 'object(xml)') {
        data.format = 'xml';
        data.type = 'object';
      }
      if (data.type === 'array(json)') {
        data.format = 'json';
        data.type = 'array';
      }
      if (data.type === 'array(xml)') {
        data.format = 'xml';
        data.type = 'array';
      }
    }
  }
  emits('change', index, data);
};

const changeSchema = (shema, item, index) => {
  emits('change', index, { ...item, ...shema });
};

const getItemSchema = (item) => {
  const type = item.type.split('(')[0];
  return {
    ...item,
    type
  };
};

const addChild = (pItem, idx) => {
  jsContentRef.value[idx].addItem({ type: pItem.type.split('(')[0], id: -1, idLine: [-1], level: 0 });
};

watch(() => props.value, (newValue) => {
  formData.value = newValue.map((i, idx) => {
    let type = i.type;
    if (i.format === 'binary') {
      type = 'file';
      if (i.type === 'array') {
        type = 'file(array)';
      }
    }
    if (props.hasFileType) {
      if (type === 'object') {
        type = 'object(json)';
        if (i.format === 'xml') {
          type = 'object(xml)';
        }
      }
      if (type === 'array' && i.format !== 'binary') {
        type = 'array(json)';
        if (i.format === 'xml') {
          type = 'array(xml)';
        }
      }
    }
    if (!i.type) {
      if (i.properties) {
        type = 'object(json)';
      } else if (i.items) {
        type = 'array(json)';
      } else {
        type = 'string';
      }
    }
    return { ...i, key: i.key || getKey(idx), type };
  });
}, {
  deep: true,
  immediate: true
});

const getItemMaxFileSize = (index) => {
  const currenSize = sizes.value[index] || 0;
  return props.maxFileSize - (props.formFileSize - currenSize);
};

watch(() => sizes.value, () => {
  if (!props.hasFileType) {
    return;
  }
  const result = sizes.value.reduce((pre, current) => {
    return pre + current;
  }, 0);
  emits('update:formFileSize', result);
}, {
  deep: true
});

const validated = ref(false);
const validateContents = async (val = true) => {
  validated.value = val;
  for (const idx in jsContentRef.value) {
    if (formData.value[idx][enabledKey]) {
      jsContentRef.value[idx].validate(val);
    }
  }
};

const getErrValue = (item) => {
  if (!validated.value || !item.name || !item[enabledKey]) {
    return false;
  }
  let type = item.type;
  if (type.includes('(')) {
    type = type.split('(')[0];
  }
  const errors = validateType(item[valueKey], deepDelAttrFromObj({ ...item, type }, ['name', enabledKey, 'key']));
  return !!errors?.length;
};

const getModelResolve = (models) => {
  formData.value.forEach((i, index) => {
    if (i.$ref) {
      models[i.$ref] = JSON.parse(JSON.stringify(i.schema));
      delete models[i.$ref].schema.$ref;
    }
    jsContentRef.value[index] && jsContentRef.value[index].getModelResolve(models);
  });
};

const updateComp = async () => {
  for (let i = 0; i < formData.value.length; i++) {
    if (formData.value[i].$ref) {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { $ref, ...content } = formData.value[i];
      await services.addComponent(apiBaseInfo.value.serviceId, 'schema', formData.value[i].name, { ...content, [valueKey]: content[valueKey] });
    }
    if (jsContentRef.value[i]) {
      await jsContentRef.value[i].updateComp();
    }
  }
};

defineExpose({ getModelResolve, updateComp, validate: validateContents });

</script>
<template>
  <div class="space-y-3 relative min-w-220" :class="{'pre-sign': props.useModel}">
    <div
      v-for="(item,index) in formData "
      :key="item.key"
      class="space-y-3"
      :class="{'opacity-50': !item[enabledKey]}">
      <div class="flex flex-nowrap items-center mb-3 whitespace-nowrap space-x-2">
        <Checkbox
          :disabled="(!item.name && !item.value) || !!props.useModel"
          :checked="item[enabledKey] && (!!item.name || !!item.value)"
          @change="handleChecked($event, index, item)" />
        <div class="w-100 flex flex-col flex-shrink-0">
          <SelectSchema
            v-if="apiBaseInfo?.serviceId"
            :id="apiBaseInfo?.serviceId"
            v-model:value="item.name"
            placeholder="请输入参数名称"
            mode="pure"
            :type="['schemas']"
            :maxLength="globalConfigs.VITE_API_PARAMETER_NAME_LENGTH"
            :disabled="!!item.$ref || !!props.useModel"
            @blur="handleBlur($event,index,item,'name')"
            @change="(_value, option) => selectModels(_value, option, index, item)" />
          <Input
            v-else
            placeholder="请输入参数名称"
            :value="item.name"
            :allowClear="false"
            :disabled="!!props.useModel"
            :maxLength="globalConfigs.VITE_API_PARAMETER_NAME_LENGTH"
            size="small"
            @blur="handleBlur($event,index,item,'name')"
            @keypress="enterHandle" />
        </div>
        <div class="flex flex-col w-25 flex-shrink-0">
          <Select
            v-model:value="item.type"
            class="w-full"
            dropdownClassName="api-select-dropdown"
            placeholder="请选择参数类型"
            :disabled="!!item.$ref || !!props.useModel"
            :options="props.hasFileType ? formDataTypes : itemTypes"
            :allowClear="false"
            @change="typeChange($event, index, item)" />
        </div>
        <div v-if="['file(array)', 'file'].includes(item.type)" class="flex flex-col flex-1 min-w-50">
          <!-- <span v-if="index === 0" class="mb-3 text-3 leading-3 text-text-sub-content select-none">参数值</span> -->
          <div class="min-h-7 border rounded border-border-input file-wrapper flex">
            <ApiUpload
              :key="item.name"
              :value="item[valueKey]"
              :maxFileSize="getItemMaxFileSize(index)"
              :type="item.type"
              :sizes="sizes"
              @change="uploadChange($event,index,item)" />
          </div>
        </div>
        <div v-else class="flex flex-col flex-1">
          <SimpleEditableSelect
            v-if="item.enum"
            :placeholder="`请输入调试值，最大支持${globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH}个字符`"
            :maxLength="globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH"
            :options="item.enum"
            :value="item[valueKey] || item.schema?.[valueKey]"
            @blur="handleValueBlur($event,index,item )"
            @select="changeEmit(index, { ...item, [valueKey]: $event, schema: {...item?.schema|| {}, [valueKey]: $event} })" />
          <ParamInput
            v-else-if="!['array(json)', 'array(xml)', 'object(xml)', 'object(json)', 'array', 'object', 'file', 'file(array)'].includes(item.type)"
            :placeholder="`请输入调试值，最大支持${globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH}个字符`"
            :maxLength="globalConfigs.VITE_API_PARAMETER_VALUE_LENGTH"
            :value="item[valueKey]"
            :error="getErrValue(item)"
            @blur="handleValueBlur($event,index,item )" />
          <Input v-else disabled />
        </div>
        <Button
          v-if="archivedId"
          type="primary"
          size="small"
          :disabled="!!props.useModel || !item.name || setVariableLoading[item.name]"
          @click="setToVariable(item)">
          <Icon icon="icon-bianliang" title="设为变量" />
        </Button>
        <Button
          size="small"
          :disabled="!['array(json)', 'array(xml)', 'object(xml)', 'object(json)', 'array', 'object'].includes(item.type) || (['object(xml)', 'object(json), object'].includes(item.type) && item.$ref)"
          @click="addChild(item, index)">
          <Icon icon="icon-jia" />
        </Button>
        <Button
          size="small"
          class="w-7 p-0"
          type="default"
          :disabled="!!props.useModel || (!item[valueKey] && !item.name)"
          @click="handleDel(index)">
          <Icon icon="icon-shanchuguanbi" />
        </Button>
      </div>
      <JsonContent
        v-if="['array(json)', 'array(xml)', 'object(xml)', 'object(json)', 'array', 'object'].includes(item.type)"
        :ref="dom => jsContentRef[index] = dom"
        v-model:data="item[valueKey]"
        :schema="getItemSchema(item)"
        :disabled="!!item.$ref || !!useModel"
        :pType="item.type.split('(')[0]"
        @change="changeSchema($event, item, index)" />
    </div>
  </div>
</template>
<style scoped>
:deep(.file-wrapper) .ant-upload-list {
  @apply flex flex-wrap;
}

.pre-sign::before {
  content: "";
  display: inline-block;
  position: absolute;
  z-index: 0;
  top: 0;
  left: 22px;
  width: 1px;
  height: 100%;
  border-left: 1px solid #1890ff;
}

.pre-sign::after {
  content: "";
  display: inline-block;
  position: absolute;
  z-index: 0;
  top: 0;
  right: 106px;
  width: 1px;
  height: 100%;
  border-left: 1px solid #1890ff;
}
</style>
<style>
.ant-select-dropdown-sm .ant-select-item.ant-select-item-option .ant-select-item-option-content {
  padding-right: 10px;
}
</style>
