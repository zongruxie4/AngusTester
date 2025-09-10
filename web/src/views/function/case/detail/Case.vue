<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, nextTick, onBeforeUnmount, onMounted, ref, watch, Ref } from 'vue';
import {
  DatePicker,
  Grid,
  Icon,
  Input,
  NoData,
  notification,
  ReviewStatus,
  Select,
  SelectUser,
  TaskPriority,
  TestResult,
  Toggle
} from '@xcan-angus/vue-ui';
import { Button, Popover, Tag, Upload } from 'ant-design-vue';
import { download, upload, TESTER, duration, utils } from '@xcan-angus/infra';
import RichEditor from '@/components/richEditor/index.vue';
import dayjs from 'dayjs';
import elementResizeDetector, { Erd } from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import { CaseInfoObj, Priority } from './types';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import {
  bigApisInfoColumns,
  bigDateInfoColumns,
  bigPeopleInfoColumns,
  bigReviewInfoColumns,
  bigTestInfoColumns,
  minApisInfoColumns,
  minDateInfoColumns,
  minPeopleInfoColumns,
  minReviewInfoColumns,
  minTestInfoColumns
} from './config';
import { funcCase } from '@/api/tester';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { useI18n } from 'vue-i18n';

interface Props {
  caseDetail: CaseInfoObj;
  actionAuth: {[key: string]: any}
}

const props = withDefaults(defineProps<Props>(), {
  caseDetail: () => ({ id: '' }),
  actionAuth: () => ({})
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'editSuccess'):void;
  (e: 'loadingChange', value:boolean):void;
}>();

const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/case/CaseSteps.vue'));

const { t } = useI18n();

const userInfo = inject('userInfo');

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const bigLayout = ref(true); // 是否大屏显示
const detailRef = ref();
let erd:Erd;
const resizeHandler = debounce(duration.resize, (element) => {
  bigLayout.value = element.offsetWidth >= 960;
});
export type GridColumns = {
  label: string;
  dataIndex: string;
  colon?:boolean;
  customRender?: ({ text }: { text: any; }) => string;
}

const peopleInfoColumns = ref<GridColumns[][]>(bigPeopleInfoColumns);

const dateInfoColumns = ref<GridColumns[][]>(bigDateInfoColumns);

const reviewInfoColumns = ref<GridColumns[][]>(bigReviewInfoColumns);

const testInfoColumns = ref<GridColumns[][]>(bigTestInfoColumns);

const apisInfoColumns = ref<GridColumns[][]>(bigApisInfoColumns);

watch(() => bigLayout.value, (newValue) => {
  peopleInfoColumns.value = newValue ? bigPeopleInfoColumns : minPeopleInfoColumns;
  dateInfoColumns.value = newValue ? bigDateInfoColumns : minDateInfoColumns;
  reviewInfoColumns.value = newValue ? bigReviewInfoColumns : minReviewInfoColumns;
  testInfoColumns.value = newValue ? bigTestInfoColumns : minTestInfoColumns;
  apisInfoColumns.value = newValue ? bigApisInfoColumns : minApisInfoColumns;
}, {
  immediate: true
});

const disabledDate = current => {
  return current && current < dayjs().subtract(1, 'day').endOf('day');
};

const handleDownload = (url:string) => {
  download(url);
};

const preconditionExpand = ref(true);
const conditionsExpand = ref(true);
const remarkExpand = ref(true);

const infoExpand = ref(true);

const infoColumns = computed<GridColumns[][]>(() => [
  [
    { label: t('functionCase.detail.caseDetail.name'), dataIndex: 'name' },
    { label: t('functionCase.detail.caseDetail.id'), dataIndex: 'id' },
    { label: t('functionCase.detail.caseDetail.code'), dataIndex: 'code' },
    { label: t('functionCase.detail.caseDetail.reviewStatus'), dataIndex: 'reviewStatus' },
    { label: t('functionCase.detail.caseDetail.version'), dataIndex: 'version' },
    { label: t('functionCase.detail.caseDetail.softwareVersion'), dataIndex: 'softwareVersion' },
    { label: t('functionCase.detail.caseDetail.priority'), dataIndex: 'priority' },
    { label: t('functionCase.detail.caseDetail.unplannedCase'), dataIndex: 'unplannedFlag', customRender: ({ text }) => text ? t('status.yes') : t('status.no') }
  ],
  [
    { label: t('functionCase.detail.caseDetail.tags'), dataIndex: 'tags' },
    { label: t('functionCase.detail.caseDetail.planName'), dataIndex: 'planName' },
    { label: t('functionCase.detail.caseDetail.moduleName'), dataIndex: 'moduleName' },
    {
      label: t('functionCase.detail.caseDetail.testResult'),
      dataIndex: 'testResult'
    },
    { label: props.caseDetail?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('functionCase.detail.caseDetail.evalStoryPoint') : t('functionCase.detail.caseDetail.evalWorkload'), dataIndex: 'evalWorkload', customRender: ({ text }) => text || '--' },
    { label: props.caseDetail?.evalWorkloadMethod?.value === 'STORY_POINT' ? t('functionCase.detail.caseDetail.actualStoryPoint') : t('functionCase.detail.caseDetail.actualWorkload'), dataIndex: 'actualWorkload', customRender: ({ text }) => text || '--' }
  ]
]);

