<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, provide, ref, watch, Ref } from 'vue';
import { Tooltip } from 'ant-design-vue';
import { AsyncComponent, BrowserTab, Icon, modal, notification, Spin, VuexHelper } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';
import { useRoute, useRouter } from 'vue-router';
import { funcCase } from '@/api/tester';
import { CaseTestResult } from '@/enums/enums';

import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';

const { t } = useI18n();

export type CaseActionAuth = 'edit'| 'debug' | 'review' | 'clone' | 'move' | 'delete' | 'updateTestResult_passed' | 'updateTestResult_notPassed' | 'updateTestResult_blocked' | 'updateTestResult_canceled' | 'resetTestResult' | 'resetReviewResult' | 'retestResult' | 'copy' | 'addFavourite' | 'addFollow' | 'copyUrl'

const CaseList = defineAsyncComponent(() => import('@/views/test/case/list/index.vue'));
const CaseInfo = defineAsyncComponent(() => import('@/views/test/case/detail/index.vue'));
const AddCaseModal = defineAsyncComponent(() => import('@/views/test/case/list/Edit.vue'));
const ReviewModal = defineAsyncComponent(() => import('@/views/test/case/list/Review.vue'));
const MoveCaseModal = defineAsyncComponent(() => import('@/views/test/case/list/Move.vue'));
const UpdateTestResultModal = defineAsyncComponent(() => import('@/views/test/case/list/UpdateResult.vue'));

const userInfo = ref(appContext.getUser());
// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
const appInfo = ref(appContext.getAccessApp());

const route = useRoute();
const router = useRouter();
const browserTabRef = ref();
const caseListRef = ref();
const addTabPane = (data) => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add(() => {
      return { ...data };
    });
  }
};

const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

const { useMutations, useState } = VuexHelper;
const { stepVisible, stepKey, stepContent } = useState(['stepVisible', 'stepKey', 'stepContent'], 'guideStore');
const { updateGuideStep } = useMutations(['updateGuideType', 'updateGuideStep'], 'guideStore');

const tabGuideStep = () => {
  updateGuideStep({ visible: true, key: 'createPlanOne' });
  addTabPane({ _id: 'case_home' });
};

const restoreFilters = (data) => {
  const filters = [];

  const filterKeys = Object.keys(data).filter(key => key.startsWith('filters['));
  const filterIndices = filterKeys.map(key => key.match(/\[\d+\]/)?.[0]);

  const uniqueFilterIndices = Array.from(new Set(filterIndices));

  uniqueFilterIndices.forEach(index => {
    const filterKey = data[`filters${index}.key`] as string;
    const filterOp = data[`filters${index}.op`] as string;
    const filterValue = data[`filters${index}.value`] as string;
    filters.push({ key: filterKey, op: filterOp, value: filterValue });
  });

  const newData: { [key: string]: string } = { ...data };
  filterKeys.forEach(key => delete newData[key]);

  return { filters, ...newData, total: +newData.total };
};

const updateCaseTab = async (value) => {
  browserTabRef.value.update(value);
};

const favoriteFollowRef = ref(null);
const updateFollowFavourite = (type: 'addFollow' | 'addFavourite') => {
  if (!favoriteFollowRef.value) {
    return;
  }
  if (type === 'addFollow') {
    favoriteFollowRef.value.followNotify++;
    return;
  }

  favoriteFollowRef.value.favouriteNotify++;
};

const caseInfoRef = ref(null);
const scrollNotify = ref(0);
const showAnchor = ref(false);
const checkScroll = (event) => {
  const container = event.target;
  caseInfoRef.value = event.target;
  if (container) {
    if (container.scrollTop === 0) {
      showAnchor.value = false;
      return;
    }
    showAnchor.value = container.scrollHeight > container.clientHeight;

    const isAtBottom = Math.floor(container.scrollHeight - container.scrollTop) === container.clientHeight;
    if (isAtBottom) {
      scrollNotify.value++;
    }
  }
};

const scrollToTop = () => {
  if (!caseInfoRef.value) {
    return;
  }
  const container = caseInfoRef.value;
  if (container) {
    container.scrollTo({ top: 0, behavior: 'smooth' });
  }
};

