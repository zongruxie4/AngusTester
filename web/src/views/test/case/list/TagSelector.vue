<script setup lang="ts">
import { inject, ref, watch, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Tag, Tooltip } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { Icon, Select } from '@xcan-angus/vue-ui';
import { tag } from '@/api/tester';

const { t } = useI18n();

// Component props interface
interface Props {
  tagIds: string[];
}
const props = withDefaults(defineProps<Props>(), {
  tagIds: () => []
});

// Component emits
const emit = defineEmits<{(e: 'change', _val: string[]): void
}>();

// Basic state management
const projectId = inject<Ref<string>>('projectId', ref(''));
const tagList = ref<{ id: string, name: string, closeFlag: boolean }[]>([]);
const checkedIds = ref<string[]>([]);

/**
 * Toggle tag selection
 * @param tagId - Tag ID to toggle
 */
const selectTag = (tagId: string) => {
  checkedIds.value.includes(tagId) ? checkedIds.value = checkedIds.value.filter(f => f !== tagId) : checkedIds.value.push(tagId);
  emit('change', checkedIds.value);
};

// UI state management
const showSelect = ref(false);
const selectValue = ref<string>();

/**
 * Handle add tag button click
 */
const handleAddTag = () => {
  showSelect.value = true;
  selectValue.value = undefined;
};

/**
 * Handle tag selection change
 * @param _value - Selected value
 * @param option - Selected option
 */
const selectChange = (_value: any, option: any) => {
  showSelect.value = false;
  const { id, name } = option;
  const tagIds = new Set(tagList.value.map(tag => tag.id));
  if (tagIds.has(id)) return;
  tagList.value.push({ id, name, closeFlag: false });
  checkedIds.value.push(id);
  emit('change', checkedIds.value);
};

/**
 * Close tag
 * @param tagId - Tag ID to close
 */
const closeTag = (tagId: string) => {
  tagList.value = tagList.value.filter(f => f.id !== tagId);
  if (checkedIds.value.includes(tagId)) {
    checkedIds.value = checkedIds.value.filter(f => f !== tagId);
    emit('change', checkedIds.value);
  }
};

/**
 * Handle select blur
 */
const handleBlur = () => {
  showSelect.value = false;
};

/**
 * Load cached tag list
 */
const getCacheTagList = async () => {
  const [error, { data }] = await tag.getTagList({ projectId: projectId.value, filters: [{ key: 'id', op: 'IN', value: props.tagIds }] });
  if (error) {
    return;
  }
  tagList.value = data.list?.map(item => {
    checkedIds.value.push(item.id);
    return { ...item, closeFlag: false };
  });
};

// Watchers
watch(() => props.tagIds, (newValue) => {
  if (newValue?.length) {
    getCacheTagList();
  } else {
    checkedIds.value = [];
  }
}, {
  immediate: true
});

// Expose functions to parent components
defineExpose({
  checkedIds
});
</script>
<template>
  <div class="flex items-center text-3.5">
    <div class="flex items-center">
      <Tooltip
        v-for="item in tagList"
        :key="item.id"
        :title="item.name"
        placement="topLeft">
        <Tag
          :class="checkedIds.includes(item.id) ?'tag-checked':''"
          closable
          class="h-6 rounded-xl px-2.5"
          @click="selectTag(item.id)"
          @close="closeTag(item.id)">
          <div class="truncate" style="max-width: 80px;">{{ item.name }}</div>
        </Tag>
      </Tooltip>
    </div>

    <template v-if="tagList.length<3">
      <template v-if="!showSelect">
        <Button
          class="h-6 flex items-center"
          size="small"
          @click="handleAddTag">
          <Icon icon="icon-jia" class="text-3 mr-1 -mt-0.25" />
          {{ t('common.tag') }}
        </Button>
      </template>

      <template v-else>
        <Select
          v-model:value="selectValue"
          size="small"
          class="w-43"
          :placeholder="t('testCase.selectTag.selectTag')"
          showSearch
          internal
          :fieldNames="{ label: 'name', value: 'id' }"
          :action="`${TESTER}/tag?projectId=${projectId}`"
          @change="selectChange"
          @blur="handleBlur" />
      </template>
    </template>
  </div>
</template>
<style scoped>
.ant-tag.tag-checked :deep(.ant-tag-close-icon)>svg {
  color: #fff;
}

:deep(.tag-checked) {
  background-color: #4ea0fd;
  color: #fff;
}
</style>
