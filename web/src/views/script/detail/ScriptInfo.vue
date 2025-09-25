<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Colon, ScriptTypeTag, Tooltip } from '@xcan-angus/vue-ui';
import { Tag } from 'ant-design-vue';

import { ScriptInfo } from '../types';
import { useScriptInfo } from './composables/useScriptInfo';

const { t } = useI18n();

type Props = {
  dataSource: ScriptInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

// Use script info composable
const {
  dataSource,
  tags,
  sourceNameLinkUrl
} = useScriptInfo(props);

</script>
<template>
  <div class="text-3 leading-5 space-y-4 overflow-auto">
    <!-- ID Information -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">ID</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="whitespace-pre-wrap break-words break-all text-theme-title font-mono bg-gray-50 px-2 py-1 rounded text-2.5">
        {{ dataSource.id }}
      </div>
    </div>

    <!-- Script Name -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('common.scriptName') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="flex items-start whitespace-pre-wrap break-words break-all text-theme-title font-medium">
        {{ dataSource.name }}
      </div>
    </div>

    <!-- Script Type -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('common.scriptType') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="whitespace-pre-wrap break-words break-all">
        <ScriptTypeTag :value="dataSource.type" />
      </div>
    </div>

    <!-- Plugin Information -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('common.plugin') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="whitespace-pre-wrap break-words break-all">
        <span
          v-if="dataSource.plugin"
          class="inline-flex items-center px-2.5 py-1 rounded-full text-2.5 font-medium"
          style="background-color:rgba(0, 119, 255, 8%);color:rgba(0, 119, 255, 100%);border: 1px solid rgba(0, 119, 255, 20%)">
          {{ dataSource.plugin }}
        </span>
        <span v-else class="text-theme-sub-content">--</span>
      </div>
    </div>

    <!-- Source Information -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('common.source') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="whitespace-pre-wrap break-words break-all text-theme-title">
        {{ dataSource.source?.message || '--' }}
      </div>
    </div>

    <!-- Resource ID -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('scriptDetail.info.resourceId') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="whitespace-pre-wrap break-words break-all text-theme-title font-mono bg-gray-50 px-2 py-1 rounded text-2.5">
        {{ dataSource.sourceId || '--' }}
      </div>
    </div>

    <!-- Resource Name -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('scriptDetail.info.resourceName') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div v-if="!sourceNameLinkUrl" class="whitespace-pre-wrap break-words break-all text-theme-title">
        {{ dataSource.sourceName || '--' }}
      </div>
      <RouterLink
        v-else
        :to="sourceNameLinkUrl"
        target="_blank"
        class="text-text-link hover:text-blue-600 transition-colors duration-200 underline decoration-dotted underline-offset-2">
        {{ dataSource.sourceName }}
      </RouterLink>
    </div>

    <!-- Tags Information -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('scriptDetail.info.tags') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div v-if="tags.length" class="flex items-start flex-wrap gap-1.5">
        <Tag
          v-for="item in tags"
          :key="item.id"
          class="h-6 leading-6 text-2.5 rounded-full text-text-content bg-blue-50 border-blue-200 text-blue-700 hover:bg-blue-100 transition-colors duration-200">
          <span v-if="item.name.length <= 15">
            {{ item.name }}
          </span>
          <Tooltip
            v-else
            :title="item.name"
            placement="bottomLeft">
            {{ item.name.slice(0, 15) }}...
          </Tooltip>
        </Tag>
      </div>
      <div v-else class="text-theme-sub-content">--</div>
    </div>

    <!-- Creator -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('scriptDetail.info.creator') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="whitespace-pre-wrap break-words break-all text-theme-title">
        {{ dataSource.createdByName }}
      </div>
    </div>

    <!-- Last Updater -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('scriptDetail.info.lastUpdater') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="whitespace-pre-wrap break-words break-all text-theme-title">
        {{ dataSource.lastModifiedByName }}
      </div>
    </div>

    <!-- Last Modified Time -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('scriptDetail.info.lastModifyTime') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="whitespace-pre-wrap break-words break-all text-theme-title">
        {{ dataSource.lastModifiedDate }}
      </div>
    </div>

    <!-- Description Information -->
    <div class="relative flex items-start group">
      <div class="w-21.75 flex items-center flex-shrink-0">
        <span class="text-theme-sub-content font-medium">{{ t('common.description') }}</span>
        <Colon class="w-1 text-theme-sub-content" />
      </div>
      <div class="whitespace-pre-wrap break-words break-all text-theme-title leading-relaxed">
        {{ dataSource.description || '--' }}
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Decorative separator lines between information items */
.group:not(:last-child)::after {
  content: '';
  position: absolute;
  bottom: -0.5rem;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(0, 0, 0, 0.06) 20%, rgba(0, 0, 0, 0.06) 80%, transparent 100%);
}

/* Hover effects for labels */
.group:hover .text-theme-sub-content {
  color: rgba(0, 119, 255, 0.8);
  transition: color 0.2s ease;
}

/* Enhanced tag hover effects */
.group:hover .bg-blue-50 {
  background-color: rgba(0, 119, 255, 0.12);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 119, 255, 0.1);
}

/* Styled code block appearance */
.font-mono {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

/* Enhanced plugin tag hover effects */
.group:hover .inline-flex {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 119, 255, 0.15);
}

/* Enhanced link hover effects */
.group:hover .text-text-link {
  text-decoration-color: rgba(0, 119, 255, 0.6);
}
</style>
