<script setup lang="ts">
import { watch, ref, inject } from 'vue';
import { Button, Checkbox } from 'ant-design-vue';
import { Icon, Input, Select } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
const { t }  = useI18n();

import { http, TESTER } from '@xcan-angus/infra';
import { qsJsonToParamList } from '../../ApiUtils';

import ApiUpload from '../upload/index.vue';
import { itemTypes, formDataTypes } from './interface';
// import { deepDelAttrFromObj, validateType } from '@/views/apis/components/api-item/utils';
import SimpleEditableSelect from '../../SimpleEditableSelect/index.vue';

import { ParamsItem, API_EXTENSION_KEY, deepDelAttrFromObj, validateType } from '@xcan-angus/vue-ui/ApiUtils/index';

export interface Props {
  value: ParamsItem[],
  useModel: boolean;
  hasFileType?: boolean;
  formFileSize: number;
  maxFileSize: number;
  hideImportBtn: boolean;
  disabled: boolean;
  viewType?: boolean;
  fieldNames?: { valueKey: string; enabledKey: string, fileNameKey: string };
}

// const { valueKey, enabledKey } = API_EXTENSION_KEY;
const apiBaseInfo = inject('apiBaseInfo', ref());
// const archivedId = inject('archivedId', ref());
const jsContentRef = ref<any[]>([]);

const props = withDefaults(defineProps<Props>(), {
  useModel: false,
  hasFileType: false,
  hideImportBtn: false,
  disabled: false,
  viewType: false,
  fieldNames: () => ({ valueKey: API_EXTENSION_KEY.valueKey, enabledKey: API_EXTENSION_KEY.enabledKey, fileNameKey: API_EXTENSION_KEY.fileNameKey })
});

// eslint-disable-next-line vue/no-setup-props-destructure
const valueKey = props.fieldNames.valueKey;
// eslint-disable-next-line vue/no-setup-props-destructure
const enabledKey = props.fieldNames.enabledKey;

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
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

const enterHandle = (e): void => {
  if (e.key !== 'Enter') {
    return;
  }

  e.target.blur();
};

