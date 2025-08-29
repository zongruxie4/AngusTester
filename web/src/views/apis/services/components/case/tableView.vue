<script lang="ts" setup>
import { defineAsyncComponent, inject, ref, watch } from 'vue';
import {
  AsyncComponent,
  Icon,
  Input,
  notification,
  Table
} from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { Case } from './type';
import { apis } from '@/api/tester';

interface Props {
  id: string; // apiId
  serviceId: string;
}
const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  serviceId: ''
});
const { t } = useI18n();

const AddCaseModal = defineAsyncComponent(() => import('@/views/apis/services/components/case/addModal/index.vue'));
const ExecCaseModal = defineAsyncComponent(() => import('@/views/apis/services/components/case/exec/index.vue'));

const projectInfo = inject('projectInfo', ref());
const keywords = ref();
const addCaseVisible = ref(false);
const execCaseVisible = ref(false);
const loading = ref(false);

const pagination = ref({
  total: 0,
  current: 1,
  pageSize: 10
});

const columns = [
  {
    dataIndex: 'id',
    title: 'ID',
    ellipsis: true,
    width: 160
  },
  {
    dataIndex: 'name',
    title: t('service.case.columns.name'),
    ellipsis: true,
    width: '18%'
  },
  {
    dataIndex: 'status',
    title: t('service.case.columns.status')
  },
  {
    dataIndex: 'type',
    title: t('service.case.columns.type'),
    customRender: ({ text }) => text?.message
  },
  {
    dataIndex: 'enabled',
    title: t('service.case.columns.enabled'),
    customRender: ({ text }) => text ? t('service.case.enabled') : t('service.case.disabled')
  },
  {
    dataIndex: 'description',
    title: t('service.case.columns.description'),
    ellipsis: true,
    width: '18%',
    customRender: ({ text }) => text || '无'
  },
  {
    dataIndex: 'createdByName',
    title: t('service.case.columns.createdByName'),
    groupName: 'create'
  },
  {
    dataIndex: 'createdDate',
    title: t('service.case.columns.createdDate'),
    groupName: 'create',
    hide: true
  },
  {
    dataIndex: 'lastModifiedByName',
    title: t('service.case.columns.lastModifiedByName'),
    groupName: 'modify'
  },
  {
    dataIndex: 'lastModifiedDate',
    title: t('service.case.columns.lastModifiedDate'),
    groupName: 'modify',
    hide: true
  },
  {
    dataIndex: 'action',
    title: t('service.case.columns.action'),
    width: 230
  }
];

const onTablechange = (page) => {
  pagination.value.current = page.current;
  pagination.value.pageSize = page.pageSize;
  loadCaseData();
};

// const priority = ref<{key: string, name: string}[]>([]);

const loadCaseData = async () => {
  const filters = keywords.value ? [{ value: keywords.value, op: 'MATCH_END', key: 'name' }] : [];
  const { current, pageSize } = pagination.value;
  loading.value = true;
  const [error, { data }] = await apis.loadApiCases({ apisId: props.id, projectId: projectInfo.value?.id, pageSize, pageNo: current, filters });
  loading.value = false;
  if (error) {
    return;
  }
  caseData.value = data.list || [];
  pagination.value.total = +data.total || 0;
};

const onKeywordChange = debounce(duration.search, () => {
  loadCaseData();
});

const caseData = ref<Case[]>([]);
const editCaseId = ref();
const isDebug = ref(false);

// 添加用例
const addOrEditCase = (item?: Case) => {
  if (item) {
    editCaseId.value = item.id;
  } else {
    editCaseId.value = undefined;
  }
  addCaseVisible.value = true;
  isDebug.value = false;
};

// 调试
const handleSingleDebug = (item: Case) => {
  if (item) {
    editCaseId.value = item.id;
  } else {
    editCaseId.value = undefined;
  }
  addCaseVisible.value = true;
  isDebug.value = true;
};

const execAll = () => {
  execCaseVisible.value = true;
};

const delLoadingMap = ref({});
// 删除
const delCase = async (item) => {
  delLoadingMap.value[item.id] = true;
  const [error] = await apis.delCase([item.id]);
  delLoadingMap.value[item.id] = false;
  if (error) {
    return;
  }
  notification.success('删除成功');
  const delIdx = caseData.value.findIndex(i => i.id === item.id);
  caseData.value.splice(delIdx, 1);
};

const cloneLoadingMap = ref({});
// 克隆
const cloneCase = async (item) => {
  cloneLoadingMap.value[item.id] = true;
  const [error] = await apis.cloneCase([item.id]);
  cloneLoadingMap.value[item.id] = false;
  if (error) {
    return;
  }
  notification.success(t('tips.cloneSuccess'));
  loadCaseData();
};