// 详情操作按钮
const currTabInfo = ref();
const selectedCase = ref();
const handleDetailAction = (type: CaseActionAuth, value: CaseDetail, tabInfo) => {
  currTabInfo.value = tabInfo;
  selectedCase.value = value;
  switch (type) {
    case 'edit':
      handleEdit(value);
      break;
    case 'clone':
      handleClone(value);
      break;
    case 'move':
      handleAction('move');
      break;
    case 'delete':
      handleDeleteCase(value);
      break;
    case 'review':
      handleAction('review');
      break;
    case 'updateTestResult_passed':
      handleAction('updateTestResult_passed');
      break;
    case 'updateTestResult_notPassed':
      handleAction('updateTestResult_notPassed');
      break;
    case 'updateTestResult_blocked':
      handleSetResultBlocked(value);
      break;
    case 'updateTestResult_canceled':
      handleSetResultCanceled(value);
      break;
    case 'resetTestResult':
      handelResetTestResults(value);
      break;
    case 'retestResult':
      handleReTest(value);
      break;
    case 'resetReviewResult':
      handleResetReviewResult(value);
      break;
    case 'addFavourite':
      handleFavourite(value);
      break;
    case 'addFollow':
      handleFollow(value);
      break;
  }
};

// 收藏
const favouriteLoading = ref(false);
const handleFavourite = async (rowData: CaseDetail) => {
  if (favouriteLoading.value) {
    return;
  }
  favouriteLoading.value = true;
  const [error] = rowData.favourite
    ? await funcCase.cancelFavouriteCase(rowData.id)
    : await funcCase.AddFavouriteCase(rowData.id);
  favouriteLoading.value = false;
  if (error) {
    return;
  }
  notification.success(rowData.favourite
    ? t('functionCase.cancelFavouriteSuccess')
    : t('functionCase.favouriteSuccess'));
  rowData.favourite = !rowData.favourite;
  updateFollowFavourite('addFavourite');
};

// 关注
const followLoading = ref(false);
const handleFollow = async (rowData: CaseDetail) => {
  if (followLoading.value) {
    return;
  }
  followLoading.value = true;
  const [error] = rowData.follow
    ? await funcCase.cancelFollowCase(rowData.id)
    : await funcCase.addFollowCase(rowData.id);
  favouriteLoading.value = false;
  followLoading.value = false;
  if (error) {
    return;
  }

  notification.success(rowData.follow
    ? t('functionCase.cancelFollowSuccess')
    : t('functionCase.followSuccess'));
  rowData.follow = !rowData.follow;
  updateFollowFavourite('addFollow');
};

// 编辑用例
const editCaseVisible = ref(false);
const isDebug = ref(false);
const editCase = ref<CaseDetail>();
const handleEdit = (rowData: CaseDetail) => {
  editCase.value = rowData;
  isDebug.value = false;
  editCaseVisible.value = true;
};

// 编辑或者添加用例成功更新列表
const addOrEditSuccess = () => {
  getCaseInfo();
};

const caseInfoLoading = ref(false);
const getCaseInfo = async () => {
  browserTabRef.value.update({ ...currTabInfo.value, notify: currTabInfo.value.notify + 1 });
};

const handleClone = async (rowData?: CaseDetail) => {
  if (caseInfoLoading.value) {
    return;
  }
  const ids = [rowData.id];
  caseInfoLoading.value = true;
  const [error] = await funcCase.cloneCase(ids);
  caseInfoLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.cloneSuccess'));
};

const caseReviewVisible = ref(false);
const caseMoveVisible = ref(false);
const caseUpdateTestResultVisible = ref(false);
const resultPassed = ref(false);
const handleAction = (action: 'review' | 'move' | 'updateTestResult_passed'| 'updateTestResult_notPassed') => {
  switch (action) {
    case 'review':
      caseReviewVisible.value = true;
      break;
    case 'move':
      caseMoveVisible.value = true;
      break;
    case 'updateTestResult_passed':
      caseUpdateTestResultVisible.value = true;
      resultPassed.value = true;
      break;
    case 'updateTestResult_notPassed':
      caseUpdateTestResultVisible.value = true;
      resultPassed.value = false;
      break;
  }
};

const updateCaseSuccess = () => {
  getCaseInfo();
};

// 设为阻塞中
const handleSetResultBlocked = async (value) => {
  const params = [
    {
      id: value.id,
      testResult: CaseTestResult.BLOCKED
    }
  ];
  const [error] = await funcCase.updateCaseResult(params);
  if (error) {
    return;
  }
  notification.success(t('functionCase.caseSetBlocked'));
  await getCaseInfo();
};

// 取消case
const handleSetResultCanceled = async (value) => {
  const params = [
    {
      id: value.id,
      testResult: CaseTestResult.CANCELED
    }
  ];
  const [error] = await funcCase.updateCaseResult(params);
  if (error) {
    return;
  }
  notification.success(t('functionCase.cancelSuccess'));
  await getCaseInfo();
};

// 重置测试
const handelResetTestResults = async (rowData: CaseDetail) => {
  if (caseInfoLoading.value) {
    return;
  }
  caseInfoLoading.value = true;
  const [error] = await funcCase.resetCaseResult([rowData.id]);
  caseInfoLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('functionCase.resetTestResultSuccess'));
  await getCaseInfo();
};

