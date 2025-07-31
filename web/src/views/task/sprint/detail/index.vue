<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Progress, TabPane, Tabs } from 'ant-design-vue';
import { Colon, Icon, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import { TESTER, toClipboard, utils, download } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { task } from '@/api/tester';
import { SprintInfo } from '../PropsType';

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

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));
const BurnDownChart = defineAsyncComponent(() => import('@/views/task/sprint/detail/burndownChart/index.vue'));
const MembersProgress = defineAsyncComponent(() => import('@/views/task/sprint/detail/memberProgress/index.vue'));
const WorkCalendar = defineAsyncComponent(() => import('@/views/task/homepage/workCalendar/index.vue'));

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const replaceTabPane = inject<(key:string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const deleteTabPane = inject<(key:string, data: { [key: string]: any }) => void>('deleteTabPane', () => ({}));
const isAdmin = inject('isAdmin', ref(false));

const dataSource = ref<SprintInfo>();
const permissions = ref<string[]>([]);
const completedRate = ref(0);

const loading = ref(false);
const exportLoading = ref(false);

const loadPermissions = async (id: string) => {
  if (isAdmin.value) {
    permissions.value = [
      'MODIFY_SPRINT',
      'DELETE_SPRINT',
      'ADD_TASK',
      'MODIFY_TASK',
      'DELETE_TASK',
      'EXPORT_TASK',
      'REVIEW',
      'TEST',
      'GRANT',
      'VIEW'
    ];

    return;
  }

  const params = {
    admin: true
  };
  loading.value = true;
  const [error, res] = await task.getCurrentUserSprintAuth(id, params);
  loading.value = false;
  if (error) {
    return;
  }

  const { taskSprintAuthFlag, permissions: _permissions } = res?.data || { taskSprintAuthFlag: true, permissions: [] };
  if (!taskSprintAuthFlag) {
    permissions.value = [
      'MODIFY_SPRINT',
      'DELETE_SPRINT',
      'ADD_TASK',
      'MODIFY_TASK',
      'DELETE_TASK',
      'EXPORT_TASK',
      'REVIEW',
      'TEST',
      'VIEW'];

    if (_permissions.includes('GRANT')) {
      permissions.value.push('GRANT');
    }
  } else {
    permissions.value = (_permissions || []).map(item => item.value);
  }
};

const loadData = async (id: string) => {
  loading.value = true;
  const [error, res] = await task.getSprintDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as SprintInfo;
  if (!data) {
    return;
  }

  dataSource.value = data;
  if (dataSource.value.progress?.completedRate) {
    completedRate.value = +dataSource.value.progress.completedRate.replace(/(\d+\.\d{2})\d+/, '$1');
  }

  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id + '-detail' });
  }
};

const toEdit = () => {
  replaceTabPane(sprintId.value + '-detail', { _id: sprintId.value, value: 'sprintEdit', noCache: true, data: dataSource });
};

const toExport = async () => {
  if (!dataSource.value) {
    return;
  }

  const { id, projectId } = dataSource.value;
  if (exportLoading.value) {
    return;
  }

  exportLoading.value = false;
  await download(`${TESTER}/task/export?projectId=${projectId}&sprintId=${id}`);
  exportLoading.value = true;
};

const toCopyHref = () => {
  const message = window.location.origin + '/task#sprint?id=' + sprintId.value;
  toClipboard(message).then(() => {
    notification.success('复制成功');
  }).catch(() => {
    notification.error('复制失败');
  });
};

const toRefresh = () => {
  const id = sprintId.value;
  if (!id) {
    return;
  }

  loadData(id);
};

const cancel = () => {
  deleteTabPane([props.data.id + '-detail']);
};

onMounted(() => {
  watch(() => props.data, async (newValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    loadPermissions(id);
    loadData(id);
  }, { immediate: true });
});

const sprintId = computed(() => {
  return dataSource.value?.id;
});

const attachments = computed(() => {
  return dataSource.value?.attachments || [];
});

