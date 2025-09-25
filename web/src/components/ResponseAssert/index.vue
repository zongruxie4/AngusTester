<script setup lang="ts">
import { computed, defineAsyncComponent, nextTick, onMounted, ref, watch } from 'vue';
import {
  BadgeRibbon,
  Badge,
  Collapse,
  CollapsePanel,
  Descriptions,
  DescriptionsItem,
  TypographyParagraph,
  Tooltip
} from 'ant-design-vue';
import { Arrow, Modal, Icon, AsyncComponent, Spin } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import beautify from 'js-beautify';
import { useI18n } from 'vue-i18n';

import { AssertResult } from '@/components/ApiAssert/utils/assert/PropsType';

import { isJSON, isHtml, isXML, isYAML } from './utils';
const { t } = useI18n();

export type TabKey = 'all' | 'success' | 'fail' | 'ignore';

export type Item = AssertResult & { id: string };

export interface Props {
  value: AssertResult[] | undefined;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'rendered', value: true);
}>();

const MonacoEditor = defineAsyncComponent(() => import('../MonacoEditor/index.vue'));

const EMPTY_LIST = ['IS_EMPTY', 'IS_NULL', 'NOT_EMPTY', 'NOT_NULL'];

const TYPE_MAP = {
  STATUS: t('xcan_responseAssert.responseStatusCode'),
  HEADER: t('xcan_responseAssert.responseHeader'),
  BODY: t('xcan_responseAssert.responseBody'),
  BODY_SIZE: t('xcan_responseAssert.responseBodySize'),
  SIZE: t('xcan_responseAssert.responseSize'),
  DURATION: t('xcan_responseAssert.duration')
};

// 断言条件
const CONDITION_MAP = {
  CONTAIN: t('xcan_responseAssert.contain'),
  EQUAL: t('xcan_responseAssert.equal'),
  GREATER_THAN: t('xcan_responseAssert.greaterThan'),
  GREATER_THAN_EQUAL: t('xcan_responseAssert.greaterThanEqual'),
  IS_EMPTY: t('xcan_responseAssert.isEmpty'),
  IS_NULL: t('xcan_responseAssert.isNull'),
  LESS_THAN: t('xcan_responseAssert.lessThan'),
  LESS_THAN_EQUAL: t('xcan_responseAssert.lessThanEqual'),
  NOT_CONTAIN: t('xcan_responseAssert.notContain'),
  NOT_EMPTY: t('xcan_responseAssert.notEmpty'),
  NOT_EQUAL: t('xcan_responseAssert.notEqual'),
  NOT_NULL: t('xcan_responseAssert.notNull'),
  REG_MATCH: t('xcan_responseAssert.regexMatch'),
  XPATH_MATCH: t('xcan_responseAssert.xpathMatch'),
  JSON_PATH_MATCH: t('xcan_responseAssert.jsonPathMatch')
};

const activeKey = ref<TabKey>('all');
const change = (key: TabKey) => {
  activeKey.value = key;
};

const openKeys = ref<string[]>([]);
const collapseChange = (keys: string[]) => {
  openKeys.value = keys;
};

const arrowChange = (key: string) => {
  if (!openKeys.value.includes(key)) {
    openKeys.value.push(key);
    return;
  }

  openKeys.value = openKeys.value.filter(_key => _key !== key);
};

const successList = ref<Item[]>([]);
const failList = ref<Item[]>([]);
const ignoreList = ref<Item[]>([]);
const totalList = ref<Item[]>([]);
const backgroundStyleMap: { [key: string]: 'background-color:#f7f8fb;' | 'background-color:#fff2f0;' | 'background-color:#f6ffed;' } = {};

const showList = computed(() => {
  if (activeKey.value === 'all') {
    return totalList.value;
  }

  if (activeKey.value === 'fail') {
    return failList.value;
  }

  if (activeKey.value === 'ignore') {
    return ignoreList.value;
  }

  return successList.value;
});

const expectedLabel = computed(() => {
  return totalList.value?.reduce((prev, cur) => {
    prev[cur.id] = cur.extraction ? t('xcan_responseAssert.extractedValue') : t('xcan_responseAssert.expectedValue');
    return prev;
  }, {}) || {};
});

