<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { PrinterOutlined } from '@ant-design/icons-vue';
import { useRoute } from 'vue-router';
import { IconDownload, Spin } from '@xcan-angus/vue-ui';
import { appContext, utils } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';
import { Watermark } from 'watermark-js-plus';
import html2pdf from 'html3pdf';
import print from 'print-js';
import { exec, report } from '@/api/tester';

import { ExecContent, ExecInfo, ExecResult, ReportContent, ReportInfo } from './PropsType';

const { t } = useI18n();

const ProjectProgressReport = defineAsyncComponent(() => import('@/views/report/preview/projectProgress/index.vue'));
const ProjectEfficiencyReport = defineAsyncComponent(() => import('@/views/report/preview/projectEfficiency/index.vue'));
const TaskSprintReport = defineAsyncComponent(() => import('@/views/report/preview/sprint/index.vue'));
const TaskReport = defineAsyncComponent(() => import('@/views/report/preview/task/index.vue'));
const FunctionPlanReport = defineAsyncComponent(() => import('@/views/report/preview/functionPlan/index.vue'));
const FunctionCaseReport = defineAsyncComponent(() => import('@/views/report/preview/functionCase/index.vue'));
const ServicesReport = defineAsyncComponent(() => import('@/views/report/preview/services/index.vue'));
const ApisReport = defineAsyncComponent(() => import('@/views/report/preview/apis/index.vue'));
const ScenarioReport = defineAsyncComponent(() => import('@/views/report/preview/scenario/index.vue'));
const ExecFunctionReport = defineAsyncComponent(() => import('@/views/report/preview/execFunction/index.vue'));
const ExecPerfReport = defineAsyncComponent(() => import('@/views/report/preview/execPerf/index.vue'));

const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<{ [key: string]: any }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = ref(appContext.getAccessApp());

const route = useRoute();
const recordId = ref();

const domId = utils.uuid('pdf');
let watermark;

const generating = ref(false);
const loading = ref(false);
const reportContent = ref<ReportContent>();
const reportInfo = ref<ReportInfo>();
const execInfo = ref<ExecInfo>();
const execContent = ref<ExecContent[]>();
const execResult = ref<ExecResult>();

const loadExecResult = async (id: string) => {
  loading.value = true;
  const [error, res] = await exec.getResult(id);
  loading.value = false;
  if (error) {
    return;
  }

  execResult.value = res?.data;
  if (execResult.value) {
    execResult.value.sampleSummary = {
      ...execResult.value.sampleSummary,
      ...(execInfo.value?.configuration?.thread || {})
    };
  }
};

const loadExecContent = async (id: string) => {
  let total = 0;
  const pageSize = 500;
  const tempList: any[] = [];
  const load = async (pageNo = 1) => {
    const [error, res] = await exec.getSampleExtensionContent(id, { pageNo, pageSize, extField: 'SampleResultContent' });
    if (error) {
      loading.value = false;
      return;
    }

    total = +res.data?.total;
    if (res.data?.list?.length) {
      tempList.push(...res.data.list);
    }
  };

  loading.value = true;
  await load();
  if (total <= pageSize) {
    execContent.value = tempList;
    return;
  }

  const totalSize = Math.ceil(total / pageSize);
  for (let i = 2; i <= totalSize; i++) {
    await load(i);
  }

  execContent.value = tempList;
};

const loadExecInfo = async (id: string) => {
  loading.value = true;
  const [error, res] = await exec.getExecDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as ExecInfo;
  execInfo.value = data;
  if (data) {
    const scriptType = data.scriptType?.value;
    if (scriptType === 'TEST_FUNCTIONALITY') {
      loadExecContent(id);
    }

    const plugin = data.plugin;
    if (plugin === 'Http') {
      loadExecResult(id);
    }
  }
};

