<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const { t } = useI18n();

const Table = defineAsyncComponent(() => import('./table.vue'));

const deletedNotify = ref<string>();

const pendingTotal = ref(0);
const confirmingTotal = ref(0);
const completedTotal = ref(0);
const createByTotal = ref(0);
const followTotal = ref(0);
const favoriteTotal = ref(0);
const commentTotal = ref(0);

const processingByParams = {
  assigneeId: props.userInfo?.id,
  status: 'PENDING'
};

const confirmingByParams = {
  confirmorId: props.userInfo?.id,
  status: 'CONFIRMING'
};

const completedByParams = {
  assigneeId: props.userInfo?.id,
  status: 'COMPLETED'
};

const createByParams = {
  createdBy: props.userInfo?.id
};

const followByParams = {
  followBy: props.userInfo?.id
};

const favouriteByParams = {
  favouriteBy: props.userInfo?.id
};

const commentByParams = {
  commentBy: props.userInfo?.id
};
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-1">{{ t('taskHome.myTasks') }}</div>
    <Tabs size="small">
      <TabPane key="createBy" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('taskHome.added') }}</span>
            <span>(</span>
            <span>{{ createByTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="createByTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="createByParams" />
      </TabPane>

      <TabPane key="pending" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('taskHome.pending') }}</span>
            <span>(</span>
            <span>{{ pendingTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="pendingTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="processingByParams" />
      </TabPane>

      <TabPane key="confirming" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('taskHome.confirming') }}</span>
            <span>(</span>
            <span>{{ confirmingTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="confirmingTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="confirmingByParams" />
      </TabPane>

      <TabPane key="completed" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('taskHome.completedStatus') }}</span>
            <span>(</span>
            <span>{{ completedTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="completedTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="completedByParams" />
      </TabPane>

      <TabPane key="follow" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('taskHome.followed') }}</span>
            <span>(</span>
            <span>{{ followTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="followTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="followByParams" />
      </TabPane>

      <TabPane key="favorite" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('taskHome.favorited') }}</span>
            <span>(</span>
            <span>{{ favoriteTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="favoriteTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="favouriteByParams" />
      </TabPane>

      <TabPane key="commentBy" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('taskHome.commented') }}</span>
            <span>(</span>
            <span>{{ commentTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="commentTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="commentByParams" />
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