const enabledLoadingMap = ref({});
const enabled = async (item) => {
  enabledLoadingMap.value[item.id] = true;
  const [error] = await apis.enabledCase(!item.enabled, [item.id]);
  enabledLoadingMap.value[item.id] = false;
  if (error) {
    return;
  }
  notification.success(item.enabled ? t('tips.disabledSuccess') : t('tips.enabledSuccess'));
  loadCaseData();
};

const getStatusColor = (status) => {
  switch (status) {
    case 'FAIL':
      return 'text-status-error';
    case 'SUCCESS':
      return 'text-status-success';
    default:
      return 'text-text-sub-content';
  }
};
watch(() => props.id, newValue => {
  if (newValue) {
    loadCaseData();
  }
}, {
  deep: true,
  immediate: true
});

</script>
<template>
  <div class="h-full">
    <div class="flex items-center space-x-3 mt-2">
      <Input
        v-model:value="keywords"
        :allowClear="true"
        class="max-w-56"
        :placeholder="t('service.case.searchPlaceholder')"
        @change="onKeywordChange" />
      <div class="flex-shrink-0 break-all whitespace-pre-wrap text-3 font-normal text-theme-sub-content">
        <Icon icon="icon-tishi1" />
        <span>{{ t('service.case.hints', {num: caseData?.length}) }}</span>
      </div>
      <div class="flex-1 min-w-0"></div>
      <Button
        type="primary"
        size="small"
        :disabled="loading"
        @click="addOrEditCase">
        <Icon icon="icon-jia" class="text-3.5 mr-1" />
        <span>{{ t('service.case.addCaseAction') }}</span>
      </Button>
      <Button
        size="small"
        :disabled="loading || !caseData.length"
        @click="execAll">
        <Icon icon="icon-zhihangceshi" class="text-3.5 mr-1" />
        <span>{{ t('service.case.execCaseAction') }}</span>
      </Button>
      <Button
        size="small"
        :loading="loading"
        @click="loadCaseData">
        <Icon icon="icon-shuaxin" class="text-3.5 mr-1" />
        <span>{{ t('actions.refresh') }}</span>
      </Button>
    </div>
    <Table
      :dataSource="caseData"
      :columns="columns"
      :pagination="pagination"
      :loading="loading"
      noDataSize="small"
      class="mt-2"
      @change="onTablechange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'name'">
          <span class="cursor-pointer text-theme-special" @click="addOrEditCase(record)">{{ record.name }}</span>
        </template>
        <template v-if="column.dataIndex === 'status'">
          <div class="inline-flex items-center truncate flex-1">
            <div class="pl-1" :class="getStatusColor(record.execResult?.value)">{{ record.execResult?.message || '未测试' }}</div>
            <div class="pl-1">{{ record.execFailureMessage }}</div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <Button
            type="link"
            class="px-1 py-0 text-3"
            @click="handleSingleDebug(record)">
            <Icon icon="icon-tiaoshi" class="mr-1" />
            <span>{{ t('actions.debug') }}</span>
          </Button>
          <Button
            :loading="cloneLoadingMap[record.id]"
            type="link"
            class="px-1 py-0 text-3"
            @click="cloneCase(record)">
            <Icon icon="icon-fuzhizujian2" class="mr-1 text-3.5" />
            <span>{{ t('actions.clone') }}</span>
          </Button>
          <Button
            :loading="enabledLoadingMap[record.id]"
            type="link"
            class="px-1 py-0 text-3"
            @click="enabled(record)">
            <Icon icon="icon-qiyong" class="mr-1 text-3.5" />
            <span>{{ record.enabled ? t('actions.disabled') : t('actions.enabled') }}</span>
          </Button>
          <Button
            :loading="delLoadingMap[record.id]"
            type="link"
            class="px-1 py-0 text-3"
            @click="delCase(record)">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <span>{{ t('actions.delete') }}</span>
          </Button>
        </template>
      </template>
    </Table>
    <AsyncComponent :visible="addCaseVisible">
      <AddCaseModal
        v-model:visible="addCaseVisible"
        :caseId="editCaseId"
        :apisId="props.id"
        :debug="isDebug"
        :serviceId="props.serviceId"
        @ok="loadCaseData" />
    </AsyncComponent>
    <AsyncComponent :visible="execCaseVisible">
      <ExecCaseModal
        v-model:visible="execCaseVisible"
        :apisId="props.id" />
    </AsyncComponent>
  </div>
</template>
