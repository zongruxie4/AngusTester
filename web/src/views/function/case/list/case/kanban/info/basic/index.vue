<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, ref } from 'vue';
import { Tag } from 'ant-design-vue';
import { Grid, Icon, Input, Popover, ReviewStatus, Select, TaskPriority, TestResult } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { funcCase } from '@/api/tester';

import SelectEnum from '@/components/SelectEnum/index.vue'
import { CaseInfo } from '../../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  dataSource: CaseInfo;
  canEdit: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: CaseInfo): void;
  (event: 'update:dataSource', value: CaseInfo): void;
}>();

const Description = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/info/description/index.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/info/precondition/index.vue'));
const TestStep = defineAsyncComponent(() => import('@/views/function/case/list/case/kanban/info/testSteps/index.vue'));

const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const nameInputRef = ref();
const tagsSelectRef = ref();
const priorityRef = ref();
const evalWorkloadInputRef = ref();
const actualWorkloadInputRef = ref();

const isEditName = ref(false);
const tagsIds = ref<string[]>([]);
const defaultTags = ref<{[key: string]: { name: string; id: string }}>({});
const isEditTag = ref(false);
const isEditPriority = ref(false);
const priority = ref();
const isEditEvalWorkload = ref(false);
const alWorkload = ref(props.dataSource?.actualWorkload);
const isEditActualWorkload = ref(false);

const openEditName = () => {
  isEditName.value = true;
  nextTick(() => {
    nameInputRef.value.focus();
  });
};

const editName = async (event) => {
  if (event.target.value === props.dataSource?.name || !event.target.value) {
    isEditName.value = false;
    return;
  }

  loadingChange(true);
  const [error] = await funcCase.putName(props.dataSource.id, event.target.value);
  loadingChange(false);
  isEditName.value = false;
  if (error) {
    return;
  }

  change();
};

const openEditPriority = () => {
  isEditPriority.value = true;
  priority.value = props.dataSource?.priority?.value;
  nextTick(() => {
    priorityRef.value.focus();
  });
};

const editPriority = async (value) => {
  // 请求加载中,不允许再次发送
  if (value === props.dataSource?.priority?.value) {
    isEditPriority.value = false;
    return;
  }

  loadingChange(true);
  const [error] = await funcCase.putPriority(props.dataSource.id, value);
  loadingChange(false);
  isEditPriority.value = false;
  if (error) {
    return;
  }

  change();
};

const openEditEvalWorkload = () => {
  isEditEvalWorkload.value = true;
  nextTick(() => {
    evalWorkloadInputRef.value.focus();
  });
};

const editEvalWorkload = async (event) => {
  if (+event.target.value === (+props.dataSource?.evalWorkload) || (!event.target.value && !props.dataSource?.evalWorkload)) {
    isEditEvalWorkload.value = false;
    return;
  }

  loadingChange(true);
  const [error] = await funcCase.putEvalWorkload(props.dataSource.id, { workload: event.target.value });
  loadingChange(false);
  isEditEvalWorkload.value = false;
  if (error) {
    return;
  }

  change();
};

const openEditActualWorkload = () => {
  alWorkload.value = props.dataSource.actualWorkload || props.dataSource.evalWorkload;
  isEditActualWorkload.value = true;
  nextTick(() => {
    actualWorkloadInputRef.value.focus();
  });
};

const editActualWorkload = async (event) => {
  if (+event.target.value === (+props.dataSource?.actualWorkload) || (!event.target.value && !props.dataSource?.evalWorkload)) {
    isEditActualWorkload.value = false;
    return;
  }

  loadingChange(true);
  const [error] = await funcCase.putActualWorkload(props.dataSource.id, { workload: event.target.value });
  loadingChange(false);
  isEditActualWorkload.value = false;
  if (error) {
    return;
  }

  change();
};

// 编辑标签
const openEditTag = () => {
  tagsIds.value = (props.dataSource?.tags || [])?.map(item => {
    defaultTags.value[item.id] = { id: item.id, name: item.name };
    return item.id;
  }) || [];
  isEditTag.value = true;
  nextTick(() => {
    tagsSelectRef.value.focus();
  });
};

const editTag = async () => {
  if (tagsIds.value?.length > 5) {
    isEditTag.value = false;
    return;
  }

  const oldTagIds = props.dataSource?.tags?.map(item => item.id) || [];
  const equalFlag = isEqual(oldTagIds, tagsIds.value);
  if (equalFlag) {
    isEditTag.value = false;
    return;
  }
  const params = { tagIds: tagsIds.value.length ? tagsIds.value : null };
  loadingChange(true);
  const [error] = await funcCase.putTag(props.dataSource.id, params);
  loadingChange(false);
  isEditTag.value = false;
  if (error) {
    return;
  }

  change();
};

