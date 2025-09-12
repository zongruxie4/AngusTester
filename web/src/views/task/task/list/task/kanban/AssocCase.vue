<script setup lang="ts">
import { computed, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, NoData, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TaskInfo } from '../../../../types';
import { TaskInfoProps } from '@/views/task/task/list/task/types';

const props = withDefaults(defineProps<TaskInfoProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: Partial<TaskInfo>): void;
}>();

const editFlag = ref(false);
const caseIds = ref<string[]>([]);

const toEdit = () => {
  editFlag.value = true;
};

const cancel = () => {
  editFlag.value = false;
};

const ok = async () => {
  const params = {
    refCaseIds: caseIds.value
  };
  editFlag.value = false;
  const [error] = await task.updateTask(taskId.value, params);
  if (error) {
    return;
  }

  emit('change', { ...props.dataSource, ...params });
};

const selectChange = (ids:string[]) => {
  caseIds.value = ids;
};

const loadData = async (): Promise<Partial<TaskInfo>> => {
  emit('loadingChange', true);
  const [error, res] = await task.getTaskDetail(taskId.value);
  emit('loadingChange', false);
  if (error || !res?.data) {
    return { id: taskId.value };
  }

  return res.data;
};

const taskId = computed(() => {
  return props.dataSource?.id;
});

const refCaseList = computed(() => {
  return props.dataSource?.refCaseInfos?.map(item => {
    return {
      ...item,
      linkUrl: `/function#cases?id=${item.id}&projectId=${props.projectId}`
    };
  }) || [];
});

const refCaseIds = computed(() => {
  return refCaseList.value.map(item => item.id);
});
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-y-auto">
    <div class="flex items-center text-theme-title mb-2.5">
      <span class="font-semibold">{{ t('task.assocCase.title') }}</span>
      <Button
        v-show="!editFlag"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="toEdit">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>

    <template v-if="!editFlag">
      <div v-if="refCaseList.length" class="w-full space-y-1.5 truncate">
        <RouterLink
          v-for="item in refCaseList"
          :key="item.id"
          :to="item.linkUrl"
          target="_blank"
          class="flex items-center overflow-hidden">
          <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0" />
          <span class="truncate ml-1">{{ item.name }}</span>
        </RouterLink>
      </div>

      <NoData
        v-else
        size="small"
        style="height: calc(100% - 30px);" />
    </template>

    <template v-else>
      <Select
        :value="refCaseIds"
        showSearch
        internal
        allowClear
        :fieldNames="{ label: 'name', value: 'id' }"
        :maxTagCount="10"
        :maxTagTextLength="15"
        :maxTags="20"
        :action="`${TESTER}/func/case?projectId=${props.projectId}&fullTextSearch=true`"
        class="w-full"
        :placeholder="t('task.assocCase.selectCasePlaceholder')"
        mode="multiple"
        @change="selectChange">
        <template #option="record">
          <div class="flex items-center leading-4.5 overflow-hidden">
            <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0" />
            <div class="link truncate ml-1" :title="record.name">
              {{ record.name }}
            </div>
            <div
              v-if="record.overdue"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
              style="transform: scale(0.9);color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu">{{ t('task.assocCase.overdue') }}</span>
            </div>
          </div>
        </template>
      </Select>

      <div class="flex items-center space-x-2.5 mt-2.5 justify-end">
        <Button
          type="default"
          size="small"
          @click="cancel">
          {{ t('actions.cancel') }}
        </Button>
        <Button
          type="primary"
          size="small"
          @click="ok">
          {{ t('actions.confirm') }}
        </Button>
      </div>
    </template>
  </div>
</template>
