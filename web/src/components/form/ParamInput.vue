<script lang="ts" setup>
import { ref, watch, inject, onMounted, onBeforeUnmount, computed, nextTick } from 'vue';
import { Popover } from 'ant-design-vue';
import { Icon, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { init, insertHtmlAtCaret, stringToDomFragment } from './selection';
import { FunctionConfig, CallbackValue } from './type';
import Casecader from '@/components/form/casecader/cascader';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Mock function interface
 * Represents a function that can be inserted into the input
 */
export interface MockFunction {
  name: string;              // Function name (e.g., "@randomInt()")
  clazz: string;             // Function class/category
  constructors?: MockFunction[];  // Constructor overloads
  maxLength?: number;        // Maximum parameter length
}

/**
 * Component props interface
 */
export interface Props {
  value: string | number | boolean | undefined;  // Input value
  placeholder?: string;      // Placeholder text
  height?: number;           // Input height in Tailwind units
  error?: boolean;           // Error state (red border)
  maxLength: number;         // Maximum input length (0 = unlimited)
  disabled?: boolean;        // Disabled state
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: '',
  placeholder: '',
  height: 7,
  error: false,
  disabled: false
});

/**
 * Computed: Effective placeholder text
 * Uses custom placeholder or default i18n text
 *
 * @returns Placeholder string to display
 */
const placeholder = computed(() => {
  return props.placeholder || t('xcan_paramInput.enterParameterValue');
});

/**
 * Ref: Search keyword for filtering functions
 * Updated as user types after @ symbol
 */
const keyword = ref('');

/**
 * Injected: All available mock functions
 * Provided by parent component or application context
 */
const allFunction = inject('allFunction', ref<MockFunction[]>([]));

/**
 * Injected: Function to load all mock functions
 * Called on component mount if functions not already loaded
 */
const getAllFunctions = inject('getAllFunctions', () => Promise);

/**
 * Computed: Filtered functions based on keyword
 * Shows only functions matching the search keyword
 *
 * @returns Filtered array of mock functions
 */
const showFunctions = computed(() => {
  return allFunction.value.filter(i => i.name.includes(keyword.value));
});

/**
 * Ref: HTML content of the input (with function nodes)
 * Contains rich text with function placeholders
 */
const inputValue = ref();

/**
 * Ref: Plain text value of the input
 * Text-only representation without HTML markup
 */
const inputTextValue = ref();

/**
 * Ref: Reference to the contenteditable div element
 * Used for cursor manipulation and content insertion
 */
const inputValueRef = ref<HTMLElement>();

/**
 * Ref: Input composition state flag
 * Tracks whether IME (Input Method Editor) composition is active
 * Used to prevent duplicate input handling during composition
 */
const input = ref(true);

/**
 * Ref: Dropdown visibility state
 * Controls whether the function selection dropdown is visible
 */
const dropVisible = ref(false);

/**
 * Emitted events interface
 */
const emit = defineEmits<{
  (e: 'blur', value: HTMLElement): void;      // Emitted when input loses focus
  (e: 'keypress', value: Event): void;        // Emitted on keypress events
  (e: 'rendered', value: true): void;         // Emitted when component is rendered
}>();

/**
 * Handle IME composition end event
 * Fired when user finishes typing with Input Method Editor (e.g., Chinese/Japanese input)
 * Updates input state and triggers change handling
 *
 * @param e - Composition event
 */
const compositionend = (e: any): void => {
  input.value = false;
  handleChange(e);
};

/**
 * Handle IME composition start event
 * Fired when user starts typing with Input Method Editor
 * Sets flag to prevent duplicate input handling
 */
const compositionstart = (): void => {
  input.value = true;
};

/**
 * Limit input text length to maxLength
 * Truncates text if it exceeds maximum length
 * Emits blur event to notify parent of the change
 */
const limitLength = (): void => {
  if (props.maxLength > 0) {
    if (inputTextValue.value.length > props.maxLength) {
      // Truncate text to max length
      inputTextValue.value = inputTextValue.value.slice(0, props.maxLength);
      if (inputValueRef.value) {
        inputValueRef.value.innerHTML = inputTextValue.value;
        emit('blur', inputValueRef.value);
      }
    }
  }
};

/**
 * Insert function text at cursor position
 * Replaces the @ keyword with the selected function
 *
 * @param func - Function configuration to insert
 */
