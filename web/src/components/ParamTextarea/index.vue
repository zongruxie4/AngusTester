<script lang="ts" setup>
import { ref, watch, inject, onMounted, onBeforeUnmount, computed, nextTick } from 'vue';
import { Popover } from 'ant-design-vue';
import { Icon, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { init, insertHtmlAtCaret, stringToDomFragment } from './utils/selection';
import { FunctionConfig, CallbackValue } from './utils/type';
import Casecader from './Casecader/cascader';

const { t } = useI18n();

export interface MockFunction {
  name: string,
  clazz: string,
  constructors?: MockFunction[],
  maxLength?: number;
}

export interface Props {
  value: string|number|boolean|undefined;
  placeholder?:string;
  height?:number;
  error?: boolean;
  maxLength: 0;
  disabled?: boolean;
}
const props = withDefaults(defineProps<Props>(), {
  value: '',
  placeholder: t('xcan_paramTextarea.enterParameterValue'),
  height: 28,
  error: false,
  disabled: false
});

const keyword = ref('');
const allFunction = inject('allFunction', ref<MockFunction[]>([]));
const getAllFunctions = inject('getAllFunctions', () => Promise);
const showFunctions = computed(() => {
  // return data.filter(i => i.name.includes(keyword.value));
  return allFunction.value.filter(i => i.name.includes(keyword.value));
});

const inputValue = ref();
const inputTextValue = ref();
const inputValueRef = ref(); // input ref

const input = ref(true);

// const currentSelect: {start?: number, end?: number} = { start: 0, end: 0 }; // 用于记录光标位置;

const dropVisible = ref(false); // 下拉显示隐藏

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'blur', value:HTMLElement);
  (e: 'keypress', value: Event);
  (e: 'rendered', value: true);
}>();

const compositionend = (e): void => {
  input.value = false;
  handleChange(e);
};

const compositionstart = (): void => {
  input.value = true;
};

const limitLength = () => {
  if (props.maxLength > 0) {
    if (inputTextValue.value.length > props.maxLength) {
      inputTextValue.value = inputTextValue.value.slice(0, props.maxLength);
      inputValueRef.value.innerHTML = inputTextValue.value;
      emit('blur', inputValueRef.value);
    }
  }
};

// 数据插入
function insertTxtAndSetcursor (func: FunctionConfig) {
  insertHtmlAtCaret(inputValueRef.value, func, keyword.value);
  // window.Selection.
}

let keyStart = 0;
const selection = window.getSelection();
// input 数据变化
const handleChange = (event) => {
  if (event.data === '@') {
    dropVisible.value = true;
    const range = selection?.getRangeAt(0);
    const { x, y } = range.getBoundingClientRect();
    position.value.positionX = x;
    position.value.positionY = y;
    keyStart = range?.startOffset || 0;
  } else if (dropVisible.value) {
    if (event.inputType === 'insertCompositionText') {
      return;
    }
    if (event.inputType === 'insertFromPaste') {
      dropVisible.value = true;
      return;
    }
    if (event.data) {
      keyword.value += event.data;
    } else if (event.inputType === 'deleteContentBackward') { // 执行删除动作
      // 如果@被删除
      const range = selection?.getRangeAt(0);
      if ((range?.startOffset || 0) < keyStart) {
        dropVisible.value = false;
        keyStart = 0;
      } else {
        keyword.value = keyword.value.slice(0, (range?.startOffset || 0) - keyStart);
      }
    }
  }

  nextTick(() => {
    inputTextValue.value = inputValueRef.value.innerText;
  });
};

const handleGeRange = (event) => {
  const nodeEle = event.target;
  if (nodeEle.className === 'parameter-tooltip' || nodeEle.className === 'fn-tooltip') {
    const range = selection?.getRangeAt(0);
    range.setStartBefore(nodeEle);
    range.setEndAfter(nodeEle);
  }
};

const onKeypress = (event: KeyboardEvent): void => {
  const { code, ctrlKey, shiftKey } = event;
  const keyCode = code.toLowerCase();
  if ((ctrlKey || shiftKey) && (keyCode === 'enter' || keyCode === 'numpadenter')) {
    // event.preventDefault();
    event.stopPropagation();
    // pasteHtmlAtCaret('<br>');
    // const value = event.target?.innerText;
    // inputValue.value = event.target?.innerHTML;
    return;
  }

  if (!ctrlKey && !shiftKey && (keyCode === 'enter' || keyCode === 'numpadenter')) {
    // event.preventDefault();
    event.stopPropagation();
  }
};

