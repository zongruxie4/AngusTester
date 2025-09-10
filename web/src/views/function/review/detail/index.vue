<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Checkbox, Popover, TabPane, Tabs, Tag } from 'ant-design-vue';
import {
  AsyncComponent,
  Icon,
  Image,
  Input,
  modal,
  notification,
  ReviewStatus,
  Spin,
  Table,
  TaskPriority
} from '@xcan-angus/vue-ui';
import { download, duration } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import RichEditor from '@/components/richEditor/index.vue';
import { funcPlan, func } from '@/api/tester';
import SelectEnum from '@/components/enum/SelectEnum.vue';

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

const SelectCaseModal = defineAsyncComponent(() => import('@/views/function/review/edit/SelectCaseModal.vue'));
const ReviewForm = defineAsyncComponent(() => import('@/views/function/review/detail/ReviewForm.vue'));
const CaseReviewResult = defineAsyncComponent(() => import('@/views/function/review/components/CaseReviewResult.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/case/CaseSteps.vue'));
const CaseBasicInfo = defineAsyncComponent(() => import('@/views/function/review/components/CaseBasicInfo.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/function/review/components/Precondition.vue'));
const Members = defineAsyncComponent(() => import('@/views/function/review/components/Member.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/function/review/components/TestInfo.vue'));
const Attachment = defineAsyncComponent(() => import('@/views/function/review/components/Attachment.vue'));
const AssocTasks = defineAsyncComponent(() => import('@/views/function/review/components/AssocTask.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/function/review/components/AssocCase.vue'));
const Description = defineAsyncComponent(() => import('@/views/function/review/components/Description.vue'));

const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const isAdmin = inject('isAdmin', ref(false));
const selectModalVisible = ref(false);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 1
});
const drawerRef = ref();
const keywords = ref();
const reviewStatus = ref();
const permissions = ref<string[]>([]);
const dataSource = ref();
const caseList = ref([]);

const reviewId = ref(); // reviewId

const selectedRowKey = ref<string|undefined>();
const selectCaseInfo = ref();
const selectCaseIds = ref<string[]>([]);

const startLoading = ref(false);

const startReview = async () => {
  startLoading.value = true;
  const [error] = await func.startReview(reviewId.value);
  startLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('caseReview.detail.reviewStartedSuccess'));
  loadData(reviewId.value);
};

const columns = [
  {
    title: ' ',
    dataIndex: 'checkbox',
    width: 32
  },
  {
    title: t('caseReview.detail.caseCode'),
    dataIndex: 'caseInfo',
    customRender: ({ text }) => {
      return text?.code;
    },
    width: 160
  },
  {
    title: t('caseReview.detail.caseName'),
    dataIndex: 'caseInfo',
    ellipsis: true,
    customRender: ({ text }) => {
      return text?.name;
    },
    customCell: () => {
      return { style: 'white-space: nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.version'),
    dataIndex: 'caseInfo',
    customRender: ({ text }) => {
      return text?.version ? `v${text.version}` : '--';
    },
    width: 90
  },
  {
    title: t('caseReview.detail.priority'),
    dataIndex: 'priority',
    customRender: ({ text }):string => text?.message,
    width: 80,
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.reviewStatus'),
    dataIndex: 'reviewStatus',
    customRender: ({ text }):string => text?.message,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.creator'),
    dataIndex: 'createdByName',
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('caseReview.detail.action'),
    dataIndex: 'action',
    width: 320
  }
];

const loadPermissions = async (id: string) => {
  if (isAdmin.value) {
    permissions.value = [
      'MODIFY_PLAN',
      'DELETE_PLAN',
      'ADD_CASE',
      'MODIFY_CASE',
      'DELETE_CASE',
      'EXPORT_CASE',
      'REVIEW',
      'RESET_REVIEW_RESULT',
      'TEST',
      'RESET_TEST_RESULT',
      'GRANT',
      'VIEW'
    ];

    return;
  }

  const params = {
    admin: true
  };
  loading.value = true;
  const [error, res] = await funcPlan.getCurrentAuthByPlanId(id, params);
  loading.value = false;
  if (error) {
    return;
  }

  permissions.value = (res?.data?.permissions || []).map(item => item.value);
};

const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await func.getReviewDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data;
  if (!data) {
    return;
  }
  await loadPermissions(data.planId);

  dataSource.value = data;
  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id + '-case' });
  }
};

