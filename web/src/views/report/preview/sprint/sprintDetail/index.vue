<script setup lang="ts">
import { computed } from 'vue';
import dayjs from 'dayjs';
import { utils } from '@xcan-angus/infra';
import { Colon } from '@xcan-angus/vue-ui';
import RichEditor from '@/components/richEditor/index.vue';
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

const sprint = computed(() => {
  return props.dataSource?.content?.sprint;
});

const meetings = computed(() => {
  return sprint.value?.meetings?.map(item => {
    const date = dayjs(item.date).format('YYYY-MM-DD');

    const time = item.time.split('~');
    const startTime = dayjs(time[0]).format('HH:mm:ss');
    const endTime = dayjs(time[1]).format('HH:mm:ss');

    const participantNames = item.participants.map(item => item.fullName).join(',');
    return {
      ...item,
      date,
      startTime,
      endTime,
      moderatorName: item.moderator?.fullName,
      participantNames,
      id: utils.uuid()
    };
  }) || [];
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>迭代信息</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a1.1">1.1<em class="inline-block w-3.5"></em>基本信息</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            名称
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ sprint?.name }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            任务前缀
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ sprint?.taskPrefix }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            状态
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ sprint?.status?.message }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            负责人
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ sprint?.ownerName }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            所属项目
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ sprint?.projectName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            迭代时间
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ sprint?.startDate }} 至 {{ sprint?.deadlineDate }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            添加人
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ sprint?.createdByName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            添加时间
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ sprint?.createdDate }}
          </div>
        </div>

        <div class="flex">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            工作量评估方式
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ sprint?.evalWorkloadMethod?.message }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          </div>
        </div>
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.2">1.2<em class="inline-block w-3.5"></em>验收标准</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ sprint?.acceptanceCriteria || '无' }}
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a1.3">1.3<em class="inline-block w-3.5"></em>会议记录</span>
      </h2>
      <div v-if="!meetings.length" class="content-text-container">无</div>
      <template v-else>
        <div
          v-for="(item, index) in meetings"
          :key="item.id"
          class="text-3 leading-5 space-y-2 mb-5 last:mb-0">
          <div class="text-theme-title font-medium">会议主题： {{ item.name }}</div>
          <div class="flex items-start space-x-5">
            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>会议类型</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.type?.message }}</div>
            </div>

            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>会议日期</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.date }}</div>
            </div>
          </div>

          <div class="flex items-start space-x-5">
            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>会议时间</span>
                <Colon class="w-1" />
              </div>

              <div class="text-3 whitespace-nowrap">
                <span>{{ item.startTime }}</span>
                <span class="mx-2">至</span>
                <span>{{ item.endTime }}</span>
              </div>
            </div>

            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>会议地点</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.location }}</div>
            </div>
          </div>

          <div class="flex items-start space-x-5">
            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>会议主持</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.moderatorName }}</div>
            </div>

            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>参会人员</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.participantNames }}</div>
            </div>
          </div>

          <div class="flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>会议内容</span>
              <Colon class="w-1" />
            </div>

            <RichEditor
              :value="item.content"
              mode="view" />
          </div>
        </div>
      </template>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-2.5 text-theme-title">
        <span id="a1.4">1.4<em class="inline-block w-3.5"></em>其他说明</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ sprint?.otherInformation || '无' }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