const assertLabel = computed(() => {
  return totalList.value?.reduce((prev, cur) => {
    let _type = cur.type;
    if (Object.prototype.toString.call(cur.type) === '[object Object]') {
      _type = cur.type.value;
    }

    if (_type) {
      if (_type === 'HEADER') {
        if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(cur.assertionCondition)) {
          prev[cur.id] = TYPE_MAP[_type] + (cur.parameterName ? cur.parameterName : '') + CONDITION_MAP[cur.assertionCondition] + t('common.value');
        } else {
          prev[cur.id] = TYPE_MAP[_type] + (cur.parameterName ? cur.parameterName : '') + t('common.value');
        }
      } else {
        if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(cur.assertionCondition)) {
          prev[cur.id] = prev[cur.id] = TYPE_MAP[_type] + CONDITION_MAP[cur.assertionCondition] + t('common.value');
        } else {
          prev[cur.id] = TYPE_MAP[_type];
        }
      }
    } else {
      prev[cur.id] = t('xcan_responseAssert.assertionData');
    }

    return prev;
  }, {}) || {};
});

onMounted(() => {
  watch(() => props.value, (newValue) => {
    successList.value = [];
    failList.value = [];
    totalList.value = [];
    ignoreList.value = [];
    if (!newValue?.length) {
      return;
    }

    for (let i = 0, len = newValue.length; i < len; i++) {
      const id = utils.uuid();
      const data = { ...newValue[i], id };
      data.assertionCondition = data.assertionCondition?.value || data.assertionCondition;
      if (data.assertionCondition) {
        if (EMPTY_LIST.includes(data.assertionCondition)) {
          data.result.expectedData = {
            data: CONDITION_MAP[data.assertionCondition],
            message: '',
            errorMessage: ''
          };
        }
      }

      if (typeof data.result.realValueData.data === 'string') {
        try {
          const jsonString = JSON.stringify(JSON.parse(data.result.realValueData.data));
          data.result.realValueData.data = jsonString;
        } catch (error) { }
      }

      if (typeof data.result.expectedData.data === 'string') {
        try {
          const jsonString = JSON.stringify(JSON.parse(data.result.expectedData.data));
          data.result.expectedData.data = jsonString;
        } catch (error) { }
      }

      // if (data.result.expectedData.data === null) {
      //   data.result.expectedData.data = 'null';
      // }

      // if (data.result.realValueData.data === null) {
      //   data.result.realValueData.data = 'null';
      // }

      if (data._condition.ignored) {
        backgroundStyleMap[id] = 'background-color:#f7f8fb;';
        ignoreList.value.push(data);
      } else {
        if (data.result.failure) {
          backgroundStyleMap[id] = 'background-color:#fff2f0;';
          failList.value.push(data);
        } else {
          backgroundStyleMap[id] = 'background-color:#f6ffed;';
          successList.value.push(data);
        }
      }

      totalList.value.push(data);
    }
  }, { immediate: true });

  nextTick(() => {
    emit('rendered', true);
  });
});

const editorLoading = ref(true);
const modalVisible = ref(false);
const editorValue = ref('');
const language = ref<'json' | 'xml' | 'text' | 'yaml'>('text');
// eslint-disable-next-line @typescript-eslint/no-empty-function
const onEllipsis = () => { };// 删除该方法不会触发省略
const ellipsis = { rows: 1, expandable: false, onEllipsis };
const openModal = (id: string, key: 'real' | 'expected') => {
  editorValue.value = editorDataMap.value[id]?.[key]?.data || '';
  language.value = editorDataMap.value[id]?.[key]?.language;
  modalVisible.value = true;
};

