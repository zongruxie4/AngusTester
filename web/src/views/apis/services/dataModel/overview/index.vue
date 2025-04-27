<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Icon, Select } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/tools';
import { Button, Popover, RadioButton, RadioGroup, Tag, Tooltip } from 'ant-design-vue';

import CodeView from './code.vue';
import FormView from './form.vue';

interface Props {
    dataSource: {[key: string]: any}; // openapi json
    selectStr?: string;
    startKey?: string;
}
const emits = defineEmits<{(e: 'cancel'): void; (e: 'ok', value: {[key: string]: any})}>();
const viewType = ref('form');
const viewTypeOpt = [
  {
    label: 'Form',
    value: 'form'
  },
  {
    label: 'Code',
    value: 'code'
  }
];

const formViewRef = ref();

const data = ref({});
const _tags = ref<{name: string; description: string;}[]>([]);
const tagsValue = ref<string[]>([]);
const docs = ref({});
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({}),
  selectStr: undefined
});
onMounted(() => {
  watch(() => props.dataSource, newValue => {
    data.value = newValue;
    _tags.value = newValue.tags;
    tagsValue.value = _tags.value.map(i => i.name);
  }, {
    immediate: true,
    deep: true
  });
  docs.value = JSON.parse(JSON.stringify(props.dataSource));
});

const onViewTypeChange = () => {
  const formData = getFormData();
  if (formData) {
    if (utils.deepCompare(formData, docs.value)) {
      return;
    }
    Object.assign(docs.value, { ...formData });
  }
};

const getFormData = () => {
  const formData = formViewRef.value.getData();
  const result = {
    ...formData,
    tags: tagsValue.value.map(i => {
      const targetTag = _tags.value.find(t => t.name === i);
      return {
        name: i,
        ...(targetTag || {})
      };
    })
  };
  return result;
};

const cancel = () => {
  emits('cancel');
};

const confirm = () => {
  const formData = formViewRef.value.getData();
  const result = {
    ...formData,
    tags: tagsValue.value.map(i => {
      const targetTag = _tags.value.find(t => t.name === i);
      return {
        name: i,
        ...(targetTag || {})
      };
    })
  };
  emits('ok', result);
};

</script>
<template>
  <div class="p-2 flex flex-col h-full">
    <div class="flex items-center">
      <Popover trigger="click" placement="bottomLeft">
        <Button
          ref="addTagRef"
          size="small"
          class="py-0 leading-5 h-5">
          <Icon icon="icon-biaoqian" />
        </Button>
        <template #content>
          <Select
            v-model:value="tagsValue"
            mode="tags"
            class="w-100"
            :getPopupContainer="(triggerNode) => triggerNode" />
        </template>
      </Popover>
      <Tooltip title="Tags">
        <div class="h-6 flex items-center ml-2">
          <!-- <Icon icon="icon-biaoqian" /> -->
          <Tag
            v-for="tag in tagsValue"
            :key="tag"
            :closable="false"
            color="green"
            class="leading-4">
            {{ tag }}
          </Tag>
        </div>
      </Tooltip>
      <div class="flex-1 text-right">
        <RadioGroup
          v-model:value="viewType"
          buttonStyle="solid"
          size="small"
          @change="onViewTypeChange">
          <RadioButton
            v-for="opt in viewTypeOpt"
            :key="opt.value"
            :value="opt.value">
            {{ opt.label }}
          </RadioButton>
        </RadioGroup>
      </div>
    </div>
    <div v-show="viewType === 'form'">
      <FormView ref="formViewRef" v-bind="props" />
    </div>
    <div v-if="viewType==='code'" class="flex-1">
      <CodeView v-bind="props" :dataSource="docs" />
    </div>
    <div class="flex space-x-2 mt-4">
      <Button
        size="small"
        type="primary"
        @click="confirm">
        确认
      </Button>
      <Button size="small" @click="cancel">取消</Button>
    </div>
  </div>
</template>