function insertTxtAndSetcursor(func: FunctionConfig): void {
  if (inputValueRef.value) {
    insertHtmlAtCaret(inputValueRef.value, func, keyword.value);
  }
}

/**
 * Variable: Position where @ symbol was typed
 * Used to track the start of function search
 */
let keyStart = 0;

/**
 * Constant: Window selection object
 * Used for cursor position and text manipulation
 */
const selection = window.getSelection();

/**
 * Handle input change events
 * Manages function dropdown visibility and keyword filtering
 *
 * Logic:
 * - @ symbol: Show dropdown at cursor position
 * - Typing: Update keyword filter
 * - Backspace: Update keyword or hide dropdown if @ is deleted
 *
 * @param event - Input event
 */
const handleChange = (event: any): void => {
  if (event.data === '@') {
    dropVisible.value = true;
    const range = selection?.getRangeAt(0);
    if (range) {
      const { x, y } = range.getBoundingClientRect();
      position.value.positionX = x;
      position.value.positionY = y;
      keyStart = range?.startOffset || 0;
    }
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
    if (inputValueRef.value) {
      inputTextValue.value = inputValueRef.value.innerText;
    }
  });
};

/**
 * Handle double-click to select function node
 * Selects the entire function node when double-clicked
 *
 * @param event - Mouse event
 */
const handleGeRange = (event: any): void => {
  const nodeEle = event.target;
  if (nodeEle.className === 'parameter-tooltip' || nodeEle.className === 'fn-tooltip') {
    const range = selection?.getRangeAt(0);
    if (range) {
      range.setStart(nodeEle, 0);
      range.setEndAfter(nodeEle);
    }
  }
};

/**
 * Handle keypress events
 * Prevents Enter key default behavior (new line insertion)
 * Allows Ctrl+Enter and Shift+Enter to pass through
 *
 * @param event - Keyboard event
 */
const onKeypress = (event: KeyboardEvent): void => {
  const { code, ctrlKey, shiftKey } = event;
  const keyCode = code.toLowerCase();

  // Allow Ctrl+Enter and Shift+Enter
  if ((ctrlKey || shiftKey) && (keyCode === 'enter' || keyCode === 'numpadenter')) {
    event.preventDefault();
    event.stopPropagation();
    return;
  }

  // Block plain Enter key
  if (!ctrlKey && !shiftKey && (keyCode === 'enter' || keyCode === 'numpadenter')) {
    event.preventDefault();
    event.stopPropagation();
  }
};

/**
 * Handle input blur event
 * Updates text value, applies length limit, and emits blur event
 */
