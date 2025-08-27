<script lang="ts" setup>
import { onMounted, ref, defineAsyncComponent, computed, inject } from 'vue';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { Tabs, TabPane, Button } from 'ant-design-vue';
import { scenario, exec } from '@/api/tester';
import { NoData, ActivityTimeline, SmartComment, Icon, modal, notification, AsyncComponent, AuthorizeModal, FavoriteFollow } from '@xcan-angus/vue-ui';

const { t } = useI18n();

interface Props {
  data: {
    scenarioId: string;
  };
  id: string;
  _id: string;
  appInfo:{[key:string]:any};
  userInfo:{[key:string]:any};
  projectId:string;
}

const props = withDefaults(defineProps<Props>(), {
  data: () => ({
    scenarioId: ''
  })
});
const deleteTabPane = inject<(data: string[]) => void>('deleteTabPane', () => { });

const TestSummary = defineAsyncComponent(() => import('./testSummary/index.vue'));
const Execdetail = defineAsyncComponent(() => import('@/views/execution/info/index.vue'));
const Task = defineAsyncComponent(() => import('./task/index.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/exportModal/index.vue'));

const activeTab = ref('func');
const dataSource = ref();
const isHttpPlugin = ref(false);
const auth = ref(false);
const authPermissions = ref<string[]>([]);

const sceanrioData = ref();

const loadScenarioDetail = async () => {
  const [error, { data }] = await scenario.getScenarioDetail(props.data?.scenarioId);
  if (error) {
    return;
  }
  sceanrioData.value = data;
  isHttpPlugin.value = data?.plugin === 'Http';
  auth.value = data.auth;

  if (data.auth) {
    loadPermissions();
  }
};

const loadPermissions = async () => {
  const [_error, { data }] = await scenario.getCurrentScenarioAuth(props.data?.scenarioId);
  authPermissions.value = (data?.permissions || []).map(i => i.value);
};

const del = () => {
  modal.confirm({
    centered: true,
    content: t('scenario.detail.messages.deleteConfirm', { name: sceanrioData.value?.name }),
    async onOk () {
      const [error] = await scenario.deleteScenario(sceanrioData.value?.id);
      if (error) {
        return;
      }
      notification.success(t('scenario.detail.messages.deleteSuccess'));
      deleteTabPane([sceanrioData.value?.id, sceanrioData.value?.id + '-detail']);
    }
  });
};

const exportVisible = ref(false);
const exportScript = () => {
  exportVisible.value = true;
};

const toAuthVisible = ref(false);
const toAuth = () => {
  toAuthVisible.value = true;
};

const followLoading = ref(false);
const handleFollow = async () => {
  followLoading.value = true;
  const [error] = await (sceanrioData.value.followFlag ? scenario.deleteScenarioFollow(props.data?.scenarioId) : scenario.addScenarioFollow(props.data?.scenarioId));
  followLoading.value = false;
  if (error) {
    return;
  }
  if (sceanrioData.value.followFlag) {
    notification.success(t('scenario.detail.messages.cancelFollowSuccess'));
  } else {
    notification.success(t('scenario.detail.messages.followSuccess'));
  }
  sceanrioData.value.followFlag = !sceanrioData.value.followFlag;
};

const favouriteLoading = ref(false);
const handleFavourite = async () => {
  favouriteLoading.value = true;
  const [error] = await (sceanrioData.value.favouriteFlag ? scenario.deleteScenarioFavorite(props.data?.scenarioId) : scenario.addScenarioFavorite(props.data?.scenarioId));
  favouriteLoading.value = false;
  if (error) {
    return;
  }
  if (sceanrioData.value.favouriteFlag) {
    notification.success(t('scenario.detail.messages.cancelFavouriteSuccess'));
  } else {
    notification.success(t('scenario.detail.messages.favouriteSuccess'));
  }
  sceanrioData.value.favouriteFlag = !sceanrioData.value.favouriteFlag;
};

const authFlagChange = (data: {auth: boolean}) => {
  auth.value = data.auth;
  if (auth.value) {
    loadPermissions();
  }
};

const loadScenarioResult = async () => {
  const [error, { data }] = await exec.getScenarioResult(props.data?.scenarioId);
  if (error) {
    return;
  }
  dataSource.value = data;
};

const funcExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_FUNCTIONALITY?.execId;
});
const perfExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_PERFORMANCE?.execId;
});
const customExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_CUSTOMIZATION?.execId;
});
const stabilityExecId = computed(() => {
  return dataSource.value?.resultDetailVoMap?.TEST_STABILITY?.execId;
});

const handleDel = () => {
  loadScenarioResult();
};

onMounted(() => {
  if (props.data?.scenarioId) {
    loadScenarioDetail();
    loadScenarioResult();
  }
});

