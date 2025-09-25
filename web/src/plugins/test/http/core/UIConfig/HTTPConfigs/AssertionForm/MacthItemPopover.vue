<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Popover } from 'ant-design-vue';
import { Icon, Hints, Grid } from '@xcan-angus/vue-ui';
import beautify from 'js-beautify';

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
    name: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.regexpMatch'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.matchValue')
      },
      {
        dataIndex: 'expression',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.expression')
      },
      {
        dataIndex: 'result',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.matchResult')
      },
      {
        dataIndex: 'item',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.matchItem')
      }
    ]],
    data: {
      data: 'hello, RegexExtraction! my phone number is 18888888888 and 13999999999.',
      expression: '(1\\d{10})',
      result: '["1888888888813999999999","18888888888","13999999999"]',
      item: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.regexpItemDescription')
    }
  },
  {
    key: 'jsonpath',
    name: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.jsonpathMatch'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.matchValue')
      },
      {
        dataIndex: 'expression',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.expression')
      },
      {
        dataIndex: 'result',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.matchResult')
      },
      {
        dataIndex: 'item',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.matchItem')
      }
    ]],
    data: {
      data: { store: { book: [{ title: 'Sayings of the Century', price: 100 }, { title: 'Confucianism', price: 200 }] } },
      expression: '$.store.book[*]',
      result: '[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]',
      item: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.jsonpathItemDescription')
    }
  },
  {
    key: 'xpath',
    name: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.xpathMatch'),
    columns: [[
      {
        dataIndex: 'data',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.matchValue')
      },
      {
        dataIndex: 'expression',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.expression')
      },
      {
        dataIndex: 'result',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.matchResult')
      },
      {
        dataIndex: 'item',
        label: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.matchItem')
      }
    ]],
    data: {
      data: prettyXml('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><persons><person><age>30</age><interests>coding</interests>' +
                '<interests>basketball</interests><name>Angus1</name></person><person><age>32</age><interests>coding</interests><name>Angus2</name></person></persons>'),
      expression: '/persons/person[age >= 30]',
      result: '["30codingbasketballAngus1", "32codingAngus2"]',
      item: t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.xpathItemDescription')
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
      <div style="max-height: 60vh; overflow: auto;" class="w-196 space-y-2 text-3 text-theme-content">
        <Hints :text="t('httpPlugin.uiConfig.httpConfigs.assertionForm.matchItemPopover.title')" class="!font-semibold !text-theme-content" />
        <div>
          {{ t('common.description') }}
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