// 重置评审
const handleResetReviewResult = async (rowData: CaseDetail) => {
  if (caseInfoLoading.value) {
    return;
  }
  caseInfoLoading.value = true;
  const [error] = await funcCase.resetReviewCase([rowData.id]);
  caseInfoLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('functionCase.resetReviewSuccess'));
  await getCaseInfo();
};

// 重新测试 将用例改为待测试
const handleReTest = async (rowData: CaseDetail) => {
  if (caseInfoLoading.value) {
    return;
  }
  const [error] = await funcCase.retestResult([rowData.id]);
  if (error) {
    return;
  }
  notification.success(t('functionCase.resetTestStatusSuccess'));
  await getCaseInfo();
};

// 删除用例
const handleDeleteCase = async (rowData?: CaseDetail) => {
  if (caseInfoLoading.value) {
    return;
  }
  modal.confirm({
    centered: true,
    title: t('functionCase.deleteCase'),
    content: rowData
      ? t('functionCase.confirmDeleteCase', { name: rowData.name })
      : t('functionCase.confirmDeleteSelectedCases'),
    async onOk () {
      await delCase(rowData);
    }
  });
};

const delCase = async (rowData?: CaseDetail) => {
  caseInfoLoading.value = true;
  const [error] = await funcCase.deleteCase([rowData.id]);
  caseInfoLoading.value = false;
  if (error) {
    return;
  }

  deleteTabPane([currTabInfo.value._id]);
  notification.success(t('actions.tips.deleteSuccess'));
};

const updateTabPane = (data) => {
  browserTabRef.value.update(data);
};

const setCacheParams = (queryParams, key: string) => {
  browserTabRef.value.update({ key, queryParams });
};

const handleMoveSuccess = (oldPlanId: string, newPlanId: string) => {
  if (typeof browserTabRef.value?.update === 'function') {
    const tabData = browserTabRef.value.getData();
    for (let i = 0; i < tabData.length; i++) {
      const tabInfo = tabData[i];
      if (tabInfo.type === 'caseList' || tabInfo.key.includes(oldPlanId) || tabInfo.key.includes(newPlanId)) {
        browserTabRef.value.update({ ...tabInfo, notify: tabInfo.notify++ });
      }
    }
  }
};

const handleViewCaseInfo = async (value) => {
  addTabPane({
    _id: 'case' + value.caseId,
    name: value.caseName,
    type: 'caseInfo',
    source: 'list',
    closable: true,
    caseId: value.caseId,
    queryParams: value.queryParams,
    notify: 0
  });
};

const setCaseListPlanParam = () => {
  browserTabRef.value.open('case_home');
  if (caseListRef.value) {
    caseListRef.value.resetParam();
  }
};

onMounted(() => {
  watch(() => browserTabRef.value, () => {
    if (typeof browserTabRef.value?.update === 'function') {
      const tabData = browserTabRef.value.getData().map(item => item.type);
      if (!tabData.includes('caseList')) {
        addTabPane({
          _id: 'case_home',
          name: t('functionCase.title'),
          type: 'caseList',
          closable: false,
          icon: 'icon-zhuye',
          notify: 0
        });
      } else {
        updateTabPane({
          _id: 'case_home',
          name: t('functionCase.title'),
          type: 'caseList',
          closable: false,
          icon: 'icon-zhuye',
          notify: 0
        });
      }
    }
  }, { immediate: true });

  watch(() => route.fullPath, () => {
    if (!route.fullPath.includes('/function#cases')) {
      return;
    }
    const fullPath = decodeURI(route.fullPath);
    const queryStr = fullPath.split('?')[1];
    if (queryStr) {
      const result:{[key:string]: string} = {};
      const queryParams = queryStr.split('&');
      queryParams.forEach(query => {
        const keyValue = query.split('=');
        result[keyValue[0]] = keyValue[1];
      });
      if (result.id) {
        addTabPane({
          _id: 'case' + result.id,
          name: result.name,
          type: 'caseInfo',
          projectId: result.projectId,
          closable: true,
          caseId: result.id,
          notify: 0,
          queryParams: restoreFilters(result)
        });
        router.replace('/function#cases');
      }
    }
  }, {
    immediate: true,
    deep: true
  });
});

provide('addTabPane', addTabPane);
provide('updateTabPane', updateTabPane);
provide('deleteTabPane', deleteTabPane);
provide('userInfo', userInfo.value);
provide('appInfo', appInfo.value);