const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

const change = async () => {
  const id = props.dataSource?.id;
  if (!id) {
    return;
  }

  loadingChange(true);
  const [error, res] = await funcCase.getCaseDetail(id);
  loadingChange(false);
  if (error) {
    return;
  }

  const data = res?.data || {};
  emit('change', data);
  emit('update:dataSource', data);
};

const infoColumns = [
  [
    { label: '名称', dataIndex: 'name' },
    { label: 'ID', dataIndex: 'id' },
    { label: '编号', dataIndex: 'code' },
    {
      label: '评审状态',
      dataIndex: 'reviewStatus'
    },
    {
      label: '版本',
      dataIndex: 'version'
    },
    {
      label: '软件版本',
      dataIndex: 'softwareVersion'
    },
    { label: '优先级', dataIndex: 'priority' },
    { label: '标签', dataIndex: 'tags' },
    { label: '所属计划', dataIndex: 'planName' },
    { label: '所属模块', dataIndex: 'moduleName' },
    {
      label: '测试结果',
      dataIndex: 'testResult'
    },
    { label: props.dataSource?.evalWorkloadMethod?.value === 'STORY_POINT' ? '评估故事点' : '评估工时', dataIndex: 'evalWorkload', customRender: ({ text }) => text || '--' },
    { label: props.dataSource?.evalWorkloadMethod?.value === 'STORY_POINT' ? '实际故事点' : '实际工时', dataIndex: 'actualWorkload', customRender: ({ text }) => text || '--' },
    { label: '计划外用例', dataIndex: 'unplannedFlag', customRender: ({ text }) => text ? t('status.yes') : t('status.no') }
  ]
];

