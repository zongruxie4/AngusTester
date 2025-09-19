<script setup lang="ts">
import { computed, ref, onMounted, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { Badge, Descriptions, DescriptionsItem, TypographyParagraph } from 'ant-design-vue';
import { AsyncComponent, Modal, Spin, MonacoEditor } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { ExecContent } from '../../../../PropsType';

const { t } = useI18n();

export interface Props {
  value: ExecContent['content']['assertions'][number];
  execContent:ExecContent['content'];
  status: 'Disabled' | 'Ignored' | 'Success' | 'Failed';
  ignoreAssertions:boolean;
  loading:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  status: undefined,
  execContent: undefined,
  ignoreAssertions: false,
  loading: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:loading', value:boolean):void;
}>();

const editorRef = ref();
const editorValue = ref('');
const editorLoading = ref(true);
const modalVisible = ref(false);

onMounted(() => {
  nextTick(() => {
    emit('update:loading', false);
  });
});

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const openModal = (key: 'real' | 'expected') => {
  if (key === 'real') {
    editorValue.value = editorRealValue.value;
  } else {
    editorValue.value = editorExpectedValue.value;
  }
  modalVisible.value = true;
  setTimeout(() => {
    if (typeof editorRef.value?.format === 'function') {
      editorRef.value.format();
    }
  }, 16.67);
};

const assertionIgnored = computed(() => {
  return props.ignoreAssertions || props.value.ignore || !props.value?.enabled;
});

const assertLabel = computed(() => {
  if (!assertionType.value) {
    return '';
  }

  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition.value)) {
    return TYPE_MAP[assertionType.value] + CONDITION_MAP[assertionCondition.value] + t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.expectedValue');
  }

  return TYPE_MAP[assertionType.value];
});

const assertionType = computed(() => {
  return props.value?.type?.value;
});

const realValue = computed(() => {
  if (!assertionType.value) {
    return '';
  }

  if (assertionType.value === 'BODY') {
    if (typeof props.execContent?.response?.rows === 'object') {
      return JSON.stringify(props.execContent?.response?.rows);
    }
    return props.execContent?.response?.rows || '';
  }

  if (assertionType.value === 'BODY_SIZE') {
    return utils.formatBytes(+props.execContent?.response?.size);
  }

  if (assertionType.value === 'DURATION') {
    return utils.formatTime(+props.execContent?.response?.timeline?.total);
  }

  return '';
});

const showRealValue = computed(() => {
  try {
    return JSON.stringify(JSON.parse(realValue.value));
  } catch (error) {
    return realValue.value;
  }
});

const editorRealValue = computed(() => {
  if (!realValue.value) {
    return '';
  }

  return JSON.stringify(JSON.parse(realValue.value), null, 2);
});

const assertionCondition = computed(() => {
  return props.value?.assertionCondition?.value;
});

const expectedLabel = computed(() => {
  return props.value?.extraction ? t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.extractedValue') : t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.expectedValue');
});

const expectedValue = computed(() => {
  const actualExpected = props.value?.actualExpected;
  if (!actualExpected) {
    return '--';
  }

  if (['BODY_SIZE', 'SIZE'].includes(assertionType.value)) {
    return utils.formatBytes(+actualExpected);
  }

  if (assertionType.value === 'DURATION') {
    return utils.formatTime(+actualExpected);
  }

  return actualExpected;
});

const showExpectedValue = computed(() => {
  try {
    return JSON.stringify(JSON.parse(expectedValue.value));
  } catch (error) {
    return expectedValue.value;
  }
});

const editorExpectedValue = computed(() => {
  try {
    return JSON.stringify(JSON.parse(expectedValue.value), null, 2);
  } catch (error) {
    return expectedValue.value;
  }
});

const resultMessage = computed(() => {
  return props.value?.result?.message;
});

const resultFailure = computed(() => {
  return !!props.value?.result?.failure;
});

// eslint-disable-next-line @typescript-eslint/no-empty-function
const onEllipsis = () => { };// 删除该方法不会触发省略
const ellipsis = { rows: 1, expandable: false, onEllipsis };

const TYPE_MAP = {
  STATUS: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.typeMap.STATUS'),
  HEADER: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.typeMap.HEADER'),
  BODY: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.typeMap.BODY'),
  BODY_SIZE: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.typeMap.BODY_SIZE'),
  SIZE: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.typeMap.SIZE'),
  DURATION: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.typeMap.DURATION')
};

