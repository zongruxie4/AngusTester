<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../PropsType';

const { t } = useI18n();

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

const services = computed(() => {
  return props.dataSource?.content?.services;
});

const apis = computed(() => {
  return props.dataSource?.content?.apis;
});

const apisTestResult = computed(() => {
  return props.dataSource?.content?.testResult?.testResultInfos || [];
});

const columns = computed(() => [
  [
    {
      dataIndex: 'endpoint',
      name: '接口地址'
    },
    {
      dataIndex: 'method',
      name: '请求方法',
      customRender: (text) => {
        return text?.message;
      }
    }
  ],
  [
    {
      dataIndex: 'operationId',
      name: '操作ID'
    },
    {
      dataIndex: 'ownerName',
      name: '负责人'
    }
  ],
  [
    {
      dataIndex: 'description',
      name: '描述'
    },
    {
      dataIndex: 'testFuncFlag',
      name: '开启功能测试',
      customRender: (text) => {
        return text ? '是' : '否';
      }
    }
  ],
  [
    {
      dataIndex: 'id',
      name: '功能测试通过',
      customRender: (text) => {
        const target = apisTestResult.value.find(i => i.id === text);
        if (target) {
          return target.funcTestPassed === true ? '是' : target.funcTestPassed === false ? '否' : '未测试';
        }
        return '';
      }
    },
    {
      dataIndex: 'id',
      name: '功能测试失败原因',
      customRender: (text) => {
        const target = apisTestResult.value.find(i => i.id === text);
        if (target) {
          return target.funcTestFailureMessage;
        }
        return '';
      }
    }
  ],
  [
    {
      dataIndex: 'testPerfFlag',
      name: '开启性能测试',
      customRender: (text) => {
        return text ? '是' : '否';
      }
    },
    {
      dataIndex: 'id',
      name: '性能测试通过',
      customRender: (text) => {
        const target = apisTestResult.value.find(i => i.id === text);
        if (target) {
          return target.perfTestPassed === true ? '是' : target.perfTestPassed === false ? '否' : '未测试';
        }
        return '';
      }
    }
  ],
  [
    {
      dataIndex: 'id',
      name: '性能测试失败原因',
      customRender: (text) => {
        const target = apisTestResult.value.find(i => i.id === text);
        if (target) {
          return target.perfTestFailureMessage;
        }
        return '';
      }
    },
    {
      dataIndex: 'testStabilityFlag',
      name: '开启稳定性测试',
      customRender: (text) => {
        return text ? '是' : '否';
      }
    }
  ],
  [
    {
      dataIndex: 'id',
      name: '稳定性测试通过',
      customRender: (text) => {
        const target = apisTestResult.value.find(i => i.id === text);
        if (target) {
          return target.stabilityTestPassed === true ? '是' : target.stabilityTestPassed === false ? '否' : '未测试';
        }
        return '';
      }
    },
    {
      dataIndex: 'id',
      name: '稳定性测试失败原因',
      customRender: (text) => {
        const target = apisTestResult.value.find(i => i.id === text);
        if (target) {
          return target.perfTestFailureMessage;
        }
        return '';
      }
    }
  ]
]);

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a3" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.3') }}<em class="inline-block w-0.25"></em>接口测试结果明细</span>
    </h1>
    <template v-for="api in apis">
      <li class="mb-2.5">
        <span class="text-3.5 text-theme-title font-medium"><em class="inline-block w-0.25"></em>{{ api.summary }}</span>
      </li>
      <div class="border-t border-l border-solid border-border-input mb-5">
        <div v-for="column in columns" class="flex border-b border-solid border-border-input">
          <template v-for="item in column">
            <div
              class="w-32 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
              {{ item.name }}
            </div>
            <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
              {{ item.customRender ? item.customRender(api[item.dataIndex]) : api[item.dataIndex] }}
            </div>
          </template>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
