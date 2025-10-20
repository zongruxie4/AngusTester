<script lang="ts" setup>
import { computed, ref, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { Grid, Icon, Input, Select } from '@xcan-angus/vue-ui';
import { Tag, Button } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';

import { testCase } from '@/api/tester';
import TaskPriority from '@/components/task/TaskPriority/index.vue';

const { t } = useI18n();

interface Props {
  caseInfo?: {[key: string]: any},
  projectId: string;
  readonly?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined,
  readonly: true
});

const emits = defineEmits<{(e: 'change')}>();

const infoColumns = computed(() => [
  [
    {
      label: t('common.name'),
      dataIndex: 'name'
    },
    {
      label: t('common.id'),
      dataIndex: 'id'
    },
    {
      label: t('common.code'),
      dataIndex: 'code'
    },
    {
      label: t('common.version'),
      dataIndex: 'version'
    },
    {
      label: t('common.softwareVersion'),
      dataIndex: 'softwareVersion'
    },
    {
      label: t('common.priority'),
      dataIndex: 'priority'
    },
    {
      label: t('common.tag'),
      dataIndex: 'tags'
    },
    {
      label: t('common.plan'),
      dataIndex: 'planName'
    },
    {
      label: t('common.module'),
      dataIndex: 'moduleName'
    },
    {
      label: t('common.evalWorkload'),
      dataIndex: 'evalWorkload',
      customRender: ({ text }) => text || '--'
    },
    {
      label: t('common.actualWorkload'),
      dataIndex: 'actualWorkload',
      customRender: ({ text }) => text || '--'
    }
  ]
]);

// eidt case name
const nameEditable = ref(false);
const nameValue = ref();
const nameEditRef = ref();
const handleEditName = (text) => {
  nameValue.value = text;
  nameEditable.value = true;
};
const saveName = async (text) => {
  if (!(nameValue.value.trim()) || nameValue.value === text) {
    nameEditable.value = false;
    return;
  }
  const [error] = await testCase.putName(props?.caseInfo?.id, nameValue.value);
  nameEditable.value = false;
  if (error) {
    return;
  }
  emits('change');
};

// edit tag
const isEditTag = ref(false);
const tagsIds = ref([]);
const tagsSelectRef = ref();
const defaultTags = ref<{[key: string]: { name: string; id: string }}>({});

const openEditTag = () => {
  tagsIds.value = (props.caseInfo?.tags || [])?.map(item => {
    defaultTags.value[item.id] = { id: item.id, name: item.name };
    return item.id;
  }) || [];
  isEditTag.value = true;
  nextTick(() => {
    tagsSelectRef.value.focus();
  });
};

const saveTags = async () => {
  if (tagsIds.value?.length > 5) {
    isEditTag.value = false;
    return;
  }

  const oldTagIds = props.caseInfo?.tags?.map(item => item.id) || [];
  const equalFlag = isEqual(oldTagIds, tagsIds.value);
  if (equalFlag) {
    isEditTag.value = false;
    return;
  }
  const params = { tagIds: tagsIds.value.length ? tagsIds.value : null };

  const [error] = await testCase.putTag(props?.caseInfo?.id, params);

  isEditTag.value = false;
  if (error) {
    return;
  }
  emits('change');
};

// edit evalWorkload
const isEditEvalWorkload = ref(false);
const evalWorkloadInputRef = ref();
const evalWorkloadValue = ref();
const openEditEvalWorkload = () => {
  evalWorkloadValue.value = props?.caseInfo?.evalWorkload;
  isEditEvalWorkload.value = true;
  nextTick(() => {
    evalWorkloadInputRef.value.focus();
  });
};

const saveEvalWorkload = async () => {
  if (+evalWorkloadValue.value === (+props.caseInfo?.evalWorkload) || (!evalWorkloadValue.value && !props.caseInfo?.evalWorkload)) {
    isEditEvalWorkload.value = false;
    return;
  }

  const [error] = await testCase.putEvalWorkload(props?.caseInfo?.id, { workload: evalWorkloadValue.value });

  isEditEvalWorkload.value = false;
  if (error) {
    return;
  }
  emits('change');
};

