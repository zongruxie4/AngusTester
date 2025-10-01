<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { ScriptType, TESTER } from '@xcan-angus/infra';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { ActivityTimeline, AsyncComponent, Icon, SmartComment } from '@xcan-angus/vue-ui';
import { ScenarioPermission } from '@/enums/enums';

// Import composables
import { useScenarioData } from './composables/useScenarioData';
import { useScenarioActions } from './composables/useScenarioActions';
import { useScenarioResults } from './composables/useScenarioResults';
import type { ScenarioDetailProps } from './types';

const { t } = useI18n();

const props = withDefaults(defineProps<ScenarioDetailProps>(), {
  data: () => ({
    scenarioId: ''
  })
});

const deleteTabPane = inject<(data: string[]) => void>('deleteTabPane', () => {});

// Async components
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const ExecDetail = defineAsyncComponent(() => import('@/views/execution/detail/index.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/ExportScriptModal.vue'));
const TestSummary = defineAsyncComponent(() => import('./TestSummary.vue'));
const Task = defineAsyncComponent(() => import('./Task.vue'));

// Tab state
const activeTab = ref('func');

// Use composables
const {
  scenarioData,
  auth,
  authPermissions,
  isHttpPlugin,
  loadScenarioDetail,
  handleAuthFlagChange
} = useScenarioData(props.data?.scenarioId);

const {
  followLoading,
  favouriteLoading,
  exportVisible,
  toAuthVisible,
  deleteScenario,
  showExportScript,
  showAuthorization,
  handleFollow,
  handleFavourite
} = useScenarioActions(scenarioData, props.data?.scenarioId, deleteTabPane);

const {
  dataSource,
  funcExecId,
  perfExecId,
  customExecId,
  stabilityExecId,
  loadScenarioResult,
  handleResultDeletion
} = useScenarioResults(props.data?.scenarioId);

// Initialize data on component mount
onMounted(() => {
  if (props.data?.scenarioId) {
    loadScenarioDetail();
    loadScenarioResult();
  }
});
</script>
<template>
  <div class="p-3 h-full" :class="[['activity', 'comment'].includes(activeTab) ? 'flex flex-col' : 'overflow-y-auto']">
    <div v-if="scenarioData" class="flex justify-end">
      <RouterLink :to="`/scenario#scenario?id=${scenarioData?.id}&name=${scenarioData?.name}&plugin=${scenarioData?.plugin}`">
        <Button size="small" type="text">
          <Icon icon="icon-xiugai" class="mr-1" />
          {{ t('actions.edit') }}
        </Button>
      </RouterLink>
      <Button
        size="small"
        type="text"
        :disabled="auth && !authPermissions.includes(ScenarioPermission.DELETE)"
        @click="deleteScenario">
        <Icon icon="icon-qingchu" class="mr-1" />
        {{ t('actions.delete') }}
      </Button>
      <Button
        size="small"
        type="text"
        :disabled="auth && !authPermissions.includes(ScenarioPermission.EXPORT)"
        @click="showExportScript">
        <Icon icon="icon-daochu" class="mr-1" />
        {{ t('actions.export') }}
      </Button>
      <Button
        size="small"
        type="text"
        :disabled="auth && !authPermissions.includes(ScenarioPermission.GRANT)"
        @click="showAuthorization">
        <Icon icon="icon-quanxian1" class="mr-1" />
        {{ t('actions.permission') }}
      </Button>
      <Button
        :loading="followLoading"
        size="small"
        type="text"
        @click="handleFollow">
        <Icon :icon="scenarioData.follow ? 'icon-quxiaoguanzhu' : 'icon-yiguanzhu'" class="mr-1" />
        {{ scenarioData.follow ? t('actions.cancelFollow') : t('actions.addFollow') }}
      </Button>
      <Button
        :loading="favouriteLoading"
        size="small"
        type="text"
        @click="handleFavourite">
        <Icon :icon="scenarioData.favourite ? 'icon-quxiaoshoucang' : 'icon-yishoucang'" class="mr-1" />
          {{ scenarioData.favourite ? t('actions.cancelFavourite') : t('actions.addFavourite') }}
      </Button>
    </div>
    <TestSummary
      v-if="isHttpPlugin"
      class="mb-4 mt-2"
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
        <ExecDetail
          v-if="scenarioData"
          class="p-0"
          :monicaEditorStyle="{height: '600px'}"
          :showBackBtn="false"
          :execId="funcExecId"
          :scriptType="ScriptType.TEST_FUNCTIONALITY"
          :plugin="scenarioData?.plugin"
          @del="handleResultDeletion" />
      </TabPane>
      <TabPane key="perf" :tab="t('scenario.detail.tabs.perf')">
        <ExecDetail
          :monicaEditorStyle="{height: '600px'}"
          :showBackBtn="false"
          :execId="perfExecId"
          :scriptType="ScriptType.TEST_PERFORMANCE"
          @del="handleResultDeletion" />
      </TabPane>
      <TabPane key="stability" :tab="t('scenario.detail.tabs.stability')">
        <ExecDetail
          :monicaEditorStyle="{height: '600px'}"
          :showBackBtn="false"
          :execId="stabilityExecId"
          :scriptType="ScriptType.TEST_STABILITY"
          @del="handleResultDeletion" />
      </TabPane>
      <TabPane key="custom" :tab="t('scenario.detail.tabs.custom')">
        <ExecDetail
          :monicaEditorStyle="{height: '600px'}"
          :showBackBtn="false"
          :execId="customExecId"
          :scriptType="ScriptType.TEST_CUSTOMIZATION"
          @del="handleResultDeletion" />
      </TabPane>
      <TabPane key="task" :tab="t('common.issue')">
        <Task :scenarioId="props.data?.scenarioId" :projectId="props.projectId" />
      </TabPane>
      <TabPane key="activity" :tab="t('scenario.detail.tabs.activity')">
        <ActivityTimeline
          :id="props.data?.scenarioId"
          :projectId="props.projectId"
          :types="['SCENARIO']"
          class="w-150 h-full" />
      </TabPane>
      <TabPane key="comment" :tab="t('common.comment')">
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
        :enumKey="ScenarioPermission"
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
        @change="handleAuthFlagChange" />
    </AsyncComponent>
  </div>
</template>
