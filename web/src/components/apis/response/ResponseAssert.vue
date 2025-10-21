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

import { AssertResult } from '@/components/apis/assertion/utils/assert/PropsType';

import { isJSON, isHtml, isXML, isYAML } from './utils';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Tab filter keys for assertion results
 */
export type TabKey = 'all' | 'success' | 'fail' | 'ignore';

/**
 * Extended assertion result item with unique ID
 */
export type Item = AssertResult & { id: string };

/**
 * Component props interface
 */
export interface Props {
  value: AssertResult[] | undefined;  // Array of assertion results to display
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

/**
 * Emitted events interface
 */
const emit = defineEmits<{
  (e: 'rendered', value: true): void;  // Emitted when component is fully rendered
}>();

/**
 * Async component: Monaco Editor for content viewing
 * Used in modal for displaying large content with syntax highlighting
 */
const MonacoEditor = defineAsyncComponent(() => import('@/components/editor/MonacoEditor/index.vue'));

/**
 * List of assertion conditions that don't require expected values
 * These conditions check for empty/null states rather than specific values
 */
const EMPTY_LIST = ['IS_EMPTY', 'IS_NULL', 'NOT_EMPTY', 'NOT_NULL'];

/**
 * Mapping of assertion types to localized display names
 * Maps internal type codes to user-friendly labels
 */
const TYPE_MAP = {
  STATUS: t('protocol.statusCode'),        // HTTP status code
  HEADER: t('protocol.responseHeader'),    // Response headers
  BODY: t('protocol.responseBody'),        // Response body content
  BODY_SIZE: t('protocol.responseBodySize'), // Response body size
  SIZE: t('protocol.responseSize'),        // Total response size
  DURATION: t('common.duration')           // Response duration
};

/**
 * Mapping of assertion conditions to localized display names
 * Maps internal condition codes to user-friendly labels
 * TODO: Consider using enums instead of this mapping
 */
const CONDITION_MAP = {
  CONTAIN: t('xcan_responseAssert.contain'),           // String contains check
  EQUAL: t('xcan_responseAssert.equal'),               // Exact equality check
  GREATER_THAN: t('xcan_responseAssert.greaterThan'),  // Greater than comparison
  GREATER_THAN_EQUAL: t('xcan_responseAssert.greaterThanEqual'), // Greater than or equal
  IS_EMPTY: t('xcan_responseAssert.isEmpty'),          // Empty value check
  IS_NULL: t('xcan_responseAssert.isNull'),            // Null value check
  LESS_THAN: t('xcan_responseAssert.lessThan'),        // Less than comparison
  LESS_THAN_EQUAL: t('xcan_responseAssert.lessThanEqual'), // Less than or equal
  NOT_CONTAIN: t('xcan_responseAssert.notContain'),    // String does not contain
  NOT_EMPTY: t('xcan_responseAssert.notEmpty'),        // Not empty check
  NOT_EQUAL: t('xcan_responseAssert.notEqual'),        // Not equal check
  NOT_NULL: t('xcan_responseAssert.notNull'),          // Not null check
  REG_MATCH: t('xcan_responseAssert.regexMatch'),      // Regular expression match
  XPATH_MATCH: t('xcan_responseAssert.xpathMatch'),    // XPath expression match
  JSON_PATH_MATCH: t('xcan_responseAssert.jsonPathMatch') // JSONPath expression match
};

/**
 * Ref: Currently active tab filter
 * Controls which assertion results are displayed
 */
const activeKey = ref<TabKey>('all');

/**
 * Change active tab filter
 * Updates the active tab and triggers filtered list recomputation
 *
 * @param key - Tab key to activate
 */
const change = (key: TabKey): void => {
  activeKey.value = key;
};

/**
 * Ref: Array of open collapse panel keys
 * Tracks which assertion detail panels are expanded
 */
const openKeys = ref<string[]>([]);

/**
 * Handle collapse panel change
 * Updates the list of open panel keys
 *
 * @param keys - Array of panel keys that should be open
 */
const collapseChange = (keys: any): void => {
  openKeys.value = Array.isArray(keys) ? keys : [keys];
};

/**
 * Toggle collapse panel state
 * Adds or removes a panel key from the open keys list
 *
 * @param key - Panel key to toggle
 */
const arrowChange = (key: string): void => {
  if (!openKeys.value.includes(key)) {
    openKeys.value.push(key);
    return;
  }

  openKeys.value = openKeys.value.filter(_key => _key !== key);
};

/**
 * Ref: List of successful assertions
 * Contains assertions that passed validation
 */
const successList = ref<Item[]>([]);

/**
 * Ref: List of failed assertions
 * Contains assertions that failed validation
 */
const failList = ref<Item[]>([]);

/**
 * Ref: List of ignored assertions
 * Contains assertions that were skipped or ignored
 */
const ignoreList = ref<Item[]>([]);

/**
 * Ref: List of all assertions
 * Contains all assertion results regardless of status
 */
const totalList = ref<Item[]>([]);

/**
 * Background style mapping for assertion panels
 * Maps assertion IDs to color-coded background styles based on status
 */
const backgroundStyleMap: {
  [key: string]: 'background-color:#f7f8fb;' | 'background-color:#fff2f0;' | 'background-color:#f6ffed;'
} = {};

/**
 * Computed: Filtered list of assertions to display
 * Returns assertions based on the currently active tab filter
 *
 * @returns Filtered array of assertion items
 */
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

/**
 * Computed: Expected value labels for each assertion
 * Maps assertion IDs to appropriate label text (extracted vs expected value)
 *
 * @returns Object mapping assertion IDs to label text
 */
const expectedLabel = computed(() => {
  return totalList.value?.reduce((prev, cur) => {
    prev[cur.id] = cur.extraction ? t('xcan_responseAssert.extractedValue') : t('xcan_responseAssert.expectedValue');
    return prev;
  }, {}) || {};
});

/**
 * Computed: Assertion labels for each assertion
 * Generates descriptive labels combining type, condition, and parameter information
 *
 * @returns Object mapping assertion IDs to descriptive labels
 */
const assertLabel = computed(() => {
  return totalList.value?.reduce((prev, cur) => {
    let _type = cur.type;

    // Handle type as object (extract value property)
    if (Object.prototype.toString.call(cur.type) === '[object Object]') {
      _type = (cur.type as any).value;
    }

    if (_type) {
      if (_type === 'HEADER') {
        // Header assertions with parameter names
        if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(cur.assertionCondition)) {
          prev[cur.id] = TYPE_MAP[_type] + (cur.parameterName ? cur.parameterName : '') + CONDITION_MAP[cur.assertionCondition] + t('common.value');
        } else {
          prev[cur.id] = TYPE_MAP[_type] + (cur.parameterName ? cur.parameterName : '') + t('common.value');
        }
      } else {
        // Other assertion types
        if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(cur.assertionCondition)) {
          prev[cur.id] = TYPE_MAP[_type] + CONDITION_MAP[cur.assertionCondition] + t('common.value');
        } else {
          prev[cur.id] = TYPE_MAP[_type];
        }
      }
    } else {
      // Fallback for unknown types
      prev[cur.id] = t('xcan_responseAssert.assertionData');
    }

    return prev;
  }, {}) || {};
});

