<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';

const Table = defineAsyncComponent(() => import('./AddedTable.vue'));

const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined,
  refreshNotify: undefined
});

// Notification state for deleted issues
const deletedNotify = ref<string>();

const createByMeTotal = ref(0);
const followTotal = ref(0);
const favoriteTotal = ref(0);

const createByParams = {
  createdBy: props.userInfo?.id
};

const followByParams = {
  followBy: props.userInfo?.id
};

const favouriteByParams = {
  favouriteBy: props.userInfo?.id
};
</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-1">
      {{ t('apis.myApis.title') }}
    </div>

    <Tabs size="small">
      <TabPane key="create" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.added') }}</span>
            <span>(</span>
            <span>{{ createByMeTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="createByMeTotal"
          :deletedNotify="props.deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="createByParams" />
      </TabPane>

      <TabPane key="follow" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.followed') }}</span>
            <span>(</span>
            <span>{{ followTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="followTotal"
          :deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="followByParams" />
      </TabPane>

      <TabPane key="favorite" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.favourited') }}</span>
            <span>(</span>
            <span>{{ favoriteTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="favoriteTotal"
          :deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="favouriteByParams" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.ant-tabs {
  line-height: 20px;
}

:deep(.ant-tabs-content-holder) {
  min-height: 225px;
}

.ant-tabs-top>:deep(.ant-tabs-nav),
.ant-tabs-bottom>:deep(.ant-tabs-nav),
.ant-tabs-top>:deep(div)>.ant-tabs-nav,
.ant-tabs-bottom>:deep(div)>.ant-tabs-nav {
  margin-bottom: 14px;
}
</style>