const loadCaseList = async (id: string) => {
  const { current, pageSize } = pagination.value;
  const [error, { data }] = await func.getReviewCaseList({
    reviewId: id,
    filters: keywords.value ? [{ value: keywords.value, key: 'caseName', op: 'MATCH_END' }] : [],
    reviewStatus: reviewStatus.value,
    pageNo: current,
    pageSize
  });

  if (error) {
    return;
  }
  caseList.value = data?.list || [];
  pagination.value.total = +data.total || 0;
  selectCaseIds.value = [];
};

const changePage = ({ current, pageSize }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  selectedRowKey.value = undefined;
  loadCaseList(reviewId.value);
};

const addReviewCase = () => {
  selectModalVisible.value = true;
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
    reviewId.value = id;
    loadData(id);
    loadCaseList(id);
  }, { immediate: true });
});

const customRow = (record) => {
  return {
    onClick: () => {
      if (record.id === selectedRowKey.value) {
        if (activeMenuKey.value) {
          activeMenuKey.value = undefined;
          drawerRef.value.close();
        }
        selectedRowKey.value = '';
      } else {
        selectedRowKey.value = record.id;
      }
    }
  };
};

const loadCaseContentInfo = async () => {
  const [error, { data }] = await func.getReviewCaseDetail(selectedRowKey.value);
  if (error) {
    return;
  }
  selectCaseInfo.value = {
    ...data,
    caseInfo: data.reviewStatus?.value === 'PENDING' ? data.latestCase : data.reviewedCase
  };
};

watch(() => selectedRowKey.value, newValue => {
  if (newValue) {
    loadCaseContentInfo();
  }
});

const onCloseDrawer = () => {
  selectedRowKey.value = '';
};

const onCheckedChange = (e, id: string) => {
  e.stopPropagation();
  if (e.target.checked) {
    selectCaseIds.value.push(id);
  } else {
    selectCaseIds.value = selectCaseIds.value.filter(i => i !== id);
  }
};

const handleKeywordChange = debounce(duration.search, () => {
  pagination.value.current = 1;
  loadCaseList(reviewId.value);
});

const handleChangeStatus = () => {
  pagination.value.current = 1;
  loadCaseList(reviewId.value);
};

const handleReviewOk = () => {
  if (selectedRowKey.value && selectCaseIds.value.includes(selectedRowKey.value)) {
    loadCaseContentInfo();
  }
  selectCaseIds.value = [];
  loadCaseList(reviewId.value);
  loadData(reviewId.value);
};

const delCase = async (record) => {
  modal.confirm({
    title: t('caseReview.detail.confirmCancelReviewCase', { name: record?.caseInfo?.name || '' }),
    async onOk () {
      const [error] = await func.deleteReviewCase([record.id]);
      if (error) {
        return;
      }
      if (pagination.value.current !== 1 && caseList.value.length === 1) {
        pagination.value.current -= 1;
      }
      loadCaseList(reviewId.value);
    }
  });
};

const restart = async (record) => {
  modal.confirm({
    title: t('caseReview.detail.restartReview'),
    content: t('caseReview.detail.confirmRestartReviewCase', { name: record?.caseInfo?.name || '' }),
    async onOk () {
      const [error] = await func.restartReviewCase([record.id]);
      if (error) {
        return;
      }
      loadCaseList(reviewId.value);
    }
  });
};

