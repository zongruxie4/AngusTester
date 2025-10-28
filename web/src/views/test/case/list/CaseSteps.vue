<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Dropdown, Icon } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { CaseTestStep } from '@/views/test/types';
import { CaseStepView } from '@/enums/enums';

import RichEditor from '@/components/editor/richEditor/index.vue';

const { t } = useI18n();

// Component props interface
interface Props {
  value: CaseTestStep[];
  defaultValue: CaseTestStep[];
  readonly: boolean;
  showOutBorder: boolean;
  stepView: CaseStepView
}

const props = withDefaults(defineProps<Props>(), {
  value: () => [],
  readonly: false,
  showOutBorder: true,
  stepView: CaseStepView.TABLE
});

// Component emits
const emits = defineEmits<{(e: 'update:value', value: CaseTestStep[]): void;
  (e:'change', hasErr: boolean):void;
}>();

// Test steps data management
const steps = ref<{ expectedResult: string; step: string; }[]>([
  { expectedResult: '', step: '' },
  { expectedResult: '', step: '' },
  { expectedResult: '', step: '' }
]);

// Drag and drop functionality
const startIndex = ref(0);
const moveItem = ref<{ expectedResult: string; step: string; }>();
const targetIndex = ref<number>();

/**
 * Handle drag start event
 * @param event - Drag event
 * @param index - Item index
 * @param item - Item being dragged
 */
const handleDragStart = (_event: DragEvent, index: number, item: { expectedResult: string; step: string; }) => {
  startIndex.value = index;
  moveItem.value = item;
};

/**
 * Handle drag end event and reorder steps
 * @param event - Drag event
 */
const handleDragEnd = (event: DragEvent) => {
  event.preventDefault();
  if (startIndex.value === targetIndex.value || targetIndex.value === undefined || moveItem.value === undefined) {
    return;
  }
  if (targetIndex.value < startIndex.value) {
    steps.value.splice(targetIndex.value, 0, moveItem.value);
    steps.value = steps.value.filter((_, i) => i !== startIndex.value + 1);
  }

  if (targetIndex.value > startIndex.value) {
    steps.value.splice(targetIndex.value + 1, 0, moveItem.value);
    steps.value = steps.value.filter((_, i) => i !== startIndex.value);
  }

  emits('update:value', steps.value.filter(item => !!item.step));
};

const handleDragOver = (event: DragEvent) => {
  event.preventDefault();
};

/**
 * Handle drop event
 * @param event - Drop event
 * @param index - Target index
 */
const handleDrop = (event: DragEvent, index: number) => {
  event.preventDefault();
  targetIndex.value = index;
};

/**
 * Add a new test step
 */
const handleAdd = () => {
  if (steps.value.length >= 100) {
    return;
  }
  steps.value.push({ expectedResult: '', step: '' });
};

/**
 * Delete a test step
 * @param index - Index of step to delete
 */
const handleDelete = (index: number) => {
  if (steps.value.length <= 3) {
    steps.value[index] = { expectedResult: '', step: '' };
    return;
  }
  steps.value = steps.value.filter((_, stepIndex) => stepIndex !== index);
  emits('update:value', steps.value.filter(item => !!item.step));
};

/**
 * Clone a test step
 * @param index - Index of step to clone
 * @param item - Step item to clone
 */
const handleClone = (index: number, item: { expectedResult: string; step: string; }) => {
  steps.value.splice(index, 0, item);
  emits('update:value', steps.value.filter(item => !!item.step));
};

/**
 * Move step to top
 * @param index - Current index of step
 * @param item - Step item to move
 */
const handleMoveTop = (index: number, item: { expectedResult: string; step: string; }) => {
  if (index === 0) {
    return;
  }
  steps.value.splice(index - 1, 0, item);
  steps.value = steps.value.filter((_, stepIndex) => stepIndex !== index + 1);
  emits('update:value', steps.value.filter(item => !!item.step));
};

/**
 * Move step to bottom
 * @param index - Current index of step
 * @param item - Step item to move
 */
const handleMoveBottom = (index: number, item: { expectedResult: string; step: string; }) => {
  if (index === steps.value.length - 1) {
    return;
  }
  steps.value.splice(index + 2, 0, item);
  steps.value = steps.value.filter((_, stepIndex) => stepIndex !== index);
  emits('update:value', steps.value.filter(item => !!item.step));
};

