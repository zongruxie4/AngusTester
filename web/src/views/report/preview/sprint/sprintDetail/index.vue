<script setup lang="ts">
import { computed } from 'vue';
import dayjs from 'dayjs';
import { utils } from '@xcan-angus/infra';
import { Colon } from '@xcan-angus/vue-ui';
import RichEditor from '@/components/editor/richEditor/index.vue';
import { useI18n } from 'vue-i18n';

import { ReportContent } from '../PropsType';
import { DATE_FORMAT, TIME_FORMAT } from '@/utils/constant';

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
    const date = dayjs(item.date).format(DATE_FORMAT);

    const time = item.time.split('~');
    const startTime = dayjs(time[0]).format(TIME_FORMAT);
    const endTime = dayjs(time[1]).format(TIME_FORMAT);

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
      <span id="a1" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.1') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.sprint.sprintDetail.title') }}</span>
    </h1>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a1.1">1.1<em class="inline-block w-3.5"></em>{{ t('reportPreview.sprint.sprintDetail.basicInfo.title') }}</span>
      </h2>
      <div class="border border-solid border-border-input">
        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.name') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ sprint?.name }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.sprint.sprintDetail.basicInfo.fields.taskPrefix') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ sprint?.taskPrefix }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.status') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ sprint?.status?.message }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.owner') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ sprint?.ownerName }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.project') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ sprint?.projectName }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('reportPreview.sprint.sprintDetail.basicInfo.fields.sprintTime') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ sprint?.startDate }} - {{ sprint?.deadlineDate }}
          </div>
        </div>

        <div class="flex border-b border-solid border-border-input">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.creator') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
            {{ sprint?.creator }}
          </div>
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.createdDate') }}
          </div>
          <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
            {{ sprint?.createdDate }}
          </div>
        </div>

        <div class="flex">
          <div
            class="w-27 flex-shrink-0 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
            {{ t('common.evalWorkloadMethod') }}
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
        <span id="a1.2">1.2<em class="inline-block w-3.5"></em>{{ t('reportPreview.sprint.sprintDetail.acceptanceCriteria.title') }}</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ sprint?.acceptanceCriteria || t('common.noData') }}
      </div>
    </div>

    <div class="mb-7">
      <h2 class="flex items-center space-x-2.5 text-3.5 mb-3.5 text-theme-title">
        <span id="a1.3">1.3<em class="inline-block w-3.5"></em>{{ t('reportPreview.sprint.sprintDetail.meetingRecord.title') }}</span>
      </h2>
      <div v-if="!meetings.length" class="content-text-container">{{ t('common.noData') }}</div>
      <template v-else>
        <div
          v-for="(item, index) in meetings"
          :key="item.id"
          class="text-3 leading-5 space-y-2 mb-5 last:mb-0">
          <div class="text-theme-title font-medium">{{ t('reportPreview.sprint.sprintDetail.meetingRecord.meetingTopic') }}： {{ item.name }}</div>
          <div class="flex items-start space-x-5">
            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>{{ t('reportPreview.sprint.sprintDetail.meetingRecord.fields.meetingType') }}</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.type?.message }}</div>
            </div>

            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>{{ t('reportPreview.sprint.sprintDetail.meetingRecord.fields.meetingDate') }}</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.date }}</div>
            </div>
          </div>

          <div class="flex items-start space-x-5">
            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>{{ t('reportPreview.sprint.sprintDetail.meetingRecord.fields.meetingTime') }}</span>
                <Colon class="w-1" />
              </div>

              <div class="text-3 whitespace-nowrap">
                <span>{{ item.startTime }}</span>
                <span class="mx-2">-</span>
                <span>{{ item.endTime }}</span>
              </div>
            </div>

            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>{{ t('reportPreview.sprint.sprintDetail.meetingRecord.fields.meetingLocation') }}</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.location }}</div>
            </div>
          </div>

          <div class="flex items-start space-x-5">
            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>{{ t('reportPreview.sprint.sprintDetail.meetingRecord.fields.meetingHost') }}</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.moderatorName }}</div>
            </div>

            <div class="w-1/2 flex items-start">
              <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
                <span>{{ t('reportPreview.sprint.sprintDetail.meetingRecord.fields.attendees') }}</span>
                <Colon class="w-1" />
              </div>

              <div class="whitespace-pre-wrap break-words break-all">{{ item.participantNames }}</div>
            </div>
          </div>

          <div class="flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>{{ t('reportPreview.sprint.sprintDetail.meetingRecord.fields.meetingContent') }}</span>
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
        <span id="a1.4">1.4<em class="inline-block w-3.5"></em>{{ t('reportPreview.sprint.sprintDetail.otherInfo.title') }}</span>
      </h2>
      <div class="break-all whitespace-pre-wrap content-text-container">
        {{ sprint?.otherInformation || t('common.noData') }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
