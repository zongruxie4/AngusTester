<script setup lang="ts">
import { computed } from 'vue';

import { ReportContent } from '../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const testResult = computed(() => {
  return props.dataSource?.content?.testResult;
});

const columns = computed(() => [
  [
    {
      dataIndex: 'passed',
      name: '是否通过',
      customRender: (text) => {
        return text === true ? '通过' : text === false ? '未通过' : '未测试';
      }
    },
    {
      dataIndex: 'enabledTestTypes',
      name: '开启功能测试',
      customRender: (text) => {
        return text.find(i => i.value === 'TEST_FUNCTIONALITY') ? '开启' : '未开启';
      }
    }
  ],
  [
    {
      dataIndex: 'resultDetailVoMap',
      name: '功能测试通过',
      customRender: (text) => {
        return text?.TEST_FUNCTIONALITY?.passed === true ? '通过' : text?.TEST_FUNCTIONALITY?.passed === false ? '未通过' : '未测试';
      }
    },
    {
      dataIndex: 'resultDetailVoMap',
      name: '功能测试失败原因',
      customRender: (text) => {
        return text?.TEST_FUNCTIONALITY?.failureMessage;
      }
    }
  ],
  [
    {
      dataIndex: 'enabledTestTypes',
      name: '开启性能测试',
      customRender: (text) => {
        return text.find(i => i.value === 'TEST_PERFORMANCE') ? '开启' : '未开启';
      }
    },
    {
      dataIndex: 'resultDetailVoMap',
      name: '性能测试通过',
      customRender: (text) => {
        return text?.TEST_PERFORMANCE?.passed === true ? '通过' : text?.TEST_PERFORMANCE?.passed === false ? '未通过' : '未测试';
      }
    }
  ],
  [
    {
      dataIndex: 'resultDetailVoMap',
      name: '性能测试失败原因',
      customRender: (text) => {
        return text?.TEST_PERFORMANCE?.failureMessage;
      }
    },
    {
      dataIndex: 'enabledTestTypes',
      name: '开启稳定性测试',
      customRender: (text) => {
        return text.find(i => i.value === 'TEST_STABILITY') ? '开启' : '未开启';
      }
    }
  ],
  [
    {
      dataIndex: 'resultDetailVoMap',
      name: '稳定性测试通过',
      customRender: (text) => {
        return text?.TEST_STABILITY?.passed === true ? '通过' : text?.TEST_STABILITY?.passed === false ? '未通过' : '未测试';
      }
    },
    {
      dataIndex: 'resultDetailVoMap',
      name: '稳定性测试失败原因',
      customRender: (text) => {
        return text?.TEST_STABILITY?.failureMessage;
      }
    }
  ]
]);

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a2" class="text-4 text-theme-title font-medium">二、<em class="inline-block w-0.25"></em>接口测试汇总结果</span>
    </h1>
    <div class="border-t border-l border-solid border-border-input">
      <div v-for="column in columns" class="flex border-b border-solid border-border-input">
        <template v-for="item in column">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ item.name }}
          </div>
          <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
            {{ item.customRender ? item.customRender(testResult[item.dataIndex]) : testResult[item.dataIndex] }}
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
