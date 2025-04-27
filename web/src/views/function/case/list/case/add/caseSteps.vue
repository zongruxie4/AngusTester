<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Dropdown, Icon, Input } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';

import RichEditor from '@/components/richEditor/index.vue';

interface Props {
  value: {
    expectedResult: string;
    step: string;
  }[];
  defaultValue: {
    expectedResult: string;
    step: string;
  }[];
  readonly: boolean;
  showOutBorder: boolean;
  stepView: 'TABLE'|'TEXT'
}

const props = withDefaults(defineProps<Props>(), {
  value: () => [],
  readonly: false,
  showOutBorder: true,
  stepView: 'TABLE'
});

const emits = defineEmits<{(e: 'update:value', value: { expectedResult: string; step: string; }[]): void;
  (e:'change', hasErr: boolean):void;
}>();

const steps = ref<{ expectedResult: string;step: string;}[]>([
  { expectedResult: '', step: '' },
  { expectedResult: '', step: '' },
  { expectedResult: '', step: '' }
]);

const startIndex = ref(0);
const moveItem = ref();
const handleDragStart = (_e, index, item) => {
  startIndex.value = index;
  moveItem.value = item;
};
const targetIndex = ref();
const handleDragEnd = e => {
  e.preventDefault();
  if (startIndex.value === targetIndex.value) {
    return;
  }
  if (targetIndex.value < startIndex.value) {
    steps.value.splice(targetIndex.value, 0, moveItem.value);
    steps.value = steps.value.filter((_f, i) => i !== startIndex.value + 1);
  }

  if (targetIndex.value > startIndex.value) {
    steps.value.splice(targetIndex.value + 1, 0, moveItem.value);
    steps.value = steps.value.filter((_f, i) => i !== startIndex.value);
  }

  emits('update:value', steps.value.filter(item => !!item.step));
};
const handleDragOver = (e) => {
  e.preventDefault();
};

const handleDrop = (e, index) => {
  e.preventDefault();
  targetIndex.value = index;
};

const hanldeAdd = () => {
  if (steps.value.length >= 100) {
    return;
  }
  steps.value.push({ expectedResult: '', step: '' });
};

const handleDel = (_index) => {
  if (steps.value.length <= 3) {
    steps.value[_index] = { expectedResult: '', step: '' };
    return;
  }
  steps.value = steps.value.filter((_item, index) => _index !== index);
  emits('update:value', steps.value.filter(item => !!item.step));
};
const handleClone = (_index, _item) => {
  steps.value.splice(_index, 0, _item);
  emits('update:value', steps.value.filter(item => !!item.step));
};
const handleMoveTop = (_index, _item) => {
  if (_index === 0) {
    return;
  }
  steps.value.splice(_index - 1, 0, _item);
  steps.value = steps.value.filter((_item, index) => _index + 1 !== index);
  emits('update:value', steps.value.filter(item => !!item.step));
};
const handleMoveBottom = (_index, _item) => {
  if (_index === steps.value.length - 1) {
    return;
  }
  steps.value.splice(_index + 2, 0, _item);
  steps.value = steps.value.filter((_item, index) => _index !== index);
  emits('update:value', steps.value.filter(item => !!item.step));
};

const inputChange = () => {
  emits('update:value', steps.value.filter(item => !!item.step));
};

const toolbarOptions = ['color', 'weight'];

watch(() => props.defaultValue, (newValue) => {
  steps.value = newValue?.length
    ? newValue
    : [
        { expectedResult: '', step: '' },
        { expectedResult: '', step: '' },
        { expectedResult: '', step: '' }
      ];
}, {
  immediate: true
});

watch(() => props.stepView, (newValue, oldValue) => {
  if (newValue === 'TEXT') {
    steps.value = steps.value.slice(0, 1);
  }
});

const menus = [
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: '删除',
    noAuth: true
  },
  {
    key: 'clone',
    icon: 'icon-fuzhi',
    name: '克隆',
    noAuth: true
  },
  {
    key: 'top',
    icon: 'icon-shangyi',
    name: '上移',
    noAuth: true
  },
  {
    key: 'bottom',
    icon: 'icon-xiayi',
    name: '下移',
    noAuth: true
  }
];