</script>
<template>
  <div class="bg-white rounded-lg p-6">
    <Grid
      :columns="infoColumns"
      :dataSource="caseInfo"
      :spacing="24"
      :marginBottom="6"
      valueClass="min-w-0"
      labelSpacing="12px"
      font-size="14px"
      class="case-basic-info-grid">
      <template #name="{text}">
        <template v-if="nameEditable">
          <Input
            ref="nameEditRef"
            v-model:value="nameValue"
            @blur="saveName(text)" />
        </template>
        <template v-else>
          <div class="flex space-x-2 items-center">
            <span class="truncate">{{ text }}</span>
            <Button
              v-show="!props.readonly"
              size="small"
              type="link"
              @click="handleEditName(text)">
              <Icon icon="icon-xiugai" class="text-theme-special" />
            </Button>
          </div>
        </template>
      </template>

      <template #version="{text}">
        <span v-if="text" class="px-2 py-1 bg-gray-100 text-gray-700 rounded">{{ text }}</span>
        <span v-else class="text-gray-400 text-sm">--</span>
      </template>

      <template #softwareVersion="{text}">
        <span v-if="text" class="px-2 py-1 bg-gray-100 text-gray-700 rounded">{{ text }}</span>
        <span v-else class="text-gray-400 text-sm">--</span>
      </template>

      <template #priority="{text}">
        <TaskPriority :value="text" />
      </template>

      <template #planName="{text}">
        <div class="flex items-center text-gray-700">
          <Icon icon="icon-jihua" class="mr-2 text-blue-500" />
          <span class="font-medium">{{ text }}</span>
        </div>
      </template>

      <template #moduleName="{text}">
        <div v-if="text" class="flex items-center">
          <Tag class="px-2 bg-gray-100 text-gray-700 border-gray-200 rounded-full flex items-center">
            <Icon icon="icon-mokuai" class="mr-2 text-gray-500" />
            <span>{{ text }}</span>
          </Tag>
        </div>
        <span v-else class="text-gray-400 text-sm">--</span>
      </template>

      <template #tags="{text}">
        <template v-if="isEditTag">
          <Select
            ref="tagsSelectRef"
            v-model:value="tagsIds"
            showSearch
            :defaultOptions="defaultTags"
            :fieldNames="{ label: 'name', value: 'id' }"
            :maxTags="5"
            :placeholder="t('testCase.kanbanView.infoBasic.addTagsPlaceholder')"
            :class="{'border-error':tagsIds && tagsIds.length > 5 }"
            :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
            mode="multiple"
            size="small"
            class="w-full"
            @blur="saveTags" />
        </template>
        <div v-else class="flex flex-wrap gap-2 items-center">
          <Tag
            v-for="(tag, index) in (text || [])"
            :key="tag.id"
            class="px-2 text-sm font-medium bg-blue-50 text-blue-700 border-blue-200 rounded-full">
            {{ tag.name }}
          </Tag>
          <span v-if="!text?.length" class="text-gray-400 text-sm">--</span>

          <Button
            v-show="!props.readonly"
            size="small"
            type="link"
            @click="openEditTag">
            <Icon icon="icon-xiugai" class="text-theme-special" />
          </Button>
        </div>
      </template>

      <template #evalWorkload="{text}">
        <div class="flex items-center relative">
          <template v-if="isEditEvalWorkload">
            <Input
              ref="evalWorkloadInputRef"
              v-model:value="evalWorkloadValue"
              :allowClear="false"
              :autofocus="isEditEvalWorkload"
              :min="0.1"
              :max="1000"
              :placeholder="t('testCase.kanbanView.infoBasic.evalWorkloadPlaceholder')"
              dataType="float"
              size="small"
              class="w-65 absolute -top-1.25"
              @blur="saveEvalWorkload" />
          </template>

          <template v-else>
            {{ text || '--' }}
            <Icon
              v-show="!props.readonly"
              class="ml-2.5 text-3 leading-3 text-theme-special text-theme-text-hover cursor-pointer"
              icon="icon-shuxie"
              @click="openEditEvalWorkload" />
          </template>
        </div>
      </template>

      <template #actualWorkload="{text}">
        {{ text || '--' }}
      </template>
    </Grid>
  </div>
</template>

<style scoped>
:deep(.case-basic-info-grid) {
  .ant-descriptions-item-label {
    @apply text-gray-600 font-medium;
  }

  .ant-descriptions-item-content {
    @apply text-gray-900;
  }
}
</style>