const handleBlur = (): void => {
  if (inputValueRef.value) {
    inputTextValue.value = inputValueRef.value.innerText;
    limitLength();
    emit('blur', inputValueRef.value);
  }
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

/**
 * Update DOM value from props
 * Converts prop value to HTML with function placeholders
 * Handles type conversion (boolean, number -> string)
 */
const updataValueDom = (): void => {
  let propsValue: string | undefined = undefined;

  // Convert different types to string
  if (typeof props.value === 'boolean') {
    propsValue = props.value.toString();
  } else if (props.value === null || props.value === undefined) {
    propsValue = undefined;
  } else if (typeof props.value === 'number') {
    propsValue = props.value.toString();
  } else {
    propsValue = props.value;
  }

  // Replace @function( patterns with function nodes
  const inputValueDom = (propsValue || '').replace(/@[a-zA-z0-9]+\(/g, (match) => {
    const funcName = match.replace('(', '');
    const funObj: any = allFunction.value.find(val => val.name.replace('()', '') === funcName);
    if (!funObj) {
      return match;
    }
    const funNode = stringToDomFragment(funObj);
    return funNode.outerHTML + '(';
  });

  inputValue.value = inputValueDom;
  if (inputValueRef.value) {
    inputValueRef.value.innerHTML = inputValueDom;
  }
  inputTextValue.value = propsValue;
};

const closeDropOption = (e) => {
  if (e.target !== inputValueRef.value) {
    dropVisible.value = false;
  }
};

/**
 * Component mounted lifecycle hook
 * Initializes functions, sets up event listeners, and configures selection behavior
 */
onMounted(async () => {
  // Load functions if not already loaded
  if (!allFunction.value.length && typeof getAllFunctions === 'function') {
    await getAllFunctions();
  }

  // Initialize DOM value if props value exists
  if (props.value && props.value !== inputTextValue.value) {
    updataValueDom();
  }

  // Close dropdown when clicking outside
  document.addEventListener('click', closeDropOption);

  // Initialize selection behavior and popover
  if (inputValueRef.value) {
    init(inputValueRef.value, (value: CallbackValue, node: HTMLElement) => {
      ghostInputStyle.width = node.offsetWidth + 'px';
      ghostInputStyle.left = node.offsetLeft + 'px';
      funcName.value = value.name;
      currentFunction.value = allFunction.value.find(i => i.name.includes(funcName.value));
      popoverVisible.value = true;
    }, () => {
      popoverVisible.value = false;
    });
  }

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

/**
 * Ref: Ghost input style for popover positioning
 * Used to position the function documentation popover
 */
const ghostInputStyle = { width: '0', left: '0' };

/**
 * Ref: Popover visibility state
 * Controls whether the function documentation popover is visible
 */
const popoverVisible = ref(false);

/**
 * Ref: Current function name
 * Name of the function being hovered
 */
const funcName = ref();

/**
 * Ref: Current function object
 * Full function details for the hovered function
 */
const currentFunction = ref();

/**
 * Table columns configuration for example values display
 * Shows constructor and return values in popover
 */
const columns = [
  { key: 'instance', title: t('xcan_paramInput.constructor'), dataIndex: 'instance' },
  { key: 'values', title: t('xcan_paramInput.returnValue'), dataIndex: 'values' }
];

/**
 * Ref: Position for function dropdown
 * Stores x, y coordinates where @ symbol was typed
 */
const position = ref({
  positionX: 0,
  positionY: 0
});

</script>
<template>
  <div class="relative w-full" :class="height?`h-${height}`:'h-7'">
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
                  <div class="text-theme-title mt-3 mb-2 leading-3">{{ t('xcan_paramInput.example') }}</div>
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
                  {{ t('common.description') }}： {{ currentFunction.description }}
                </li>
                <li>
                  {{ t('xcan_paramInput.constructor') }}：
                  <ol class="pl-4" style="list-style-type:circle;">
                    <li v-for="con in currentFunction.constructors" :key="con.instance">
                      {{ `${con.instance} ： ${con.description}` }}
                    </li>
                  </ol>
                </li>
                <li>
                  {{ t('xcan_paramInput.parameterDescription') }}：
                  <ol class="pl-4" style="list-style-type:circle;">
                    <li v-for="param in currentFunction.parameters" :key="param.name">
                      {{ `${param.name} ： ${param.description}` }}
                    </li>
                  </ol>
                </li>
                <li>
                  {{ t('xcan_paramInput.usageExample') }}：
                  <ol class="pl-4" style="list-style-type:decimal;">
                    <li v-for="con in currentFunction.constructors" :key="con.instance">
                      {{ t('xcan_paramInput.callResult', { example: con.example, count: con.exampleValues.length, result: con.exampleValues.join(' ') }) }}
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
          <Icon class="mr-1 text-3.5 text-tips" icon="icon-tishi1" />{{ t('xcan_paramInput.usageTip') }}
        </div>
      </template> -->
      <div :style="ghostInputStyle" class="absolute left-0 top-0 h-7 -z-999"></div>
    </Popover>
    <div
      v-if="!inputTextValue"
      class="-z-1 absolute left-2 top-1.25 text-3 text-theme-placeholder overflow-hidden whitespace-nowrap select-none"
      style="max-width: calc(100% - 20px)">
      {{ placeholder }}
    </div>
    <div
      class="w-full overflow-hidden rounded border flex justify-between px-2 relative z-9"
      :class="[props.error && '!border-rule', `h-${props.height}`, props.disabled ? 'ant-input-affix-wrapper-disabled' : 'border-theme-text-box border-theme-divider-hover']">
      <div
        ref="inputValueRef"
        tabindex="0"
        class="flex-1 outline-none absolute top-1 bottom-0 left-1 right-1 z-1 text-theme-content text-3 overflow-x-scroll overflow-y-hidden leading-5 cursor-text whitespace-nowrap"
        style="width: calc(100% - 16px);height: 100%"
        :contenteditable="!props.disabled"
        @input="handleChange"
        @dblclick="handleGeRange"
        @blur="handleBlur"
        @paste="handlePaste"
        @keypress="onKeypress"
        @compositionend="compositionend"
        @compositionstart="compositionstart"
        v-html="inputValue">
      </div>
      <div>
        <slot name="suffix"></slot>
      </div>
    </div>
  </div>
</template>
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
