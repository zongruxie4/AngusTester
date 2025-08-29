<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Input, notification } from '@xcan-angus/vue-ui';

const { t } = useI18n();

import SelectEnum from '@/components/selectEnum/index.vue';
import { Condition, ResponseMatchConfig } from './PropsType';
import { utils } from '@xcan-angus/infra';

interface Props {
  value: ResponseMatchConfig['body']&{condition:{message:string;value:Condition;}};
  notify?: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const CodeEditor = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/codeEditor/index.vue'));

const editorRef = ref();
const idList = ref<string[]>([]);
const inputValue = ref<string>();
const selectValue = ref<Condition>('EQUAL');
const errorFlag = ref(false);

const conditionChange = () => {
  errorFlag.value = false;
  inputValue.value = undefined;
};

const inputChange = () => {
  errorFlag.value = false;
};

const deleteHandler = () => {
  reset();
};

const format = () => {
  if (typeof editorRef.value?.format === 'function') {
    editorRef.value.format();
  }
};

const clear = () => {
  inputValue.value = '';
  if (typeof editorRef.value?.clear === 'function') {
    editorRef.value.clear();
  }
};

const reset = () => {
  idList.value = [];
  inputValue.value = undefined;
  selectValue.value = 'EQUAL';
  errorFlag.value = false;
  if (typeof editorRef.value?.clear === 'function') {
    editorRef.value.clear();
  }
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
    let value:string|undefined;
    if (['EQUAL', 'NOT_EQUAL', 'CONTAIN', 'NOT_CONTAIN'].includes(condition)) {
      value = expected;
    } else if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(condition)) {
      value = expression;
    }

    idList.value = [utils.uuid()];
    selectValue.value = condition;

    // @TODO 编辑器内容改变没有同步到父级，父级数据一直是原始数据，刷新时由于父级数据没有变化，所以编辑器的watch不会触发
    setTimeout(() => {
      inputValue.value = value;
    }, 0);
  }, { immediate: true });
});

const add = () => {
  if (idList.value.length) {
    notification.info(t('mock.mockApisComp.matchForm.requestBody.notifications.onlyOneBody'));
    return;
  }

  idList.value = [utils.uuid()];
  inputValue.value = undefined;
  selectValue.value = 'EQUAL';
  errorFlag.value = false;
};

const isValid = (): boolean => {
  errorFlag.value = false;
  if (!idList.value.length) {
    return true;
  }

  if (!isNoInput.value && showEditor.value) {
    if (typeof editorRef.value?.isValid === 'function') {
      errorFlag.value = !editorRef.value.isValid();
    }

    return !errorFlag.value;
  }

  if (!inputValue.value) {
    errorFlag.value = true;
    return false;
  }

  return true;
};

const getData = (): ResponseMatchConfig['body']|undefined => {
  if (!idList.value.length) {
    return undefined;
  }

  const condition = selectValue.value;
  const data: ResponseMatchConfig['body'] = { condition, expected: undefined, expression: undefined };
  let value = inputValue.value;
  if (!isNoInput.value && showEditor.value) {
    if (typeof editorRef.value?.getData === 'function') {
      value = editorRef.value.getData();
    }
  }

  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(condition)) {
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

const isNoInput = computed(() => {
  const condition = selectValue.value;
  if (!condition) {
    return false;
  }

  return ['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL'].includes(condition);
});

const showEditor = computed(() => {
  const condition = selectValue.value;
  if (!condition) {
    return false;
  }

  return ['EQUAL', 'NOT_EQUAL', 'CONTAIN', 'NOT_CONTAIN'].includes(condition);
});

const placeholderMap = {
  GREATER_THAN: t('mock.mockApisComp.matchForm.requestBody.value'),
  GREATER_THAN_EQUAL: t('mock.mockApisComp.matchForm.requestBody.value'),
  LESS_THAN: t('mock.mockApisComp.matchForm.requestBody.value'),
  LESS_THAN_EQUAL: t('mock.mockApisComp.matchForm.requestBody.value'),
  REG_MATCH: t('mock.mockApisComp.matchForm.parameterForm.regMatch'),
  XPATH_MATCH: t('mock.mockApisComp.matchForm.requestBody.xpathMatch'),
  JSON_PATH_MATCH: t('mock.mockApisComp.matchForm.requestBody.jsonPathMatch')
};
</script>
<template>
  <div v-if="!!idList.length" class="leading-5">
    <div class="flex items-center mb-0.5">{{ t('mock.mockApisComp.contentForm.requestBody') }}</div>
    <div class="flex items-center justify-between space-x-2">
      <SelectEnum
        v-model:value="selectValue"
        class="w-48.5"
        enumKey="FullMatchCondition"
        @change="conditionChange" />
      <div v-if="!isNoInput" class="flex-1 flex items-center justify-end space-x-2">
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="deleteHandler">
          <span>{{ t('actions.delete') }}</span>
        </Button>
        <template v-if="showEditor">
          <Button
            style="padding: 0;"
            type="link"
            size="small"
            @click="format">
            <span>{{ t('mock.mockApisComp.matchForm.requestBody.format') }}</span>
          </Button>
          <Button
            style="padding: 0;"
            type="link"
            size="small"
            @click="clear">
            <span>{{ t('mock.mockApisComp.matchForm.requestBody.clear') }}</span>
          </Button>
        </template>
      </div>
    </div>
    <template v-if="!isNoInput">
      <template v-if="showEditor">
        <CodeEditor
          ref="editorRef"
          v-model:value="inputValue"
          :showAction="false"
          class="mt-2" />
      </template>
      <template v-else>
        <Input
          v-model:value="inputValue"
          :maxlength="4096"
          :error="errorFlag"
          :placeholder="placeholderMap[selectValue]"
          trim
          class="w-full mt-2"
          @change="inputChange" />
      </template>
    </template>
  </div>
</template>