// 断言条件
const CONDITION_MAP = {
  CONTAIN: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.CONTAIN'),
  EQUAL: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.EQUAL'),
  GREATER_THAN: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.GREATER_THAN'),
  GREATER_THAN_EQUAL: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.GREATER_THAN_EQUAL'),
  IS_EMPTY: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.IS_EMPTY'),
  IS_NULL: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.IS_NULL'),
  LESS_THAN: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.LESS_THAN'),
  LESS_THAN_EQUAL: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.LESS_THAN_EQUAL'),
  NOT_CONTAIN: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.NOT_CONTAIN'),
  NOT_EMPTY: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.NOT_EMPTY'),
  NOT_EQUAL: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.NOT_EQUAL'),
  NOT_NULL: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.NOT_NULL'),
  REG_MATCH: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.REG_MATCH'),
  XPATH_MATCH: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.XPATH_MATCH'),
  JSON_PATH_MATCH: t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.conditionMap.JSON_PATH_MATCH')
};

const EMPTY_LIST = ['IS_EMPTY', 'IS_NULL', 'NOT_EMPTY', 'NOT_NULL'];
</script>
<template>
  <Descriptions
    class="px-3 pt-2 pb-3"
    size="small"
    bordered>
    <DescriptionsItem :label="assertLabel">
      <template v-if="(assertionType === 'BODY') && showRealValue">
        <div class="flex items-center justify-between flex-nowrap">
          <TypographyParagraph
            style="word-wrap: unset;word-break: break-all;white-space: break-spaces;"
            :ellipsis="ellipsis"
            :content="showRealValue" />
          <div class="flex-shrink-0 text-text-link cursor-pointer" @click="openModal('real')">{{ t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.view') }}</div>
        </div>
      </template>

      <template v-else>
        <div :title="showRealValue">{{ showRealValue }}</div>
      </template>
    </DescriptionsItem>

    <DescriptionsItem :label="t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.assertionCondition')">
      {{ CONDITION_MAP[assertionCondition] }}
    </DescriptionsItem>

    <DescriptionsItem>
      <template #label>
        <div class="flex items-center">
          <div class="mr-1.5">{{ expectedLabel }}</div>
        </div>
      </template>

      <template v-if="!EMPTY_LIST.includes(assertionCondition) && assertionType === 'BODY' && showExpectedValue">
        <div class="flex items-center justify-between flex-nowrap">
          <TypographyParagraph
            style="word-wrap: unset;word-break: break-all;white-space: break-spaces;"
            :ellipsis="ellipsis"
            :content="showExpectedValue" />
          <div class="flex-shrink-0 text-text-link cursor-pointer" @click="openModal('expected')">{{ t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.view') }}</div>
        </div>
      </template>

      <template v-else>
        <div :title="showExpectedValue">{{ showExpectedValue }}</div>
      </template>
    </DescriptionsItem>

    <template v-if="assertionIgnored">
      <DescriptionsItem :label="t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.assertionResult')" :span="3">
        <Badge status="default" :text="t('jdbcPlugin.FunctionTestDetailJdbc.assertionStatus.ignored')" />
      </DescriptionsItem>
    </template>

    <template v-else>
      <template v-if="resultFailure">
        <DescriptionsItem :label="t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.assertionResult')" :span="!!resultMessage ? 1 : 3">
          <Badge
            class="flex-shrink-0"
            status="error"
            text="失败" />
        </DescriptionsItem>

        <DescriptionsItem
          v-if="!!resultMessage"
          :label="t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.failureReason')"
          :span="2">
          <div :title="resultMessage">{{ resultMessage }}</div>
        </DescriptionsItem>
      </template>

      <template v-else>
        <DescriptionsItem :label="t('jdbcPlugin.FunctionTestDetailJdbc.assertionResult.assertionResult')" :span="3">
          <Badge status="success" :text="t('jdbcPlugin.FunctionTestDetailJdbc.assertionStatus.passed')" />
        </DescriptionsItem>
      </template>
    </template>
  </Descriptions>

  <AsyncComponent :visible="modalVisible">
    <Modal
      v-model:visible="modalVisible"
      wrapClassName="assert-modal"
      width="95%"
      :footer="false">
      <Spin
        class="w-full h-full py-3"
        :spinning="editorLoading"
        :mask="false">
        <MonacoEditor
          ref="editorRef"
          v-model:loading="editorLoading"
          readOnly
          class="h-full"
          :value="editorValue"
          language="json" />
      </Spin>
    </Modal>
  </AsyncComponent>
</template>

<style>
.assert-modal .ant-modal {
  top: 0;
  max-width: 100%;
  margin: 0;
  padding-bottom: 0;
}

.assert-modal .ant-modal-content {
  display: flex;
  flex-direction: column;
  height: calc(92vh);
}

.assert-modal .ant-modal-body {
  flex: 1;
  height: calc(100% - 44px);
  padding: 0;
}
</style>

<style scoped>
.ant-descriptions-bordered.ant-descriptions-small :deep(.ant-descriptions-item-label),
.ant-descriptions-bordered.ant-descriptions-small :deep(.ant-descriptions-item-content) {
  width: 16.67%;
}
</style>
