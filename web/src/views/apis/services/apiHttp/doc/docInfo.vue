<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Icon, IconRequired, Input, Select, SelectInput } from '@xcan-angus/vue-ui';
import { Button, CheckboxGroup, Dropdown, Popover } from 'ant-design-vue';

import { API_EXTENSION_KEY } from '@/views/apis/utils';

interface Props {
  data: any[];
  editable: boolean;
  tab: string;
}

const { valueKey, enabledKey } = API_EXTENSION_KEY;

const props = withDefaults(defineProps<Props>(), {
  data: () => ([]),
  editable: false,
  tab: 'parameters'
});
const parameterTypeOptions = [
  {
    value: 'string',
    label: 'string'
  },
  {
    value: 'interger',
    label: 'interger'
  },
  {
    value: 'number',
    label: 'number'
  }
  // {
  //   value: 'boolean',
  //   label: 'boolean'
  // }
  // {
  //   value: 'null',
  //   label: 'null'
  // }
  // {
  //   value: 'object',
  //   label: 'object'
  // },
  // {
  //   value: 'array',
  //   label: 'array'
  // }
];
const requiredOpt = [
  {
    value: true,
    label: '必填'
  },
  {
    value: false,
    label: '可选'
  }
];

const visible = ref(false);
const checkedCols = ref(['parameters', 'type', 'required', 'default', 'description']);
const itemCol = [
  {
    label: '参数名称',
    value: 'parameters',
    checked: true,
    disabled: true
  },
  {
    label: '类型',
    value: 'type',
    checked: true,
    disabled: true
  },
  {
    label: '是否必填',
    value: 'required',
    checked: true,
    disabled: true
  },
  {
    label: '默认值',
    value: 'default',
    checked: true,
    disabled: true
  },
  {
    label: '示例值',
    value: 'example'
  },
  {
    label: '最小值',
    value: 'min'
  },
  {
    label: '最大值',
    value: 'max'
  },
  {
    label: '最小长度',
    value: 'minLength'
  },
  {
    label: '最大长度',
    value: 'maxLength'
  },
  {
    label: '格式',
    value: 'format'
  },
  {
    label: '是否弃用',
    value: 'deprecated'
  },
  // {
  //   label: '正则',
  //   value: 'reg'
  // },
  {
    label: '描述',
    value: 'description',
    disabled: true
  }
];

const dataSource = ref<any[]>([]);
const editData = ref<any[]>([]);
// const activeKey = ref('parameter');

const getSchema = () => {
  return editData.value.filter(i => !!i.name);
};

const handleAdd = () => {
  editData.value.push({
    name: '',
    in: 'query',
    [enabledKey]: true,
    entensions: {
      [valueKey]: ''
    },
    required: '',
    schema: {}
  });
};

const handleRduce = (index) => {
  editData.value.splice(index, 1);
};

watch(() => props.data, newValue => {
  dataSource.value = newValue;
}, {
  immediate: true
});

watch(() => props.editable, newValue => {
  if (newValue) {
    editData.value = JSON.parse(JSON.stringify(dataSource.value));
    editData.value.forEach(i => {
      if (!i.schema) {
        i.schema = {};
      }
    });
  }
});

defineExpose({
  getSchema
});

