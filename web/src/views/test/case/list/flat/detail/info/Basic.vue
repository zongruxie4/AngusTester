<script setup lang="ts">
import { computed, nextTick, ref } from 'vue';
import { Grid, Icon, Input, Select, ReviewStatus, Toggle } from '@xcan-angus/vue-ui';
import { Button, Tag } from 'ant-design-vue';
import { Priority, TESTER, utils, SearchCriteria } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { SoftwareVersionStatus } from '@/enums/enums';
import { CaseActionAuth } from '@/views/test/case/types';

import SelectEnum from '@/components/enum/SelectEnum.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';
import TestResult from '@/components/TestResult/index.vue';

interface Props {
  id?: number;
  dataSource?: CaseDetail;
  projectId?: string;
  taskId?: number;
  actionAuth?: CaseActionAuth[];
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  dataSource: undefined,
  projectId: undefined,
  taskId: undefined,
  actionAuth: () => ([])
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'change', value: Partial<CaseDetail>): void;
  (event: 'loadingChange', value: boolean): void;
}>();

const { t } = useI18n();

// Refs for editing states
const nameInputRef = ref();
const tagsSelectRef = ref();
const priorityRef = ref();
const versionRef = ref();

// Editing states
const isEditName = ref(false);
const editNameLoading = ref(false);
const isEditPriority = ref(false);
const editPriorityLoading = ref(false);
const priority = ref();
const tagsIds = ref<string[]>([]);
const defaultTags = ref<{[key: string]: { label: string; value: string }}>({});
const isEditTag = ref(false);
const editTagLoading = ref(false);
const isVersionEditMode = ref(false);
const selectedVersionValue = ref();
const infoExpand = ref(true);

// Computed properties
const infoColumns = computed(() => [
  [
    { label: t('common.name'), dataIndex: 'name' },
    { label: t('common.id'), dataIndex: 'id' },
    { label: t('common.code'), dataIndex: 'code' },
    { label: t('common.reviewStatus'), dataIndex: 'reviewStatus' },
    { label: t('common.version'), dataIndex: 'version' },
    { label: t('common.softwareVersion'), dataIndex: 'softwareVersion' },
    { label: t('common.priority'), dataIndex: 'priority' },
    {
      label: t('common.unplanned'),
      dataIndex: 'unplanned',
      customRender: ({ text }) => text ? t('status.yes') : t('status.no')
    }
  ],
  [
    { label: t('common.tag'), dataIndex: 'tags' },
    { label: t('common.plan'), dataIndex: 'planName' },
    { label: t('common.module'), dataIndex: 'moduleName' },
    {
      label: t('common.testResult'),
      dataIndex: 'testResult'
    }
  ]
]);

// Name editing functions
const openEditName = () => {
  isEditName.value = true;
  nextTick(() => {
    nameInputRef.value.focus();
  });
};

const editName = async (event) => {
  if (event.target.value === props.dataSource?.name || !event.target.value || !props.dataSource) {
    isEditName.value = false;
    return;
  }

  editNameLoading.value = true;
  const [error] = await testCase.putName(props.dataSource.id, event.target.value);
  editNameLoading.value = false;
  isEditName.value = false;
  if (error) {
    return;
  }
  emit('change', { name: event.target.value });
};

// Priority editing functions
const openEditPriority = () => {
  isEditPriority.value = true;
  priority.value = props.dataSource?.priority?.value;
  nextTick(() => {
    priorityRef.value.focus();
  });
};

const editPriority = async (value: Priority) => {
  if (editPriorityLoading.value || value === props.dataSource?.priority?.value || !props.dataSource) {
    isEditPriority.value = false;
    return;
  }

  editPriorityLoading.value = true;
  const [error] = await testCase.putPriority(props.dataSource.id, value);
  editPriorityLoading.value = false;
  isEditPriority.value = false;
  if (error) {
    return;
  }
  emit('change', { priority: { value, message: '' } });
};

// Tag editing functions
const openEditTag = () => {
  tagsIds.value = (props.dataSource?.tags || [])?.map(item => {
    const idStr = item.id.toString();
    defaultTags.value[idStr] = { label: item.name, value: idStr };
    return idStr;
  }) || [];
  isEditTag.value = true;
  nextTick(() => {
    tagsSelectRef.value.focus();
  });
};