const meetings = computed(() => {
  return dataSource.value?.meetings?.map(item => {
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
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-start flex-wrap space-y-b-2 space-x-2.5 mb-3.5">
      <Button
        :disabled="!isAdmin && !permissions.includes('MODIFY_SPRINT')"
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="toEdit">
        <Icon icon="icon-shuxie" class="text-3.5" />
        <span>编辑</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="p-0">
        <RouterLink
          class="flex items-center space-x-1 leading-6.5 px-1.75"
          :to="`/task#task?sprintId=${sprintId}&sprintName=${dataSource?.name}`">
          <Icon icon="icon-renwu2" class="text-3.5" />
          <span>查看任务</span>
        </RouterLink>
      </Button>

      <Button
        :disabled="!isAdmin && !permissions.includes('EXPORT_CASE')"
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="toExport">
        <Icon icon="icon-daochu" class="text-3.5" />
        <span>导出任务</span>
      </Button>

      <Button
        size="small"
        class="flex items-center"
        @click="toCopyHref">
        <Icon class="mr-1 flex-shrink-0" icon="icon-fuzhi" />
        <span>复制链接</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="toRefresh">
        <Icon class="mr-1 flex-shrink-0" icon="icon-shuaxin" />
        <span>刷新</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="cancel">
        <span>取消</span>
      </Button>
    </div>

    <div class="max-w-250 mb-2">
      <div class="text-theme-title mb-2">基本信息</div>
      <div class="space-y-2.5">
        <div class="flex items-start space-x-5">
          <div class="w-1/2 flex items-start">
            <div class="w-12.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>名称</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.name }}</div>
          </div>

          <div class="w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>时间计划</span>
              <Colon class="w-1" />
            </div>

            <div class="text-3 whitespace-nowrap">
              <span>{{ dataSource?.startDate }}</span>
              <span class="mx-2">至</span>
              <span>{{ dataSource?.deadlineDate }}</span>
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="w-1/2 flex items-start">
            <div class="w-12.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>负责人</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.ownerName }}</div>
          </div>

          <div class="w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>任务前缀</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.taskPrefix }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="w-1/2 flex items-center">
            <div class="w-12.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>状态</span>
              <Colon class="w-1" />
            </div>

            <div class="text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5">
              <div class="h-1.5 w-1.5 rounded-full mr-1" :class="dataSource?.status?.value"></div>
              <div>{{ dataSource?.status?.message }}</div>
            </div>
          </div>

          <div class="w-1/2 flex items-center">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>工作量评估</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              {{ dataSource?.evalWorkloadMethod?.message }}
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="w-1/2 flex items-center">
            <div class="w-12.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>进度</span>
              <Colon class="w-1" />
            </div>

            <Progress :percent="completedRate" style="width:150px;" />
          </div>
        </div>

        <div class="flex items-start">
          <div style="width:calc(50% - 10px);" class="flex items-start">
            <div class="w-12.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>附件</span>
              <Colon class="w-1" />
            </div>

            <div class="space-y-1 truncate">
              <a
                v-for="item in attachments"
                :key="item.id"
                class="block w-auto truncate"
                :download="item.name"
                :href="item.url">
                {{ item.name }}
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Tabs size="small" class="max-w-250">
      <TabPane key="acceptanceCriteria" tab="验收标准">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <!-- {{ dataSource?.acceptanceCriteria }} -->
          <RichEditor
            v-if="dataSource?.acceptanceCriteria"
            :value="dataSource?.acceptanceCriteria"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="otherInformation" tab="其他说明">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <!-- {{ dataSource?.otherInformation }} -->
          <RichEditor
            v-if="dataSource?.otherInformation"
            :value="dataSource?.otherInformation"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="chart" tab="燃尽图">
        <BurnDownChart :sprintId="sprintId" />
      </TabPane>
      <TabPane key="progress" tab="成员进度">
        <MembersProgress :sprintId="sprintId" :projectId="props.projectId" />
      </TabPane>
      <TabPane key="workCalendar" tab="工作日历">
        <WorkCalendar
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :sprintId="sprintId" />
      </TabPane>
      <TabPane key="meetings" tab="会议记录">
        <div
          v-for="(item, index) in meetings"
          :key="item.id"
          class="text-3 leading-5 space-y-2.5 py-2.5 px-3.5 mb-3.5 last:mb-0 meeting-container">
          <div class="text-theme-title font-medium">{{ item.subject }}</div>
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
        <NoData
          v-if="!meetings?.length"
          size="small"
          class="mt-20" />
      </TabPane>
    </Tabs>
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
  background-color: #fafafa;
}
</style>