const loadReportDetail = async (id: string) => {
  loading.value = true;
  const [error, res] = await report.getReportDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as ReportInfo;
  reportInfo.value = data;
  if (data) {
    const templateValue = data.template?.value;
    if (['EXEC_CUSTOMIZATION_RESULT', 'EXEC_FUNCTIONAL_RESULT', 'EXEC_PERFORMANCE_RESULT', 'EXEC_STABILITY_RESULT'].includes(templateValue)) {
      const execId = data.targetId;
      loadExecInfo(execId);
    } else {
      loadReportContent(id);
    }
  }
};

const loadReportContent = async (id: string) => {
  loading.value = true;
  const [error, res] = await report.getReportWideContent(id, {
    recordId: recordId.value
  });
  loading.value = false;
  if (error) {
    return;
  }

  const data: ReportContent = res?.data;
  reportContent.value = data;
};

const toExport = () => {
  generating.value = true;

  setTimeout(() => {
    const element = document.getElementById(domId);
    const option = {
      filename: `${reportInfo.value?.name}.pdf`,
      enableLinks: false,
      margin: [20, 20, 20, 20],
      image: {
        type: 'jpeg',
        quality: 1
      },
      html2canvas: {
        scale: 3,
        useCORS: true
      },
      jsPDF: {
        unit: 'px',
        format: [834, 1122],
        orientation: 'portrait'
      },
      pagebreak: {
        mode: ['avoid-all', 'css']
      }

    };
    html2pdf().set(option).from(element).save().then((res) => {
      generating.value = false;
    });
  }, 50);
};

const printDom = () => {
  // print(domId, 'html');
  generating.value = true;

  setTimeout(() => {
    const element = document.getElementById(domId);
    const option = {
      filename: `${reportInfo.value?.name}.pdf`,
      enableLinks: false,
      margin: [20, 20, 20, 20],
      image: {
        type: 'jpeg',
        quality: 1
      },
      html2canvas: {
        scale: 3,
        useCORS: true
      },
      jsPDF: {
        unit: 'px',
        format: [834, 1122],
        orientation: 'portrait'
      },
      pagebreak: {
        mode: ['avoid-all', 'css']
      }

    };

    html2pdf().set(option).from(element).output('dataurlstring').then((res) => {
      generating.value = false;
      print({ type: 'pdf', font_size: '12px', maxWidth: 1600, showModal: true, base64: true, printable: res.split(',')[1] });
    });
  }, 50);
};

onMounted(() => {
  watch(() => route.params, async (newValue) => {
    const id = newValue.id;
    if (route.query?.recordId) {
      recordId.value = route.query?.recordId as string;
    }
    if (!id || typeof id !== 'string') {
      return;
    }

    await loadReportDetail(id);
  }, { immediate: true });

  watch(() => reportInfo.value, (newValue) => {
    if (watermark) {
      watermark.destroy();
    }

    const watermarkText = newValue?.basicInfoSetting?.watermark;
    if (!watermarkText) {
      return;
    }

    watermark = new Watermark({
      parent: `#${domId}`,
      contentType: 'rich-text',
      content: `<div style="max-width: 15em;color:#e0e0e0;font-size: 14px; line-height: 20px;word-break: break-all; white-space: pre-wrap;">${watermarkText}</div>`,
      width: 240,
      height: 200,
      rotate: 15,
      layout: 'grid',
      gridLayoutOptions: {
        rows: 2,
        cols: 2,
        gap: [0, 0],
        matrix: [[1, 0], [0, 1]]
      }
    });

    watermark.create();
  }, { immediate: true });
});

const templateType = computed(() => {
  return reportInfo.value?.template?.value;
});

const tipText = computed(() => {
  if (loading.value) {
    return t('reportPreview.loading');
  }

  if (generating.value) {
    return t('reportPreview.generating');
  }

  return '';
});
</script>