const handleValueBlur = (target: HTMLElement, index:number, data:ParamsItem):void => {
  let value = target?.innerText || target?.target?.value;
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

const handleBlur = (e, index:number, data:ParamsItem, key:string):void => {
  const value = e.target.value?.trim();
  if (value === data[key]) {
    return;
  }

  const temp = { ...data, [key]: value } as ParamsItem;
  changeEmit(index, temp);
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
const handleChecked = (e, index: number, data:ParamsItem) => {
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

// const setVariableLoading = reactive({});
// // 设为变量
// const setToVariable = async (data:ParamsItem):void => {
//   if (setVariableLoading[data.name as string]) {
//     return;
//   }
//   setVariableLoading[data.name as string] = true;
//   if (!variableNameReg.test(data.name as string)) {
//     notification.warning('名称不符合变量要求,允许数字字母!@$%^&*()_-+=./等');
//     return;
//   }

//   const value = typeof data[valueKey] === 'object' ? JSON.stringify(data[valueKey]) : data[valueKey];
//   await variableApi.addVariables({ name: data.name, targetId: archivedId.value, scope: 'CURRENT', targetType: 'API', enabled: true, value });
//   // const temp = { ...data, [exportVariableKey]: !data[exportVariableKey] } as ParamsItem;
//   // changeEmit(index, temp);
//   setVariableLoading[data.name as string] = false;
// };

const handleDel = (index:number):void => {
  sizes.value.splice(index, 1);
  emit('del', index);
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
  emit('change', index, data);
};

watch(() => props.value, (newValue) => {
  formData.value = [];
  newValue.forEach((i, idx) => {
    let type = i.type;
    if (i.format === 'binary') {
      type = 'file';
      if (i.type === 'array') {
        type = 'file(array)';
      }
      formData.value.push({ ...i, key: i.key || getKey(idx), type });
      return;
    }
    if (props.hasFileType) {
      if (type === 'object') {
        const jsonSchema = {
          [i.name as string]: i[valueKey]
        };
        if (typeof i[valueKey] === 'object' && JSON.stringify(i[valueKey]) !== '{}') {
          formData.value.push(...qsJsonToParamList(jsonSchema).map(item => ({ ...item, [enabledKey]: i[enabledKey] })));
        } else {
          formData.value.push({ ...i, key: i.key || getKey(idx), type: 'string' });
        }
        return;
      }
      if (type === 'array' && i.format !== 'binary') {
        if (i[valueKey].length) {
          const jsonSchema = {
            [i.name as string]: i[valueKey]
          };
          formData.value.push(...qsJsonToParamList(jsonSchema).map(item => ({ ...item, [enabledKey]: i[enabledKey] })));
        } else {
          formData.value.push({ ...i, key: i.key || getKey(idx), type: 'string' });
        }
        return;
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
    formData.value.push({ ...i, key: i.key || getKey(idx), type });
    // return { ...i, key: i.key || getKey(idx), type };
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
  emit('update:formFileSize', result);
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
      await http.put(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/schema/${formData.value[i].name}`, formData.value[i].schema);
    }
    if (jsContentRef.value[i]) {
      await jsContentRef.value[i].updateComp();
    }
  }
};

defineExpose({ getModelResolve, updateComp, validate: validateContents });

</script>
<template>
  <div class="space-y-3 relative" :class="{'pre-sign': props.useModel, 'not-button': props.viewType}">
    <div
      v-for="(item,index) in formData"
      :key="item.key"
      class="space-y-3"
      :class="{'opacity-50': !item[enabledKey]}">
      <div class="flex flex-nowrap items-center mb-3 whitespace-nowrap space-x-2">
        <Checkbox
          :disabled="(!item.name && !item.value) || !!props.useModel"
          :checked="item[enabledKey] && (!!item.name || !!item.value)"
          @change="handleChecked($event, index, item)" />
        <div class="max-w-100 flex flex-col flex-1">
          <Input
            :placeholder="t('xcan_apiBody.enterParameterName')"
            :value="item.name"
            :allowClear="false"
            :readonly="!!props.useModel || props.disabled"
            size="small"
            @blur="handleBlur($event,index,item,'name')"
            @keypress="enterHandle" />
        </div>
        <div class="flex flex-col w-25">
          <Select
            v-model:value="item.type"
            class="w-full"
            dropdownClassName="api-select-dropdown"
            :placeholder="t('xcan_apiBody.selectParameterType')"
            :readonly="!!item.$ref || !!props.useModel || props.disabled"
            :options="props.hasFileType ? formDataTypes : itemTypes"
            :allowClear="false"
            @change="typeChange($event, index, item)" />
        </div>
        <div v-if="['file(array)', 'file'].includes(item.type)" class="flex flex-col flex-1 min-w-50">
          <!-- <span v-if="index === 0" class="mb-3 text-3 leading-3 text-theme-sub-content select-none">参数值</span> -->
          <div class="min-h-7 border rounded border-border-input file-wrapper flex bg-white">
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
            :placeholder="t('xcan_apiBody.enterParameterValue')"
            :options="item.enum"
            :value="item[valueKey] || item.schema?.[valueKey]"
            @blur="handleValueBlur($event,index,item )"
            @select="changeEmit(index, { ...item, [valueKey]: $event })" />
          <!-- <param-input
            v-else-if="!['array(json)', 'array(xml)', 'object(xml)', 'object(json)', 'array', 'object', 'file', 'file(array)'].includes(item.type)"
            placeholder="请输入调试值"
            :value="item[valueKey]"
            :error="getErrValue(item)"
            @blur="handleValueBlur($event,index,item )" /> -->
          <Input
            v-else
            v-model:value="item[valueKey]"
            :placeholder="t('xcan_apiBody.enterParameterValue')"
            :maxlength="4096"
            :error="getErrValue(item)"
            @blur="handleValueBlur($event,index,item )" />
        </div>
        <!-- <Button
          v-if="archivedId"
          type="primary"
          size="small"
          :disabled="!!props.useModel || !item.name || setVariableLoading[item.name]"
          @click="setToVariable(item)">
          <Icon icon="icon-bianliang" :title="t('xcan_apiBody.setAsVariable')" />
        </Button> -->
        <template v-if="!props.viewType">
          <Button
            size="small"
            class="w-7 p-0"
            type="default"
            :disabled="!!props.useModel || (!item[valueKey] && !item.name)"
            @click="handleDel(index)">
            <Icon icon="icon-shanchuguanbi" />
          </Button>
        </template>
      </div>
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
  right: 70px;
  width: 1px;
  height: 100%;
  border-left: 1px solid #1890ff;
}

.not-button::after {
  right: 0;
}
</style>
<style>
.ant-select-dropdown-sm .ant-select-item.ant-select-item-option .ant-select-item-option-content {
  padding-right: 10px;
}
</style>
