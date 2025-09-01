<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Breadcrumb, BreadcrumbItem } from 'ant-design-vue';
import type { CrumbType } from '@/views/data/file/types';
import { useCrumb } from './composables/useCrumb';

interface Props {
  crumb: CrumbType[]
}

const props = withDefaults(defineProps<Props>(), {
  crumb: () => []
});

const emit = defineEmits<{(e: 'jump', id: string): void}>();

const { t } = useI18n();

// Use the crumb composable
const { jumpToPath, processCrumbItems, shouldShowEllipsis } = useCrumb();

/**
 * Handle breadcrumb item click
 * 
 * @param id - ID of the item clicked
 */
const handleJump = (id?: string): void => {
  jumpToPath(id);
  if (id) {
    emit('jump', id);
  }
};

</script>

<template>
  <div class="flex items-center text-3.5">
    <div class="mr-3">{{ t('fileSpace.crumb.currentPath') }}</div>
    <Breadcrumb separator=">">
      <BreadcrumbItem>
        <a @click="handleJump()">{{ t('fileSpace.crumb.home') }}</a>
      </BreadcrumbItem>
      <template v-if="shouldShowEllipsis(props.crumb)">
        <BreadcrumbItem>
          <span>{{ t('fileSpace.crumb.ellipsis') }}</span>
        </BreadcrumbItem>
        <BreadcrumbItem v-for="item in processCrumbItems(props.crumb)" :key="item.id">
          <a @click="handleJump(item.id)">{{ item.name }}</a>
        </BreadcrumbItem>
      </template>
      <template v-else>
        <BreadcrumbItem v-for="item in props.crumb" :key="item.id">
          <a @click="handleJump(item.id)">{{ item.name }}</a>
        </BreadcrumbItem>
      </template>
    </Breadcrumb>
  </div>
</template>

<style scoped>
:deep(.ant-breadcrumb-link) {
  @apply inline-block leading-4 overflow-hidden whitespace-nowrap overflow-ellipsis align-text-bottom;

  max-width: 200px;
}

.ant-breadcrumb a {
  color: rgba(0, 0, 0, 85%);
}

.ant-breadcrumb a:hover {
  color: #40a9ff;
}
</style>