<script setup lang="ts">
import { computed, ref, onMounted, nextTick } from 'vue';
import { Badge, Descriptions, DescriptionsItem, TypographyParagraph } from 'ant-design-vue';
import { AsyncComponent, Modal, Spin, MonacoEditor } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/tools';

import { ExecContent } from '../../../../PropsType';

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
    return TYPE_MAP[assertionType.value] + CONDITION_MAP[assertionCondition.value] + '值';
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
  return props.value?.extraction ? '提取值' : '期望值';
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
  STATUS: '响应状态码',
  HEADER: '响应头',
  BODY: '响应体',
  BODY_SIZE: '响应体大小',
  SIZE: '响应大小',
  DURATION: '耗时'
};

// 断言条件
const CONDITION_MAP = {
  CONTAIN: '包含',
  EQUAL: '等于',
  GREATER_THAN: '大于',
  GREATER_THAN_EQUAL: '大于等于',
  IS_EMPTY: '为空(空字符串或null值)',
  IS_NULL: '为null',
  LESS_THAN: '小于',
  LESS_THAN_EQUAL: '小于等于',
  NOT_CONTAIN: '不包含',
  NOT_EMPTY: '不为空(非空字符串或null值)',
  NOT_EQUAL: '不等于',
  NOT_NULL: '不为null',
  REG_MATCH: '正则匹配',
  XPATH_MATCH: 'XPath匹配',
  JSON_PATH_MATCH: 'JSONPath匹配'
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
          <div class="flex-shrink-0 text-text-link cursor-pointer" @click="openModal('real')">查看</div>
        </div>
      </template>

      <template v-else>
        <div :title="showRealValue">{{ showRealValue }}</div>
      </template>
    </DescriptionsItem>

    <DescriptionsItem label="断言条件">
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
          <div class="flex-shrink-0 text-text-link cursor-pointer" @click="openModal('expected')">查看</div>
        </div>
      </template>

      <template v-else>
        <div :title="showExpectedValue">{{ showExpectedValue }}</div>
      </template>
    </DescriptionsItem>

    <template v-if="assertionIgnored">
      <DescriptionsItem label="断言结果" :span="3">
        <Badge status="default" text="忽略" />
      </DescriptionsItem>
    </template>

    <template v-else>
      <template v-if="resultFailure">
        <DescriptionsItem label="断言结果" :span="!!resultMessage ? 1 : 3">
          <Badge
            class="flex-shrink-0"
            status="error"
            text="失败" />
        </DescriptionsItem>

        <DescriptionsItem
          v-if="!!resultMessage"
          label="失败原因"
          :span="2">
          <div :title="resultMessage">{{ resultMessage }}</div>
        </DescriptionsItem>
      </template>

      <template v-else>
        <DescriptionsItem label="断言结果" :span="3">
          <Badge status="success" text="通过" />
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
