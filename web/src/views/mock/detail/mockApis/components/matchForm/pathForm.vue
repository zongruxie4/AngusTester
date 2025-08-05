<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Composite, Icon, Input, notification, Popover } from '@xcan-angus/vue-ui';

import SelectEnum from '@/components/SelectEnum/index.vue'
import { StringCondition } from './PropsType';
import { utils } from '@xcan-angus/infra';

type PathInfo = {
  condition: StringCondition;
  expected: string | undefined;
  expression: string | undefined
}

interface Props {
  value: { [key: string]: any };
  notify: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const idList = ref<string[]>([]);
const selectValue = ref<StringCondition>('EQUAL');
const inputValue = ref<string>();
const errorFlag = ref(false);

const inputChange = () => {
  errorFlag.value = false;
};

const deleteHandler = () => {
  reset();
};

const reset = () => {
  idList.value = [];
  selectValue.value = 'EQUAL';
  inputValue.value = undefined;
  errorFlag.value = false;
};

onMounted(() => {
  watch(() => props.notify, () => {
    reset();
  });

  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }

    const { condition, expected, expression } = newValue;
    selectValue.value = condition;
    if (condition === 'REG_MATCH') {
      inputValue.value = expression;
    } else {
      inputValue.value = expected;
    }

    idList.value = [utils.uuid()];
  }, { immediate: true });
});

const add = () => {
  if (idList.value.length) {
    notification.info('只能添加一个路径');
    return;
  }

  idList.value = [utils.uuid()];
  selectValue.value = 'EQUAL';
  inputValue.value = undefined;
  errorFlag.value = false;
};

// @TODO 暂不校验
const isValidPath = (_pathname: string): boolean => {
  if (!_pathname) {
    return false;
  }

  return true;
};

const isValid = (): boolean => {
  errorFlag.value = false;
  if (!idList.value.length) {
    return true;
  }

  const value = inputValue.value;
  if (!value) {
    errorFlag.value = true;
    return false;
  }

  if (selectValue.value === 'REG_MATCH') {
    return true;
  }

  if (!isValidPath(value)) {
    errorFlag.value = true;
    return false;
  }

  return true;
};

const getData = (): PathInfo|undefined => {
  if (!idList.value.length) {
    return undefined;
  }

  const condition = selectValue.value;
  const value = inputValue.value;
  const data: PathInfo = { condition, expected: undefined, expression: undefined };
  if (condition === 'REG_MATCH') {
    data.expression = value;
  } else {
    data.expected = value;
  }

  return data;
};

defineExpose({
  getData,
  isValid,
  add
});

const ENUMS: readonly StringCondition[] = ['EQUAL', 'NOT_EQUAL', 'CONTAIN', 'NOT_CONTAIN', 'REG_MATCH'];
const excludes = ({ value }: { value: StringCondition }) => {
  return !ENUMS.includes(value);
};

const placeholderMap = {
  REG_MATCH: '正则表达式',
  EQUAL: '以斜杠 / 开始；路径段之间使用斜杠 / 分隔，且不包含特殊字符（如?、#等）',
  NOT_EQUAL: '以斜杠 / 开始；路径段之间使用斜杠 / 分隔，且不包含特殊字符（如?、#等）',
  CONTAIN: '以斜杠 / 开始；路径段之间使用斜杠 / 分隔，且不包含特殊字符（如?、#等）',
  NOT_CONTAIN: '以斜杠 / 开始；路径段之间使用斜杠 / 分隔，且不包含特殊字符（如?、#等）'
};
</script>
<template>
  <div v-if="idList.length" class="leading-5">
    <div class="flex items-center">
      <span class="mr-1 mb-0.5">路径</span>
      <Popover destroyTooltipOnHide>
        <template #content>
          <ul style="max-width: 520px;" class="pl-4 list-disc whitespace-pre-line break-all space-y-1">
            <li>路径以斜杠 / 开始，表示根路径。</li>
            <li>路径由多个路径段组成，路径段之间使用斜杠 / 分隔。</li>
            <li>每个路径段可以是字母、数字、减号、下划线等字符组成，且不包含特殊字符（如?、#等）。</li>
            <li>路径段不可以为空字符串。</li>
          </ul>
        </template>
        <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5" />
      </Popover>
    </div>
    <div class="flex items-center space-x-2">
      <Composite>
        <SelectEnum
          v-model:value="selectValue"
          :excludes="excludes"
          enumKey="FullMatchCondition"
          class="w-48"
          placeholder="运算条件" />
        <Input
          v-model:value="inputValue"
          :maxlength="4096"
          :placeholder="placeholderMap[selectValue]"
          :error="errorFlag"
          trim
          class="flex-1"
          @change="inputChange" />
      </Composite>
      <div class="flex-shrink-0 space-x-1">
        <Button type="text" size="small">
          <Icon
            icon="icon-qingchu"
            class="text-3.5"
            @click="deleteHandler" />
        </Button>
        <Button
          class="invisible"
          type="text"
          size="small">
          <Icon icon="icon-jia" class="text-3.5" />
        </Button>
      </div>
    </div>
  </div>
</template>
<style scoped>
.ant-btn {
  padding: 0;
}

.ant-btn:hover {
  color: var(--content-special-text-hover);
}
</style>
