<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { IconRequired, Input, Modal, Select, SelectEnum, SelectInput } from '@xcan-angus/vue-ui';
import { enumLoader } from '@xcan-angus/tools';
import { Button, Divider } from 'ant-design-vue';
import { CompObj, ComponentsType, ExampleObject, HeaderObject } from './PropsType';
import YAML from 'yaml';

import { services } from 'src/api/tester';

interface Props {
  visible: boolean;
  id: string;
  modalType:'add' | 'edit' | 'view';
  component?: CompObj
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: '',
  modalType: 'view',
  component: undefined
});

const emit = defineEmits<{(e: 'update:visible', value:boolean): void, (e: 'ok'): void}>();

const handleCancel = () => {
  emit('update:visible', false);
};

const compType = ref<ComponentsType>('schemas');

const compTypesEnum = ref<{label:string, value:string, disabled:boolean}[]>([]);
const getCompTypesEnum = async () => {
  const [error, data] = await enumLoader.load('ServicesCompType');
  if (error || !data?.length) {
    return;
  }
  compTypesEnum.value = data.map(item => ({
    label: item.message,
    value: item.value,
    disabled: ['securitySchemes', 'links', 'callbacks', 'extensions', 'pathItems'].includes(item.value)
  }));
};

const compName = ref('');

const examples = ref<ExampleObject>({
  value: '',
  summary: '',
  description: ''
});

const headers = ref<HeaderObject>({
  schema: {
    type: 'string',
    format: undefined,
    'x-xc-value': ''
  },
  description: ''
});

const handleSave = () => {
  if (!compName.value) {
    compNameErr.value = true;
    return;
  }
  switch (compType.value) {
    case 'examples':
      {
        if (!examples.value.value) {
          exampleValueErr.value = true;
          return;
        }
        let params:any = {
          ...examples.value
        };

        if (props.component?.isQuote) {
          params = {
            ...params,
            resolvedRefModels: null
          };
        }

        if (!examples.value.description) {
          delete params.description;
        }

        if (!examples.value.summary) {
          delete params.summary;
        }

        saveHeaderTypeData(params);
      }
      break;
    case 'headers':
      {
        let params = {
          ...headers.value.schema,
          description: headers.value.description
        };

        if (props.component?.isQuote) {
          params = {
            ...params,
            resolvedRefModels: null
          };
        }

        if (!headers.value.description) {
          delete params.description;
        }
        saveHeaderTypeData(params);
      }
      break;
  }
};

const loading = ref(false);
// 保存组件数据
const saveHeaderTypeData = async (params):Promise<void> => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error] = await services.addComponent(props.id, compType.value, compName.value, params);
  loading.value = false;
  if (error) {
    return;
  }
  emit('update:visible', false);
  emit('ok');
};

onMounted(() => {
  getCompTypesEnum();
  if (props.component) {
    setDefaultData();
  }
});

const setDefaultData = () => {
  const model = props.component?.model;
  switch (props.component.type?.value) {
    case 'headers': {
      headers.value.schema.type = model?.schema?.type;
      if (!['object', 'array'].includes(model.schema?.type)) {
        headers.value.schema.format = model?.schema?.format;
      }
      if (model.description) {
        headers.value.description = model?.description;
      }
      if (model?.schema?.['x-xc-value']) {
        headers.value.schema['x-xc-value'] = model?.schema['x-xc-value'];
      }
      break;
    }
    case 'examples': {
      examples.value.value = model?.value;
      if (model.description) {
        examples.value.description = model?.description;
      }
      if (model.summary) {
        examples.value.summary = model?.summary;
      }
      break;
    }
  }
};

const getExcludes = (option:{label:string, value:string}):boolean => {
  return ['object', 'array'].includes(option.value);
};

const compNameErr = ref(false);
const compNameChange = (event:ChangeEvent) => {
  const value = event.target.value;
  compNameErr.value = !value;
};

const exampleValueErr = ref(false);
const exampleValueChange = (event:ChangeEvent) => {
  const value = event.target.value;
  exampleValueErr.value = !value;
};

const enumKeyMap = {
  integer: 'IntegerParameterFormat',
  number: 'NumberParameterFormat'
};

const openEdit = ref(false);

const component = ref<CompObj>();
const cancelRef = () => {
  component.value.quoteName = '';
  openEdit.value = true;
};

const recoveryRef = () => {
  component.value.quoteName = props.component.quoteName;
  openEdit.value = false;
  setDefaultData();
};

onMounted(() => {
  if (props.component) {
    component.value = JSON.parse(JSON.stringify(props.component));
    compName.value = props.component.key;
    compType.value = props.component?.type?.value;
  }
});

const title = computed(() => {
  switch (props.modalType) {
    case 'view':
      return '详情';
    case 'edit':
      return '编辑';
    case 'add':
      return '添加';
    default:
      return '详情';
  }
});