</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-auto">
    <div>
      <div class="text-theme-title mb-2.5 font-semibold">基本信息</div>
      <Grid
        :columns="infoColumns"
        :dataSource="props.dataSource"
        :spacing="0"
        :marginBottom="4"
        font-size="12px">
        <template #name="{text}">
          <div class="flex items-center w-full relative">
            <template v-if="isEditName">
              <Input
                ref="nameInputRef"
                :value="text"
                :allowClear="false"
                :maxlength="200"
                size="small"
                class="absolute -top-1.25"
                placeholder="用例名称"
                @blur="editName" />
            </template>
            <template v-else>
              <span>
                <span>{{ text }}</span>
                <Icon
                  v-if="props.canEdit"
                  class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer flex-none -mt-1"
                  icon="icon-shuxie"
                  @click="openEditName" />
              </span>
            </template>
          </div>
        </template>
        <template #priority="{text}">
          <div class="flex items-center relative">
            <template v-if="isEditPriority">
              <SelectEnum
                ref="priorityRef"
                v-model:value="priority"
                :allowClear="false"
                :autofocus="isEditPriority"
                enumKey="Priority"
                size="small"
                class="w-52 absolute -top-1.25"
                placeholder="优先级"
                @blur="editPriority($event.target.value)">
                <template #option="item">
                  <TaskPriority :value="item" />
                </template>
              </SelectEnum>
            </template>
            <template v-else>
              <TaskPriority :value="text" />
              <Icon
                v-if="props.canEdit"
                class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer flex-none"
                icon="icon-shuxie"
                @click="openEditPriority" />
            </template>
          </div>
        </template>
        <template #tags="{text}">
          <div class="flex items-center flex-wrap">
            <template v-if="isEditTag">
              <Select
                ref="tagsSelectRef"
                v-model:value="tagsIds"
                showSearch
                :defaultOptions="defaultTags"
                :fieldNames="{ label: 'name', value: 'id' }"
                :maxTags="5"
                placeholder="添加标签数量1~5个"
                :class="{'border-error':tagsIds && tagsIds.length > 5 }"
                :action="`${TESTER}/tag?projectId=${projectId}&fullTextSearch=true`"
                mode="multiple"
                size="small"
                class="w-full"
                @blur="editTag" />
            </template>
            <template v-else>
              <div class="inline-flex items-center leading-6">
                <Tag
                  v-for="(tag,index) in (text || [])"
                  :key="tag.id"
                  :class="{'min-w-17.5':!tag.name,'last-child':index===text.length-1}"
                  color="rgba(252, 253, 255, 1)"
                  class="text-3 px-2 font-normal text-theme-sub-content mr-2 h-6 py-1 border-border-divider">
                  <span>{{ tag.name }}</span>
                </Tag>
                <template v-if="!text?.length">--</template>
                <Icon
                  v-if="props.canEdit"
                  :class="{'transform-gpu':text?.length}"
                  class="ml-2.5 text-3 text-theme-special text-theme-text-hover cursor-pointer "
                  icon="icon-shuxie"
                  @click="openEditTag" />
              </div>
            </template>
          </div>
        </template>
        <template #evalWorkload="{text}">
          <div class="flex items-center relative">
            <template v-if="isEditEvalWorkload">
              <Input
                ref="evalWorkloadInputRef"
                :value="props.dataSource?.evalWorkload"
                :allowClear="false"
                :autofocus="isEditEvalWorkload"
                :min="0.1"
                :max="1000"
                placeholder="最小0.1，最大1000，最多支持2位小数"
                dataType="float"
                size="small"
                class="w-65 absolute -top-1.25"
                @blur="editEvalWorkload" />
            </template>
            <template v-else>
              {{ text || '--' }}
              <Icon
                v-if="props.canEdit"
                class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer"
                icon="icon-shuxie"
                @click="openEditEvalWorkload" />
              <Popover
                placement="rightTop"
                arrowPointAtCenter>
                <template #content>
                  <div class="text-3 text-theme-sub-content max-w-75 leading-4">{{ props.dataSource?.evalWorkloadMethod?.value === 'STORY_POINT'?'工作任务量、综合难度、复杂度等的评估值。':'工作评估所花费的时间，以小时为单位计算。' }}</div>
                </template>
                <Icon icon="icon-tishi1" class="text-3.5 text-tips ml-2 cursor-pointer flex-none" />
              </Popover>
            </template>
          </div>
        </template>
        <template #actualWorkload="{text}">
          <div class="flex items-center relative">
            <template v-if="isEditActualWorkload">
              <Input
                ref="actualWorkloadInputRef"
                :value="alWorkload"
                :allowClear="false"
                :autofocus="isEditActualWorkload"
                :min="0.1"
                :max="1000"
                placeholder="最小0.1，最大1000，最多支持2位小数"
                dataType="float"
                size="small"
                class="w-65 absolute -top-1.25"
                @blur="editActualWorkload" />
            </template>
            <template v-else>
              {{ text || '--' }}
              <Icon
                v-if="props.canEdit && props.dataSource?.evalWorkload"
                class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer"
                icon="icon-shuxie"
                @click="openEditActualWorkload" />
              <Popover
                placement="rightTop"
                arrowPointAtCenter>
                <template #content>
                  <div class="text-3 text-theme-sub-content max-w-75 leading-4">{{ props.dataSource?.evalWorkloadMethod?.value === 'STORY_POINT'?'工作任务量、综合难度、复杂度等的实际值。':'工作实际所花费的时间，以小时为单位计算。' }}</div>
                </template>
                <Icon icon="icon-tishi1" class="text-3.5 text-tips ml-2 cursor-pointer flex-none" />
              </Popover>
            </template>
          </div>
        </template>
        <template #planName="{text}">
          <span>
            <Icon icon="icon-jihua" class="mr-1.25 flex-none -mt-0.25" />{{ text }}
          </span>
        </template>
        <template #moduleName="{text}">
          <template v-if="!text">
            --
          </template>
          <div v-else class="-mt-1 flex">
            <Tag
              class="px-0 py-1 font-normal text-theme-content rounded bg-white flex border-none">
              <Icon icon="icon-mokuai" class="mr-1.25 flex-none mt-0.5" />
              <div class="flex-1  whitespace-break-spaces break-all leading-4">{{ text }}</div>
            </Tag>
          </div>
        </template>
        <template #reviewStatus="{text}">
          <template v-if="text">
            <ReviewStatus :value="text" />
          </template>
          <template v-else>
            --
          </template>
        </template>
        <template #testResult="{text}">
          <div class="flex items-center">
            <TestResult :value="text" />
            <div
              v-if="props.dataSource?.overdue"
              class="border border-status-error rounded px-0.5 ml-5"
              style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span>已逾期</span>
            </div>
          </div>
        </template>
        <template #version="{text}">
          <span v-if="text">v{{ text }}</span>
          <template v-else>--</template>
        </template>
        <template #softwareVersion="{text}">
          <span v-if="text">{{ text }}</span>
          <template v-else>--</template>
        </template>
      </Grid>
    </div>
    <Precondition
      :projectId="props.projectId"
      :appInfo="props.appInfo"
      :dataSource="props.dataSource"
      :canEdit="props.canEdit"
      @change="change"
      @loadingChange="loadingChange" />
    <TestStep
      :projectId="props.projectId"
      :appInfo="props.appInfo"
      :dataSource="props.dataSource"
      :canEdit="props.canEdit"
      @change="change"
      @loadingChange="loadingChange" />
    <Description
      :projectId="props.projectId"
      :appInfo="props.appInfo"
      :dataSource="props.dataSource"
      :canEdit="props.canEdit"
      @change="change"
      @loadingChange="loadingChange" />
  </div>
</template>
