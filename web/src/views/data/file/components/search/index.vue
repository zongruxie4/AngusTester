<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { computed } from '@vue/reactivity';
import { useRoute } from 'vue-router';
import { STORAGE } from '@xcan-angus/infra';
import { Select } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import FileIcon from '@/views/data/file/components/icon/index.vue';

const { t } = useI18n();

const emit = defineEmits<{(e: 'search', id: string):void}>();

const isShowIcon = ref(false);
const spaceId = ref();

const showSearch = () => {
  isShowIcon.value = true;
};

const params = computed(() => {
  return {
    spaceId: spaceId.value
  };
});

const route = useRoute();

onMounted(() => {
  if (route.params.id) {
    spaceId.value = route.params.id as string;
  }
});

let inputValue = '';
const handleChange = (value, valueObj) => {
  if (!value) {
    inputValue = '';
    return;
  }
  inputValue = value;
  emit('search', valueObj.parentDirectoryId);
};

const handleVisible = (visible) => {
  if (!visible && !inputValue) {
    isShowIcon.value = false;
  }
};
</script>
<template>
  <div class="flex">
    <FileIcon
      v-if="!isShowIcon"
      :title="t('fileSpace.search.title')"
      icon="icon-sousuo"
      class="text-4 text-theme-sub-content"
      @click="showSearch" />
    <Select
      v-else
      showSearch
      :fieldNames="{label: 'name', value: 'id'}"
      :params="params"
      class="w-40 ml-5"
      :action="`${STORAGE}/space/object?fullTextSearch=true`"
      @change="handleChange"
      @dropdownVisibleChange="handleVisible" />
  </div>
</template>