const modeContent = computed(() => {
  if (!props.component) {
    return;
  }
  return YAML.stringify(props.component.model, null, 2);
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="title"
    :footer="props.modalType === 'view'?null:''"
    :width="680"
    @cancel="handleCancel">
    <div class="text-3 text-text-sub-content">
      <div class="flex flex-col">
        <div><IconRequired />类型</div>
        <Select
          v-model:value="compType"
          :disabled="props.modalType === 'view' || props.modalType === 'edit'"
          :options="compTypesEnum"
          placeholder="选择类型"
          class="w-full mb-2" />
        <div><IconRequired />名称</div>
        <Input
          v-model:value="compName"
          :disabled="props.modalType === 'view' || props.modalType === 'edit'"
          :maxlength="400"
          :error="compNameErr"
          placeholder="输入名称，最多400字符"
          class="w-full"
          @change="compNameChange" />
        <template v-if="props.component?.isQuote">
          <div class="pl-1.5 mt-2">引用</div>
          <Input :value="component?.quoteName" disabled>
            <template #suffix>
              <a
                class="text-3 hover:text-text-link-hover text-text-content"
                @click="cancelRef">取消引用</a>
              <Divider type="vertical" />
              <a
                class="text-3 hover:text-text-link-hover text-text-content"
                @click="recoveryRef">恢复引用</a>
            </template>
          </Input>
        </template>
        <template v-if="props.modalType === 'view' && !['headers','examples'].includes(compType)">
          <div
            class="whitespace-pre border border-border-divider p-2 rounded mt-5 overflow-y-auto text-text-content"
            style="max-height: 500px;scrollbar-gutter: stable;">
            {{ modeContent }}
          </div>
        </template>
        <template v-else>
          <template v-if="compType === 'examples'">
            <div class="mt-2 pl-1.75">摘要</div>
            <Input
              v-model:value="examples.summary"
              :maxlength="400"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component.isQuote)"
              type="textarea"
              placeholder="输入摘要，最多400字符"
              class="w-full" />
            <div class="mt-2 pl-1.75">描述</div>
            <Input
              v-model:value="examples.description"
              :maxlength="2000"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component.isQuote)"
              type="textarea"
              placeholder="输入描述，最多2000字符，支持Markdown语法"
              class="w-full" />
            <div class="mt-2"><IconRequired />示例</div>
            <Input
              v-model:value="examples.value"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component.isQuote)"
              :maxlength="400"
              :autoSize="{ minRows: 8}"
              :error="exampleValueErr"
              type="textarea"
              placeholder="输入示例"
              class="w-full"
              @change="exampleValueChange" />
          </template>
          <template v-if="compType === 'headers'">
            <div class="mt-2"><IconRequired />属性类型</div>
            <SelectEnum
              v-model:value="headers.schema.type"
              :excludes="getExcludes"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component.isQuote)"
              size="small"
              enumKey="ParameterType"
              placeholder="选择类型"
              class="w-full" />
            <div class="mt-2 pl-1.75">格式</div>
            <template v-if="['integer','number'].includes(headers.schema.type)">
              <SelectEnum
                v-model:value="headers.schema.format"
                :enumKey="enumKeyMap[headers.schema.type]"
                :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component.isQuote)"
                size="small"
                placeholder="格式"
                class="w-full" />
            </template>
            <template v-else-if="headers.schema.type === 'string'">
              <SelectInput
                v-model:value="headers.schema.format"
                enumKey="StringParameterFormat"
                :fieldNames="{label:'message',value:'value'}"
                :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component.isQuote)"
                placeholder="格式"
                class="w-full" />
            </template>
            <template v-else>
              <Input
                :value="headers.schema.type"
                disabled
                class="w-full"
                @change="()=>{headers.schema.format = headers.schema.type}" />
            </template>
            <div class="pl-1.75 mt-2">值</div>
            <Input
              v-model:value="headers.schema['x-xc-value']"
              :maxlength="400"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component.isQuote)"
              placeholder="输入值，最多4096字符"
              class="w-full" />
            <div class="mt-2 pl-1.75">描述</div>
            <Input
              v-model:value="headers.description"
              :maxlength="2000"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component.isQuote)"
              :autoSize="{ minRows: 8}"
              type="textarea"
              placeholder="输入描述，最多2000字符，支持Markdown语法"
              class="w-full" />
          </template>
        </template>
      </div>
    </div>
    <template v-if="props.modalType !=='view'" #footer>
      <Button size="small" @click="handleCancel">取消</Button>
      <Button
        :disabled="!openEdit && modalType === 'edit' && props.component.isQuote"
        type="primary"
        size="small"
        :loading="loading"
        @click="handleSave">
        确定
      </Button>
    </template>
  </Modal>
</template>
