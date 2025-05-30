<script lang="ts" setup>
import { onMounted, ref, defineAsyncComponent, computed, inject } from 'vue';
import { TESTER } from '@xcan-angus/tools';
import { Tabs, TabPane, Button } from 'ant-design-vue';
import { scenario, exec } from 'src/api/tester';
import { NoData, ActivityTimeline, SmartComment, Icon, modal, notification, AsyncComponent, AuthorizeModal, FavoriteFollow } from '@xcan-angus/vue-ui';

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
  const [error, { data }] = await scenario.loadInfo(props.data?.scenarioId);
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
  const [_error, { data }] = await scenario.loadScenePermissions(props.data?.scenarioId);
  authPermissions.value = (data?.permissions || []).map(i => i.value);
};

const del = () => {
  modal.confirm({
    centered: true,
    content: `删除场景会同步删除关联关注、收藏、指标、变量等信息，请确认是否删除【${sceanrioData.value?.name}】？`,
    async onOk () {
      const [error] = await scenario.deleteScenario(sceanrioData.value?.id);
      if (error) {
        return;
      }
      notification.success('删除成功，您可以在回收站查看删除后的场景。');
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
  const [error] = await (sceanrioData.value.followFlag ? scenario.delFollowScenario(props.data?.scenarioId) : scenario.addFollowScript(props.data?.scenarioId));
  followLoading.value = false;
  if (error) {
    return;
  }
  if (sceanrioData.value.followFlag) {
    notification.success('取消关注');
  } else {
    notification.success('关注成功');
  }
  sceanrioData.value.followFlag = !sceanrioData.value.followFlag;
};

const favouriteLoading = ref(false);
const handleFavourite = async () => {
  favouriteLoading.value = true;
  const [error] = await (sceanrioData.value.favouriteFlag ? scenario.delFavoriteScript(props.data?.scenarioId) : scenario.addFavoriteScript(props.data?.scenarioId));
  favouriteLoading.value = false;
  if (error) {
    return;
  }
  if (sceanrioData.value.favouriteFlag) {
    notification.success('取消收藏');
  } else {
    notification.success('收藏成功');
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
          编辑
        </Button>
      </RouterLink>
      <Button
        size="small"
        type="text"
        :disabled="auth && !authPermissions.includes('DELETE')"
        @click="del">
        <Icon icon="icon-qingchu" class="mr-1" />
        删除
      </Button>
      <Button
        size="small"
        type="text"
        :disabled="auth && !authPermissions.includes('EXPORT')"
        @click="exportScript">
        <Icon icon="icon-daochu" class="mr-1" />
        导出脚本
      </Button>
      <Button
        size="small"
        type="text"
        :disabled="auth && !authPermissions.includes('GRANT')"
        @click="toAuth">
        <Icon icon="icon-quanxian1" class="mr-1" />
        权限
      </Button>
      <Button
        :loading="followLoading"
        size="small"
        type="text"
        @click="handleFollow">
        <Icon :icon="sceanrioData.followFlag ? 'icon-quxiaoguanzhu' : 'icon-yiguanzhu'" class="mr-1" />
        {{ sceanrioData.followFlag ? '取消关注' : '关注' }}
      </Button>
      <Button
        :loading="favouriteLoading"
        size="small"
        type="text"
        @click="handleFavourite">
        <Icon :icon="sceanrioData.favouriteFlag ? 'icon-quxiaoshoucang' : 'icon-yishoucang'" class="mr-1" />
        {{ sceanrioData.favouriteFlag ? '取消收藏' : '收藏' }}
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
      <TabPane key="func" tab="功能测试">
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
      <TabPane key="perf" tab="性能测试">
        <Execdetail
          :monicaEditorStyle="{height: '600px'}"
          :showBackBtn="false"
          :execId="perfExecId"
          scriptType="TEST_PERFORMANCE"
          @del="handleDel" />
      </TabPane>
      <TabPane key="stability" tab="稳定性测试">
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
      <TabPane key="custom" tab="自定义测试">
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
      <TabPane key="task" tab="测试任务">
        <Task :scenarioId="props.data?.scenarioId" :projectId="props.projectId" />
      </TabPane>
      <TabPane key="activity" tab="活动记录">
        <ActivityTimeline
          :id="props.data?.scenarioId"
          :projectId="props.projectId"
          :types="['SCENARIO']"
          class="w-150 h-full" />
      </TabPane>
      <TabPane key="comment" tab="评论">
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
        onTips="开启&quot;有权限控制&quot;后，需要手动授权服务权限后才会有场景相应操作权限，默认开启&quot;有权限控制&quot;。注意：如果授权对象没有父级目录权限将自动授权查看权限。"
        offTips="开启&quot;无权限控制&quot;后，将允许所有用户公开查看和操作当前场景，查看用户同时需要有当前场景父级目录权限。"
        title="场景权限"
        @change="authFlagChange" />
    </AsyncComponent>
  </div>
</template>
