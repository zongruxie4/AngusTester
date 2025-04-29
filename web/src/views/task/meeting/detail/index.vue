<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { Colon, Spin } from '@xcan-angus/vue-ui';
import { TESTER, http } from '@xcan-angus/tools';
import dayjs from 'dayjs';
import RichEditor from '@/components/richEditor/index.vue';

import { MeetingInfo } from '../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  data: {
    _id: string;
    id: string | undefined;
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));

const dataSource = ref<MeetingInfo>();

const loading = ref(false);
const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await http.get(`${TESTER}/task/meeting/${id}`);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as MeetingInfo;
  if (!data) {
    return;
  }

  const date = dayjs(data.date).format('YYYY-MM-DD');

  const time = (data?.time || '').split('~');
  const startTime = dayjs(time[0]).format('HH:mm:ss');
  const endTime = dayjs(time[1]).format('HH:mm:ss');

  const participantNames = data.participants.map(item => item.fullName).join(',');
  dataSource.value = {
    ...data,
    date,
    startTime,
    endTime,
    moderatorName: data.moderator?.fullName,
    participantNames
  };

  const name = data.subject;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id + '-detail' });
  }
};

onMounted(() => {
  watch(() => props.data, async (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }
    await loadData(id);
  }, { immediate: true });
});

</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div
      :key="dataSource?.id"
      class="text-3 leading-5 space-y-2.5 py-2.5 px-3.5 mb-3.5 last:mb-0 meeting-container">
      <div class="text-theme-title font-medium"> {{ dataSource?.subject }}</div>
      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>会议类型</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.type?.message }}</div>
        </div>

        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>所属迭代</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.sprintName || '--' }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>会议日期</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.date }}</div>
        </div>
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>会议时间</span>
            <Colon class="w-1" />
          </div>

          <div class="text-3 whitespace-nowrap">
            <span>{{ dataSource?.startTime }}</span>
            <span class="mx-2">至</span>
            <span>{{ dataSource?.endTime }}</span>
          </div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>会议地点</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.location || '--' }}</div>
        </div>
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>会议主持</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.moderator?.fullName }}</div>
        </div>
      </div>

      <div class="flex items-start">
        <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>参会人员</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.participantNames }}</div>
      </div>
      <div class="flex items-start">
        <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>会议内容</span>
          <Colon class="w-1" />
        </div>

        <RichEditor
          :value="dataSource?.content"
          mode="view" />
      </div>
    </div>
  </Spin>
</template>

<style scoped>
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

.meeting-container {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
}
</style>
