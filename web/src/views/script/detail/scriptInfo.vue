<script lang="ts" setup>
import { computed } from 'vue';
import { Colon, ScriptTypeTag, Tooltip } from '@xcan-angus/vue-ui';
import { Tag } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';

import { ScriptInfo } from '../PropsType';

type Props = {
  dataSource: ScriptInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const dataSource = computed(() => {
  return props.dataSource || {};
});

const tags = computed(() => {
  return dataSource.value.tags?.map(item => {
    return {
      id: utils.uuid(),
      name: item
    };
  }) || [];
});

const sourceNameLinkUrl = computed(() => {
  const { source, name, plugin, sourceId } = dataSource.value;
  const sourceValue = source?.value;
  if (sourceValue === 'SERVICE_SMOKE') {
    return `/apis#services?id=${sourceId}&name=${name}&value=group`;
  } else if (sourceValue === 'SERVICE_SECURITY') {
    return `/apis#services?id=${sourceId}&name=${name}&value=group`;
  } else if (sourceValue === 'API') {
    return `/apis#services?id=${sourceId}&name=${name}&value=API`;
  } else if (sourceValue === 'SCENARIO') {
    return `/scenario#scenario?id=${sourceId}&name=${name}&plugin=${plugin}`;
  }

  return '';
});
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
        <span>脚本名称</span>
        <Colon class="w-1" />
      </div>

      <div class="flex items-start whitespace-pre-wrap break-words break-all">{{ dataSource.name }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>脚本类型</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">
        <ScriptTypeTag :value="dataSource.type" />
      </div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>插件</span>
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
        <span>来源</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.source?.message }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>资源ID</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.sourceId || '--' }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>资源名称</span>
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
        <span>标签</span>
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
        <span>添加人</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.createdByName }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>最后更新人</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.lastModifiedByName }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>最后修改时间</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.lastModifiedDate }}</div>
    </div>

    <div class="relative flex items-start">
      <div class="w-21.75 flex items-center whitespace-nowrap flex-shrink-0">
        <span>描述</span>
        <Colon class="w-1" />
      </div>

      <div class="whitespace-pre-wrap break-words break-all">{{ dataSource.description || '--' }}</div>
    </div>
  </div>
</template>
