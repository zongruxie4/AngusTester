<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
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

const Table = defineAsyncComponent(() => import('./Table.vue'));

const deletedNotify = ref<string>();

const pendingTotal = ref(0);
const createByMeTotal = ref(0);
const followTotal = ref(0);
const favoriteTotal = ref(0);
const commentTotal = ref(0);

const pendingParams = {
  testerId: props.userInfo?.id,
  testResult: 'PENDING'
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
    <div class="text-3.5 font-semibold mb-1">我的用例</div>
    <Tabs size="small">
      <TabPane key="create" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">添加的</span>
            <span>(</span>
            <span>{{ createByMeTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="createByMeTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="createByParams" />
      </TabPane>

      <TabPane key="testResult" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">待测试</span>
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
          :params="pendingParams" />
      </TabPane>

      <TabPane key="follow" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">关注的</span>
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
            <span class="mr-1">收藏的</span>
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
            <span class="mr-1">评论的</span>
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