const editorDataMap = computed(() => {
  return totalList.value.reduce((prev, cur) => {
    prev[cur.id] = {
      real: {
        language: 'text',
        data: ''
      },
      expected: {
        language: 'text',
        data: ''
      }
    };

    const realValue = utils.isEmpty(cur.result.realValueData.data) ? '' : (typeof cur.result.realValueData.data === 'string' ? cur.result.realValueData.data : JSON.stringify(cur.result.realValueData.data));
    if (realValue) {
      if (isJSON(realValue)) {
        prev[cur.id].real = {
          language: 'json',
          data: beautify.js(realValue)
        };
      } else if (isXML(realValue) || isHtml(realValue)) {
        prev[cur.id].real = {
          language: 'xml',
          data: beautify.html(realValue)
        };
      } else if (isYAML(realValue)) {
        prev[cur.id].real = {
          language: 'yaml',
          data: realValue
        };
      } else {
        prev[cur.id].real = {
          language: 'text',
          data: realValue
        };
      }
    }

    const expectedValue = utils.isEmpty(cur.result.expectedData.data) ? '' : (typeof cur.result.expectedData.data === 'string' ? cur.result.expectedData.data : JSON.stringify(cur.result.expectedData.data));
    if (expectedValue) {
      if (isJSON(expectedValue)) {
        prev[cur.id].expected = {
          language: 'json',
          data: beautify.js(expectedValue)
        };
      } else if (isXML(expectedValue) || isHtml(expectedValue)) {
        prev[cur.id].expected = {
          language: 'xml',
          data: beautify.html(expectedValue)
        };
      } else if (isYAML(expectedValue)) {
        prev[cur.id].expected = {
          language: 'yaml',
          data: expectedValue
        };
      } else {
        prev[cur.id].expected = {
          language: 'text',
          data: expectedValue
        };
      }
    }

    return prev;
  }, {} as {
    [key: string]: {
      real: { language: 'json' | 'xml' | 'text' | 'yaml'; data: string; },
      expected: { language: 'json' | 'xml' | 'text' | 'yaml'; data: string; }
    }
  });
});
</script>
<template>
  <div class="h-full py-3 text-3">
    <div
      class="inline-flex items-center h-7 rounded select-none overflow-hidden cursor-pointer bg-gray-light text-theme-sub-content">
      <div
        :class="{ 'tab-selected': activeKey === 'all' }"
        class="flex justify-center items-center min-w-25 px-3 h-full"
        @click="change('all')">
        <span>{{ t('xcan_responseAssert.all') }}</span>
        <span class="ml-1">({{ totalList.length }})</span>
      </div>
      <div
        :class="{ 'tab-selected': activeKey === 'success' }"
        class="flex justify-center items-center min-w-25 px-3 h-full"
        @click="change('success')">
        <span>{{ t('status.passed') }}</span>
        <span class="ml-1">(<span class="text-status-success">{{ successList.length }}</span>)</span>
      </div>
      <div
        :class="{ 'tab-selected': activeKey === 'fail' }"
        class="flex justify-center items-center min-w-25 px-3 h-full"
        @click="change('fail')">
        <span>{{ t('status.failed') }}</span>
        <span class="ml-1">(<span class="text-status-error">{{ failList.length }}</span>)</span>
      </div>
      <div
        :class="{ 'tab-selected': activeKey === 'ignore' }"
        class="flex justify-center items-center min-w-25 px-3 h-full"
        @click="change('ignore')">
        <span>{{ t('status.ignored') }}</span>
        <span class="ml-1">(<span class="text-status-close">{{ ignoreList.length }}</span>)</span>
      </div>
    </div>
    <div
      style="width: calc(100% + 20px);height: calc(100% - 35px); padding-right: 20px;padding-left: 7px;"
      class="overflow-auto">
      <Collapse
        :activeKey="openKeys"
        :bordered="false"
        style="margin-top: 12px;background-color: #fff;font-size: 12px;"
        @change="collapseChange">
        <CollapsePanel
          v-for="item in showList"
          :key="item.id"
          :style="backgroundStyleMap[item.id]"
          :showArrow="false"
          style="margin-bottom: 6px;border: none;border-radius: 4px;line-height: 18px;">
          <template #header>
            <div class="flex-1">
              <template v-if="item._condition.ignored">
                <BadgeRibbon
                  placement="start"
                  :text="t('status.ignored')"
                  style="top: -4px;left: -49px;transform: scale(0.9);font-size: 12px;"
                  color="rgba(217, 217, 217, 1)">
                  <div class="flex justify-between items-center">
                    <div class="break-all">{{ item.name }}</div>
                    <Arrow :open="openKeys.includes(item.id)" @change="arrowChange(item.id)" />
                  </div>
                </BadgeRibbon>
              </template>

              <template v-else>
                <template v-if="item.result.failure">
                  <BadgeRibbon
                    placement="start"
                    :text="t('xcan_responseAssert.fail')"
                    style="top: -4px;left: -49px;transform: scale(0.9);font-size: 12px;"
                    color="rgba(245, 34, 45, 1)">
                    <div class="flex justify-between items-center">
                      <div class="break-all">{{ item.name }}</div>
                      <Arrow :open="openKeys.includes(item.id)" @change="arrowChange(item.id)" />
                    </div>
                  </BadgeRibbon>
                </template>

                <template v-else>
                  <BadgeRibbon
                    placement="start"
                    :text="t('xcan_responseAssert.pass')"
                    style="top: -4px;left: -49px;transform: scale(0.9);font-size: 12px;"
                    color="rgba(82, 196, 26, 1)">
                    <div class="flex justify-between items-center">
                      <div class="break-all">{{ item.name }}</div>
                      <Arrow :open="openKeys.includes(item.id)" @change="arrowChange(item.id)" />
                    </div>
                  </BadgeRibbon>
                </template>
              </template>
            </div>
          </template>

          <Descriptions size="small" bordered>
            <DescriptionsItem>
              <template #label>
                <div class="flex items-center">
                  <div class="mr-1.5">{{ t('xcan_responseAssert.executionConditionExpression') }}</div>
                  <template v-if="item._condition.conditionMessage">
                    <Tooltip>
                      <template #title>{{ item._condition.conditionMessage }}</template>
                      <Icon class="text-status-warn cursor-pointer" icon="icon-jinggao" />
                    </Tooltip>
                  </template>
                </div>
              </template>
              {{ item.condition }}
            </DescriptionsItem>

            <DescriptionsItem>
              <template #label>
                <div class="flex items-center">
                  <div class="mr-1.5">{{ item._condition.name || t('common.variables') }}值</div>
                  <template v-if="item._condition.failureMessage">
                    <Tooltip>
                      <template #title>{{ item._condition.failureMessage }}</template>
                      <Icon class="text-status-warn cursor-pointer" icon="icon-jinggao" />
                    </Tooltip>
                  </template>
                </div>
              </template>
              {{ item._condition.value }}
            </DescriptionsItem>

            <DescriptionsItem :label="t('xcan_responseAssert.executionConditionResult')">
              <template v-if="item._condition.ignored">
                <Badge status="default" :text="item._condition.message" />
              </template>
              <template v-else-if="item._condition.failure">
                <Badge status="error" :text="item._condition.message" />
              </template>
              <template v-else>
                <Badge status="success" :text="item._condition.message" />
              </template>
            </DescriptionsItem>

            <DescriptionsItem :label="assertLabel[item.id]">
              <template v-if="(item.type === 'BODY' || item.type === 'HEADER')&&item.result.realValueData.data">
                <div class="flex items-center justify-between flex-nowrap">
                  <TypographyParagraph
                    style="word-wrap: unset;word-break: break-all;white-space: break-spaces;"
                    :ellipsis="ellipsis"
                    :content="item.result.realValueData.data" />
                  <div class="flex-shrink-0 text-text-link cursor-pointer" @click="openModal(item.id, 'real')">{{ t('actions.view') }}</div>
                </div>
              </template>

              <template v-else>
                {{ item.result.realValueData.data }}
              </template>
            </DescriptionsItem>

            <DescriptionsItem :label="t('xcan_responseAssert.assertionCondition')">
              {{ item.assertionCondition && CONDITION_MAP[item.assertionCondition] }}
            </DescriptionsItem>

            <DescriptionsItem>
              <template #label>
                <div class="flex items-center">
                  <div class="mr-1.5">{{ expectedLabel[item.id] }}</div>
                  <template v-if="item.result.expectedData.message">
                    <Tooltip>
                      <template #title>{{ item.result.expectedData.message }}</template>
                      <Icon class="text-status-warn cursor-pointer" icon="icon-jinggao" />
                    </Tooltip>
                  </template>
                </div>
              </template>

              <template v-if="!EMPTY_LIST.includes(item.assertionCondition)&&(item.type === 'BODY' || item.type === 'HEADER')&&item.result.expectedData.data">
                <div class="flex items-center justify-between flex-nowrap">
                  <TypographyParagraph
                    style="word-wrap: unset;word-break: break-all;white-space: break-spaces;"
                    :ellipsis="ellipsis"
                    :content="item.result.expectedData.data" />
                  <div class="flex-shrink-0 text-text-link cursor-pointer" @click="openModal(item.id, 'expected')">{{ t('actions.view') }}</div>
                </div>
              </template>

              <template v-else>
                {{ item.result.expectedData.data }}
              </template>
            </DescriptionsItem>

            <template v-if="item._condition.ignored">
              <DescriptionsItem :label="t('xcan_responseAssert.assertionResult')" :span="3">
                <Badge status="default" :text="t('status.ignored')" />
              </DescriptionsItem>
            </template>

            <template v-else>
              <template v-if="item.result.failure">
                <DescriptionsItem :label="t('xcan_responseAssert.assertionResult')" :span="!!item.result.message ? 1 : 3">
                  <Badge
                    class="flex-shrink-0"
                    status="error"
                    :text="t('status.failed')" />
                </DescriptionsItem>

                <DescriptionsItem
                  v-if="!!item.result.message"
                  :label="t('xcan_responseAssert.failureReason')"
                  :span="2">
                  {{ item.result.message }}
                </DescriptionsItem>
              </template>

              <template v-else>
                <DescriptionsItem :label="t('xcan_responseAssert.assertionResult')" :span="3">
                  <Badge status="success" :text="t('status.passed')" />
                </DescriptionsItem>
              </template>
            </template>
          </Descriptions>
        </CollapsePanel>
      </Collapse>
    </div>
    <AsyncComponent :visible="modalVisible">
      <Modal
        v-model:visible="modalVisible"
        wrapClassName="assert-modal"
        width="98%"
        :footer="false">
        <Spin
          class="w-full h-full"
          :spinning="editorLoading"
          :mask="false">
          <MonacoEditor
            v-model:loading="editorLoading"
            readOnly
            :value="editorValue"
            :language="language" />
        </Spin>
      </Modal>
    </AsyncComponent>
  </div>
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
  height: calc(95vh);
}

