<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Composite, Icon, Input, notification, Popover } from '@xcan-angus/vue-ui';

import SelectEnum from '@/components/selectEnum/index.vue';
import { StringCondition } from './PropsType';
import { utils } from '@xcan-angus/infra';

const { t } = useI18n();

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
    notification.info(t('mock.mockApisComp.matchForm.pathForm.notifications.onlyOnePath'));
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
  EQUAL: t('mock.mockApisComp.matchForm.pathForm.descriptions.equal'),
  NOT_EQUAL: t('mock.mockApisComp.matchForm.pathForm.descriptions.notEqual'),
  CONTAIN: t('mock.mockApisComp.matchForm.pathForm.descriptions.contain'),
  NOT_CONTAIN: t('mock.mockApisComp.matchForm.pathForm.descriptions.notContain')
};
</script>
<template>
  <div v-if="idList.length" class="leading-5">
    <div class="flex items-center">
      <span class="mr-1 mb-0.5">{{ t('mock.mockApisComp.matchForm.pathForm.path') }}</span>
      <Popover destroyTooltipOnHide>
        <template #content>
          <ul style="max-width: 520px;" class="pl-4 list-disc whitespace-pre-line break-all space-y-1">
            <li>{{ t('mock.mockApisComp.matchForm.pathForm.rules.rootPath') }}</li>
            <li>{{ t('mock.mockApisComp.matchForm.pathForm.rules.pathSegments') }}</li>
            <li>{{ t('mock.mockApisComp.matchForm.pathForm.rules.segmentChars') }}</li>
            <li>{{ t('mock.mockApisComp.matchForm.pathForm.rules.segmentNotEmpty') }}</li>
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
          :placeholder="t('mock.mockApisComp.matchForm.pathForm.operationCondition')" />
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
