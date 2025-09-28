<script setup lang="ts">
import { computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import { Icon, Input, Modal, Scroll } from '@xcan-angus/vue-ui';
import { Checkbox, CheckboxGroup, Divider } from 'ant-design-vue';
import { AuthObjectType, duration } from '@xcan-angus/infra';
import { usePolicyManagement } from './composables/usePolicyManagement';

interface Props {
  visible: boolean;
  appId: string;
  type?: AuthObjectType;
  userId?: string;
  deptId?: string;
  groupId?: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  appId: undefined,
  userId: undefined,
  deptId: undefined,
  type: undefined,
  groupId: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'change', addIds: string[], addPolicies: { id: string; name: string }[]): void;
}>();

const { t } = useI18n();

// Use composable for policy management functionality
const {
  // Reactive data
  params,
  notify,
  dataList,
  checkedList,
  indeterminate,
  loading,

  // Computed properties
  action,

  // Methods
  onCheckAllChange,
  handleChange
} = usePolicyManagement(
  props.type || AuthObjectType.USER,
  props.appId,
  computed(() => {
    switch (props.type || AuthObjectType.USER) {
      case AuthObjectType.USER:
        return props.userId || '';
      case AuthObjectType.DEPT:
        return props.deptId || '';
      case AuthObjectType.GROUP:
        return props.groupId || '';
      default:
        return '';
    }
  }),
  computed(() => props.userId),
  computed(() => props.deptId),
  computed(() => props.groupId)
);

/**
 * Handle search input change with debouncing
 * @param event - The input change event
 */
const handleInputChange = debounce(duration.search, (event: any) => {
  const value = event.target.value;
  if (value) {
    params.value.filters[0] = { key: 'name', op: 'MATCH', value: value };
    return;
  }
  params.value.filters = [];
});

/**
 * Handle modal confirmation
 */
const handleOk = () => {
  const checkedPolicies = dataList.value.filter(item => checkedList.value.includes(item.id));
  // Pass the correct parameters to the change event
  // The parent component will handle the selectId parameter
  emit('change', checkedList.value, checkedPolicies);
  emit('update:visible', false);
};

/**
 * Handle modal cancellation
 */
const handleCancel = () => {
  emit('update:visible', false);
};

/**
 * Watch for visibility changes to refresh data
 */
watch(() => props.visible, newValue => {
  if (newValue) {
    notify.value++;
  }
});
</script>
<template>
  <Modal
    :title="t('app.config.members.modal.grantPolicy')"
    :visible="props.visible"
    :centered="true"
    :keyboard="true"
    :width="1160"
    class="my-modal"
    @cancel="handleCancel"
    @ok="handleOk">
    <div class="-mt-3">
      <div class="mb-2 flex space-x-2">
        <Input
          :placeholder="t('organization.placeholders.searchPolicy')"
          size="small"
          class="w-1/2"
          allowClear
          @change="handleInputChange">
          <template #suffix>
            <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
          </template>
        </Input>
      </div>
      <div class="flex py-0.5 bg-theme-form-head text-theme-title text-3 font-normal mb-1">
        <div class="pl-6 w-46">
          {{ t('common.id') }}
        </div>
        <div class="w-40 mr-2">
          {{ t('common.name') }}
        </div>
        <div class="w-60 mr-2">
          {{ t('common.code') }}
        </div>
        <div class="w-100 mr-2">
          {{ t('common.description') }}
        </div>
        <div class="w-20">
          {{ t('common.status') }}
        </div>
      </div>
      <Scroll
        v-model="loading"
        :action="action"
        class="pr-2 h-65"
        :params="params"
        :notify="notify"
        @change="handleChange">
        <CheckboxGroup
          v-model:value="checkedList"
          style="width: 100%;"
          class="space-y-2">
          <div
            v-for="item,index in dataList"
            :key="item.id"
            class="flex-1 items-center flex text-3 text-theme-content"
            :class="{'mt-2':index>0}">
            <Checkbox
              :value="item.id">
            </Checkbox>
            <div class="truncate w-40 ml-2 mt-0.5">{{ item.id }}</div>
            <div class="truncate w-40 mr-2 mt-0.5" :title="item.name">{{ item.name }}</div>
            <div class="truncate w-60 mr-2 mt-0.5" :title="item.code">{{ item.code }}</div>
            <div class="truncate w-100 mr-2 mt-0.5" :title="item.description">{{ item.description }}</div>
            <div class="truncate w-20 mt-0.5">{{ item.enabled ? t('status.enabled') : t('status.disabled') }}</div>
          </div>
        </CheckboxGroup>
      </Scroll>
      <Divider class="my-2" />
      <Checkbox :indeterminate="indeterminate" @change="onCheckAllChange">
        {{ t('actions.selectAll') }}
      </Checkbox>
    </div>
  </Modal>
</template>