.assert-modal .ant-modal-body {
  flex: 1;
  height: calc(100% - 44px);
  padding: 0;
}
</style>

<style scoped>
.tab-selected {
  @apply bg-blue-active text-text-content;
}

.ant-collapse > :deep(.ant-collapse-item) > .ant-collapse-header {
  padding: 7px 14px 7px 40px;
  line-height: 18px;
}

.ant-collapse > :deep(.ant-collapse-item) > .ant-collapse-header .ant-collapse-arrow {
  margin-right: 4px;
}

.ant-collapse-borderless > :deep(.ant-collapse-item) > .ant-collapse-content {
  background-color: #fff;
}

.ant-collapse-borderless :deep(.ant-descriptions-bordered.ant-descriptions-small) .ant-descriptions-item-label {
  width: 200px;
}

.ant-collapse-borderless :deep(.ant-descriptions-bordered.ant-descriptions-small) .ant-descriptions-item-content {
  width: 320px;
}

.ant-collapse-borderless :deep(.ant-descriptions-bordered.ant-descriptions-small) .ant-descriptions-item-label,
.ant-collapse-borderless :deep(.ant-descriptions-bordered.ant-descriptions-small) .ant-descriptions-item-content {
  height: 32px;
  padding: 0 12px;
}

:deep(.ant-collapse-content) > .ant-collapse-content-box {
  padding: 8px;
}

.ant-collapse-borderless > :deep(.ant-collapse-item) > .ant-collapse-content > .ant-collapse-content-box {
  padding-top: 8px;
}
</style>