/**
 * Component mounted lifecycle hook
 * Sets up data processing and emits rendered event
 */
onMounted(() => {
  /**
   * Watch: Process assertion results when props.value changes
   * Categorizes assertions by status and prepares data for display
   */
  watch(() => props.value, (newValue) => {
    // Clear existing lists
    successList.value = [];
    failList.value = [];
    totalList.value = [];
    ignoreList.value = [];

    if (!newValue?.length) {
      return;
    }

    // Process each assertion result
    for (let i = 0, len = newValue.length; i < len; i++) {
      const id = utils.uuid();
      const data = { ...newValue[i], id };

      // Normalize assertion condition (handle object format)
      data.assertionCondition = (data.assertionCondition as any)?.value || data.assertionCondition;

      // Handle empty/null conditions (set expected data to condition description)
      if (data.assertionCondition) {
        if (EMPTY_LIST.includes(data.assertionCondition)) {
          data.result.expectedData = {
            data: CONDITION_MAP[data.assertionCondition],
            message: '',
            errorMessage: ''
          };
        }
      }

      // Format JSON strings for better display
      if (typeof data.result.realValueData.data === 'string') {
        try {
          const jsonString = JSON.stringify(JSON.parse(data.result.realValueData.data));
          data.result.realValueData.data = jsonString;
        } catch (error) {
          // Keep original string if not valid JSON
        }
      }

      if (typeof data.result.expectedData.data === 'string') {
        try {
          const jsonString = JSON.stringify(JSON.parse(data.result.expectedData.data));
          data.result.expectedData.data = jsonString;
        } catch (error) {
          // Keep original string if not valid JSON
        }
      }

      // Categorize assertions by status and set background colors
      if (data._condition.ignored) {
        // Ignored assertions (gray background)
        backgroundStyleMap[id] = 'background-color:#f7f8fb;';
        ignoreList.value.push(data);
      } else {
        if (data.result.failure) {
          // Failed assertions (red background)
          backgroundStyleMap[id] = 'background-color:#fff2f0;';
          failList.value.push(data);
        } else {
          // Successful assertions (green background)
          backgroundStyleMap[id] = 'background-color:#f6ffed;';
          successList.value.push(data);
        }
      }

      // Add to total list
      totalList.value.push(data);
    }
  }, { immediate: true });

  // Emit rendered event after component is fully mounted
  nextTick(() => {
    emit('rendered', true);
  });
});

