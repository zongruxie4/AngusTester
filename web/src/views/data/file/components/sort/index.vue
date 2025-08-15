<script setup lang="ts">
import FileIcon from '@/views/data/file/components/icon/index.vue';
import { DropdownSort } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface SortType {
    orderBy?: string,
    orderSort?: string
}

const emit = defineEmits<{(e: 'sort', param: SortType):void}>();

const menus = [
  {
    key: 'lastModifiedDate',
    name: t('fileSpace.sort.menuItems.lastModifiedDate'),
    orderSort: 'ASC'
  },
  {
    key: 'type',
    name: t('fileSpace.sort.menuItems.type'),
    orderSort: 'ASC'
  }
];

const handleSelect = ({ orderBy, orderSort }) => {
  emit('sort', { orderSort, orderBy });
};
</script>
<template>
  <DropdownSort :menuItems="menus" @click="handleSelect">
    <file-icon icon="icon-shunxu" :title="t('fileSpace.sort.title')"></file-icon>
  </DropdownSort>
</template>
<style scoped>
.sort-file:hover .icon {
  @apply visible;
}

.sor-active {
  @apply text-text-link;
}
</style>
