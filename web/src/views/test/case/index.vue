<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, provide, ref, watch, Ref } from 'vue';
import { Tooltip } from 'ant-design-vue';
import { AsyncComponent, BrowserTab, Icon, modal, notification, Spin, VuexHelper } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';
import { useRoute, useRouter } from 'vue-router';
import { testCase } from '@/api/tester';
import { CaseTestResult } from '@/enums/enums';
import { TestMenuKey } from '@/views/test/menu';
import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';

const { t } = useI18n();

// Type definitions
export type CaseActionAuth = 'edit'| 'debug' | 'review' | 'clone' | 'move' | 'delete' | 'updateTestResult' | 'updateTestResult_passed' | 'updateTestResult_notPassed' | 'updateTestResult_blocked' | 'updateTestResult_canceled' | 'resetTestResult' | 'resetReviewResult' | 'retestResult' | 'copy' | 'addFavourite' | 'addFollow' | 'cancelFollow' | 'cancelFavourite' | 'copyUrl'

// Component imports
const CaseList = defineAsyncComponent(() => import('@/views/test/case/list/index.vue'));
const CaseInfo = defineAsyncComponent(() => import('@/views/test/case/detail/index.vue'));
const AddCaseModal = defineAsyncComponent(() => import('@/views/test/case/list/Edit.vue'));
const ReviewModal = defineAsyncComponent(() => import('@/views/test/case/list/Review.vue'));
const MoveCaseModal = defineAsyncComponent(() => import('@/views/test/case/list/Move.vue'));
const UpdateTestResultModal = defineAsyncComponent(() => import('@/views/test/case/list/UpdateResult.vue'));

// Basic state management
const userInfo = ref(appContext.getUser());
const projectId = inject<Ref<string>>('projectId', ref(''));
const appInfo = ref(appContext.getAccessApp());

enum TabKey {
  caseList = "caseList",
  plan = 'plan',
  caseInfo = 'caseInfo'
}

const route = useRoute();
const router = useRouter();
const browserTabRef = ref();
const caseListRef = ref();
// Tab management functions
const addTabPane = (tabData) => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add(() => {
      return { ...tabData };
    });
  }
};

const deleteTabPane = (tabKeys: string[]) => {
  tabKeys.forEach(key => browserTabRef.value.remove(key));
};

/**
 * Restore filter parameters from URL query string
 * @param data - Query parameters object
 * @returns Object with filters array and other parameters
 */
