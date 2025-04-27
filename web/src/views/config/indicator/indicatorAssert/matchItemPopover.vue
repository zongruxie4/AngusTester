<script setup lang="ts">
import { Popover } from 'ant-design-vue';
import { Grid, Hints, Icon } from '@xcan-angus/vue-ui';
import beautify from 'js-beautify';

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
    name: '正则表达式匹配',
    columns: [[
      {
        dataIndex: 'data',
        label: '匹配值'
      },
      {
        dataIndex: 'expression',
        label: '表达式'
      },
      {
        dataIndex: 'result',
        label: '匹配结果'
      },
      {
        dataIndex: 'item',
        label: '匹配项'
      }
    ]],
    data: {
      data: 'hello, RegexExtraction! my phone number is 18888888888 and 13999999999.',
      expression: '(1\\d{10})',
      result: '["18888888888","13999999999"]',
      item: '不指定位置默认取合并结果合并匹配值："1888888888813999999999"，指定位置0取值："18888888888"，指定位置1取值："13999999999"。'
    }
  },
  {
    key: 'jsonpath',
    name: 'JSONPath匹配',
    columns: [[
      {
        dataIndex: 'data',
        label: '匹配值'
      },
      {
        dataIndex: 'expression',
        label: '表达式'
      },
      {
        dataIndex: 'result',
        label: '匹配结果'
      },
      {
        dataIndex: 'item',
        label: '匹配项'
      }
    ]],
    data: {
      data: { store: { book: [{ title: 'Sayings of the Century', price: 100 }, { title: 'Confucianism', price: 200 }] } },
      expression: '$.store.book[*]',
      result: '[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]',
      item: '不指定位置默认取合并结果：[{"title":"Sayings of the Century","price":100},{"title":"confucianism","price":200}]，指定位置0取值：{"title":"Sayings of the Century","price":100}，指定位置1取值：{"title":"confucianism","price":200}。'
    }
  },
  {
    key: 'xpath',
    name: 'XPath匹配',
    columns: [[
      {
        dataIndex: 'data',
        label: '匹配值'
      },
      {
        dataIndex: 'expression',
        label: '表达式'
      },
      {
        dataIndex: 'result',
        label: '匹配结果'
      },
      {
        dataIndex: 'item',
        label: '匹配项'
      }
    ]],
    data: {
      data: prettyXml('<?xml version="1.0" encoding="UTF-8" standalone="yes"?><persons><person><age>30</age><interests>coding</interests>' +
                '<interests>basketball</interests><name>Angus1</name></person><person><age>32</age><interests>coding</interests><name>Angus2</name></person></persons>'),
      expression: '/persons/person[age >= 30]',
      result: '["30codingbasketballAngus1", "32codingAngus2"]',
      item: '不指定位置默认取合并结果："30codingbasketballAngus132codingAngus2"，指定位置0取值："30codingbasketballAngus1"，指定位置1取值："32codingAngus2"。'
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
        <Hints text="匹配项" class="!font-semibold !text-theme-content" />
        <div>
          当表达式匹配到多个值或者结果为数组时，指定第几个值作为表达式期望值，支持位置从0开始到最大值2000，指定位置数据不存在时返回null，不指定时合并多个值为一个值。
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
