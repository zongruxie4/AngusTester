<script lang="ts" setup>
import { defineAsyncComponent, inject, nextTick, Ref, ref, watch } from 'vue';
import {
  AsyncComponent,
  Colon,
  Dropdown,
  Hints,
  Icon,
  IconRefresh,
  Input,
  NoData,
  notification,
  Spin
} from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { Case } from './type';
import { apis } from '@/api/tester';
import { ProjectInfo } from '@/layout/types';

interface Props {
  id: string; // apiId
  layout: 'horizontal'|'inline'
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  layout: 'horizontal'
});

const { t } = useI18n();

const AddCaseModal = defineAsyncComponent(() => import('@/views/apis/services/components/case/addModal/index.vue'));
const ExecCaseModal = defineAsyncComponent(() => import('@/views/apis/services/components/case/exec/index.vue'));

const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));

const keywords = ref();
const addCaseVisible = ref(false);
const execCaseVisible = ref(false);
const editInputRef = ref();
const loading = ref(false);

// const priority = ref<{key: string, name: string}[]>([]);

const loadCaseData = async () => {
  const filters = keywords.value ? [{ value: keywords.value, op: 'MATCH', key: 'name' }] : [];
  loading.value = true;
  const [error, { data }] = await apis.loadApiCases({ apisId: props.id, projectId: projectInfo.value?.id, pageSize: 100, pageNo: 1, filters });
  loading.value = false;
  if (error) {
    return;
  }
  caseData.value = data.list || [];
};

const onKeywordChange = debounce(duration.search, () => {
  loadCaseData();
});

// 用例
const caseAction = [
  {
    name: t('actions.clone'),
    key: 'clone'
  },
  {
    name: t('actions.delete'),
    key: 'del'
  }
];

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

// 删除
const delCase = async (item) => {
  const [error] = await apis.delCase([item.id]);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.deleteSuccess'));
  const delIdx = caseData.value.findIndex(i => i.id === item.id);
  caseData.value.splice(delIdx, 1);
};

// 克隆
const cloneCase = async (item) => {
  const [error] = await apis.cloneCase([item.id]);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.cloneSuccess'));
  loadCaseData();
};

// 打开编辑用例名称
const editableMap = ref({});
const editableName = ref();
const editCaseName = (caseItem) => {
  editableName.value = caseItem.name;
  editableMap.value[caseItem.id] = true;
  nextTick(() => {
    editInputRef.value[0].focus();
  });
};
const onNameBlur = async (item) => {
  if (item.name === editableName.value) {
    editableMap.value[item.id] = false;
    return;
  }
  const [error] = await apis.updateCaseName({ name: editableName.value, id: item.id });
  if (error) {
    return;
  }
  editableMap.value[item.id] = false;
  notification.success(t('service.case.updateNameSuccess'));
  item.name = editableName.value;
};

const handleCase = (select, item: Case) => {
  switch (select.key) {
    case 'edit':
      addOrEditCase(item);
      return;
    case 'del':
      return delCase(item);
    case 'clone':
      return cloneCase(item);
  }
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
    <div class=" space-x-2 mt-2 max-w-100" :class="[props.layout === 'horizontal' ? '' : 'flex items-center']">
      <Input
        v-model:value="keywords"
        :allowClear="true"
        :placeholder="t('common.placeholders.searchKeyword')"
        @change="onKeywordChange" />
      <Hints :text="t('service.case.hints', {num: caseData?.length})" class="mt-2" />
      <div class="space-x-2">
        <Button
          type="primary"
          size="small"
          :disabled="loading"
          @click="addOrEditCase">
          {{ t('service.case.addCaseAction') }}
        </Button>
        <Button
          type="primary"
          size="small"
          :disabled="loading || !caseData.length"
          @click="execAll">
          {{ t('service.case.execCaseAction') }}
        </Button>
        <Button
          type="primary"
          size="small"
          :loading="loading"
          @click="loadCaseData">
          <IconRefresh />
        </Button>
      </div>
    </div>
    <Spin :spinning="loading">
      <div class="flex" :class="[props.layout === 'horizontal' ? 'flex-col' : 'flex-wrap']">
        <div
          v-for="(item) in caseData"
          :key="item.id"
          class="rounded border-l border-b border-t border-r p-1 text-3 leading-5 mt-2"
          :class="{'mr-2 w-1/4 min-w-25': props.layout === 'inline'}">
          <div class="flex items-center">
            <div class="flex items-center truncate flex-1">
              <span class="text-gray-text">{{ t('service.case.nameLabel') }}<Colon /></span>
              <span v-if="!editableMap[item.id]" class="pl-1 flex-1 min-w-0 truncate">
                {{ item.name }}
              </span>
              <Input
                v-else
                ref="editInputRef"
                v-model:value="editableName"
                class="ml-1 min-w-0 flex-1"
                :maxlength="100"
                @blur="onNameBlur(item)" />
            </div>
            <div class="flex items-center justify-between space-x-2">
              <Icon
                icon="icon-shuxie"
                class="text-theme-special ml-2"
                @click="editCaseName(item)" />
              <div>
                <Button
                  type="link"
                  class="px-1 py-0 text-3"
                  @click="handleCase({key: 'edit'}, item)">
                  {{ t('actions.edit') }}
                </Button>
                <Button
                  type="link"
                  class="px-1 py-0 text-3"
                  @click="handleSingleDebug(item)">
                  {{ t('actions.debug') }}
                </Button>
              </div>
            </div>
          </div>
          <div class="flex items-center justify-between">
            <div class="inline-flex items-center truncate flex-1">
              <span class="text-gray-text">{{ t('service.case.statusLabel') }} <Colon /></span>
              <div class="pl-1" :class="getStatusColor(item.execResult?.value)">{{ item.execResult?.message || t('service.case.statusNotTested') }}</div>
              <div class="pl-1">{{ item.execFailureMessage }}</div>
            </div>
            <div class="px-1">
              <Dropdown :menuItems="caseAction" @click="handleCase($event, item)">
                <Icon icon="icon-gengduo" class="cursor-pointer" />
              </Dropdown>
            </div>
          </div>
        </div>
      </div>
    </Spin>
    <NoData v-if="!caseData.length" size="small" />
    <AsyncComponent :visible="addCaseVisible">
      <AddCaseModal
        v-model:visible="addCaseVisible"
        :caseId="editCaseId"
        :apisId="props.id"
        :debug="isDebug"
        @ok="loadCaseData" />
    </AsyncComponent>
    <AsyncComponent :visible="execCaseVisible">
      <ExecCaseModal
        v-model:visible="execCaseVisible"
        :apisId="props.id" />
    </AsyncComponent>
  </div>
</template>
