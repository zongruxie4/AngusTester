<script lang="ts" setup>
// Vue core imports
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Hints, IconCopy, Input } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { utils } from '@xcan-angus/infra';

const { t } = useI18n();

/**
 * Option interface for static parameter
 */
export interface Option {
    name: string;
    value: string;
}

/**
 * Component props interface for static parameter
 */
export interface Props {
    dataSource: {
        name: string;
        value: string;
    }[];
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => []
});

// Computed properties
const processedDataSource = computed(() => {
  return props.dataSource?.map(item => {
    return {
      ...item,
      id: utils.uuid()
    };
  }) || [];
});
</script>
<template>
  <div>
    <Hints class="mb-1.5" :text="t('commonComp.apis.parameterizationDataset.staticParameter.hintText')" />

    <div class="flex items-center space-x-2 mb-1">
      <div class="w-90 flex items-center">
        <span>{{ t('commonComp.apis.parameterizationDataset.staticParameter.name') }}</span>
      </div>
      <div class="flex-1 flex items-center">
        <span>{{ t('common.value') }}</span>
      </div>
    </div>

    <div class="space-y-2.5">
      <div
        v-for="item in processedDataSource"
        :key="item.id"
        class="flex items-center space-x-2">
        <div class="flex items-center flex-1 space-x-2">
          <div class="w-90 flex items-center">
            <Input
              :value="item.name"
              readonly
              size="small"
              class="flex-1 has-suffix">
              <template #suffix>
                <div v-if="item.name" class="h-full flex items-center overflow-hidden">
                  <div :title="`{${item.name}}`" class="flex-1 flex items-center text-3 overflow-hidden">
                    <span>{</span>
                    <span class="truncate">{{ item.name }}</span>
                    <span>}</span>
                  </div>
                  <IconCopy :copyText="`{${item.name}}`" class="flex-shrink-0 ml-1.75" />
                </div>
              </template>
            </Input>
          </div>
          <Input
            :value="item.value"
            class="flex-1"
            readonly />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.has-suffix :deep(.ant-input-suffix) {
    display: inline-block;
    width: 120px;
    margin-left: 4px;
    padding-left: 7px;
    overflow: hidden;
    border-left: 1px solid var(--border-text-box);
}

.ant-input-affix-wrapper:focus,
.ant-input-affix-wrapper-focused,
.ant-input-affix-wrapper:hover {
    border: 1px solid var(--border-text-box)
}
</style>
