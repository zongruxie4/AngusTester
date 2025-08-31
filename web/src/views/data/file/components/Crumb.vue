<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Breadcrumb, BreadcrumbItem } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface CrumbType {
    id: string,
    name: string
}

interface Props {
    crumb: CrumbType[],
}

const emit = defineEmits<{(e: 'jump', id: string): void}>();

const props = withDefaults(defineProps<Props>(), {
  crumb: () => []
});

const router = useRouter();
const jump = (id?: string) => {
  if (!id) {
    router.push('/data');
    return;
  }
  emit('jump', id as string);
};

</script>
<template>
  <div class="flex items-center text-3.5">
    <div class="mr-3">{{ t('fileSpace.crumb.currentPath') }}</div>
    <Breadcrumb separator=">">
      <BreadcrumbItem>
        <a @click="jump()">{{ t('fileSpace.crumb.home') }}</a>
      </BreadcrumbItem>
      <template v-if="crumb.length > 4">
        <BreadcrumbItem>
          <span>{{ t('fileSpace.crumb.ellipsis') }}</span>
        </BreadcrumbItem>
        <BreadcrumbItem v-for="item in props.crumb.slice(-3)" :key="item.id">
          <a @click="jump(item.id)">{{ item.name }}</a>
        </BreadcrumbItem>
      </template>
      <template v-else>
        <BreadcrumbItem v-for="item in props.crumb" :key="item.id">
          <a @click="jump(item.id)">{{ item.name }}</a>
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