const editTag = async () => {
  if (!props.dataSource) {
    isEditTag.value = false;
    return;
  }

  if (editTagLoading.value || tagsIds.value?.length > 5) {
    isEditTag.value = false;
    return;
  }
  const oldTagIds = props.dataSource?.tags?.map(item => item.id.toString()) || [];
  const isEqual = utils.deepCompare(oldTagIds, tagsIds.value);
  if (isEqual) {
    isEditTag.value = false;
    return;
  }
  editTagLoading.value = true;
  const tagIdsAsNumbers = tagsIds.value.map(id => parseInt(id));
  const [error] = await testCase.putTag(props.dataSource.id, { tagIds: tagsIds.value.length ? tagIdsAsNumbers : null });
  editTagLoading.value = false;
  isEditTag.value = false;
  if (error) {
    return;
  }
  // Convert tag IDs back to tag objects for the change event
  const updatedTags = tagsIds.value.map(id => {
    const tagInfo = defaultTags.value[id];
    return tagInfo ? { id: parseInt(id), name: tagInfo.label } : null;
  }).filter((tag): tag is { id: number; name: string } => tag !== null);
  emit('change', { tags: updatedTags });
};

// Version editing functions
const openVersionEditMode = () => {
  isVersionEditMode.value = true;
  selectedVersionValue.value = props.dataSource?.softwareVersion;
  nextTick(() => {
    setTimeout(() => {
      if (typeof versionRef.value?.focus === 'function') {
        versionRef.value?.focus();
      }
    }, 100);
  });
};

const handleVersionChange = (newVersionValue) => {
  selectedVersionValue.value = newVersionValue;
};

const handleVersionBlur = async () => {
  if (!props.dataSource) {
    isVersionEditMode.value = false;
    return;
  }

  const versionValue = selectedVersionValue.value;
  if (versionValue === props.dataSource?.softwareVersion) {
    isVersionEditMode.value = false;
    return;
  }

  emit('loadingChange', true);
  const [error] = await testCase.updateCase([{ softwareVersion: versionValue || '', id: props.dataSource.id }]);
  emit('loadingChange', false);
  isVersionEditMode.value = false;
  if (error) {
    return;
  }
  emit('change', { softwareVersion: versionValue });
};
</script>
<template>
  <Toggle
    v-model:open="infoExpand"
    :title="t('common.basicInfo')">
    <Grid
      :columns="infoColumns"
      :dataSource="dataSource"
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
              :placeholder="t('common.name')"
              @blur="editName" />
          </template>
          <template v-else>
            <div class="flex items-center">
              <span>{{ text }}</span>
              <Button
                v-if="props.actionAuth.includes('edit')"
                type="link"
                class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
                @click="openEditName">
                <Icon icon="icon-shuxie" class="text-3.5" />
              </Button>
            </div>
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
              :placeholder="t('common.priority')"
              @blur="editPriority(priority as Priority)">
              <template #option="item">
                <TaskPriority :value="item as any" />
              </template>
            </SelectEnum>
          </template>
          <template v-else>
            <div class="flex items-center">
              <TaskPriority :value="text" />
              <Button
                v-if="props.actionAuth.includes('edit')"
                type="link"
                class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
                @click="openEditPriority">
                <Icon icon="icon-shuxie" class="text-3.5" />
              </Button>
            </div>
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
              :fieldNames="{ label: 'label', value: 'value' }"
              :maxTags="5"
              :placeholder="t('common.tag')"
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
              <Button
                v-if="props.actionAuth.includes('edit')"
                type="link"
                class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
                @click="openEditTag">
                <Icon icon="icon-shuxie" class="text-3.5" />
              </Button>
            </div>
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
            v-if="dataSource?.overdue"
            class="border border-status-error rounded px-0.5 ml-5"
            style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
            {{ t('status.overdue') }}
          </div>
        </div>
      </template>

      <template #version="{text}">
        <span v-if="text">v{{ text }}</span>
        <template v-else>--</template>
      </template>

      <template #softwareVersion>
        <template v-if="isVersionEditMode">
          <Select
            ref="versionRef"
            v-model:value="selectedVersionValue"
            allowClear
            :placeholder="t('common.placeholders.selectSoftwareVersion')"
            lazy
            class="w-full max-w-60"
            :action="`${TESTER}/software/version?projectId=${projectId}`"
            :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: SearchCriteria.OpEnum.In}]}"
            :fieldNames="{value:'name', label: 'name'}"
            @blur="handleVersionBlur"
            @change="handleVersionChange">
          </Select>
        </template>

        <template v-else>
          <div class="flex items-center">
            <RouterLink
              v-if="dataSource?.softwareVersion"
              class="text-theme-special"
              :to="`/task#version?name=${dataSource?.softwareVersion}`">
              {{ dataSource?.softwareVersion }}
            </RouterLink>
            <template v-else>
              --
            </template>
            <Button
              v-if="props.actionAuth.includes('edit')"
              type="link"
              class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
              @click="openVersionEditMode">
              <Icon icon="icon-shuxie" class="text-3.5" />
            </Button>
          </div>
        </template>
      </template>
    </Grid>
  </Toggle>
</template>
<style scoped>
:deep(.toggle-title) {
  font-size: 0.875rem;
}

.border-none {
  border: none;
}
</style>
