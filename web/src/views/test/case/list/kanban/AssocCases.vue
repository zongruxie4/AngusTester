<script setup lang="ts">
import { computed, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, NoData, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { funcCase } from '@/api/tester';

import { CaseDetail } from '@/views/test/types';
import { CaseInfoEditProps } from '@/views/test/case/list/types';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  canEdit: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'loadingChange', value: boolean): void;
  (event: 'change', value: CaseDetail): void;
  (event: 'update:dataSource', value: CaseDetail): void;
}>();

const isEditing = ref(false);
const selectedCaseIds = ref<string[]>([]);

/**
 * Enter edit mode to modify associated cases
 * Shows multi-select control and prepares local selection state
 */
const enterEdit = () => {
  isEditing.value = true;
};

/**
 * Cancel edit mode without persisting changes
 * Restores view-only state
 */
const cancelEdit = () => {
  isEditing.value = false;
};

/**
 * Persist associated case changes and refresh detail
 * Emits loading states and reloads the latest case detail on success
 */
const submitChanges = async () => {
  const params = [{
    id: caseId.value,
    refCaseIds: selectedCaseIds.value
  }];
  isEditing.value = false;
  emitLoadingChange(true);
  const [error] = await funcCase.updateCase(params);
  emitLoadingChange(false);
  if (error) {
    return;
  }
  refreshCaseDetail();
};

/**
 * Handle multi-select change for associated cases
 * Keeps local selected ids in sync
 */
const handleSelectChange = (ids:string[]) => {
  selectedCaseIds.value = ids;
};

/**
 * Emit loading state to parent container
 */
const emitLoadingChange = (value:boolean) => {
  emit('loadingChange', value);
};

/**
 * Reload full case detail after association changes
 */
const refreshCaseDetail = async () => {
  const id = props.dataSource?.id;
  if (!id) {
    return;
  }

  emitLoadingChange(true);
  const [error, res] = await funcCase.getCaseDetail(id);
  emitLoadingChange(false);
  if (error) {
    return;
  }

  const data = res?.data || {};
  emit('change', data);
  emit('update:dataSource', data);
};

const caseId = computed(() => {
  return props.dataSource?.id;
});

const associatedCaseList = computed(() => {
  return props.dataSource?.refCaseInfos?.map(item => {
    return {
      ...item,
      linkUrl: `/test#cases?id=${item.id}&projectId=${props.projectId}`
    };
  }) || [];
});

const associatedCaseIds = computed(() => {
  return associatedCaseList.value.map(item => item.id);
});
</script>

<template>
  <div class="h-full text-3 leading-5 pl-5 overflow-y-auto">
    <div class="flex items-center text-theme-title mb-2.5">
      <span class="font-semibold">{{ t('testCase.kanbanView.assocCase.title') }}</span>
      <Button
        v-if="props.canEdit"
        v-show="!isEditing"
        type="link"
        class="flex-shrink-0 ml-2 p-0 h-3.5 leading-3.5 border-none"
        @click="enterEdit">
        <Icon icon="icon-shuxie" class="text-3.5" />
      </Button>
    </div>

    <template v-if="!isEditing">
      <div v-if="associatedCaseList.length" class="w-full space-y-1.5 truncate">
        <RouterLink
          v-for="item in associatedCaseList"
          :key="item.id"
          :to="item.linkUrl"
          target="_blank"
          class="flex items-center overflow-hidden">
          <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0" />
          <span class="truncate ml-1.5">{{ item.name }}</span>
        </RouterLink>
      </div>

      <NoData
        v-else
        size="small"
        style="height: calc(100% - 30px);" />
    </template>

    <template v-else>
      <Select
        :value="associatedCaseIds"
        showSearch
        internal
        allowClear
        :fieldNames="{ label: 'name', value: 'id' }"
        :maxTagCount="10"
        :maxTagTextLength="15"
        :maxTags="20"
        :action="`${TESTER}/func/case?projectId=${props.projectId}&fullTextSearch=true`"
        :placeholder="t('testCase.kanbanView.assocCase.placeholder')"
        mode="multiple"
        @change="handleSelectChange">
        <template #option="record">
          <div class="flex items-center leading-4.5 overflow-hidden">
            <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0" />
            <div class="link truncate ml-1.5" :title="record.name">
              {{ record.name }}
            </div>
            <div
              v-if="record.overdue"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
              style="transform: scale(0.9);color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu">{{ t('status.overdue') }}</span>
            </div>
          </div>
        </template>
      </Select>

      <div class="flex items-center space-x-2.5 mt-2.5 justify-end">
        <Button
          type="default"
          size="small"
          @click="cancelEdit">
          {{ t('actions.cancel') }}
        </Button>
        <Button
          type="primary"
          size="small"
          @click="submitChanges">
          {{ t('actions.confirm') }}
        </Button>
      </div>
    </template>
  </div>
</template>