<template>
  <Spin
    :delay="0"
    :tip="tipText"
    :spinning="loading || generating"
    class="h-full overflow-hidden text-3 leading-5">
    <div class="w-198 my-1 mx-auto flex items-center justify-end">
      <Button
        :disabled="loading || generating"
        size="small"
        type="link"
        class="inline-flex items-center space-x-1"
        @click="toExport">
        <IconDownload class="text-4" />
        <span>{{ t('reportPreview.actions.exportPDF') }}</span>
      </Button>

      <Button
        ref="printBtnRef"
        size="small"
        type="link"
        class="inline-flex items-center space-x-1"
        @click="printDom">
        <PrinterOutlined class="text-3.5" />
        <span>{{ t('reportPreview.actions.printPDF') }}</span>
      </Button>
    </div>

    <div style="height:calc(100% - 50px);" class="overflow-x-hidden overflow-y-auto">
      <div :id="domId" class="w-198 mx-auto">
        <template v-if="reportContent">
          <ProjectProgressReport
            v-if="templateType === 'PROJECT_PROGRESS'"
            :userInfo="userInfo"
            :projectInfo="projectInfo"
            :appInfo="appInfo"
            :dataSource="reportContent" />
          <ProjectEfficiencyReport
            v-else-if="templateType === 'PROJECT_EFFICIENCY'"
            :userInfo="userInfo"
            :projectInfo="projectInfo"
            :appInfo="appInfo"
            :dataSource="reportContent" />
          <TaskSprintReport
            v-else-if="templateType === 'TASK_SPRINT'"
            :userInfo="userInfo"
            :projectInfo="projectInfo"
            :appInfo="appInfo"
            :dataSource="reportContent" />
          <TaskReport
            v-else-if="templateType === 'TASK'"
            :userInfo="userInfo"
            :projectInfo="projectInfo"
            :appInfo="appInfo"
            :dataSource="reportContent" />
          <FunctionPlanReport
            v-else-if="templateType === 'FUNC_TESTING_PLAN'"
            :userInfo="userInfo"
            :projectInfo="projectInfo"
            :appInfo="appInfo"
            :dataSource="reportContent" />
          <FunctionCaseReport
            v-else-if="templateType === 'FUNC_TESTING_CASE'"
            :userInfo="userInfo"
            :projectInfo="projectInfo"
            :appInfo="appInfo"
            :dataSource="reportContent" />
          <ServicesReport
            v-else-if="templateType === 'SERVICES_TESTING_RESULT'"
            :userInfo="userInfo"
            :projectInfo="projectInfo"
            :appInfo="appInfo"
            :dataSource="reportContent" />
          <ApisReport
            v-else-if="templateType === 'APIS_TESTING_RESULT'"
            :userInfo="userInfo"
            :projectInfo="projectInfo"
            :appInfo="appInfo"
            :dataSource="reportContent" />
          <ScenarioReport
            v-else-if="templateType === 'SCENARIO_TESTING_RESULT'"
            :userInfo="userInfo"
            :projectInfo="projectInfo"
            :appInfo="appInfo"
            :dataSource="reportContent"
            :execContent="execContent" />
        </template>
        <ExecFunctionReport
          v-else-if="templateType === 'EXEC_FUNCTIONAL_RESULT'"
          :userInfo="userInfo"
          :projectInfo="projectInfo"
          :appInfo="appInfo"
          :reportInfo="reportInfo"
          :execInfo="execInfo"
          :execContent="execContent"
          :execResult="execResult" />
        <ExecPerfReport
          v-else-if="['EXEC_CUSTOMIZATION_RESULT', 'EXEC_PERFORMANCE_RESULT', 'EXEC_STABILITY_RESULT'].includes(templateType!)"
          :userInfo="userInfo"
          :projectInfo="projectInfo"
          :appInfo="appInfo"
          :reportInfo="reportInfo"
          :execInfo="execInfo"
          :execContent="execContent"
          :execResult="execResult" />
      </div>
    </div>
  </Spin>
</template>
<style>
body {
  font-size: 11px;
}

img,
svg,
video,
canvas,
audio,
iframe,
embed,
object {
  display: inline-block;
}

.pdf-page-break {
  page-break-before: always;
}
</style>
