<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Popover } from 'ant-design-vue';
import { Grid, Hints, Icon } from '@xcan-angus/vue-ui';
import { useMatchItemPopover } from './composables/useMatchItemPopover';

const { t } = useI18n();

// Use the match item popover composable for example data
const {
  matchItemList
} = useMatchItemPopover();
</script>

<template>
  <!-- Popover container -->
  <Popover destroyTooltipOnHide overlayClassName="overflow-auto">
    <!-- Trigger element (info icon) -->
    <div class="flex-shrink-0 flex items-center cursor-pointer w-4 h-7">
      <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
    </div>

    <!-- Popover content -->
    <template #content>
      <div class="w-196 space-y-2 text-3 text-theme-content">
        <!-- Title -->
        <Hints :text="t('dataVariable.detail.matchItemPopover.title')" class="!font-semibold !text-theme-content" />

        <!-- Description -->
        <div>
          {{ t('common.description') }}
        </div>

        <!-- Examples list -->
        <ol class="pl-4 space-y-2">
          <li
            v-for="item in matchItemList"
            :key="item.key"
            style="list-style-type: circle;"
            class="space-y-2">
            <!-- Example title -->
            <div class="font-semibold">{{ item.name }}</div>

            <!-- Example details grid -->
            <Grid
              labelStyle="color: var(--content-text-sub-content);"
              valueStyle="color: var(--content-text-sub-content);"
              labelSpacing="4px"
              :marginBottom="8"
              :columns="item.columns"
              :dataSource="item.data">
              <template #data="{ text }">
                <pre><code>{{ text }}</code></pre>
              </template>
              <template #expression="{ text }">
                <pre><code>{{ text }}</code></pre>
              </template>
              <template #result="{ text }">
                <pre><code>{{ text }}</code></pre>
              </template>
            </Grid>
          </li>
        </ol>
      </div>
    </template>
  </Popover>
</template>

<style scoped>
/* Code block styling */
pre code {
  font-size: 13px;
  line-height: 18px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
}
</style>

<style>
/* Popover scrollable content styling */
.ant-popover.overflow-auto .ant-popover-inner-content {
  max-height: 60vh;
  overflow: auto;
}
</style>
