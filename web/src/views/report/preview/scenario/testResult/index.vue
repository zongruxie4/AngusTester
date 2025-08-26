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

const testResult = computed(() => {
  return props.dataSource?.content?.testResult;
});

const columns = computed(() => [
  [
    {
      dataIndex: 'passed',
      name: t('reportPreview.scenario.testResult.fields.passed'),
      customRender: (text) => {
        return text === true ? t('reportPreview.scenario.testResult.options.passed') : text === false ? t('reportPreview.scenario.testResult.options.failed') : t('reportPreview.scenario.testResult.options.notTested');
      }
    },
    {
      dataIndex: 'enabledTestTypes',
      name: t('reportPreview.scenario.testResult.fields.enableFuncTest'),
      customRender: (text) => {
        return text.find(i => i.value === 'TEST_FUNCTIONALITY') ? t('reportPreview.scenario.testResult.options.enabled') : t('reportPreview.scenario.testResult.options.disabled');
      }
    }
  ],
  [
    {
      dataIndex: 'resultDetailVoMap',
      name: t('reportPreview.scenario.testResult.fields.funcTestPassed'),
      customRender: (text) => {
        return text?.TEST_FUNCTIONALITY?.passed === true ? t('reportPreview.scenario.testResult.options.passed') : text?.TEST_FUNCTIONALITY?.passed === false ? t('reportPreview.scenario.testResult.options.failed') : t('reportPreview.scenario.testResult.options.notTested');
      }
    },
    {
      dataIndex: 'resultDetailVoMap',
      name: t('reportPreview.scenario.testResult.fields.funcTestFailReason'),
      customRender: (text) => {
        return text?.TEST_FUNCTIONALITY?.failureMessage;
      }
    }
  ],
  [
    {
      dataIndex: 'enabledTestTypes',
      name: t('reportPreview.scenario.testResult.fields.enablePerfTest'),
      customRender: (text) => {
        return text.find(i => i.value === 'TEST_PERFORMANCE') ? t('reportPreview.scenario.testResult.options.enabled') : t('reportPreview.scenario.testResult.options.disabled');
      }
    },
    {
      dataIndex: 'resultDetailVoMap',
      name: t('reportPreview.scenario.testResult.fields.perfTestPassed'),
      customRender: (text) => {
        return text?.TEST_PERFORMANCE?.passed === true ? t('reportPreview.scenario.testResult.options.passed') : text?.TEST_PERFORMANCE?.passed === false ? t('reportPreview.scenario.testResult.options.failed') : t('reportPreview.scenario.testResult.options.notTested');
      }
    }
  ],
  [
    {
      dataIndex: 'resultDetailVoMap',
      name: t('reportPreview.scenario.testResult.fields.perfTestFailReason'),
      customRender: (text) => {
        return text?.TEST_PERFORMANCE?.failureMessage;
      }
    },
    {
      dataIndex: 'enabledTestTypes',
      name: t('reportPreview.scenario.testResult.fields.enableStabilityTest'),
      customRender: (text) => {
        return text.find(i => i.value === 'TEST_STABILITY') ? t('reportPreview.scenario.testResult.options.enabled') : t('reportPreview.scenario.testResult.options.disabled');
      }
    }
  ],
  [
    {
      dataIndex: 'resultDetailVoMap',
      name: t('reportPreview.scenario.testResult.fields.stabilityTestPassed'),
      customRender: (text) => {
        return text?.TEST_STABILITY?.passed === true ? t('reportPreview.scenario.testResult.options.passed') : text?.TEST_STABILITY?.passed === false ? t('reportPreview.scenario.testResult.options.failed') : t('reportPreview.scenario.testResult.options.notTested');
      }
    },
    {
      dataIndex: 'resultDetailVoMap',
      name: t('reportPreview.scenario.testResult.fields.stabilityTestFailReason'),
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
      <span id="a2" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.scenario.testResult.title') }}</span>
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
