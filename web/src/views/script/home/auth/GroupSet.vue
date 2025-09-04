<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { Icon, Image, Input, Scroll } from '@xcan-angus/vue-ui';
import { useGroupData } from './composables/useGroupData';
import { GroupSetProps } from './types';

/**
 * <p>
 * Component props with default values.
 * </p>
 */
const props = withDefaults(defineProps<GroupSetProps>(), {
  type: 'user',
  loaded: false,
  visible: false,
  checkedId: undefined,
  appId: undefined
});

/**
 * <p>
 * Component emits definition for two-way binding.
 * </p>
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:checkedId', id: string | undefined): void;
  (e: 'update:loaded', value: boolean): void;
}>();

/**
 * <p>
 * Initialize group data management using composable.
 * Handles data loading, searching, and selection logic.
 * </p>
 */
const {
  dataSource,
  inputValue,
  activeId,
  notify,
  nameKey,
  idKey,
  placeholder,
  apiPath,
  params,
  scrollChange,
  inputChange,
  checkedHandler
} = useGroupData(props.type, props.appId, props.visible);

/**
 * <p>
 * Handles scroll change events and updates loaded state.
 * </p>
 */
const handleScrollChange = (data: any[]) => {
  const newActiveId = scrollChange(data);
  if (newActiveId) {
    emit('update:checkedId', newActiveId);
  }

  if (!props.loaded) {
    emit('update:loaded', true);
  }
};

/**
 * <p>
 * Handles item selection and emits checked ID.
 * </p>
 */
const handleChecked = (id: string) => {
  const newActiveId = checkedHandler(id);
  emit('update:checkedId', newActiveId);
};

/**
 * <p>
 * Component lifecycle: Set up watchers for props changes.
 * </p>
 */
onMounted(() => {
  // Watch for checkedId changes from parent
  watch(() => props.checkedId, (newValue) => {
    if (newValue && newValue !== activeId.value) {
      activeId.value = newValue;
    }
  });

  // Watch for apiPath changes to trigger data loading
  watch(() => apiPath.value, (newPath) => {
    if (newPath) {
      // Trigger data loading by incrementing notify
      notify.value++;
    }
  });
});
</script>

<template>
  <div class="h-full text-3 text-theme-title">
    <!-- Search input -->
    <Input
      :value="inputValue"
      :allowClear="true"
      :placeholder="placeholder"
      size="small"
      class="mb-2"
      @change="(e) => inputChange(e)" />

    <!-- Scrollable list container -->
    <Scroll
      :lineHeight="44"
      :params="params"
      :action="apiPath"
      :notify="notify"
      style="height: calc(100% - 36px);"
      @change="handleScrollChange">
      <!-- List items -->
      <div
        v-for="item in dataSource"
        :key="item[idKey]"
        :class="{ 'active-item': activeId === item[idKey] }"
        class="flex items-center justify-between h-11 py-1.5 px-3 rounded cursor-pointer hover:bg-gray-hover"
        @click.stop="handleChecked(item[idKey])">
        <div class="flex items-center flex-nowrap">
          <!-- Group icon -->
          <Icon
            v-if="type === 'group'"
            class="mr-3 text-7"
            icon="icon-zu" />
          <!-- Department icon -->
          <Icon
            v-else-if="type === 'dept'"
            class="mr-3 text-7"
            icon="icon-bumen" />
          <!-- User avatar -->
          <Image
            v-else
            class="w-7 h-7 rounded-2xl mr-3"
            type="avatar"
            :src="item.avatar" />
          <!-- Item name -->
          <span :title="item[nameKey]" class="leading-5 truncate">{{ item[nameKey] }}</span>
        </div>
      </div>
    </Scroll>
  </div>
</template>

<style scoped>
.active-item {
  background-color: rgba(239, 240, 243, 100%);
  color: #1890ff;
}
</style>