const handleClick = (key:string, item, index) => {
  switch (key) {
    case 'clone':
      handleClone(index, { ...item });
      break;
    case 'delete':
      handleDel(index);
      break;
    case 'top':
      handleMoveTop(index, item);
      break;
    case 'bottom':
      handleMoveBottom(index, item);
      break;
  }
};
</script>
<template>
  <div class="text-3">
    <template v-if="props.stepView === 'TABLE'">
      <div class="border-theme-text-box rounded" :class="{'border': props.showOutBorder}">
        <div class="flex">
          <div class="w-8 flex justify-center pt-1 flex-none">#</div>
          <div class="px-1.5 py-1 border-theme-text-box border-r flex-1/2">测试步骤</div>
          <div class="px-1.5 py-1 border-theme-text-box flex-1/2" :class="{'border-r': !props.readonly || props.showOutBorder}">预期结果</div>
          <div v-show="!props.readonly" class="w-12 flex-none py-1 text-center">操作</div>
        </div>
        <div
          class="border-t border-theme-text-box step-list-input">
          <div
            v-for="(item,index) in steps"
            :key="index"
            :class="{'border-t':index > 0}"
            class="flex  cursor-pointer">
            <div
              class="w-8 flex justify-center pt-1 flex-none"
              :draggable="!props.readonly ? true : false"
              @drop="(e)=>handleDrop(e,index)"
              @dragend="handleDragEnd"
              @dragstart="(e)=>handleDragStart(e,index,item)"
              @dragover.prevent="handleDragOver">
              <div class="h-4 text-center leading-4 mt-1.25 whitespace-nowrap">{{ index+1 }}.</div>
            </div>
            <div class="p-1 border-theme-text-box border-r flex-1/2">
              <RichEditor
                v-if="props.readonly"
                :key="`${index}_step_view`"
                v-model:value="item.step"
                mode="view"
                :toolbarOptions="toolbarOptions"
                :options="{theme: 'bubble', placeholder: '输入测试步骤'}"
                class="step-content"
                height="auto" />
              <RichEditor
                v-else
                :key="`${index}_step`"
                v-model:value="item.step"
                mode="edit"
                :toolbarOptions="toolbarOptions"
                :options="{theme: 'bubble', placeholder: '输入测试步骤'}"
                class="step-content"
                height="auto"
                @change="inputChange" />
            </div>
            <div class="py-1 px-1 border-theme-text-box flex-1/2" :class="{'border-r': !props.readonly || props.showOutBorder}">
              <RichEditor
                v-if="props.readonly"
                :key="`${index}_expectedResult_view`"
                v-model:value="item.expectedResult"
                mode="view"
                :toolbarOptions="toolbarOptions"
                :options="{theme: 'bubble', placeholder: '输入预期结果'}"
                class="step-content"
                height="auto" />
              <RichEditor
                v-else
                :key="`${index}_expectedResult`"
                v-model:value="item.expectedResult"
                :toolbarOptions="toolbarOptions"
                :options="{theme: 'bubble', placeholder: '输入预期结果'}"
                class="step-content"
                height="auto"
                @change="inputChange" />
            </div>
            <div
              v-show="!props.readonly"
              class="w-12 flex-none py-1  flex flex-col justify-center"
              style="min-width: 48px;">
              <Dropdown
                :menuItems="menus"
                @click="handleClick($event.key, item, index)">
                <Icon
                  class="cursor-pointer text-theme-text-hover mx-auto"
                  icon="icon-gengduo" />
              </Dropdown>
            </div>
          </div>
        </div>
      </div>
      <div v-show="!props.readonly" class="flex items-center justify-between  mt-1">
        <Button
          type="link"
          class="flex items-center px-0 text-3 leading-3 h-4"
          @click="hanldeAdd">
          <Icon icon="icon-jia" class="mr-1 -mt-0.5" />添加步骤
        </Button>
      </div>
    </template>
    <template v-if="props.stepView === 'TEXT'">
      <div class="pl-1">测试步骤</div>
      <RichEditor
        v-model:value="steps[0].step"
        :mode="props.readonly ? 'view' : 'edit'"
        :disabled="props.readonly"
        :toolbarOptions="toolbarOptions"
        :options="{theme: 'bubble', placeholder: '输入测试步骤'}"
        :height="100"
        class="border"
        @change="inputChange" />
      <div class="mt-3 pl-1">预期结果</div>
      <RichEditor
        v-model:value="steps[0].expectedResult"
        :mode="props.readonly ? 'view' : 'edit'"
        :disabled="props.readonly"
        :toolbarOptions="toolbarOptions"
        :options="{theme: 'bubble', placeholder: '输入预期结果'}"
        :height="100"
        class="border"
        @change="inputChange" />
    </template>
  </div>
</template>
<style scoped>

:deep(.step-content) .ql-editor {
  padding: 4px;
}

.step-list-input :deep(.ant-input-affix-wrapper > textarea) {
  overflow: hidden;
}

.step-list-input :deep(.ant-input.ant-input-sm) {
  padding-right: 20px;
}

</style>
