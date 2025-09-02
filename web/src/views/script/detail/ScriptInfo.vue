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
  <div class="text-3 leading-5 space-y-3.5 overflow-auto">
    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>ID</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.id }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.scriptName') }}</span>
        <Colon class="w-1" />
      </div>

      <div class="flex items-start whitespace-pre-wrap break-words break-all">{{ dataSource.name }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.scriptType') }}</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">
        <ScriptTypeTag :value="dataSource.type" />
      </div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.plugin') }}</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">
        <span
          v-if="dataSource.plugin"
          class="px-2 py-0.5 rounded"
          style="background-color:rgba(0, 119, 255, 10%);color:rgba(0, 119, 255, 100%)">{{ dataSource.plugin
          }}</span>

        <span v-else>--</span>
      </div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.source') }}</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.source?.message }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.resourceId') }}</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.sourceId || '--' }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.resourceName') }}</span>
        <Colon class="w-1" />
      </div>

      <div v-if="!sourceNameLinkUrl" class="whitespace-pre-wrap break-words break-all">
        {{ dataSource.sourceName || '--' }}
      </div>
      <RouterLink
        v-else
        :to="sourceNameLinkUrl"
        target="_blank"
        class="text-text-link">
        {{ dataSource.sourceName }}
      </RouterLink>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.tags') }}</span>
        <Colon class="w-1" />
      </div>

      <div v-if="tags.length" class="flex items-start flex-wrap">
        <Tag
          v-for="item in tags"
          :key="item.id"
          class="mr-1.5 mb-1.5 h-5 leading-5 text-3 rounded text-text-content">
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
      <div v-else>--</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.creator') }}</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.createdByName }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.lastUpdater') }}</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.lastModifiedByName }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.lastModifyTime') }}</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.lastModifiedDate }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>{{ t('scriptDetail.info.description') }}</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.description || '--' }}</div>
    </div>
  </div>
</template>