/**
 * Ref: Monaco editor loading state
 * Controls spinner display while editor is initializing
 */
const editorLoading = ref(true);

/**
 * Ref: Modal visibility state
 * Controls whether the content viewer modal is open
 */
const modalVisible = ref(false);

/**
 * Ref: Content to display in Monaco editor
 * The actual data content being viewed in the modal
 */
const editorValue = ref('');

/**
 * Ref: Monaco editor language mode
 * Determines syntax highlighting (json, html, text, yaml)
 */
const language = ref<'json' | 'html' | 'text' | 'yaml'>('text');

/**
 * Empty function for ellipsis callback
 * Required by TypographyParagraph component to trigger ellipsis behavior
 * Removing this method would prevent ellipsis from working
 */
// eslint-disable-next-line @typescript-eslint/no-empty-function
const onEllipsis = (): void => { };

/**
 * Ellipsis configuration for TypographyParagraph
 * Limits text display to 1 row with no expansion
 */
const ellipsis = { rows: 1, expandable: false, onEllipsis };

/**
 * Open modal to view assertion data
 * Sets up editor content and language, then opens the modal
 *
 * @param id - Assertion ID to view
 * @param key - Data type to view ('real' or 'expected')
 */
const openModal = (id: string, key: 'real' | 'expected'): void => {
  editorValue.value = editorDataMap.value[id]?.[key]?.data || '';
  language.value = editorDataMap.value[id]?.[key]?.language;
  modalVisible.value = true;
};

/**
 * Computed: Editor data mapping for modal display
 * Processes assertion data and determines appropriate language and formatting
 * for Monaco editor display in the modal
 *
 * @returns Object mapping assertion IDs to formatted editor data
 */