</script>
<template>
  <div class="p-3 h-full" :class="[['activity', 'comment'].includes(activeTab) ? 'flex flex-col' : 'overflow-y-auto']">
    <div v-if="sceanrioData" class="flex justify-end">
      <RouterLink :to="`/scenario#scenario?id=${sceanrioData?.id}&name=${sceanrioData?.name}&plugin=${sceanrioData?.plugin}`">
        <Button size="small" type="text">
          <Icon icon="icon-xiugai" class="mr-1" />
          {{ t('scenario.detail.actions.edit') }}
        </Button>
      </RouterLink>
      <Button
        size="small"
        type="text"
        :disabled="auth && !authPermissions.includes('DELETE')"
        @click="del">
        <Icon icon="icon-qingchu" class="mr-1" />
        {{ t('scenario.detail.actions.delete') }}
      </Button>
      <Button
        size="small"
        type="text"
        :disabled="auth && !authPermissions.includes('EXPORT')"
        @click="exportScript">
        <Icon icon="icon-daochu" class="mr-1" />
        {{ t('scenario.detail.actions.export') }}
      </Button>
      <Button
        size="small"
        type="text"
        :disabled="auth && !authPermissions.includes('GRANT')"
        @click="toAuth">
        <Icon icon="icon-quanxian1" class="mr-1" />
        {{ t('scenario.detail.actions.auth') }}
      </Button>
      <Button
        :loading="followLoading"
        size="small"
        type="text"
        @click="handleFollow">
        <Icon :icon="sceanrioData.followFlag ? 'icon-quxiaoguanzhu' : 'icon-yiguanzhu'" class="mr-1" />
        {{ sceanrioData.followFlag ? t('scenario.detail.actions.cancelFollow') : t('scenario.detail.actions.follow') }}
      </Button>
      <Button
        :loading="favouriteLoading"
        size="small"
        type="text"
        @click="handleFavourite">
        <Icon :icon="sceanrioData.favouriteFlag ? 'icon-quxiaoshoucang' : 'icon-yishoucang'" class="mr-1" />
        {{ sceanrioData.favouriteFlag ? t('scenario.detail.actions.cancelFavourite') : t('scenario.detail.actions.favourite') }}
      </Button>
    </div>
    <TestSummary
      v-if="isHttpPlugin"
      class="mb-4"
      :projectId="props.projectId"
      :appInfo="props.appInfo"
      :userInfo="props.userInfo"
      :dataSource="dataSource" />
    <Tabs
      v-model:activeKey="activeTab"
      size="small"
      type="card"
      :class="[['activity', 'comment'].includes(activeTab) ? 'min-h-0' : '']"
      class="flex-1">
      <TabPane key="func" :tab="t('scenario.detail.tabs.func')">
        <Execdetail
          v-if="sceanrioData"
          class="p-0"
          :monicaEditorStyle="{height: '600px'}"
          :showBackBtn="false"
          :execId="funcExecId"
          scriptType="TEST_FUNCTIONALITY"
          :plugin="sceanrioData?.plugin"
          @del="handleDel" />
        <!--        <NoData-->
        <!--          v-else-->
        <!--          size="small"-->
        <!--          class="mt-25" />-->
      </TabPane>
      <TabPane key="perf" :tab="t('scenario.detail.tabs.perf')">
        <Execdetail
          :monicaEditorStyle="{height: '600px'}"
          :showBackBtn="false"
          :execId="perfExecId"
          scriptType="TEST_PERFORMANCE"
          @del="handleDel" />
      </TabPane>
      <TabPane key="stability" :tab="t('scenario.detail.tabs.stability')">
        <Execdetail
          :monicaEditorStyle="{height: '600px'}"
          :showBackBtn="false"
          :execId="stabilityExecId"
          scriptType="TEST_STABILITY"
          @del="handleDel" />
        <!--        <NoData-->
        <!--          v-else-->
        <!--          size="small"-->
        <!--          class="mt-25" />-->
      </TabPane>
      <TabPane key="custom" :tab="t('scenario.detail.tabs.custom')">
        <Execdetail
          :monicaEditorStyle="{height: '600px'}"
          :showBackBtn="false"
          :execId="customExecId"
          scriptType="TEST_CUSTOMIZATION"
          @del="handleDel" />
        <!--        <NoData-->
        <!--          v-else-->
        <!--          size="small"-->
        <!--          class="mt-25" />-->
      </TabPane>
      <TabPane key="task" :tab="t('scenario.detail.tabs.task')">
        <Task :scenarioId="props.data?.scenarioId" :projectId="props.projectId" />
      </TabPane>
      <TabPane key="activity" :tab="t('scenario.detail.tabs.activity')">
        <ActivityTimeline
          :id="props.data?.scenarioId"
          :projectId="props.projectId"
          :types="['SCENARIO']"
          class="w-150 h-full" />
      </TabPane>
      <TabPane key="comment" :tab="t('scenario.detail.tabs.comment')">
        <SmartComment
          ref="smartCommentRef"
          targetType="SCENARIO"
          avatar
          size="small"
          :showPublishTitle="false"
          :bordered="false"
          :public0="false"
          :userId="props.userInfo.id"
          :targetId="props.data?.scenarioId"
          :action="`${TESTER}/comment`" />
      </TabPane>
    </Tabs>

    <AsyncComponent :visible="exportVisible">
      <ExportScriptModal v-model:visible="exportVisible" :ids="[props.data?.scenarioId]" />
    </AsyncComponent>

    <AsyncComponent :visible="toAuthVisible">
      <AuthorizeModal
        v-model:visible="toAuthVisible"
        enumKey="ScenarioPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/scenario/auth?scenarioId=${props.data?.scenarioId}`"
        :delUrl="`${TESTER}/scenario/auth`"
        :addUrl="`${TESTER}/scenario/${props.data?.scenarioId}/auth`"
        :updateUrl="`${TESTER}/scenario/auth`"
        :enabledUrl="`${TESTER}/scenario/${props.data?.scenarioId}/auth/enabled`"
        :initStatusUrl="`${TESTER}/scenario/${props.data?.scenarioId}/auth/status`"
        :onTips="t('scenario.detail.tips.authOn')"
        :offTips="t('scenario.detail.tips.authOff')"
        :title="t('scenario.detail.tips.authTitle')"
        @change="authFlagChange" />
    </AsyncComponent>
  </div>
</template>