// input 失去焦点
const handleBlur = () => {
  // inputTextValue.value = inputValueRef.value.innerText;
  limitLength();
  let inner = inputValueRef.value?.innerHTML?.replace('<br>', '\n').replace();
  inner = inner.replaceAll(/<div>.*?<\/div>/g, (match) => {
    const target = match.replace('<div>', '\n').replace('</div>', '');
    return target;
  });
  const div = document.createElement('div');
  div.innerHTML = inner;
  const value = div?.innerText?.trim().replace('\n', '');
  inputTextValue.value = value;
  emit('blur', value);
};

const handlePaste = (event) => {
  event.preventDefault();
  let text;
  const clp = event.clipboardData;

  if (clp) {
    const items = clp.items;
    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      if (item.kind === 'string' && item.type === 'text/plain') {
        item.getAsString((str) => {
          text = str;
          document.execCommand('insertText', false, text);
        });
      }
    }
  }
};

// 选择函数
const handleSelect = (currentFun: string | any[]) => {
  if (currentFun.length > 1) {
    insertTxtAndSetcursor({
      ...currentFun[1],
      parameters: currentFun[0].parameters
    });
  } else {
    insertTxtAndSetcursor(currentFun[0]);
  }
};

watch(() => dropVisible.value, () => {
  keyword.value = '';
});

watch(() => props.value, newValue => {
  if (newValue === inputTextValue.value) {
    return;
  }
  updataValueDom();
});

