<script setup lang="ts">
import { inject, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { Popover, Radio } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { services } from '@/api/tester';
import { ServerInfo } from '@/views/apis/server/types';
import { VARIABLE_NAME_WIDE_REG } from '@/utils/apis';

const { t } = useI18n();

interface Props {
  value: string;
  valueObj: ServerInfo;
  readonly?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  readonly: false
});

const emits = defineEmits<{
  (e: 'update:value', value: string): void;
  (e: 'focus'): void;
  (e: 'handleBlur'): void;
  (e: 'enterPress'): void;
}>();

const apiBaseInfo = inject('apiBaseInfo', ref());
const inputRef = ref();
const inputWrapRef = ref();
const focused = ref(false);
const value = ref();
const serverObj = ref();
const valueHtml = ref();
const inputFlag = ref(true);
const popoverVisible = ref(false);
const showErrorPopover = ref(false); // 变量是否异常， 不存在则显示异常
const popPosition = reactive({
  left: '0',
  width: '0'
});

/**
 * Handle composition end event
 * <p>Resets input flag and triggers change handler after IME input</p>
 */
const handleCompositionEnd = (): void => {
  inputFlag.value = false;
  handleInputChange();
};

/**
 * Handle composition start event
 * <p>Sets input flag to prevent change handling during IME input</p>
 */
const handleCompositionStart = (): void => {
  inputFlag.value = true;
};

/**
 * Handle paste event with custom text processing
 * <p>Processes pasted text and updates input content</p>
 * @param event - The paste event
 */
const handlePasteEvent = (event) => {
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
  emits('update:value', inputRef.value.innerText);
  valueHtml.value = inputRef.value.innerHTML;
};

/**
 * Handle input blur event
 * <p>Updates value and emits blur event when user leaves input</p>
 */
const handleInputBlur = () => {
  focused.value = false;
  value.value = inputRef.value.innerText.replace(/\n/mg, '');
  emits('update:value', value.value);
  emits('handleBlur');
  valueHtml.value = inputRef.value.innerHTML;
};

/**
 * Handle input focus event
 * <p>Sets focused state and emits focus event</p>
 */
const handleInputFocus = () => {
  focused.value = true;
  emits('focus');
};

/**
 * Handle key press event
 * <p>Processes Enter key to blur input and emit enter press event</p>
 * @param e - The keyboard event
 */
const handleKeyPressEvent = (e) => {
  if (e.code === 'Enter') {
    e.preventDefault();
    inputRef.value.blur();
    emits('enterPress');
  }
};

/**
 * Handle input change event
 * <p>Updates HTML content when input changes</p>
 */
const handleInputChange = () => {
  valueHtml.value = inputRef.value.innerHTML;
};

/**
 * Handle mouse over event for variable tooltips
 * <p>Shows popover with variable information when hovering over variables</p>
 * @param e - The mouse event
 */
const handleMouseOverEvent = (e) => {
  if (e.target.tagName === 'SPAN' && e.target.className.includes('xc-tip')) {
    popoverVisible.value = true;
    popPosition.width = e.target.offsetWidth + 'px';
    popPosition.left = e.target.offsetLeft + 'px';
    const key = e.target.innerText.replace('{', '').replace('}', '');
    const variables = serverObj.value.variables || {};
    showErrorPopover.value = !variables[key];
  }
};

let delayTimer: NodeJS.Timeout | null = null;

const isServerDefaultChanging = ref(false);

/**
 * Change enum default value for server variable
 * <p>Updates server variable default value and saves to backend</p>
 * @param value - The new default value
 * @param serverKey - The server variable key
 */
const changeServerVariableDefault = async (value: string, serverKey) => {
  isServerDefaultChanging.value = true;
  const params = {
    ...serverObj.value,
    variables: {
      ...serverObj.value.variables,
      [serverKey]: {
        ...serverObj.value.variables[serverKey],
        default: value
      }
    }
  };
  const [error] = await services.putServicesServerUrl(apiBaseInfo.value.serviceId, params);
  if (error) {
    return;
  }
  serverObj.value.variables[serverKey].default = value;
  emits('handleBlur');
};

/**
 * Set delay timer for popover visibility
 * <p>Manages popover visibility with delay to prevent flickering</p>
 */