const restoreFilters = (data) => {
  const filters: Array<{ key: string; op: string; value: string }> = [];

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

// Tab update functions
const updateCaseTab = async (tabValue) => {
  browserTabRef.value.update(tabValue);
};

// Follow and favorite notification management
const favoriteFollowRef = ref<{ followNotify: number; favouriteNotify: number } | null>(null);
const updateFollowFavourite = (actionType: 'addFollow' | 'addFavourite') => {
  if (!favoriteFollowRef.value) {
    return;
  }
  if (actionType === 'addFollow') {
    favoriteFollowRef.value.followNotify++;
    return;
  }

  favoriteFollowRef.value.favouriteNotify++;
};

// Scroll management for case info panel
const caseInfoRef = ref<HTMLElement | null>(null);
const scrollNotify = ref(0);
const showAnchor = ref(false);

/**
 * Check scroll position and show/hide back to top button
 * @param event - Scroll event
 */
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

// Case detail action handlers
const currentTabInfo = ref();
const selectedCase = ref();

/**
 * Handle case detail actions based on action type
 * @param actionType - Type of action to perform
 * @param caseData - Case data object
 * @param tabInfo - Tab information
 */
const handleDetailAction = (actionType: CaseActionAuth, caseData: CaseDetail, tabInfo) => {
  currentTabInfo.value = tabInfo;
  selectedCase.value = caseData;
  switch (actionType) {
    case 'edit':
      handleEdit(caseData);
      break;
    case 'clone':
      handleClone(caseData);
      break;
    case 'move':
      handleAction('move');
      break;
    case 'delete':
      handleDeleteCase(caseData);
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
      handleSetResultBlocked(caseData);
      break;
    case 'updateTestResult_canceled':
      handleSetResultCanceled(caseData);
      break;
    case 'resetTestResult':
      handleResetTestResults(caseData);
      break;
    case 'retestResult':
      handleReTest(caseData);
      break;
    case 'resetReviewResult':
      handleResetReviewResult(caseData);
      break;
    case 'addFavourite':
      handleFavourite(caseData);
      break;
    case 'addFollow':
      handleFollow(caseData);
      break;
  }
};

// Favorite functionality
const favouriteLoading = ref(false);

/**
 * Toggle case favorite status
 * @param caseData - Case data object
 */
const handleFavourite = async (caseData: CaseDetail) => {
  if (favouriteLoading.value) {
    return;
  }
  favouriteLoading.value = true;
  const [error] = caseData.favourite
    ? await testCase.cancelFavouriteCase(caseData.id)
    : await testCase.AddFavouriteCase(caseData.id);
  favouriteLoading.value = false;
  if (error) {
    return;
  }
  notification.success(caseData.favourite
    ? t('actions.tips.cancelFavouriteSuccess')
    : t('actions.tips.favouriteSuccess'));
  caseData.favourite = !caseData.favourite;
  updateFollowFavourite('addFavourite');
};

// Follow functionality
const followLoading = ref(false);

/**
 * Toggle case follow status
 * @param caseData - Case data object
 */
const handleFollow = async (caseData: CaseDetail) => {
  if (followLoading.value) {
    return;
  }
  followLoading.value = true;
  const [error] = caseData.follow
    ? await testCase.cancelFollowCase(caseData.id)
    : await testCase.addFollowCase(caseData.id);
  favouriteLoading.value = false;
  followLoading.value = false;
  if (error) {
    return;
  }

  notification.success(caseData.follow
    ? t('actions.tips.cancelFollowSuccess')
    : t('actions.tips.followSuccess'));
  caseData.follow = !caseData.follow;
  updateFollowFavourite('addFollow');
};

// Case editing functionality
const editCaseVisible = ref(false);
const isDebug = ref(false);
const editCase = ref<CaseDetail & { checked: boolean }>();

const handleEdit = (caseData: CaseDetail) => {
  editCase.value = { ...caseData, checked: false };
  isDebug.value = false;
  editCaseVisible.value = true;
};

const addOrEditSuccess = () => {
  getCaseInfo();
};

const caseInfoLoading = ref(false);
const getCaseInfo = async () => {
  browserTabRef.value.update({ ...currentTabInfo.value, notify: currentTabInfo.value.notify + 1 });
};

/**
 * Clone a test case
 * @param caseData - Case data to clone
 */
const handleClone = async (caseData?: CaseDetail) => {
  if (caseInfoLoading.value || !caseData) {
    return;
  }
  const caseIds = [caseData.id];
  caseInfoLoading.value = true;
  const [error] = await testCase.cloneCase(caseIds);
  caseInfoLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.cloneSuccess'));
};

// Modal visibility states
const caseReviewVisible = ref(false);
const caseMoveVisible = ref(false);
const caseUpdateTestResultVisible = ref(false);
const resultPassed = ref(false);

/**
 * Handle modal actions for case operations
 * @param action - Action type to perform
 */
const handleAction = (action: 'review' | 'move' | 'updateTestResult_passed'| 'updateTestResult_notPassed') => {
  switch (action) {
    case 'review':
      // caseReviewVisible.value = true;
      router.push(`/test#reviews`);
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

/**
 * Set case test result to blocked
 * @param caseData - Case data object
 */
const handleSetResultBlocked = async (caseData) => {
  const updateParams = [
    {
      id: caseData.id,
      testResult: CaseTestResult.BLOCKED
    }
  ];
  const [error] = await testCase.updateCaseResult(updateParams);
  if (error) {
    return;
  }
  notification.success(t('testCase.messages.caseSetBlocked'));
  await getCaseInfo();
};

/**
 * Set case test result to canceled
 * @param caseData - Case data object
 */
const handleSetResultCanceled = async (caseData) => {
  const updateParams = [
    {
      id: caseData.id,
      testResult: CaseTestResult.CANCELED
    }
  ];
  const [error] = await testCase.updateCaseResult(updateParams);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.cancelSuccess'));
  await getCaseInfo();
};

/**
 * Reset case test results
 * @param caseData - Case data object
 */
const handleResetTestResults = async (caseData: CaseDetail) => {
  if (caseInfoLoading.value) {
    return;
  }
  caseInfoLoading.value = true;
  const [error] = await testCase.resetCaseResult([caseData.id]);
  caseInfoLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('testCase.messages.resetTestResultSuccess'));
  await getCaseInfo();
};

/**
 * Reset case review results
 * @param caseData - Case data object
 */
const handleResetReviewResult = async (caseData: CaseDetail) => {
  if (caseInfoLoading.value) {
    return;
  }
  caseInfoLoading.value = true;
  const [error] = await testCase.resetReviewCase([caseData.id]);
  caseInfoLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('testCase.messages.resetReviewSuccess'));
  await getCaseInfo();
};

/**
 * Retest case - change case status to pending test
 * @param caseData - Case data object
 */
const handleReTest = async (caseData: CaseDetail) => {
  if (caseInfoLoading.value) {
    return;
  }
  const [error] = await testCase.retestResult([caseData.id]);
  if (error) {
    return;
  }
  notification.success(t('testCase.messages.resetTestStatusSuccess'));
  await getCaseInfo();
};

/**
 * Show confirmation dialog and delete case
 * @param caseData - Case data to delete
 */
const handleDeleteCase = async (caseData?: CaseDetail) => {
  if (caseInfoLoading.value) {
    return;
  }
  modal.confirm({
    centered: true,
    title: t('actions.delete'),
    content: caseData
      ? t('actions.tips.confirmDelete', { name: caseData.name })
      : t('actions.tips.confirmDataDelete'),
    async onOk () {
      await deleteCase(caseData);
    }
  });
};

/**
 * Execute case deletion
 * @param caseData - Case data to delete
 */
const deleteCase = async (caseData?: CaseDetail) => {
  if (!caseData) {
    return;
  }
  caseInfoLoading.value = true;
  const [error] = await testCase.deleteCase([caseData.id]);
  caseInfoLoading.value = false;
  if (error) {
    return;
  }

  if (currentTabInfo.value?._id) {
    deleteTabPane([currentTabInfo.value._id]);
  }
  notification.success(t('actions.tips.deleteSuccess'));
};

// Tab and cache management
const updateTabPane = (tabData) => {
  browserTabRef.value.update(tabData);
};

const setCacheParams = (queryParams, tabKey: string) => {
  browserTabRef.value.update({ key: tabKey, queryParams });
};

/**
 * Handle successful case move operation
 * @param oldPlanId - Original plan ID
 * @param newPlanId - New plan ID
 */
const handleMoveSuccess = (oldPlanId: string, newPlanId: string) => {
  if (typeof browserTabRef.value?.update === 'function') {
    const tabData = browserTabRef.value.getData();
    for (let i = 0; i < tabData.length; i++) {
      const tabInfo = tabData[i];
      if (tabInfo.type === TabKey.caseList || tabInfo.key.includes(oldPlanId) || tabInfo.key.includes(newPlanId)) {
        browserTabRef.value.update({ ...tabInfo, notify: tabInfo.notify++ });
      }
    }
  }
};

/**
 * Open case info in new tab
 * @param caseInfo - Case information object
 */
const handleViewCaseInfo = async (caseInfo) => {
  addTabPane({
    _id: 'case' + caseInfo.caseId,
    name: caseInfo.caseName,
    type: 'caseInfo',
    source: 'list',
    closable: true,
    caseId: caseInfo.caseId,
    queryParams: caseInfo.queryParams,
    notify: 0
  });
};

/**
 * Reset case list parameters and open home tab
 */
const setCaseListPlanParam = () => {
  browserTabRef.value.open('case_home');
  if (caseListRef.value) {
    caseListRef.value.resetParam();
  }
};

// Component lifecycle and watchers
onMounted(() => {
  // Watch for browser tab changes and ensure case list tab exists
  watch(() => browserTabRef.value, () => {
    if (typeof browserTabRef.value?.update === 'function') {
      const tabData = browserTabRef.value.getData()
      const tabDataTypes = tabData.map(item => item.type);
      const delTab = tabData.filter(item => !TabKey[item.type]).map(i => i._id);
      deleteTabPane(delTab);
      if (!tabDataTypes.includes(TabKey.caseList)) {
        addTabPane({
          _id: 'case_home',
          name: t('testCase.title'),
          type: TabKey.caseList,
          closable: false,
          icon: 'icon-zhuye',
          notify: 0
        });
      } else {
        updateTabPane({
          _id: 'case_home',
          name: t('testCase.title'),
          type: TabKey.caseList,
          closable: false,
          icon: 'icon-zhuye',
          notify: 0
        });
      }
    }
  }, { immediate: true });

  // Watch for route changes and handle case info opening from URL
  watch(() => route.fullPath, () => {
    if (!route.fullPath.includes(`/test#${TestMenuKey.CASES}`)) {
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
          type: TabKey.caseInfo,
          projectId: result.projectId,
          closable: true,
          caseId: result.id,
          notify: 0,
          queryParams: restoreFilters(result)
        });
        router.replace(`/test#${TestMenuKey.CASES}`);
      }
    }
  }, {
    immediate: true,
    deep: true
  });
});

// Provide functions to child components
provide('addTabPane', addTabPane);
provide('updateTabPane', updateTabPane);
provide('deleteTabPane', deleteTabPane);
provide('userInfo', userInfo.value);
provide('appInfo', appInfo.value);

// Expose functions to parent components
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
      :userId="userInfo?.id?.toString()"
      class="flex-1 h-full">
      <template #default="record">
        <template v-if="record.type===TabKey.caseList">
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

        <template v-if="record.type===TabKey.plan">
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

        <template v-if="record.type === TabKey.caseInfo">
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
                :userInfo="{ id: userInfo?.id }"
                :queryParams="record.queryParams"
                :notify="record.notify"
                type="tab"
                @updateCaseTab="updateCaseTab"
                @updateFollowFavourite="updateFollowFavourite"
                @onClick="(type, value) => handleDetailAction(type, value, record)" />
            </Spin>

            <Tooltip placement="topLeft" :title="t('testCase.actions.backToTop')">
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
