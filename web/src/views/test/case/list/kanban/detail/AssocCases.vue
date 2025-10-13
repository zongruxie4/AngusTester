<script setup lang="ts">
import { computed, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, NoData, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { testCase } from '@/api/tester';
import { TestMenuKey } from '@/views/test/menu';
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
  const [error] = await testCase.updateCase(params);
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
  const [error, res] = await testCase.getCaseDetail(id);
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
      linkUrl: `/test#${TestMenuKey.CASES}?id=${item.id}&projectId=${props.projectId}`
    };
  }) || [];
});

const associatedCaseIds = computed(() => {
  return associatedCaseList.value.map(item => item.id);
});
</script>

<template>
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <div class="flex items-center justify-between">
        <h3 class="basic-info-title">{{ t('common.assocCases') }}</h3>
        <Button
          v-if="props.canEdit"
          v-show="!isEditing"
          type="link"
          class="edit-btn"
          @click="enterEdit">
          <Icon icon="icon-shuxie" />
        </Button>
      </div>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <template v-if="!isEditing">
          <div v-if="associatedCaseList.length" class="w-full space-y-1.5 truncate">
            <RouterLink
              v-for="item in associatedCaseList"
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
            :value="associatedCaseIds"
            showSearch
            internal
            allowClear
            :fieldNames="{ label: 'name', value: 'id' }"
            :maxTagCount="10"
            :maxTagTextLength="15"
            :maxTags="20"
            :action="`${TESTER}/func/case?projectId=${props.projectId}&fullTextSearch=true`"
            class="w-full"
            :placeholder="t('common.placeholders.selectCase')"
            mode="multiple"
            @change="handleSelectChange">
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
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Edit button styles */
.edit-btn {
  flex-shrink: 0;
  padding: 0;
  height: 16px;
  width: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: none;
  color: #1890ff !important;
  cursor: pointer;
  transition: color 0.2s;
}

.edit-btn:focus {
  color: #1890ff !important;
  background: none !important;
  border: none !important;
  box-shadow: none !important;
}

.edit-btn:hover {
  color: #1890ff;
}

.edit-btn .anticon {
  font-size: 12px;
}
</style>