const setPopoverDelayTimer = () => {
  delayTimer = setTimeout(() => {
    if (isServerDefaultChanging.value) {
      delayTimer && clearTimeout(delayTimer);
      delayTimer = null;
      isServerDefaultChanging.value = false;
      return;
    }
    popoverVisible.value = false;
  }, 100);
};

watch(() => props.value, newValue => {
  if (newValue === value.value) {
    return;
  }
  value.value = newValue;
}, {
  immediate: true
});

watch(() => props.valueObj, newValue => {
  serverObj.value = newValue;
}, {
  immediate: true
});

watch(() => value.value, newValue => {
  const content = (newValue || '').replaceAll('\n', '');
  // Replace ${abc} with <span>${abc}</span>
  const replaced = content.replace(VARIABLE_NAME_WIDE_REG,
    (match) => `<span class="text-blue-1 cursor-pointer xc-tip">${match}</span>`);
  valueHtml.value = replaced;
  if (inputRef.value?.innerHTML && inputRef.value.innerHTML === replaced) {
    return;
  }
  nextTick(() => {
    inputRef.value && (inputRef.value.innerHTML = valueHtml.value);
  });
}, {
  immediate: true
});

onMounted(() => {
  document.addEventListener('click', setPopoverDelayTimer);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', setPopoverDelayTimer);
  // Clean up timer to prevent memory leaks
  if (delayTimer) {
    clearTimeout(delayTimer);
    delayTimer = null;
  }
});
</script>
<template>
  <div ref="inputWrapRef" class="relative border-r overflow-auto">
    <div
      v-show="!value && !focused"
      class="absolute left-2 top-0 h-full leading-5.5 py-1 text-theme-placeholder select-none pointer-events-none">
      {{ t('service.apiServerPath.form.serverPlaceholder') }}
    </div>
    <div
      ref="inputRef"
      class="px-2 py-1 outline-none h-7.5 leading-5.5 whitespace-nowrap flex"
      :contenteditable="!props.readonly"
      @keypress="handleKeyPressEvent"
      @input="handleInputChange"
      @blur="handleInputBlur"
      @focus="handleInputFocus"
      @compositionend="handleCompositionEnd"
      @compositionstart="handleCompositionStart"
      @paste="handlePasteEvent"
      @mouseover="handleMouseOverEvent">
    </div>
    <Popover
      placement="bottom"
      arrowPointAtCenter
      :overlayStyle="{'max-width':'600px'}"
      :autoAdjustOverflow="false"
      :visible="popoverVisible">
      <template #content>
        <div class="text-3">
          <div v-show="showErrorPopover" class="text-rule">
            {{ t('service.apiServerPath.tips.variableNotFound') }}
          </div>
          <div v-show="!showErrorPopover">
            <div class="font-bold text-text-title flex items-center">
              <Icon icon="icon-fuwuqi" class="mr-1" />{{ value }}
            </div>
            <div class="mb-1.5 pl-5">{{ serverObj.description || '' }}</div>
            <div class="border-t border-border-divider border-dashed mb-1"></div>
            <ul class="list-disc space-y-1 pl-4">
              <li v-for="(_value, key) in (serverObj.variables || {})" :key="key">
                <div
                  class="text-3 text-text-title rounded-sm leading-5 truncate cursor-pointer inline font-bold"
                  :title="key + ''"
                  style="max-width: 400px;">
                  {{ key }}
                </div>
                <div class="space-y-0.5 pl-4">
                  <div
                    v-for="en in _value.enum"
                    :key="en"
                    class="flex items-center justify-between rounded-sm leading-3">
                    <div
                      class="truncate cursor-pointer"
                      style="max-width: 400px;"
                      :title="en">
                      {{ en }}
                    </div>
                    <div class="flex items-center">
                      <span v-show="_value.default === en">{{ t('common.default') }}</span>
                      <Radio
                        size="small"
                        :checked="_value.default === en"
                        class="-mr-0.25 ml-1"
                        @click="changeServerVariableDefault(en, key)" />
                    </div>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </template>
      <div :style="popPosition" class="absolute left-0 top-0 h-7 -z-999"></div>
    </Popover>
  </div>
</template>
<style scoped>
::-webkit-scrollbar {
  width: 0 !important;
  height: 0 !important;
}
</style>
