<script setup lang="ts">
// Vue composition API imports
import { useI18n } from 'vue-i18n';

// UI component imports
import { Popover } from 'ant-design-vue';
import { Icon, Hints, Grid } from '@xcan-angus/vue-ui';

// Third-party utility imports
import beautify from 'js-beautify';

const { t } = useI18n();

/**
 * Format XML string with proper indentation for better readability
 * @param rawXmlString - Raw XML string to format
 * @returns Formatted XML string with proper indentation
 */
const formatXmlWithIndentation = (rawXmlString: string): string => {
  return beautify.html(rawXmlString, {
    unformatted: ['code', 'pre', 'em', 'strong', 'span'],
    indent_inner_html: true,
    indent_char: ' ',
    indent_size: 2
  });
};

// Configuration for expression matching examples
const expressionMatchingExamplesConfig = [
  {
    key: 'regexp',
    name: t('xcan_apiAssert.regexExpressionMatch'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('xcan_apiAssert.matchValue')
      },
      {
        dataIndex: 'expression',
        label: t('xcan_apiAssert.expression')
      },
      {
        dataIndex: 'result',
        label: t('xcan_apiAssert.matchResult')
      },
      {
        dataIndex: 'item',
        label: t('xcan_apiAssert.matchItemLabel')
      }
    ]],
    data: {
      data: 'hello, RegexExtraction! my phone number is 18888888888 and 13999999999.',
      expression: '(1\\d{10})',
      result: '[,"18888888888","13999999999"]',
      item: t('xcan_apiAssert.regexExample')
    }
  },
  {
    key: 'jsonpath',
    name: t('xcan_apiAssert.jsonpathMatch'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('xcan_apiAssert.matchValue')
      },
      {
        dataIndex: 'expression',
        label: t('xcan_apiAssert.expression')
      },
      {
        dataIndex: 'result',
        label: t('xcan_apiAssert.matchResult')
      },
      {
        dataIndex: 'item',
        label: t('xcan_apiAssert.matchItemLabel')
      }
    ]],
    data: {
      data: { store: { book: [{ title: 'Sayings of the Century', price: 100 }, { title: 'Confucianism', price: 200 }] } },
      expression: '$.store.book[*]',
      result: '[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]',
      item: t('xcan_apiAssert.jsonpathExample')
    }
  },
  {
    key: 'xpath',
    name: t('xcan_apiAssert.xpathMatch'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('xcan_apiAssert.matchValue')
      },
      {
        dataIndex: 'expression',
        label: t('xcan_apiAssert.expression')
      },
      {
        dataIndex: 'result',
        label: t('xcan_apiAssert.matchResult')
      },
      {
        dataIndex: 'item',
        label: t('xcan_apiAssert.matchItemLabel')
      }
    ]],
    data: {
      data: formatXmlWithIndentation('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><persons><person><age>30</age><interests>coding</interests>' +
                '<interests>basketball</interests><name>Angus1</name></person><person><age>32</age><interests>coding</interests><name>Angus2</name></person></persons>'),
      expression: '/persons/person[age >= 30]',
      result: '["30codingbasketballAngus1", "32codingAngus2"]',
      item: t('xcan_apiAssert.xpathExample')
    }
  }
];
</script>
<template>
  <Popover
    destroyTooltipOnHide
    placement="left"
    :trigger="['click']">
    <div class="flex-shrink-0 flex items-center cursor-pointer w-4 h-7">
      <Icon icon="icon-jieshaoshuoming" class="text-4 text-theme-text-hover" />
    </div>
    <template #content>
      <div style="max-height: 60vh; overflow: auto;" class="w-196 leading-4.5 space-y-5 text-3 text-theme-content">
        <div>
          <Hints :text="t('xcan_apiAssert.expectedValueTitle')" class="mb-2 !font-semibold !text-theme-content" />
          <div class="mb-3">{{ t('xcan_apiAssert.expectedValueDesc') }}</div>
          <ol class="pl-4 space-y-3">
            <li style="list-style-type: circle;" class="space-y-2">
              <div class="font-semibold">{{ t('xcan_apiAssert.expectedValueEmpty') }}</div>
              <div class="text-theme-sub-content">{{ t('xcan_apiAssert.expectedValueEmptyDesc') }}</div>
            </li>
            <li style="list-style-type: circle;" class="space-y-2">
              <div class="font-semibold">{{ t('xcan_apiAssert.expectedValueNotEmpty') }}</div>
              <div class="text-theme-sub-content">{{ t('xcan_apiAssert.expectedValueNotEmptyDesc') }}</div>
            </li>
          </ol>
        </div>

        <div>
          <Hints :text="t('xcan_apiAssert.matchItemTitle')" class="mb-2 !font-semibold !text-theme-content" />
          <div class="mb-3">
            {{ t('xcan_apiAssert.matchItemDesc') }}
          </div>
          <ol class="pl-4 space-y-3">
            <li
              v-for="example in expressionMatchingExamplesConfig"
              :key="example.key"
              style="list-style-type: circle;"
              class="space-y-2">
              <div class="font-semibold">{{ example.name }}</div>
              <Grid
                labelStyle="color: var(--content-text-sub-content);"
                valueStyle="color: var(--content-text-sub-content);"
                labelSpacing="4px"
                :marginBottom="8"
                :columns="example.columns"
                :dataSource="example.data">
                <template #data="{ text }">
                  <pre><code>{{ text }}</code></pre>
                </template>
                <template #expression="{ text }">
                  <pre><code>{{ text }}</code></pre>
                </template>
                <template #result="{ text }">
                  <pre><code>{{ text }}</code></pre>
                </template>
              </Grid>
            </li>
          </ol>
        </div>
      </div>
    </template>
  </Popover>
</template>
<style scoped>
pre code {
  font-size: 13px;
  line-height: 18px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>
