<script setup lang="ts">
import FileIcon from '@/views/data/file/components/icon/index.vue';
import { DropdownSort } from '@xcan-angus/vue-ui';

interface SortType {
    orderBy?: string,
    orderSort?: string
}

const emit = defineEmits<{(e: 'sort', param: SortType):void}>();
const menus = [
  {
    key: 'lastModifiedDate',
    name: '按最后修改时间',
    orderSort: 'ASC'
  },
  {
    key: 'type',
    name: '按添加类型',
    orderSort: 'ASC'
  }
];

const handleSelect = ({ orderBy, orderSort }) => {
  emit('sort', { orderSort, orderBy });
};
</script>
<template>
  <DropdownSort :menuItems="menus" @click="handleSelect">
    <file-icon icon="icon-shunxu" title="排序"></file-icon>
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
