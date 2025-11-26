<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '@/views/report/preview/PropsType';

const { t } = useI18n();


type Props = {
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});


const basicColumns = computed(() => [
   [
    {
      name: '测评对象类型',
      dataIndex: 'totalCases',
      customRender: () => {
        return props.dataSource?.report?.targetType?.message;
      }
    },
    {
      name: '测评对象名称',
      dataIndex: 'overallScore',
      customRender: () => {
        return props.dataSource?.report?.targetName;
      }
    },
  ],
  [
    {
      name: '总测评数',
      dataIndex: 'totalCases',
      customRender: () => {
        return props.dataSource?.content?.totalCases;
      }
    },
    {
      name: '综合评分',
      dataIndex: 'overallScore',
      customRender: () => {
        return props.dataSource?.content?.overallScore;
      }
    },
  ],

]);

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>测评信息</span>
    </h1>

    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1.1" class="text-3.5 text-theme-title font-medium">1.1、<em class="inline-block w-0.25"></em>测评概览</span>
    </h1>

    <div class="border-t border-l border-solid border-border-input mb-8">
        <div
            v-for="(column,index) in basicColumns"
            :key="index"
            class="flex border-b border-solid border-border-input">
            <template v-for="item in column" :key="item.dataIndex">
            <div
                class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
                {{ item.name }}
            </div>
            <div class="flex-1 flex items-center px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
                {{ item.customRender()}}
            </div>
            </template>
        </div>
    </div>

    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a1.2" class="text-3.5 text-theme-title font-medium">1.2、<em class="inline-block w-0.25"></em>测评信息详情</span>
    </h1>





  </div>
</template>