// Input change handler
const inputChange = () => {
  emits('update:value', steps.value.filter(item => !!item.step));
};

// Rich editor configuration
const toolbarOptions = ['color', 'weight'];

/**
 * Handle context menu click actions
 * @param key - Action key
 * @param item - Step item
 * @param index - Step index
 */
const handleClick = (key: string, item: { expectedResult: string; step: string; }, index: number) => {
  switch (key) {
    case 'clone':
      handleClone(index, { ...item });
      break;
    case 'delete':
      handleDelete(index);
      break;
    case 'top':
      handleMoveTop(index, item);
      break;
    case 'bottom':
      handleMoveBottom(index, item);
      break;
  }
};

// Watchers for props changes
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

watch(() => props.stepView, (newValue) => {
  if (newValue === CaseStepView.TEXT) {
    steps.value = steps.value.slice(0, 1);
  }
});

// Context menu configuration
const menus = [
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: t('testCase.actions.deleteStep'),
    noAuth: true
  },
  {
    key: 'clone',
    icon: 'icon-fuzhi',
    name: t('testCase.actions.cloneStep'),
    noAuth: true
  },
  {
    key: 'top',
    icon: 'icon-shangyi',
    name: t('testCase.actions.moveToTop'),
    noAuth: true
  },
  {
    key: 'bottom',
    icon: 'icon-xiayi',
    name: t('testCase.actions.moveToBottom'),
    noAuth: true
  }
];
</script>
<template>
  <div class="text-3">
    <template v-if="props.stepView === CaseStepView.TABLE">
      <div
        class="border-theme-text-box rounded"
        :class="{'border': props.showOutBorder}">
        <div class="flex">
          <div class="w-8 flex justify-center pt-1 flex-none">#</div>
          <div class="px-1.5 py-1 border-theme-text-box border-r flex-1/2">
            {{ t('testCase.messages.stepDescription') }}
          </div>
          <div
            class="px-1.5 py-1 border-theme-text-box flex-1/2"
            :class="{'border-r': !props.readonly || props.showOutBorder}">
            {{ t('testCase.messages.expectedResult') }}
          </div>
          <div v-show="!props.readonly" class="w-12 flex-none py-1 text-center">
            {{ t('common.actions') }}
          </div>
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
              :draggable="!props.readonly"
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
                :options="{theme: 'bubble', placeholder: t('testCase.messages.enterStepDescription')}"
                class="step-content"
                height="auto" />

              <RichEditor
                v-else
                :key="`${index}_step`"
                v-model:value="item.step"
                mode="edit"
                :toolbarOptions="toolbarOptions"
                :options="{theme: 'bubble', placeholder: t('testCase.messages.enterStepDescription')}"
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
                :options="{theme: 'bubble', placeholder: t('testCase.messages.enterExpectedResult')}"
                class="step-content"
                height="auto" />

              <RichEditor
                v-else
                :key="`${index}_expectedResult`"
                v-model:value="item.expectedResult"
                mode="edit"
                :toolbarOptions="toolbarOptions"
                :options="{theme: 'bubble', placeholder: t('testCase.messages.enterExpectedResult')}"
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
          @click="handleAdd">
          <Icon icon="icon-jia" class="mr-1 -mt-0.5" />{{ t('testCase.actions.addStep') }}
        </Button>
      </div>
    </template>

    <template v-if="props.stepView === CaseStepView.TEXT">
      <div class="pl-1">{{ t('testCase.messages.stepDescription') }}</div>
      <RichEditor
        v-model:value="steps[0].step"
        :mode="props.readonly ? 'view' : 'edit'"
        :disabled="props.readonly"
        :toolbarOptions="toolbarOptions"
        :options="{theme: 'bubble', placeholder: t('testCase.messages.enterStepDescription')}"
        :height="100"
        class="border"
        @change="inputChange" />

      <div class="mt-3 pl-1">{{ t('testCase.messages.expectedResult') }}</div>

      <RichEditor
        v-model:value="steps[0].expectedResult"
        :mode="props.readonly ? 'view' : 'edit'"
        :disabled="props.readonly"
        :toolbarOptions="toolbarOptions"
        :options="{theme: 'bubble', placeholder: t('testCase.messages.enterExpectedResult')}"
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
