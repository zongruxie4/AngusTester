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

const caseDetails = computed(() => {
  return props.dataSource?.content?.caseDetails || [];
});

const len = computed(() => {
  let _len = 0;
  if (caseDetails.value?.length) {
    _len = caseDetails.value?.length;
  }
  return _len;
});

//  编码、名称、测试结果、测试得分、测试备注、测试人
</script>

<template>
  <div>
    <div id="a3" class="test-case-detail mb-4">
      <span id="a3" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.3') }}<em class="inline-block w-0.25"></em>测试用例明细</span>
    </div>

    <div v-if="len>0" class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-35 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          编码
        </div>
        <div
          class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          名称
        </div>
        <div
          class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          测试结果
        </div>
        <div class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          测试得分
        </div>
        <div  class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          测试备注
        </div>
        <div class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 ">
          测试人
        </div>
      </div>

      <div
        v-for="(item,_index) in caseDetails"
        :key="item.id"
        :class="{'border-b':_index<len}"
        class="flex border-solid border-border-input">
        <div class="w-35 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.code }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.name }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.testResult?.message }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.testScore }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.testRemark }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap">
          {{ item.testerName }}
        </div>
      </div>
    </div>

    <div v-else class="content-text-container">{{ t('common.noData') }}</div>
  </div>
</template>

<style scoped>
.browser-container {
 text-indent: 2em;
}
</style>
