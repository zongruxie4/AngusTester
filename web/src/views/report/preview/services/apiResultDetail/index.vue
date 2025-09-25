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
      name: t('reportPreview.services.apiResultDetail.fields.apiUrl')
    },
    {
      dataIndex: 'method',
      name: t('reportPreview.services.apiResultDetail.fields.requestMethod'),
      customRender: (text) => {
        return text?.message;
      }
    }
  ],
  [
    {
      dataIndex: 'operationId',
      name: t('reportPreview.services.apiResultDetail.fields.operationId')
    },
    {
      dataIndex: 'ownerName',
      name: t('common.owner')
    }
  ],
  [
    {
      dataIndex: 'description',
      name: t('common.description')
    },
    {
      dataIndex: 'testFuncFlag',
      name: t('reportPreview.services.apiResultDetail.fields.enableFuncTest'),
      customRender: (text) => {
        return text ? t('status.yes') : t('status.no');
      }
    }
  ],
  [
    {
      dataIndex: 'id',
      name: t('reportPreview.services.apiResultDetail.fields.funcTestPassed'),
      customRender: (text) => {
        const target = apisTestResult.value.find(i => i.id === text);
        if (target) {
          return target.funcTestPassed === true ? t('status.yes') : target.funcTestPassed === false ? t('status.no') : t('reportPreview.services.apiResultDetail.options.notTested');
        }
        return '';
      }
    },
    {
      dataIndex: 'id',
      name: t('reportPreview.services.apiResultDetail.fields.funcTestFailReason'),
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
      name: t('reportPreview.services.apiResultDetail.fields.enablePerfTest'),
      customRender: (text) => {
        return text ? t('status.yes') : t('status.no');
      }
    },
    {
      dataIndex: 'id',
      name: t('reportPreview.services.apiResultDetail.fields.perfTestPassed'),
      customRender: (text) => {
        const target = apisTestResult.value.find(i => i.id === text);
        if (target) {
          return target.perfTestPassed === true ? t('status.yes') : target.perfTestPassed === false ? t('status.no') : t('reportPreview.services.apiResultDetail.options.notTested');
        }
        return '';
      }
    }
  ],
  [
    {
      dataIndex: 'id',
      name: t('reportPreview.services.apiResultDetail.fields.perfTestFailReason'),
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
      name: t('reportPreview.services.apiResultDetail.fields.enableStabilityTest'),
      customRender: (text) => {
        return text ? t('status.yes') : t('status.no');
      }
    }
  ],
  [
    {
      dataIndex: 'id',
      name: t('reportPreview.services.apiResultDetail.fields.stabilityTestPassed'),
      customRender: (text) => {
        const target = apisTestResult.value.find(i => i.id === text);
        if (target) {
          return target.stabilityTestPassed === true ? t('status.yes') : target.stabilityTestPassed === false ? t('status.no') : t('reportPreview.services.apiResultDetail.options.notTested');
        }
        return '';
      }
    },
    {
      dataIndex: 'id',
      name: t('reportPreview.services.apiResultDetail.fields.stabilityTestFailReason'),
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
      <span id="a3" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.3') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.services.apiResultDetail.title') }}</span>
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
