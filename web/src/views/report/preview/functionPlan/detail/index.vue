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

const plan = computed(() => {
  return props.dataSource?.content?.plan;
});

const testerResponsibilities = computed(() => {
  const _testers = props.dataSource?.content?.cases?.testers;
  const _testerResponsibilities = plan.value?.testerResponsibilities;
  if (!_testerResponsibilities || !_testers) {
    return [];
  }

  return Object.entries(_testerResponsibilities).map(([id, value]) => {
    return {
      id,
      name: _testers[id].fullName,
      responsibility: value
    };
  });
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>计划详细信息</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a1.1">1.1<em class="inline-block w-3.5"></em>基本信息</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            计划名称
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.name }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            用例前缀
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.casePrefix }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            状态
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.status?.message }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            负责人
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.ownerName }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            所属项目
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.projectName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            时间计划
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.startDate }} 至 {{ plan?.deadlineDate }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            添加人
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.createdByName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            添加时间
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.createdDate }}
          </div>
        </div>

        <div class="flex">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            工作量评估方式
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ plan?.evalWorkloadMethod?.message }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            是否评审用例
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ plan?.review?'是':'否' }}
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a.2">1.2<em class="inline-block w-3.5"></em>测试人与职责</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            测试人
          </div>
          <div
            class="flex-1 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5">
            职责
          </div>
        </div>

        <div
          v-for="(item,index) in testerResponsibilities"
          :key="item.id"
          :class="{'border-b':index<testerResponsibilities.length-1}"
          class="flex border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ item.name }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ item.responsibility }}
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.3">1.3<em class="inline-block w-3.5"></em>测试目标</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ plan?.testingObjectives || '无' }}
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.4">1.4<em class="inline-block w-3.5"></em>测试范围</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ plan?.testingScope || '无' }}
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.5">1.5<em class="inline-block w-3.5"></em>验收标准</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ plan?.acceptanceCriteria || '无' }}
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.6">1.6<em class="inline-block w-3.5"></em>其他说明</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ plan?.otherInformation || '无' }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
