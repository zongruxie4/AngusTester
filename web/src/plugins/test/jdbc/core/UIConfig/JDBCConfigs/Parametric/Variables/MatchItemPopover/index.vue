<script setup lang="ts">
import { Popover } from 'ant-design-vue';
import { Icon, Hints, Grid } from '@xcan-angus/vue-ui';
import beautify from 'js-beautify';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const prettyXml = (data: string) => {
  return beautify.html(data, {
    unformatted: ['code', 'pre', 'em', 'strong', 'span'],
    indent_inner_html: true,
    indent_char: ' ',
    indent_size: 2
  });
};

const matchItemList = [
  {
    key: 'regexp',
    name: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.regexp.title'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.dataColumn')
      },
      {
        dataIndex: 'expression',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.expressionColumn')
      },
      {
        dataIndex: 'result',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.resultColumn')
      },
      {
        dataIndex: 'item',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.itemColumn')
      }
    ]],
    data: {
      data: 'hello, RegexExtraction! my phone number is 18888888888 and 13999999999.',
      expression: '(1\\d{10})',
      result: '["18888888888","13999999999"]',
      item: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.regexp.data')
    }
  },
  {
    key: 'jsonpath',
    name: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.jsonpath.title'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.dataColumn')
      },
      {
        dataIndex: 'expression',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.expressionColumn')
      },
      {
        dataIndex: 'result',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.resultColumn')
      },
      {
        dataIndex: 'item',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.itemColumn')
      }
    ]],
    data: {
      data: { store: { book: [{ title: 'Sayings of the Century', price: 100 }, { title: 'Confucianism', price: 200 }] } },
      expression: '$.store.book[*]',
      result: '[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]',
      item: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.jsonpath.data')
    }
  },
  {
    key: 'xpath',
    name: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.xpath.title'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.dataColumn')
      },
      {
        dataIndex: 'expression',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.expressionColumn')
      },
      {
        dataIndex: 'result',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.resultColumn')
      },
      {
        dataIndex: 'item',
        label: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.itemColumn')
      }
    ]],
    data: {
      data: prettyXml('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><persons><person><age>30</age><interests>coding</interests>' +
        '<interests>basketball</interests><name>Angus1</name></person><person><age>32</age><interests>coding</interests><name>Angus2</name></person></persons>'),
      expression: '/persons/person[age >= 30]',
      result: '["30codingbasketballAngus1", "32codingAngus2"]',
      item: t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.xpath.data')
    }
  }
];
</script>
<template>
  <Popover destroyTooltipOnHide overlayClassName="overflow-auto">
    <div class="flex-shrink-0 flex items-center cursor-pointer w-4 h-7">
      <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
    </div>
    <template #content>
      <div class="w-196 space-y-2 text-3 text-theme-content">
        <Hints :text="t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.hints')" class="!font-semibold !text-theme-content" />
        <div>
          {{ t('httpPlugin.uiConfig.httpConfigs.parametric.variables.matchItemPopover.description') }}
        </div>
        <ol class="pl-4 space-y-2">
          <li
            v-for="item in matchItemList"
            :key="item.key"
            style="list-style-type: circle;"
            class="space-y-2">
            <div class="font-semibold">{{ item.name }}</div>
            <Grid
              labelStyle="color: var(--content-text-sub-content);"
              valueStyle="color: var(--content-text-sub-content);"
              labelSpacing="4px"
              :marginBottom="8"
              :columns="item.columns"
              :dataSource="item.data">
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

<style>
.ant-popover.overflow-auto .ant-popover-inner-content {
  max-height: 60vh;
  overflow: auto;
}
</style>