defineExpose({
  addTabPane,
  updateTabPane,
  setCaseListPlanParam
});
</script>
<template>
  <div class="flex flex-1 h-full">
    <BrowserTab
      v-if="projectId"
      ref="browserTabRef"
      :key="`func-browser-tab_${projectId}`"
      :storageKey="`func-browser-tab_${projectId}`"
      hideAdd
      :stepVisible="stepVisible"
      :stepKey="stepKey"
      :stepContent="stepContent"
      :userId="userInfo?.id"
      class="flex-1 h-full"
      @updateGuideStep="tabGuideStep">
      <template #default="record">
        <template v-if="record.type==='caseList'">
          <CaseList
            ref="caseListRef"
            :userInfo="userInfo"
            :notify="record.notify"
            :queryParams="record.queryParams"
            :tabInfo="record"
            @cacheParams="(query) => setCacheParams(query, record.key)"
            @moveSuccess="handleMoveSuccess"
            @openInfo="handleViewCaseInfo" />
        </template>

        <template v-if="record.type==='plan'">
          <CaseList
            :userInfo="userInfo"
            :notify="record.notify"
            :queryParams="record.queryParams"
            :planId="record?.planId"
            :planName="record?.planName"
            :tabInfo="record"
            @cacheParams="(query) => setCacheParams(query, record.key)"
            @moveSuccess="handleMoveSuccess"
            @openInfo="handleViewCaseInfo" />
        </template>

        <template v-if="record.type === 'caseInfo'">
          <div class="relative h-full overflow-auto">
            <Spin
              ref="caseInfoRef"
              :spinning="caseInfoLoading"
              class="overflow-y-auto overflow-x-hidden h-full relative"
              @scroll="checkScroll">
              <CaseInfo
                v-model:loading="caseInfoLoading"
                :tabKey="record._id"
                :caseId="record.caseId"
                :currIndex="record.currIndex"
                :total="record.total"
                :userInfo="userInfo"
                :queryParams="record.queryParams"
                :notify="record.notify"
                type="tab"
                @updateCaseTab="updateCaseTab"
                @updateFollowFavourite="updateFollowFavourite"
                @onClick="(type, value) => handleDetailAction(type, value, record)" />
            </Spin>

            <Tooltip placement="topLeft" :title="t('functionCase.backToTop')">
              <div
                v-if="showAnchor"
                class="absolute right-2 bottom-2 z-999 h-6 w-6  cursor-pointer border border-border-divider rounded-full text-center leading-5"
                @click="scrollToTop">
                <Icon icon="icon-shuangjiantou" class="-rotate-90 text-text-link-hover" />
              </div>
            </Tooltip>
          </div>
        </template>
      </template>
    </BrowserTab>
  </div>

  <AsyncComponent :visible="editCaseVisible">
    <AddCaseModal
      v-model:visible="editCaseVisible"
      :editCase="editCase"
      @update="addOrEditSuccess" />
  </AsyncComponent>

  <AsyncComponent :visible="caseReviewVisible">
    <ReviewModal
      v-model:visible="caseReviewVisible"
      :selectedCase="selectedCase"
      :selectedRowKeys="[selectedCase.id]"
      type="one"
      @update="updateCaseSuccess" />
  </AsyncComponent>

  <AsyncComponent :visible="caseMoveVisible">
    <MoveCaseModal
      v-model:visible="caseMoveVisible"
      :selectedCase="selectedCase"
      type="one"
      @update="updateCaseSuccess" />
  </AsyncComponent>

  <AsyncComponent :visible="caseUpdateTestResultVisible">
    <UpdateTestResultModal
      v-model:visible="caseUpdateTestResultVisible"
      :resultPassed="resultPassed"
      :selectedCase="selectedCase"
      type="one"
      @update="updateCaseSuccess" />
  </AsyncComponent>
</template>
<style scoped>
:deep(.ant-tabs-tab) {
  font-size: 12px;
}

.recycl-tab :deep(.ant-tabs-nav-wrap) {
  padding: 0 14px;
}

:deep(.ant-tree) {
  background-color: transparent;
  font-size: 12px;
}

:deep(.ant-tree .ant-tree-treenode) {
  width: 100%;
  height: 36px;
  padding-left: 0;
  line-height: 20px;
}

:deep(.ant-tree .ant-tree-treenode.ant-tree-treenode-selected) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-treenode:hover) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-switcher) {
  width: 16px;
  height: 34px;
  margin-top: 2px;
  line-height: 34px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-node-content-wrapper) {
  display: flex;
  flex: 1 1 0%;
  flex-direction: column;
  justify-content: center;
  height: 36px;
  margin: 0;
  padding: 0;
  line-height: 36px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper .ant-tree-iconEle) {
  height: 36px;
  line-height: 36px;
  vertical-align: initial;
}

:deep(.ant-tree .ant-tree-node-selected) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-indent-unit) {
  width: 18px;
}
</style>
