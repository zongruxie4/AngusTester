<script setup lang="ts">
import { inject, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { Popover, Radio } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

import { services } from '@/api/altester';
import { HttpServer } from './PropsType';

interface Props {
  value: string;
  valueObj: HttpServer;
  readonly?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  readonly: false
});

// eslint-disable-next-line func-call-spacing
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
const variableReg = /\{[a-zA-Z0-9_]+\}/g; // 匹配任意字母数字（大小写均可）

const compositionend = (e): void => {
  inputFlag.value = false;
  handleChange(e);
};

const compositionstart = (): void => {
  inputFlag.value = true;
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
  emits('update:value', inputRef.value.innerText);
  valueHtml.value = inputRef.value.innerHTML;
};

const handleBlur = () => {
  focused.value = false;
  value.value = inputRef.value.innerText.replace(/\n/mg, '');
  emits('update:value', value.value);
  emits('handleBlur');
  valueHtml.value = inputRef.value.innerHTML;
};

const handleFocus = () => {
  focused.value = true;
  emits('focus');
};

const handleKeyPress = (e) => {
  if (e.code === 'Enter') {
    e.preventDefault();
    inputRef.value.blur();
    emits('enterPress');
  }
};

const handleChange = () => {
  valueHtml.value = inputRef.value.innerHTML;
};

const handleMouseover = (e) => {
  if (e.target.tagName === 'SPAN' && e.target.className.includes('xc-tip')) {
    popoverVisible.value = true;
    popPosition.width = e.target.offsetWidth + 'px';
    popPosition.left = e.target.offsetLeft + 'px';
    const key = e.target.innerText.replace('{', '').replace('}', '');
    const variables = serverObj.value.variables || {};
    if (variables[key]) {
      showErrorPopover.value = false;
    } else {
      showErrorPopover.value = true;
    }
  }
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
  const replaced = content.replace(variableReg, (match) => `<span class="text-blue-1 cursor-pointer xc-tip">${match}</span>`); // 将${abc}替换为<span>${abc}</span>
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

let delayTimer = null;

const serverDefaultChange = ref(false);
const changeEnumDefault = async (value: string, serverKey) => {
  serverDefaultChange.value = true;
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

const setTimer = () => {
  delayTimer = setTimeout(() => {
    if (serverDefaultChange.value) {
      delayTimer && clearTimeout(delayTimer);
      delayTimer = null;
      serverDefaultChange.value = false;
      return;
    }
    popoverVisible.value = false;
  }, 100);
};

onMounted(() => {
  document.addEventListener('click', setTimer);
});
onBeforeUnmount(() => {
  document.removeEventListener('click', setTimer);
});
</script>
<template>
  <div ref="inputWrapRef" class="relative border-r overflow-auto">
    <div
      v-show="!value && !focused"
      class="absolute left-2 top-0 h-full leading-5.5 py-1 text-theme-placeholder select-none pointer-events-none">
      服务器地址，接口地址URL前缀
    </div>
    <div
      ref="inputRef"
      class="px-2 py-1 outline-none h-7.5 leading-5.5 whitespace-nowrap flex"
      :contenteditable="!props.readonly"
      @keypress="handleKeyPress"
      @input="handleChange"
      @blur="handleBlur"
      @focus="handleFocus"
      @compositionend="compositionend"
      @compositionstart="compositionstart"
      @paste="handlePaste"
      @mouseover="handleMouseover">
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
            找不到该变量，请先定义变量或刷新再试
          </div>
          <div v-show="!showErrorPopover">
            <div class="font-bold text-text-title flex items-center">
              <Icon icon="icon-fuwuqi" class="mr-1" />{{ value }}
            </div>
            <div class="mb-1.5 pl-5">{{ serverObj.description || '' }}</div>
            <div class="border-t border-border-divider border-dashed mb-1"></div>
            <!-- <div v-if="valueObj.variables">变量：</div> -->
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
                      <span v-show="_value.default === en">默认</span>
                      <Radio
                        size="small"
                        :checked="_value.default === en"
                        class="-mr-0.25 ml-1"
                        @click="changeEnumDefault(en, key)" />
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