const editorDataMap = computed(() => {
  return totalList.value.reduce((prev, cur) => {
    // Initialize default structure
    prev[cur.id] = {
      real: {
        language: 'text' as const,
        data: ''
      },
      expected: {
        language: 'text' as const,
        data: ''
      }
    };

    // Process real value data
    const realValue = utils.isEmpty(cur.result.realValueData.data)
      ? ''
      : (typeof cur.result.realValueData.data === 'string'
          ? cur.result.realValueData.data
          : JSON.stringify(cur.result.realValueData.data));

    if (realValue) {
      if (isJSON(realValue)) {
        // JSON data with beautification
        prev[cur.id].real = {
          language: 'json',
          data: beautify.js(realValue)
        };
      } else if (isXML(realValue) || isHtml(realValue)) {
        // XML/HTML data with beautification
        prev[cur.id].real = {
          language: 'html',
          data: beautify.html(realValue)
        };
      } else if (isYAML(realValue)) {
        // YAML data (no beautification needed)
        prev[cur.id].real = {
          language: 'yaml',
          data: realValue
        };
      } else {
        // Plain text data
        prev[cur.id].real = {
          language: 'text',
          data: realValue
        };
      }
    }

    // Process expected value data
    const expectedValue = utils.isEmpty(cur.result.expectedData.data)
      ? ''
      : (typeof cur.result.expectedData.data === 'string'
          ? cur.result.expectedData.data
          : JSON.stringify(cur.result.expectedData.data));

    if (expectedValue) {
      if (isJSON(expectedValue)) {
        // JSON data with beautification
        prev[cur.id].expected = {
          language: 'json',
          data: beautify.js(expectedValue)
        };
      } else if (isXML(expectedValue) || isHtml(expectedValue)) {
        // XML/HTML data with beautification
        prev[cur.id].expected = {
          language: 'html',
          data: beautify.html(expectedValue)
        };
      } else if (isYAML(expectedValue)) {
        // YAML data (no beautification needed)
        prev[cur.id].expected = {
          language: 'yaml',
          data: expectedValue
        };
      } else {
        // Plain text data
        prev[cur.id].expected = {
          language: 'text',
          data: expectedValue
        };
      }
    }

    return prev;
  }, {} as {
    [key: string]: {
      real: { language: 'json' | 'html' | 'text' | 'yaml'; data: string; },
      expected: { language: 'json' | 'html' | 'text' | 'yaml'; data: string; }
    }
  });
});
</script>
<template>
  <!-- Response Assert Component Container -->
  <div class="h-full py-3 text-3">
    <!--
      Tab Filter Navigation
      - Displays assertion counts by status
      - Allows filtering by All, Success, Fail, Ignore
      - Shows color-coded counts for each status
    -->
    <div
      class="inline-flex items-center h-7 rounded select-none overflow-hidden cursor-pointer bg-gray-light text-theme-sub-content">

      <!-- All Tab -->
      <div
        :class="{ 'tab-selected': activeKey === 'all' }"
        class="flex justify-center items-center min-w-25 px-3 h-full"
        @click="change('all')">
        <span>{{ t('common.all') }}</span>
        <span class="ml-1">({{ totalList.length }})</span>
      </div>

      <!-- Success Tab -->
      <div
        :class="{ 'tab-selected': activeKey === 'success' }"
        class="flex justify-center items-center min-w-25 px-3 h-full"
        @click="change('success')">
        <span>{{ t('status.passed') }}</span>
        <span class="ml-1">(<span class="text-status-success">{{ successList.length }}</span>)</span>
      </div>

      <!-- Fail Tab -->
      <div
        :class="{ 'tab-selected': activeKey === 'fail' }"
        class="flex justify-center items-center min-w-25 px-3 h-full"
        @click="change('fail')">
        <span>{{ t('status.failed') }}</span>
        <span class="ml-1">(<span class="text-status-error">{{ failList.length }}</span>)</span>
      </div>

      <!-- Ignore Tab -->
      <div
        :class="{ 'tab-selected': activeKey === 'ignore' }"
        class="flex justify-center items-center min-w-25 px-3 h-full"
        @click="change('ignore')">
        <span>{{ t('status.ignored') }}</span>
        <span class="ml-1">(<span class="text-status-close">{{ ignoreList.length }}</span>)</span>
      </div>
    </div>
    <!--
      Assertion Results Container
      - Scrollable area for assertion details
      - Contains collapsible panels for each assertion
    -->
    <div
      style="width: calc(100% + 20px);height: calc(100% - 35px); padding-right: 20px;padding-left: 7px;"
      class="overflow-auto">

      <!--
        Collapsible Assertion Panels
        - Each panel shows detailed assertion information
        - Color-coded backgrounds based on assertion status
        - Expandable/collapsible with custom arrow controls
      -->
      <Collapse
        :activeKey="openKeys"
        :bordered="false"
        style="margin-top: 12px;background-color: #fff;font-size: 12px;"
        @change="collapseChange">

        <!-- Individual Assertion Panel -->
        <CollapsePanel
          v-for="item in showList"
          :key="item.id"
          :style="backgroundStyleMap[item.id]"
          :showArrow="false"
          style="margin-bottom: 6px;border: none;border-radius: 4px;line-height: 18px;">

          <!-- Panel Header with Status Badge -->
          <template #header>
            <div class="flex-1">
              <!-- Ignored Assertion Badge -->
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

              <!-- Failed Assertion Badge -->
              <template v-else>
                <template v-if="item.result.failure">
                  <BadgeRibbon
                    placement="start"
                    :text="t('status.failed')"
                    style="top: -4px;left: -49px;transform: scale(0.9);font-size: 12px;"
                    color="rgba(245, 34, 45, 1)">
                    <div class="flex justify-between items-center">
                      <div class="break-all">{{ item.name }}</div>
                      <Arrow :open="openKeys.includes(item.id)" @change="arrowChange(item.id)" />
                    </div>
                  </BadgeRibbon>
                </template>

                <!-- Success Assertion Badge -->
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
              <DescriptionsItem :label="t('common.assertionResult')" :span="3">
                <Badge status="default" :text="t('status.ignored')" />
              </DescriptionsItem>
            </template>

            <template v-else>
              <template v-if="item.result.failure">
                <DescriptionsItem :label="t('common.assertionResult')" :span="!!item.result.message ? 1 : 3">
                  <Badge
                    class="flex-shrink-0"
                    status="error"
                    :text="t('status.failed')" />
                </DescriptionsItem>

                <DescriptionsItem
                  v-if="!!item.result.message"
                  :label="t('common.failureReason')"
                  :span="2">
                  {{ item.result.message }}
                </DescriptionsItem>
              </template>

              <template v-else>
                <DescriptionsItem :label="t('common.assertionResult')" :span="3">
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
