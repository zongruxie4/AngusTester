<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';
import { CaseTestResult } from '@/enums/enums';

// Props and Composables
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const { t } = useI18n();

// Component Imports
const CaseTable = defineAsyncComponent(() => import('./AddedTable.vue'));

// Reactive State
const deletedNotification = ref<string>();

// Tab Counters
const pendingCasesCount = ref(0);
const createdByMeCount = ref(0);
const followedCasesCount = ref(0);
const favoritedCasesCount = ref(0);
const commentedCasesCount = ref(0);

// Filter Parameters
const pendingCasesParams = {
  testerId: props.userInfo?.id as string,
  testResult: CaseTestResult.PENDING
};

const createdByMeParams = {
  createdBy: props.userInfo?.id as string
};

const followedCasesParams = {
  followBy: true
};

const favoritedCasesParams = {
  favouriteBy: true
};

const commentedCasesParams = {
  commentBy: props.userInfo?.id as string
};
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-1">
      {{ t('functionHome.myCases.title') }}
    </div>

    <Tabs size="small">
      <TabPane key="create" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('functionHome.myCases.added') }}</span>
            <span>(</span>
            <span>{{ createdByMeCount }}</span>
            <span>)</span>
          </div>
        </template>

        <CaseTable
          v-model:total="createdByMeCount"
          v-model:deletedNotify="deletedNotification"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="createdByMeParams" />
      </TabPane>

      <TabPane key="testResult" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('functionHome.myCases.pendingTest') }}</span>
            <span>(</span>
            <span>{{ pendingCasesCount }}</span>
            <span>)</span>
          </div>
        </template>

        <CaseTable
          v-model:total="pendingCasesCount"
          v-model:deletedNotify="deletedNotification"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="pendingCasesParams" />
      </TabPane>

      <TabPane key="follow" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('functionHome.myCases.followed') }}</span>
            <span>(</span>
            <span>{{ followedCasesCount }}</span>
            <span>)</span>
          </div>
        </template>

        <CaseTable
          v-model:total="followedCasesCount"
          v-model:deletedNotify="deletedNotification"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="followedCasesParams" />
      </TabPane>

      <TabPane key="favorite" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('functionHome.myCases.favorited') }}</span>
            <span>(</span>
            <span>{{ favoritedCasesCount }}</span>
            <span>)</span>
          </div>
        </template>

        <CaseTable
          v-model:total="favoritedCasesCount"
          v-model:deletedNotify="deletedNotification"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="favoritedCasesParams" />
      </TabPane>

      <TabPane key="commentBy" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('functionHome.myCases.commented') }}</span>
            <span>(</span>
            <span>{{ commentedCasesCount }}</span>
            <span>)</span>
          </div>
        </template>

        <CaseTable
          v-model:total="commentedCasesCount"
          v-model:deletedNotify="deletedNotification"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="commentedCasesParams" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.ant-tabs {
  line-height: 20px;
}

:deep(.ant-tabs-content-holder) {
  min-height: 166px;
}

.ant-tabs-top>:deep(.ant-tabs-nav),
.ant-tabs-bottom>:deep(.ant-tabs-nav),
.ant-tabs-top>:deep(div)>.ant-tabs-nav,
.ant-tabs-bottom>:deep(div)>.ant-tabs-nav {
  margin-bottom: 14px;
}
</style>