</script>
<template>
  <div class="text-3">
    <template v-if="!props.editable">
      <div class="flex">
        <span class="flex-1">参数名称</span>
        <span class="flex-1">类型</span>
        <span class="flex-1">默认值</span>
      </div>
      <div
        v-for="(item, key) in dataSource"
        :key="key"
        class="border-t">
        <div class="flex py-1">
          <div class="flex-1">
            <Popover>
              <span class="px-1 py-0.5 border text-blue-1 bg-bg-selected"><IconRequired v-if="item.required" />{{ item.name }}</span>
              <template #content>
                <ul class="text-3">
                  <li><span class="w-20 inline-block">格式：</span>{{ item.schema?.format || '--' }}</li>
                  <li><span class="w-20 inline-block">示例值: </span>{{ item.schema?.example || '--' }}</li>
                  <li><span class="w-20 inline-block">枚举值：</span>{{ item.schema?.enum || '--' }}</li>
                  <li><span class="w-20 inline-block">最小值：</span>{{ item.schema?.minItems || '--' }}</li>
                  <li><span class="w-20 inline-block">最大值：</span>{{ item.schema?.maxItems || '--' }}</li>
                  <li><span class="w-20 inline-block">最小长度：</span>{{ item.schema?.minLength || '--' }}</li>
                  <li><span class="w-20 inline-block">最大长度：</span>{{ item.schema?.maxLength || '--' }}</li>
                </ul>
              </template>
            </Popover>
          </div>
          <span class="flex-1">{{ item.schema?.type || '' }}</span>
          <span class="flex-1">{{ item.schema?.default || '' }}</span>
        </div>
        {{ item.description }}
      </div>
    </template>
    <template v-else>
      <!-- <Tabs :activeKey="props.tab">
        <TabPane key="parameters" tab="查询参数"></TabPane>
        <TabPane key="request-body" tab="请求体"></TabPane>
        <TabPane key="header" tab="请求头"></TabPane>
        <TabPane key="authorization" tab="Authorization"></TabPane>
        <template #rightExtra>
          <Dropdown v-model:visible="visible" placement="bottomRight">
            <Icon
              icon="icon-gengduocaozuo"
              class="cursor-pointer"
              @click.prevent />
            <template #overlay>
              <div
                class="flex flex-col bg-white border p-1">
                <CheckboxGroup
                  v-model:value="checkedCols"
                  :options="itemCol"
                  class="flex flex-col" />
              </div>
            </template>
          </Dropdown>
        </template>
      </Tabs> -->
      <Dropdown v-model:visible="visible" placement="bottomRight">
        <Icon
          icon="icon-gengduocaozuo"
          class="cursor-pointer text-4 mb-2"
          @click.prevent />
        <template #overlay>
          <div
            class="flex flex-col bg-white border p-1">
            <CheckboxGroup
              v-model:value="checkedCols"
              :options="itemCol"
              class="flex flex-col" />
          </div>
        </template>
      </Dropdown>
      <div class="overflow-auto">
        <div
          v-for="(item, key) in editData"
          :key="key"
          class="flex mb-1">
          <SelectInput
            v-model:value="item.name"
            placeholder="参数名称"
            class="min-w-25"
            mode="pure" />
          <!-- <Input
            v-model:value="item.name"
            placeholder="参数名称"
            class="min-w-25" /> -->
          <Select
            v-model:value="item.schema.type"
            placeholder="类型"
            class="min-w-25"
            :options="parameterTypeOptions" />
          <Select
            v-model:value="item.required"
            placeholder="是否必填"
            class="min-w-25"
            :options="requiredOpt" />
          <Input
            v-if="checkedCols.includes('default')"
            v-model:value="item.schema.default"
            placeholder="默认值"
            class="min-w-25" />
          <Input
            v-if="checkedCols.includes('example')"
            v-model:value="item.schema.example"
            placeholder="示例值"
            class="min-w-25" />
          <Input
            v-if="checkedCols.includes('min')"
            v-model:value="item.schema.minItems"
            placeholder="最小值"
            class="min-w-25" />
          <Input
            v-if="checkedCols.includes('max')"
            v-model:value="item.schema.maxItems"
            placeholder="最大值"
            class="min-w-25" />
          <Input
            v-if="checkedCols.includes('minLength')"
            v-model:value="item.schema.minLength"
            placeholder="最小长度"
            class="min-w-25" />
          <Input
            v-if="checkedCols.includes('maxLength')"
            v-model:value="item.schema.maxLength"
            placeholder="最小长度"
            class="min-w-25" />
          <Input
            v-if="checkedCols.includes('format')"
            v-model:value="item.schema.format"
            placeholder="格式"
            class="min-w-25" />
          <Select
            v-if="checkedCols.includes('deprecated')"
            v-model:value="item.deprecated"
            :options="[{value: true, label: '是'}, {value: false, label: '否'}]"
            placeholder="是否弃用"
            class="min-w-25" />
          <!-- <Input
            v-if="checkedCols.includes('reg')"
            v-model:value="item.deprecated"
            placeholder="正则"
            class="min-w-25" /> -->
          <Input
            v-model:value="item.description"
            placeholder="描述"
            class="min-w-25" />
          <Button
            size="small"
            @click="handleAdd">
            <Icon icon="icon-jia" />
          </Button>
          <Button
            size="small"
            @click="handleRduce(key)">
            <Icon icon="icon-jian" />
          </Button>
        </div>
      </div>
    </template>
  </div>
</template>
