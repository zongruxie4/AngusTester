<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { Icon, Scroll } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';
import { task } from 'src/api/tester';

type Remark = {
  content: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  id: string;
  taskId: string;
}

type Props = {
  id: string;
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  notify: undefined
});

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const dataList = ref<Remark[]>([]);

const scrollChange = (data: Remark[]) => {
  dataList.value = data;
};

const toDelete = async (id: string) => {
  const [error] = await task.delTaskRemark(id);
  if (error) {
    return;
  }

  dataList.value = dataList.value.filter(item => item.id !== id);
};

const params = computed(() => {
  return { orderBy: 'createdDate', orderSort: 'DESC', taskId: props.id };
});
</script>
<template>
  <div class="h-full text-3 leading-5 pl-5">
    <div class="text-theme-title mb-2.5 font-semibold">备注</div>

    <Scroll
      v-if="!!props.id"
      :action="`${TESTER}/task/remark`"
      :hideNoData="true"
      :params="params"
      :lineHeight="56"
      :notify="props.notify"
      style="height: calc(100% - 30px);"
      transition
      @change="scrollChange">
      <div
        v-for="(item) in dataList"
        :key="item.id"
        class="mb-1.5 last:mb-0">
        <div class="flex items-center mb-0.5">
          <div class="border-2 border-theme-text-box w-2.5 h-2.5 rounded-full"></div>

          <div class="ml-3 font-normal text-3 flex items-center space-x-2 leading-4">
            <div class="text-theme-content font-medium">{{ item.createdByName }}</div>
            <div class="text-theme-content font-medium">添加了备注</div>
            <div class="text-theme-sub-content">{{ item.createdDate }}</div>
            <Icon
              icon="icon-qingchu"
              class="cursor-pointer text-theme-text-hover text-3.5"
              @click="toDelete(item.id)" />
          </div>
        </div>

        <div class="browser-container">
          <RichEditor :value="item.content" mode="view" />
        </div>
      </div>
    </Scroll>
  </div>
</template>

<style scoped>
.browser-container  {
  padding-left: 22px;
}
</style>