const reset = async (record) => {
  modal.confirm({
    title: t('caseReview.detail.resetReview'),
    content: t('caseReview.detail.confirmResetReviewCase', { name: record?.caseInfo?.name || '' }),
    async onOk () {
      const [error] = await func.resetReviewCase([record.id]);
      if (error) {
        return;
      }
      loadCaseList(reviewId.value);
    }
  });
};

const handleAddCase = async (caseIds: string[]) => {
  selectModalVisible.value = false;
  const [error] = await func.addReviewCase({
    caseIds: caseIds,
    reviewId: reviewId.value
  });
  if (error) {
    return;
  }
  loadCaseList(reviewId.value);
  loadData(reviewId.value);
};

const activeMenuKey = ref();
const menuItems = [
  {
    name: t('caseReview.detail.basicInfo'),
    key: 'basic',
    icon: 'icon-jibenxinxi'
  },
  {
    name: t('caseReview.detail.precondition'),
    key: 'precondition',
    icon: 'icon-fuwuqi'
  },
  {
    name: t('caseReview.detail.steps'),
    key: 'steps',
    icon: 'icon-zuxiao'
  },
  {
    name: t('caseReview.detail.reviewInfo'),
    key: 'reviewResult',
    icon: 'icon-pingshen'
  },
  {
    name: t('caseReview.detail.members'),
    key: 'members',
    icon: 'icon-quanburenyuan'
  },
  {
    name: t('caseReview.detail.testInfo'),
    key: 'testInfo',
    icon: 'icon-ceshijieguomingxi'
  },
  {
    name: t('caseReview.detail.assocTasks'),
    key: 'refTasks',
    icon: 'icon-renwu2'
  },
  {
    name: t('caseReview.detail.assocCases'),
    key: 'refUseCases',
    icon: 'icon-yongli1'
  },
  {
    name: t('caseReview.detail.attachments'),
    key: 'attachment',
    icon: 'icon-wenjian1'
  }
];