const getOneTestPass = computed(() => {
  if (props.caseDetail?.testNum && Number(props.caseDetail.testNum) > 0) {
    return props.caseDetail?.testFailNum === '0' && props.caseDetail?.testResult?.value === 'PASSED' ? t('status.yes') : t('status.no');
  }
  return '--';
});

const getOneReviewPass = computed(() => {
  if (props.caseDetail?.reviewNum && Number(props.caseDetail.reviewNum) > 0) {
    return props.caseDetail?.reviewFailNum === '0' && props.caseDetail?.reviewStatus?.value === 'PASSED' ? t('status.yes') : t('status.no');
  }
  return '--';
});

const nameInputRef = ref();
const tagsSelectRef = ref();
const priorityRef = ref();
const evalWorkloadInputRef = ref();
const actualWorkloadInputRef = ref();

// 编辑名称
const isEditName = ref(false);
const editNameLoading = ref(false);
const openEditName = () => {
  isEditName.value = true;
  nextTick(() => {
    nameInputRef.value.focus();
  });
};

const editName = async (event) => {
  if (event.target.value === props.caseDetail?.name || !event.target.value) {
    isEditName.value = false;
    return;
  }

  editNameLoading.value = true;
  const [error] = await funcCase.putName(props.caseDetail.id, event.target.value);
  editNameLoading.value = false;
  isEditName.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

// 编辑优先级
const isEditPriority = ref(false);
const editPriorityLoading = ref(false);
const priority = ref();
const openEditPriority = () => {
  isEditPriority.value = true;
  priority.value = props.caseDetail?.priority?.value;
  nextTick(() => {
    priorityRef.value.focus();
  });
};

const editPriority = async (value:Priority) => {
  // 请求加载中,不允许再次发送
  if (editPriorityLoading.value || value === props.caseDetail?.priority?.value) {
    isEditPriority.value = false;
    return;
  }

  editPriorityLoading.value = true;
  const [error] = await funcCase.putPriority(props.caseDetail.id, value);
  editPriorityLoading.value = false;
  isEditPriority.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

// 编辑评估工作量
const isEditEvalWorkload = ref(false);
const editEvalWorkloadLoading = ref(false);
const openEditEvalWorkload = () => {
  isEditEvalWorkload.value = true;
  nextTick(() => {
    evalWorkloadInputRef.value.focus();
  });
};

const editEvalWorkload = async (event) => {
  if (+event.target.value === (+props.caseDetail?.evalWorkload) || (!event.target.value && !props.caseDetail?.evalWorkload)) {
    isEditEvalWorkload.value = false;
    return;
  }

  editEvalWorkloadLoading.value = true;
  const [error] = await funcCase.putEvalWorkload(props.caseDetail.id, { workload: event.target.value });
  editEvalWorkloadLoading.value = false;
  isEditEvalWorkload.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

// 编辑实际工作量
const alWorkload = ref(props.caseDetail?.actualWorkload);
const isEditActualWorkload = ref(false);
const editActualWorkloadLoading = ref(false);
const openEditActualWorkload = () => {
  alWorkload.value = props.caseDetail.actualWorkload || props.caseDetail.evalWorkload;
  isEditActualWorkload.value = true;
  nextTick(() => {
    actualWorkloadInputRef.value.focus();
  });
};

const editActualWorkload = async (event) => {
  if (+event.target.value === (+props.caseDetail?.actualWorkload) || (!event.target.value && !props.caseDetail?.evalWorkload)) {
    isEditActualWorkload.value = false;
    return;
  }

  editActualWorkloadLoading.value = true;
  const [error] = await funcCase.putActualWorkload(props.caseDetail.id, { workload: event.target.value });
  editActualWorkloadLoading.value = false;
  isEditActualWorkload.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

// 编辑标签
const tagsIds = ref<string[]>([]);
const defaultTags = ref<{[key: string]: { name: string; id: string }}>({});
const isEditTag = ref(false);
const editTagLoading = ref(false);
const openEditTag = () => {
  tagsIds.value = (props.caseDetail?.tags || [])?.map(item => {
    defaultTags.value[item.id] = { id: item.id, name: item.name };
    return item.id;
  }) || [];
  isEditTag.value = true;
  nextTick(() => {
    tagsSelectRef.value.focus();
  });
};

const editTag = async () => {
  if (editTagLoading.value || tagsIds.value?.length > 5) {
    isEditTag.value = false;
    return;
  }
  const oldTagIds = props.caseDetail?.tags?.map(item => item.id) || [];
  const isEqual = utils.deepCompare(oldTagIds, tagsIds.value);
  if (isEqual) {
    isEditTag.value = false;
    return;
  }
  editTagLoading.value = true;
  const [error] = await funcCase.putTag(props.caseDetail.id, { tagIds: tagsIds.value.length ? tagsIds.value : null });
  editTagLoading.value = false;
  isEditTag.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

// 修改时间
const datePickertRef = ref();
const isEditDisabledDate = ref(false);
const deadlineDate = ref(dayjs().add(1, 'day').format(DATE_TIME_FORMAT));
const loading = ref(false);
const openEditDeadlineDate = () => {
  deadlineDate.value = props.caseDetail?.deadlineDate;
  isEditDisabledDate.value = true;
  nextTick(() => {
    datePickertRef.value.focus();
  });
};

const editDeadlineDate = async () => {
  if (loading.value || props.caseDetail?.deadlineDate === deadlineDate.value) {
    isEditDisabledDate.value = false;
    return;
  }

  if (dayjs(deadlineDate.value).isBefore(dayjs(), 'minute')) {
    notification.warning(t('functionCase.detail.caseDetail.deadlineMustBeFuture'));
    return;
  }

  loading.value = true;
  const [error] = await funcCase.putDeadline(props.caseDetail.id, deadlineDate.value);
  loading.value = false;
  isEditDisabledDate.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};
const attachmentsData = computed(() => {
  return props.caseDetail?.attachments || [];
});

const isUpload = ref(false);
const handleMouseenter = () => {
  isUpload.value = true;
};
const handleMouseleave = () => {
  isUpload.value = false;
};

// 删除附件
const cancelFile = async (i:number) => {
  if (!attachmentsData.value.length) {
    return;
  }
  const attachments = attachmentsData.value?.filter((_item, index) => index !== i);
  uploadLoading.value = true;
  const [error] = await funcCase.putAttachment(props.caseDetail.id, { attachments });
  uploadLoading.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

const fileList = ref<{id?:string, name:string, url:string}[]>([]);
// 上传附件
const upLoadFile = async function (file) {
  if (fileList.value.length >= 5 || uploadLoading.value) {
    return;
  }

  uploadLoading.value = true;
  const [error, { data = [] }] = await upload(file.file, { bizKey: 'angusTesterCaseAttachments' });
  uploadLoading.value = false;
  if (error) {
    return;
  }

  updateAttachment(data);
};

const uploadLoading = ref<boolean>(false);
const updateAttachment = async (data) => {
  if (uploadLoading.value) {
    return;
  }
  const attachments = data?.map(item => ({ name: item.name, url: item.url }));
  if (attachmentsData.value.length) {
    attachments.push(...attachmentsData.value);
  }
  uploadLoading.value = true;
  const [error] = await funcCase.putAttachment(props.caseDetail.id, { attachments });
  uploadLoading.value = false;

  if (error) {
    return;
  }

  emit('editSuccess');
};

// 修改前置条件
const preconditionRichRef = ref();
const preconditionError = ref();
const isEditPrecondition = ref(false);
const preconditionContent = ref();
const savePreconditionLoading = ref(false);
const handleEditPrecondition = () => {
  isEditPrecondition.value = true;
  preconditionContent.value = props.caseDetail.precondition || undefined;
};
const cancelEditPrecondition = () => {
  isEditPrecondition.value = false;
};
const savePrecondition = async () => {
  if (preconditionContent.value === props.caseDetail.precondition) {
    isEditPrecondition.value = false;
    return;
  }
  if (preconditionRichRef.value.getLength() > 6000) {
    preconditionError.value = true;
    return;
  }
  preconditionError.value = false;
  if (savePreconditionLoading.value) {
    return;
  }
  savePreconditionLoading.value = true;
  const [error] = await funcCase.updateCase([{
    id: props.caseDetail.id,
    precondition: preconditionContent.value
  }]);
  savePreconditionLoading.value = false;
  if (error) {
    return;
  }
  isEditPrecondition.value = false;
  emit('editSuccess');
};

// 修改测试步骤
const isEditSteps = ref(false);
const stepsContent = ref([]);
const saveStepsLoading = ref(false);
const handleEditSteps = () => {
  isEditSteps.value = true;
  stepsContent.value = JSON.parse(JSON.stringify(props.caseDetail?.steps || []));
};
const cancelEditSteps = () => {
  isEditSteps.value = false;
};
const saveSteps = async () => {
  if (saveStepsLoading.value) {
    return;
  }
  saveStepsLoading.value = true;
  const [error] = await funcCase.updateCase([{
    id: props.caseDetail.id,
    steps: stepsContent.value
  }]);
  saveStepsLoading.value = false;
  if (error) {
    return;
  }
  isEditSteps.value = false;
  emit('editSuccess');
};

// 修改描述
const uploadOptions = { bizKey: 'angusTesterCaseAttachments' };
const editorInit = {
  menubar: false,
  height: 368,
  selector: 'textarea', // 选择器，指定要转换为富文本编辑器的textarea元素
  content_style: `
  body { font-size: 12px; }
    #tinymce.mce-content-body[data-mce-placeholder]:not(.mce-visualblocks)::before {
    color: #BFBFBF !important;
  }
  `,
  placeholder: t('functionCase.detail.caseDetail.inputPrecondition') // 设置内容区的占位文字提示
};

const descRichRef = ref();
const descError = ref(false);
const isEditDescription = ref(false);
const descriptionContent = ref();
const savedescriptionLoading = ref(false);
const handleEditDescription = () => {
  isEditDescription.value = true;
  descriptionContent.value = props.caseDetail?.description || undefined;
};
const cancelEditDescription = () => {
  isEditDescription.value = false;
};
const saveDescription = async () => {
  if (descRichRef.value.getLength() > 6000) {
    descError.value = true;
    return;
  }
  descError.value = false;
  if (descriptionContent.value === props.caseDetail?.description) {
    isEditDescription.value = false;
    return;
  }
  if (savedescriptionLoading.value) {
    return;
  }
  savedescriptionLoading.value = true;
  const [error] = await funcCase.updateCase([{
    id: props.caseDetail.id,
    description: descriptionContent.value
  }]);
  savedescriptionLoading.value = false;
  if (error) {
    return;
  }
  isEditDescription.value = false;
  emit('editSuccess');
};

// 修改测试人
const testerSelectRef = ref();
const isEditTester = ref(false);
const testerIcContent = ref();
const saveTesterLoading = ref(false);
const handleEditTester = () => {
  isEditTester.value = true;
  testerIcContent.value = props.caseDetail?.testerId;
  nextTick(() => {
    testerSelectRef.value.focus();
  });
};

const saveTester = async () => {
  if (testerIcContent.value === props.caseDetail?.testerId) {
    isEditTester.value = false;
    return;
  }
  if (saveTesterLoading.value) {
    return;
  }
  saveTesterLoading.value = true;
  const [error] = await funcCase.updateCase([{
    id: props.caseDetail.id,
    testerId: testerIcContent.value
  }]);
  saveTesterLoading.value = false;
  isEditTester.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

const handleSetTester = async () => {
  if (userInfo?.id === props.caseDetail?.testerId) {
    return;
  }
  if (saveTesterLoading.value) {
    return;
  }
  saveTesterLoading.value = true;
  const [error] = await funcCase.updateCase([{
    id: props.caseDetail.id,
    testerId: userInfo.id
  }]);
  saveTesterLoading.value = false;
  if (error) {
    return;
  }
  emit('editSuccess');
};

// 修改版本
const versionRef = ref();
const versionEditFlag = ref(false);
const versionValue = ref();
const toEditVersion = () => {
  versionEditFlag.value = true;
  versionValue.value = props.caseDetail?.softwareVersion;
  nextTick(() => {
    setTimeout(() => {
      if (typeof versionRef.value?.focus === 'function') {
        versionRef.value?.focus();
      }
    }, 100);
  });
};

const versionChange = (value) => {
  versionValue.value = value;
};

const versionBlur = async () => {
  const value = versionValue.value;
  if (value === props.caseDetail?.softwareVersion) {
    versionEditFlag.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await funcCase.updateCase([{ softwareVersion: value || '', id: props.caseDetail.id }]);
  emit('loadingChange', false);
  versionEditFlag.value = false;
  if (error) {
    return;
  }

  emit('editSuccess');
};

const editOk = () => {
  emit('editSuccess');
};

const loadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

onMounted(() => {
  erd = elementResizeDetector();
  erd.listenTo(detailRef.value, resizeHandler);
});

onBeforeUnmount(() => {
  erd.removeListener(detailRef.value, resizeHandler);
});
</script>
<template>
  <div
    ref="detailRef"
    :class="bigLayout?'flex pr-2':'pr-3.5'"
    class="overflow-y-auto h-full">
    <div class="flex-1">
      <Toggle
        v-model:open="infoExpand"
        :title="t('functionCase.detail.caseDetail.basicInfo')">
        <Grid
          :columns="infoColumns"
          :dataSource="caseDetail"
          :spacing="20"
          :marginBottom="4"
          labelSpacing="10px"
          font-size="12px"
          class="pt-2 pl-5.5">
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
                  :placeholder="t('functionCase.detail.caseDetail.name')"
                  @blur="editName" />
              </template>
              <template v-else>
                <span> {{ text }}
                  <Icon
                    v-if="props.actionAuth.includes('edit')"
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
                  :disabled="editPriorityLoading"
                  :autofocus="isEditPriority"
                  enumKey="Priority"
                  size="small"
                  class="w-52 absolute -top-1.25"
                  :placeholder="t('functionCase.detail.caseDetail.priority')"
                  @blur="editPriority($event.target.value)">
                  <template #option="item">
                    <TaskPriority :value="item" />
                  </template>
                </SelectEnum>
              </template>
              <template v-else>
                <TaskPriority :value="text" />
                <Icon
                  v-if="props.actionAuth.includes('edit')"
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
                  :placeholder="t('functionCase.detail.caseDetail.tags')"
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
                    {{ tag.name }}
                  </Tag>
                  <template v-if="!text?.length">--</template>
                  <Icon
                    v-if="props.actionAuth.includes('edit')"
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
                  :value="caseDetail?.evalWorkload"
                  :allowClear="false"
                  :autofocus="isEditEvalWorkload"
                  :min="0.1"
                  :max="1000"
                  :placeholder="t('functionCase.kanbanView.infoBasic.evalWorkloadPlaceholder')"
                  dataType="float"
                  size="small"
                  class="w-65 absolute -top-1.25"
                  @blur="editEvalWorkload" />
              </template>
              <template v-else>
                {{ text || '--' }}
                <Icon
                  v-if="props.actionAuth.includes('edit')"
                  class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer"
                  icon="icon-shuxie"
                  @click="openEditEvalWorkload" />
                <Popover
                  placement="rightTop"
                  arrowPointAtCenter>
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">{{ caseDetail?.evalWorkloadMethod?.value === 'STORY_POINT'?t('functionCase.kanbanView.infoBasic.evalWorkloadTip'):t('functionCase.kanbanView.infoBasic.evalWorkloadTipTime') }}</div>
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
                  :placeholder="t('functionCase.kanbanView.infoBasic.actualWorkloadPlaceholder')"
                  dataType="float"
                  size="small"
                  class="w-65 absolute -top-1.25"
                  @blur="editActualWorkload" />
              </template>
              <template v-else>
                {{ text || '--' }}
                <Icon
                  v-if="props.actionAuth.includes('edit') && caseDetail?.evalWorkload"
                  class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer"
                  icon="icon-shuxie"
                  @click="openEditActualWorkload" />
                <Popover
                  placement="rightTop"
                  arrowPointAtCenter>
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">{{ caseDetail?.evalWorkloadMethod?.value === 'STORY_POINT'?t('functionCase.kanbanView.infoBasic.actualWorkloadTip'):t('functionCase.kanbanView.infoBasic.actualWorkloadTipTime') }}</div>
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
                v-if="caseDetail?.overdue"
                class="border border-status-error rounded px-0.5 ml-5"
                style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                {{ t('functionCase.detail.caseDetail.overdue') }}
              </div>
            </div>
          </template>
          <template #version="{text}">
            <span v-if="text">v{{ text }}</span>
            <template v-else>--</template>
          </template>
          <template #softwareVersion="{text}">
            <template v-if="versionEditFlag">
              <Select
                ref="versionRef"
                v-model:value="versionValue"
                allowClear
                :placeholder="t('functionCase.detail.caseDetail.selectVersion')"
                lazy
                class="w-full max-w-60"
                :action="`${TESTER}/software/version?projectId=${projectId}`"
                :params="{filters: [{value: ['NOT_RELEASED', 'RELEASED'], key: 'status', op: 'IN'}]}"
                :fieldNames="{value:'name', label: 'name'}"
                @blur="versionBlur"
                @change="versionChange">
              </Select>
            </template>
            <template v-else>
              <div class="flex space-x-1">
                <RouterLink
                  v-if="caseDetail?.softwareVersion"
                  class="text-theme-special"
                  :to="`/task#version?name=${caseDetail?.softwareVersion}`">
                  {{ caseDetail?.softwareVersion }}
                </RouterLink>
                <template v-else>
                  --
                </template>
                <Button
                  type="link"
                  class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none transform-gpu translate-y-0.75"
                  @click="toEditVersion">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                </Button>
              </div>
            </template>
          </template>
        </Grid>
      </Toggle>
      <template v-if="!bigLayout">
        <Toggle
          :title="t('functionCase.detail.caseDetail.personnel')"
          class="mt-3.5">
          <Grid
            :columns="peopleInfoColumns"
            :dataSource="caseDetail"
            :spacing="20"
            :marginBottom="4"
            labelSpacing="10px"
            font-size="12px"
            class="pt-2 pl-5.5">
            <template #testerName="{text}">
              <template v-if="isEditTester">
                <SelectUser
                  ref="testerSelectRef"
                  v-model:value="testerIcContent"
                  class="flex-1"
                  size="small"
                  @blur="saveTester" />
              </template>
              <template v-else>
                <span>{{ text }}</span>
                <Icon
                  v-if="props.actionAuth.includes('edit')"
                  icon="icon-xiugai"
                  class="text-3.5 text-theme-special text-theme-text-hover cursor-pointer ml-2"
                  @click="handleEditTester" />
                <Button
                  v-if="props.actionAuth.includes('edit') && caseDetail.testerId !== userInfo?.id"
                  :loading="saveTesterLoading"
                  type="link"
                  size="small"
                  class="p-0 h-3.5 leading-3.5 ml-1"
                  @click="handleSetTester">
                  {{ t('functionCase.detail.caseDetail.assignToMe') }}
                </Button>
              </template>
            </template>
          </Grid>
        </Toggle>
        <Toggle
          :title="t('functionCase.detail.caseDetail.date')"
          class="mt-3.5">
          <Grid
            :columns="dateInfoColumns"
            :dataSource="caseDetail"
            :marginBottom="4"
            labelSpacing="10px"
            font-size="12px"
            class="pt-2 pl-5.5">
            <template #deadlineDate="{ text }">
              <div class="flex items-center relative w-full">
                <template v-if="!isEditDisabledDate">
                  {{ text }}
                  <Icon
                    v-if="props.actionAuth.includes('edit')"
                    class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer -mt-0.5"
                    icon="icon-shuxie"
                    @click="openEditDeadlineDate" />
                </template>
                <template v-else>
                  <DatePicker
                    ref="datePickertRef"
                    v-model:value="deadlineDate"
                    :allowClear="false"
                    :disabledDate="disabledDate"
                    :showTime="{ efaultValue: dayjs('00:00:00', 'HH:mm:ss') }"
                    size="small"
                    type="date"
                    class="w-full absolute -top-1.25"
                    @blur="editDeadlineDate" />
                </template>
              </div>
            </template>
          </Grid>
        </Toggle>
        <Toggle
          :title="t('functionCase.detail.caseDetail.reviewInfo')"
          class="mt-3.5">
          <Grid
            :columns="reviewInfoColumns"
            :dataSource="caseDetail"
            :marginBottom="4"
            labelSpacing="10px"
            font-size="12px"
            class="pt-2 pl-5.5">
            <template #oneReviewPass>
              {{ getOneReviewPass }}
            </template>
          </Grid>
        </Toggle>
        <Toggle
          :title="t('functionCase.detail.caseDetail.testInfo')"
          class="mt-3.5">
          <Grid
            :columns="testInfoColumns"
            :dataSource="caseDetail"
            :spacing="20"
            :marginBottom="4"
            labelSpacing="10px"
            font-size="12px"
            class="pt-2 pl-5.5">
            <template #oneTestPass>
              {{ getOneTestPass }}
            </template>
            <template #testResult="{text}">
              <TestResult :value="text" />
            </template>
          </Grid>
        </Toggle>
      </template>
      <Toggle
        v-model:open="preconditionExpand"
        class="mt-3.5">
        <template #title>
          <div class="flex items-center space-x-2">
            <span>{{ t('functionCase.detail.caseDetail.precondition') }}</span>
            <template v-if="isEditPrecondition">
              <Button
                class="font-normal text-theme-special"
                type="link"
                size="small"
                @click="savePrecondition">
                {{ t('functionCase.detail.caseDetail.save') }}
              </Button>
              <Button
                class="font-normal text-theme-special"
                type="link"
                size="small"
                @click="cancelEditPrecondition">
                {{ t('functionCase.detail.caseDetail.cancel') }}
              </Button>
            </template>
            <Icon
              v-else-if="props.actionAuth.includes('edit')"
              icon="icon-xiugai"
              class="text-3.5 text-theme-special text-theme-text-hover cursor-pointer"
              @click="handleEditPrecondition" />
          </div>
        </template>
        <template #default>
          <template v-if="isEditPrecondition">
            <RichEditor
              ref="preconditionRichRef"
              v-model:value="preconditionContent"
              :options="{ placeholder: t('functionCase.detail.caseDetail.inputPrecondition')}" />
            <div v-show="preconditionError" class="text-status-error">{{ t('functionCase.detail.caseDetail.descCharLimit') }}</div>
          </template>
          <template v-else>
            <template v-if="caseDetail?.precondition">
              <RichEditor :value="caseDetail.precondition" mode="view" />
            </template>
            <template v-else>
              <NoData
                :text="t('functionCase.detail.caseDetail.noData')"
                class="my-8"
                size="small" />
            </template>
          </template>
        </template>
      </Toggle>
      <Toggle
        v-model:open="conditionsExpand"
        class="mt-3.5">
        <template #title>
          <div class="flex items-center space-x-2">
            <span>{{ t('functionCase.detail.caseDetail.testSteps') }}</span>
            <template v-if="isEditSteps">
              <Button
                class="font-normal text-theme-special"
                type="link"
                size="small"
                @click="saveSteps">
                {{ t('actions.save') }}
              </Button>
              <Button
                class="font-normal text-theme-special"
                type="link"
                size="small"
                @click="cancelEditSteps">
                {{ t('actions.cancel') }}
              </Button>
            </template>
            <Icon
              v-else-if="props.actionAuth.includes('edit')"
              icon="icon-xiugai"
              class="text-3.5 text-theme-special text-theme-text-hover cursor-pointer"
              @click="handleEditSteps" />
          </div>
        </template>
        <template #default>
          <template v-if="isEditSteps || caseDetail?.steps?.length">
            <CaseStep
              v-model:value="stepsContent"
              class="mt-3 mx-2"
              :defaultValue="isEditSteps ? stepsContent : caseDetail.steps"
              :readonly="!isEditSteps" />
          </template>
          <template v-else>
            <NoData
              :text="t('functionCase.detail.caseDetail.noData')"
              size="small"
              class="my-8" />
          </template>
        </template>
      </Toggle>
      <Toggle
        v-model:open="remarkExpand"
        class="mt-3.5">
        <template #title>
          <div class="flex items-center space-x-2">
            <span>{{ t('functionCase.detail.caseDetail.description') }}</span>
            <template v-if="isEditDescription">
              <Button
                class="font-normal text-theme-special"
                type="link"
                size="small"
                @click="saveDescription">
                {{ t('actions.save') }}
              </Button>
              <Button
                class="font-normal text-theme-special"
                type="link"
                size="small"
                @click="cancelEditDescription">
                {{ t('actions.cancel') }}
              </Button>
            </template>
            <Icon
              v-else-if="props.actionAuth.includes('edit')"
              icon="icon-xiugai"
              class="text-3.5 cursor-pointer text-theme-special text-theme-text-hover "
              @click="handleEditDescription" />
          </div>
        </template>
        <template #default>
          <template v-if="isEditDescription">
            <div class="mt-3 mx-2">
              <RichEditor
                ref="descRichRef"
                v-model:value="descriptionContent"
                class="add-case" />
              <div v-show="descError" class="text-status-error">{{ t('functionCase.detail.caseDetail.descCharLimit2000') }}</div>
            </div>
          </template>
          <template v-else-if="caseDetail?.description">
            <RichEditor
              :value="caseDetail.description"
              mode="view" />
          </template>
          <template v-else>
            <NoData
              :text="t('functionCase.detail.caseDetail.noData')"
              size="small"
              class="mt-20" />
          </template>
        </template>
      </Toggle>
      <template v-if="!bigLayout">
        <Toggle
          :title="t('functionCase.detail.caseDetail.attachments')"
          class="mt-3.5">
          <div
            style="height: 108px; border-color: rgba(0, 119, 255);background-color: rgba(0, 119, 255, 4%);"
            class="border border-dashed rounded flex flex-col px-2 py-1 mx-2 mt-3 mb-3"
            :class="attachmentsData.length?'justify-between':'justify-center'"
            @mouseenter="handleMouseenter"
            @mouseleave="handleMouseleave">
            <template v-if="attachmentsData.length">
              <div style="height: 90px;scrollbar-gutter: stable;" class="overflow-hidden hover:overflow-y-auto -mr-2 pr-1">
                <div
                  v-for="(item,index) in caseDetail?.attachments"
                  :key="index"
                  :class="{'rounded-b':index===caseDetail?.attachments.length-1}"
                  class="flex items-center justify-between text-3 leading-3">
                  <div
                    :title="item.name"
                    class="truncate text-theme-sub-content leading-4 cursor-pointer"
                    style="width: 250px;"
                    @click="handleDownload(item.url)">
                    {{ item.name }}
                  </div>
                  <Icon
                    icon="icon-qingchu"
                    class="text-theme-special text-theme-text-hover cursor-pointer flex-shrink-0 leading-4 text-3.5"
                    @click="cancelFile(index)" />
                </div>
              </div>
            </template>
            <div class="flex justify-around h-6">
              <template v-if="props.actionAuth.includes('edit') && (caseDetail?.attachments?.length || 0) < 5">
                <Upload
                  :fileList="[]"
                  name="file"
                  :customRequest="upLoadFile">
                  <Icon icon="icon-shangchuan" class="text-theme-special mr-1" />
                  <span class="text-3 text-theme-text-hover">{{ t('functionCase.detail.caseDetail.uploadAttachments') }}</span>
                </Upload>
              </template>
            </div>
          </div>
        </Toggle>
      </template>
    </div>
    <div v-if="bigLayout" class="w-75 flex-none ml-2">
      <Toggle :title="t('functionCase.detail.caseDetail.personnel')">
        <Grid
          :columns="peopleInfoColumns"
          :dataSource="caseDetail"
          :spacing="20"
          :marginBottom="4"
          labelSpacing="10px"
          font-size="12px"
          class="pt-2 pl-5.5">
          <template #testerName="{text}">
            <template v-if="isEditTester">
              <SelectUser
                ref="testerSelectRef"
                v-model:value="testerIcContent"
                class="flex-1 w-full"
                size="small"
                @blur="saveTester" />
            </template>
            <template v-else>
              <span>{{ text }}</span>
              <Icon
                v-if="props.actionAuth.includes('edit')"
                icon="icon-xiugai"
                class="text-3.5 text-theme-special text-theme-text-hover cursor-pointer ml-2"
                @click="handleEditTester" />
              <Button
                v-if="props.actionAuth.includes('edit') && caseDetail.testerId !== userInfo?.id"
                :loading="saveTesterLoading"
                type="link"
                size="small"
                class="p-0 h-3.5 leading-3.5 ml-1"
                @click="handleSetTester">
                {{ t('functionCase.detail.caseDetail.assignToMe') }}
              </Button>
            </template>
          </template>
        </Grid>
      </Toggle>

      <Toggle
        :title="t('functionCase.detail.caseDetail.date')"
        class="mt-3.5">
        <Grid
          :columns="dateInfoColumns"
          :dataSource="caseDetail"
          :spacing="20"
          :marginBottom="4"
          labelSpacing="10px"
          font-size="12px"
          class="pt-2 pl-5.5">
          <template #deadlineDate="{ text }">
            <div class="flex items-center relative w-full">
              <template v-if="!isEditDisabledDate">
                {{ text }}
                <Icon
                  v-if="props.actionAuth.includes('edit')"
                  class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer -mt-0.5"
                  icon="icon-shuxie"
                  @click="openEditDeadlineDate" />
              </template>
              <template v-else>
                <DatePicker
                  ref="datePickertRef"
                  v-model:value="deadlineDate"
                  :allowClear="false"
                  :disabledDate="disabledDate"
                  :showTime="{ efaultValue: dayjs('00:00:00', 'HH:mm:ss') }"
                  size="small"
                  type="date"
                  class="w-full absolute -top-1.25"
                  @blur="editDeadlineDate" />
              </template>
            </div>
          </template>
        </Grid>
      </Toggle>

      <Toggle
        :title="t('functionCase.detail.caseDetail.reviewInfo')"
        class="mt-3.5">
        <Grid
          :columns="reviewInfoColumns"
          :dataSource="caseDetail"
          :spacing="20"
          :marginBottom="4"
          labelSpacing="10px"
          font-size="12px"
          class="pt-2 pl-5.5">
          <template #oneReviewPass>
            {{ getOneReviewPass }}
          </template>
        </Grid>
      </Toggle>

      <Toggle
        :title="t('functionCase.detail.caseDetail.testInfo')"
        class="mt-3.5">
        <Grid
          :columns="testInfoColumns"
          :dataSource="caseDetail"
          :spacing="20"
          :marginBottom="4"
          labelSpacing="10px"
          font-size="12px"
          class="pt-2 pl-5.5">
          <template #oneTestPass>
            {{ getOneTestPass }}
          </template>
          <template #testResult="{text}">
            <TestResult :value="text" />
          </template>
        </Grid>
      </Toggle>
      <!-- <refTasks
        class="mt-3.5"
        :dataSource="caseDetail?.refTaskInfos"
        :projectId="projectId"
        :caseId="caseDetail?.id"
        :case="caseDetail?.refCaseInfos"
        @ok="editOk" />
      <assocCase
        class="mt-3.5"
        :dataSource="caseDetail?.refCaseInfos"
        :projectId="projectId"
        :caseId="caseDetail?.id"
        :taskList="caseDetail?.refTaskInfos"
        @ok="editOk" /> -->
      <Toggle
        :title="t('functionCase.detail.caseDetail.attachments')"
        class="mt-3.5">
        <div
          style="height: 108px; border-color: rgba(0, 119, 255);background-color: rgba(0, 119, 255, 4%);"
          class="border border-dashed rounded flex flex-col px-2 py-1 mx-2 mt-3.5 mb-7"
          :class="attachmentsData.length?'justify-between':'justify-center'"
          @mouseenter="handleMouseenter"
          @mouseleave="handleMouseleave">
          <template v-if="attachmentsData.length">
            <div style="height: 90px;scrollbar-gutter: stable;" class="overflow-hidden hover:overflow-y-auto -mr-2 pr-1">
              <div
                v-for="(item,index) in caseDetail?.attachments"
                :key="index"
                :class="{'rounded-b':index===caseDetail?.attachments.length-1}"
                class="flex items-center justify-between text-3 leading-3">
                <div
                  :title="item.name"
                  class="truncate text-theme-sub-content leading-4 cursor-pointer"
                  style="width: 250px;"
                  @click="handleDownload(item.url)">
                  {{ item.name }}
                </div>
                <Icon
                  icon="icon-qingchu"
                  class="text-theme-special text-theme-text-hover cursor-pointer flex-shrink-0 leading-4 text-3.5"
                  @click="cancelFile(index)" />
              </div>
            </div>
          </template>
          <div class="flex justify-around h-6">
            <template v-if="props.actionAuth.includes('edit') && (caseDetail?.attachments?.length || 0) < 5">
              <Upload
                :fileList="[]"
                name="file"
                :customRequest="upLoadFile">
                <Icon icon="icon-shangchuan" class="text-theme-special mr-1" />
                <span class="text-3 text-theme-text-hover">{{ t('functionCase.detail.caseDetail.uploadAttachments') }}</span>
              </Upload>
            </template>
          </div>
        </div>
      </Toggle>
    </div>
  </div>
</template>
