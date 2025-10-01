<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue';
import { Input, Select } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

// Interfaces
interface Props {
    value: {[userId: string]: string};
    members: {fullName: string; id: string}[];
    membersOptions: {fullName: string; id: string, value: string; label: string;}[];
}

// Props
const props = withDefaults(defineProps<Props>(), {
  value: () => ({}),
  members: () => ([]),
  membersOptions: () => ([])
});

// Emits
const emits = defineEmits<{(e: 'change', value: {[userId: string]: string})}>();

// Reactive data
const testerList = ref<{id?: string; fullName?: string; description: string}[]>([]);
const defaultUserOptions = ref({});

// Computed properties
const duplicateUserIds = computed(() => {
  const result: string[] = [];
  const seenIds: string[] = [];
  testerList.value.forEach(currentTester => {
    if (currentTester.id) {
      if (seenIds.includes(currentTester.id)) {
        result.push(currentTester.id);
      } else {
        seenIds.push(currentTester.id);
      }
    }
  });
  return result;
});

/**
 * Emits the current tester data to parent component.
 */
const emitTesterData = () => {
  emits('change', getTesterData());
};

/**
 * Handles user selection change event.
 * Adds a new empty row if all current rows have users selected.
 */
const handleUserSelectionChange = () => {
  if (testerList.value.every(tester => tester.id)) {
    testerList.value.push({ id: undefined, fullName: '', description: '' });
  }
  emitTesterData();
};

/**
 * Extracts tester data from the current list.
 * Returns an object mapping user IDs to their descriptions.
 */
const getTesterData = () => {
  const result = {};
  testerList.value.forEach(tester => {
    if (tester.id) {
      result[tester.id] = tester.description;
    }
  });
  return result;
};

// Lifecycle hooks
onMounted(() => {
  watch(() => props.value, () => {
    if (props.value) {
      const existingTesterData = Object.keys(props.value).map(userId => {
        const fullName = (props.members || []).find(member => member.id === userId)?.fullName;
        defaultUserOptions.value[userId] = { label: fullName, value: userId, fullName, id: userId };
        if (testerList.value.find(tester => tester.id === userId)) {
          return null;
        }
        return {
          id: userId,
          description: props.value[userId],
          fullName
        };
      }).filter((item): item is {id: string; description: string; fullName: string | undefined} => item !== null);
      testerList.value.unshift(...existingTesterData);
    }

    if (testerList.value.find(tester => !tester.id)) {
      return;
    }
    testerList.value.push({ id: undefined, fullName: '', description: '' });
  }, {
    immediate: true
  });
});

// Exposed methods
defineExpose({
  getData: getTesterData,
  validate: () => {
    return !duplicateUserIds.value.length;
  }
});
</script>
<template>
  <div class="text-3 rounded space-y-2">
    <div
      v-for="(tester, index) in testerList"
      :key="index"
      class="flex space-x-2">
      <Select
        v-model:value="testerList[index].id"
        :defaultOptions="defaultUserOptions"
        :options="props.membersOptions"
        :error="!!tester.id && duplicateUserIds.includes(tester.id)"
        size="small"
        allowClear
        :placeholder="t('functionPlan.editForm.testerSelect.selectPersonnel')"
        class="!w-50"
        @change="handleUserSelectionChange" />
      <div class="flex-1">
        <Input
          v-model:value="tester.description"
          :maxlength="1000"
          :placeholder="t('functionPlan.editForm.testerSelect.workResponsibilities')"
          @blur="emitTesterData" />
      </div>
    </div>
  </div>
</template>