</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5">
    <div class="flex h-full">
      <div class="flex-1 px-5 py-5 overflow-auto">
        <div class="font-semibold text-4 mb-3">
          {{ dataSource?.name }}
          <Tag class="ml-2 text-white" :class="dataSource?.status?.value">{{ dataSource?.status?.message }}</Tag>
          <Button
            v-if="dataSource?.status?.value === 'PENDING'"
            size="small"
            type="primary"
            ghost
            class="ml-3"
            @click="startReview">
            <Icon icon="icon-kaishi" class="mr-1" />
            {{ t('caseReview.detail.startReview') }}
          </Button>
        </div>
        <div class="flex space-x-12">
          <div class="space-y-1">
            <div class="text-center h-6 leading-6 space-x-3">
              <span>{{ t('caseReview.detail.progress') }}</span>
              <span class="font-semibold text-3.5">{{ dataSource?.progress?.completed || 0 }} / {{ dataSource?.progress?.total || 0 }}</span>
            </div>
            <div class="w-30 flex h-2 rounded bg-gray-3">
              <div class="text-center bg-status-success rounded" :style="{width: `${dataSource?.progress?.completedRate || 0}%`}">
              </div>
            </div>
            <div class="text-center text-4 font-medium">
              {{ dataSource?.progress?.completedRate || 0 }} %
            </div>
          </div>
          <div class="flex-1">
            <div class="flex leading-8 h-8">
              <div class="inline-flex flex-1 pr-2">
                <label class="w-16">{{ t('caseReview.detail.testPlan') }}：</label>
                <div>{{ dataSource?.planName }}</div>
              </div>
              <div class="inline-flex flex-1 pr-2">
                <label class="w-16">{{ t('caseReview.detail.owner') }}：</label>
                <div>{{ dataSource?.ownerName }}</div>
              </div>
            </div>
            <div class="flex leading-8 h-8">
              <div class="inline-flex flex-1 pr-2">
                <label class="w-16">{{ t('caseReview.detail.attachments') }}：</label>
                <div class="flex space-x-2 flex-1">
                  <a
                    v-for="file in (dataSource?.attachments || [])"
                    class="text-blue-hover"
                    @click="download(file.url)">
                    {{ file.name }}
                  </a>
                  <div v-if="!dataSource?.attachments?.length">{{ t('caseReview.detail.noAttachments') }}</div>
                </div>
              </div>
              <div class="inline-flex flex-1 pr-2">
                <label class="w-16">{{ t('caseReview.detail.participants') }}：</label>
                <div class="flex space-x-2 flex-wrap flex-1 items-center">
                  <div
                    v-for="person in (dataSource?.participants || []).slice(0, 5)"
                    :key="person.id"
                    class="inline-flex space-x-1 items-center">
                    <Image
                      type="avatar"
                      :src="person.avatar"
                      class="w-4 h-4 rounded-full" />
                    <span>{{ person.fullName }}</span>
                  </div>

                  <Popover placement="bottom">
                    <template #content>
                      <div class="max-w-100">
                        <div
                          v-for="person in (dataSource?.participants || []).slice(5)"
                          :key="person.id"
                          class="inline-flex space-x-1 items-center">
                          <Image
                            type="avatar"
                            :src="person.avatar"
                            class="w-4 h-4 rounded-full" />
                          <span>{{ person.fullName }}</span>
                        </div>
                      </div>
                    </template>
                    <Icon
                      v-if="(dataSource?.participants || []).length > 5"
                      icon="icon-gengduo"
                      class="text-blue-1" />
                  </Popover>
                </div>
              </div>
            </div>
          </div>
        </div>

        <Tabs size="small" class="mt-5">
          <TabPane key="testerResponsibilities" :tab="t('caseReview.detail.testCases')">
            <div class="flex mb-3">
              <Input
                v-model:value="keywords"
                :placeholder="t('caseReview.detail.enterQueryName')"
                class="w-50"
                @change="handleKeywordChange" />
              <SelectEnum
                v-model:value="reviewStatus"
                class="w-50 ml-2"
                :placeholder="t('caseReview.detail.selectReviewStatus')"
                enumKey="ReviewStatus"
                :allowClear="true"
                @change="handleChangeStatus">
                <template #option="item">
                  <ReviewStatus :value="item" />
                </template>
              </SelectEnum>
              <div class="flex-1"></div>
              <Button
                :disabled="!dataSource?.planId || !permissions.includes('REVIEW') || dataSource?.status?.value === 'COMPLETED'"
                size="small"
                type="primary"
                @click="addReviewCase">
                <Icon icon="icon-jia" class="mr-1" />
                {{ t('caseReview.detail.addReviewCase') }}
              </Button>
            </div>
            <Table
              size="small"
              :columns="columns"
              :dataSource="caseList"
              :customRow="customRow"
              :rowClassName="(record) => record.id === selectedRowKey ? 'ant-table-row-selected' : ''"
              :pagination="pagination"
              noDataSize="small"
              @change="changePage">
              <template #bodyCell="{record, column}">
                <template v-if="column.dataIndex === 'checkbox'">
                  <Checkbox
                    :disabled="record.reviewStatus?.value !== 'PENDING' || !permissions.includes('REVIEW') || dataSource?.status?.value === 'PENDING'"
                    :checked="selectCaseIds.includes(record.id)"
                    @click="onCheckedChange($event, record.id)">
                  </Checkbox>
                </template>
                <template v-if="column.dataIndex === 'reviewStatus'">
                  <ReviewStatus :value="record.reviewStatus" />
                </template>
                <template v-if="column.dataIndex === 'priority'">
                  <TaskPriority :value="record?.caseInfo?.priority" />
                </template>
                <template v-if="column.dataIndex === 'action'">
                  <Button
                    type="text"
                    size="small"
                    :disabled="!permissions.includes('REVIEW')"
                    @click.stop="delCase(record)">
                    <Icon icon="icon-qingchu" class="mr-1" />
                    {{ t('caseReview.detail.cancel') }}
                  </Button>
                  <Button
                    :disabled="!permissions.includes('REVIEW') || record.reviewStatus?.value === 'PENDING' || dataSource?.status?.value === 'PENDING'"
                    type="text"
                    size="small"
                    @click.stop="restart(record)">
                    <Icon icon="icon-zhongxinkaishi" class="mr-1" />
                    {{ t('caseReview.detail.restartNewReview') }}
                  </Button>
                  <Button
                    :disabled="!permissions.includes('REVIEW') || record.reviewStatus?.value === 'PENDING' || dataSource?.status?.value === 'PENDING'"
                    type="text"
                    size="small"
                    @click.stop="reset(record)">
                    <Icon icon="icon-zhongzhipingshenjieguo" class="mr-1" />
                    {{ t('caseReview.detail.resetReview') }}
                  </Button>
                  <RouterLink :to="`/function#cases?id=${record.caseId}`">
                    <Button
                      type="text"
                      size="small"
                      @click.stop>
                      <Icon icon="icon-chakanhuodong" class="mr-1" />
                      {{ t('caseReview.detail.view') }}
                    </Button>
                  </RouterLink>
                </template>
              </template>
            </Table>

            <template v-if="selectCaseIds.length">
              <div class="font-semibold text-3.5 mt-5">
                {{ t('caseReview.detail.reviewInfoSelected') }}
                <span class="text-sub-content">{{ t('caseReview.detail.selectedCount', { count: selectCaseIds.length }) }}</span>
              </div>
              <ReviewForm
                class="mt-5 w-100"
                :selectedRowKeys="selectCaseIds"
                @update="handleReviewOk" />
            </template>
          </TabPane>
          <TabPane key="description" :tab="t('caseReview.detail.reviewDescription')">
            <div v-if="dataSource?.description" class="">
              <RichEditor
                :value="dataSource?.description"
                mode="view" />
            </div>
            <div v-else class="text-sub-content">
              {{ t('caseReview.detail.noDescription') }}
            </div>
          </TabPane>
        </Tabs>
      </div>
      <div class="flex flex-col transition-all" :class="{'w-0': !selectedRowKey, 'w-100 border-l p-2': !!selectedRowKey}">
        <div>
          <Icon
            icon="icon-shanchuguanbi"
            class="text-5 cursor-pointer"
            @click="onCloseDrawer" />
        </div>

        <div class="flex-1 overflow-auto p-4">
          <CaseBasicInfo
            class="pb-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <Precondition
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <div class="font-semibold text-3.5">
            {{ t('caseReview.detail.testSteps') }}
          </div>

          <CaseStep
            class="pb-5 pt-3"
            :defaultValue="selectCaseInfo?.caseInfo?.steps || []"
            :stepView="selectCaseInfo?.caseInfo?.stepView?.value"
            :readonly="true" />

          <CaseReviewResult
            class="py-5"
            :caseInfo="selectCaseInfo" />

          <Description
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <Members
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <TestInfo
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <AssocTasks
            class="py-5"
            :dataSource="selectCaseInfo?.caseInfo?.refTaskInfos"
            :projectId="props.projectId"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <AssocCases
            class="py-5"
            :dataSource="selectCaseInfo?.caseInfo?.refCaseInfos"
            :projectId="props.projectId"
            :caseInfo="selectCaseInfo?.caseInfo" />

          <Attachment
            class="py-5"
            :caseInfo="selectCaseInfo?.caseInfo" />
        </div>
      </div>
    </div>
    <AsyncComponent :visible="selectModalVisible">
      <SelectCaseModal
        v-model:visible="selectModalVisible"
        :planId="dataSource?.planId"
        :reviewId="reviewId"
        :projectId="props.projectId"
        @ok="handleAddCase" />
    </AsyncComponent>
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