const updataValueDom = () => {
  let propsValue = props.value;
  if (typeof propsValue === 'boolean') {
    propsValue = propsValue.toString();
  }
  if (propsValue === null) {
    propsValue = undefined;
  }
  if (typeof propsValue === 'number') {
    propsValue = propsValue + '';
  }
  const inputValueDom = (propsValue || '').replaceAll(/@[a-zA-z0-9]+\(/g, (match) => {
    const funcName = match.replace('(', '');
    const funObj: any = allFunction.value.find(val => val.name.replace('()', '') === funcName);
    if (!funObj) {
      return match;
    }
    const funNode = stringToDomFragment(funObj);
    return funNode.outerHTML + '(';
  });
  inputValue.value = inputValueDom;
  inputValueRef.value && (inputValueRef.value.innerHTML = inputValueDom);
  inputTextValue.value = propsValue;
};

const closeDropOption = (e) => {
  if (e.target !== inputValueRef.value) {
    dropVisible.value = false;
  }
};

onMounted(async () => {
  if (!allFunction.value.length && typeof getAllFunctions === 'function') {
    await getAllFunctions();
  }
  if (props.value && props.value !== inputTextValue.value) {
    updataValueDom();
  }

  document.addEventListener('click', closeDropOption);

  init(inputValueRef.value, (value: CallbackValue, node:HTMLElement) => {
    ghostInputStyle.width = node.offsetWidth + 'px';
    ghostInputStyle.left = node.offsetLeft + 'px';
    funcName.value = value.name;
    currentFunction.value = allFunction.value.find(i => i.name.includes(funcName.value));
    popoverVisible.value = true;
  }, () => {
    popoverVisible.value = false;
  });

  nextTick(() => {
    emit('rendered', true);
  });

  watch(() => allFunction.value, () => {
    if (allFunction.value.length > 0) {
      updataValueDom();
    }
  });
});

onBeforeUnmount(() => {
  document.removeEventListener('click', closeDropOption);
});

const ghostInputStyle = { width: '0', left: '0' };
const popoverVisible = ref(false);
const funcName = ref();
const currentFunction = ref();
const columns = [
  { title: t('xcan_paramTextarea.constructor'), dataIndex: 'instance' },
  { title: t('xcan_paramTextarea.returnValue'), dataIndex: 'values' }
];

const position = ref({
  positionX: 0,
  positionY: 0
});

</script>
<template>
  <div class="relative w-full" :class="height?`h-${height}`:'h-28'">
    <Casecader
      :visible="dropVisible"
      labelKey="name"
      valueKey="name"
      childKey="constructors"
      :position="position"
      :options="showFunctions"
      @onSelect="handleSelect">
      <template #labelRenderer="item">
        <div class="flex items-center text-3 leading-3 h-6">
          <Popover
            placement="left"
            arrowPointAtCenter
            overlayClassName="cascader-popover-container"
            color="white">
            <template #content>
              <div class="w-100">
                <div class="text-theme-title mt-1 mb-1.5 leading-3">{{ item.name }}</div>
                <div class="text-theme-content leading-4.5 break-words">{{ item.description }}</div>
                <template v-if="item.exampleValues?.length">
                  <div class="text-theme-title mt-3 mb-2 leading-3">{{ t('xcan_paramTextarea.example') }}</div>
                  <Table
                    class="popover-table-container"
                    size="small"
                    :pagination="false"
                    :columns="columns"
                    :dataSource="[item]">
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.dataIndex==='values'">
                        <div
                          v-for="value in record.exampleValues"
                          :key="value"
                          :title="value"
                          class="leading-4.5 truncate max-w-75">
                          {{ value }}
                        </div>
                      </template>
                    </template>
                  </Table>
                </template>
              </div>
            </template>
            <Icon icon="icon-shuoming" class="text-text-link mr-2" />
          </Popover>
          <span>{{ item.name }}</span>
        </div>
      </template>
    </Casecader>
    <Popover
      placement="left"
      overlayClassName="cascader-popover-container"
      :autoAdjustOverflow="true"
      :visible="popoverVisible">
      <template #content>
        <div class="min-w-37.5 max-w-150">
          <div v-if="currentFunction" class="mt-4">
            <div class="text-3.5">{{ currentFunction?.name }}</div>
            <div class="p-2 pl-5 bg-gray-bg leading-6 rounded">
              <ul style="list-style-type:disc;">
                <li class="whitespace-pre-wrap">
                  {{ t('xcan_paramTextarea.description') }}： {{ currentFunction.description }}
                </li>
                <li>
                  {{ t('xcan_paramTextarea.constructor') }}：
                  <ol class="pl-4" style="list-style-type:circle;">
                    <li v-for="con in currentFunction.constructors" :key="con.instance">
                      {{ `${con.instance} ： ${con.description}` }}
                    </li>
                  </ol>
                </li>
                <li>
                  {{ t('xcan_paramTextarea.parameterDescription') }}：
                  <ol class="pl-4" style="list-style-type:circle;">
                    <li v-for="param in currentFunction.parameters" :key="param.name">
                      {{ `${param.name} ： ${param.description}` }}
                    </li>
                  </ol>
                </li>
                <li>
                  {{ t('xcan_paramTextarea.usageExample') }}：
                  <ol class="pl-4" style="list-style-type:decimal;">
                    <li v-for="con in currentFunction.constructors" :key="con.instance">
                      {{ t('xcan_paramTextarea.callResult', { example: con.example, count: con.exampleValues.length, result: con.exampleValues.join(' ') }) }}
                    </li>
                  </ol>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </template>

      <!-- <template #title>
        <div class="flex items-center select-none">
          <Icon class="mr-1 text-3.5 text-tips" icon="icon-tishi1" />{{ t('xcan_paramTextarea.usageTip') }}
        </div>
      </template> -->
      <div :style="ghostInputStyle" class="absolute left-0 top-0 h-7 -z-999"></div>
    </Popover>
    <div
      v-if="!inputTextValue?.length"
      class="z-0 absolute left-2 top-1.5 text-3 text-theme-placeholder overflow-hidden whitespace-nowrap select-none"
      style="max-width: calc(100% - 20px)">
      {{ placeholder }}
    </div>
    <div
      class="relative w-full overflow-hidden rounded border z-9"
      :class="[props.error && '!border-rule', `h-${props.height}`, props.disabled ? 'ant-input-affix-wrapper-disabled' : 'border-theme-text-box border-theme-divider-hover']">
      <div
        ref="inputValueRef"
        tabindex="0"
        :contenteditable="!props.disabled"
        class="flex-1  outline-none absolute top-1 bottom-0 left-1 right-1 z-1 text-theme-content text-3 overflow-x-scroll leading-5 cursor-text whitespace-pre-wrap content-wrapper"
        style="width: calc(100% - 16px); height: 100%;"
        @dblclick="handleGeRange"
        @input="handleChange"
        @blur="handleBlur"
        @paste="handlePaste"
        @keypress="onKeypress"
        @compositionend="compositionend"
        @compositionstart="compositionstart"
        v-html="inputValue">
      </div>
    </div>
  </div>
</template>
<style scoped>
.fn-tooltip {
  color: #07F;
}
</style>
<style>
.cascader-popover-container .ant-popover-inner-content {
  @apply px-3 py-2 text-3;

  color: var(--content-text-content);
}

.cascader-popover-container .ant-popover-inner {
  max-width: fit-content;
}

.cascader-popover-container .ant-popover-title {
  @apply py-0 px-3 leading-8 text-3;

  min-height: auto;
  border-bottom-color: rgba(245, 245, 245, 100%);
  color: var(--content-text-sub-content);
}

.popover-table-container.ant-table-wrapper .ant-table.ant-table-small .ant-table-thead > tr > th,
.popover-table-container.ant-table-wrapper .ant-table.ant-table-small .ant-table-tbody > tr > td {
  @apply p-1 text-3;
}

/* .content-wrapper::-webkit-scrollbar {
  background-color: transparent !important;
}
.content-wrapper {
  scrollbar-color: transparent !important;
  scrollbar-width: none;
}
.content-wrapper::-webkit-scrollbar-thumb {
  background-color: transparent !important;
} */
</style>
