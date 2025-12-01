<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { testerSetting } from '@/api/tester';
import ExpandGrid from './ExpandGrid.vue';
import { EvaluationPurpose } from '@/enums/enums';
import { enumUtils } from '@xcan-angus/infra';
import { InputNumber, Button } from 'ant-design-vue';
import { notification, Icon } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';
const { t } = useI18n();

const evaluationData = ref({});
const evaluationDataShow= ref({});

const purposes = ref<{ value: EvaluationPurpose; message: string }[]>([]);
const loadPurposes = () => {
    const data = enumUtils.enumToMessages(EvaluationPurpose);
    purposes.value = data.map(item => ({ value: item.value as EvaluationPurpose, message: item.message }));
};
const loadEvaluationData = async () => {
    const [error, data] = await testerSetting.getEvaluationIndicator();
    if (error) {
        return;
    }
    evaluationData.value = data.data;
    evaluationDataShow.value = JSON.parse(JSON.stringify(evaluationData.value));
};

const editable = ref(false);
const toggleEditMode = () => {
    editable.value = !editable.value;
    if (editable.value) {
        evaluationDataShow.value = JSON.parse(JSON.stringify(evaluationData.value));
    } else {
        evaluationData.value = JSON.parse(JSON.stringify(evaluationDataShow.value));
    }
};

const validateEvaluationInfo = () => {
    for (const key in evaluationData.value) {
        if (evaluationData.value[key] < 0 || evaluationData.value[key] > 100) {
            notification.error('评估指标权重必须为0-100之间的整数');
            return false;
        }
    }
    const total = Object.values(evaluationData.value).reduce((acc: number, curr: string) => acc + (+curr), 0) as number;
    if (total !== 100) {
        notification.error('评估指标权重总和必须为100');
        return false;
    }
    return true;
};

const saveEvaluationInfo = async () => {
    if (validateEvaluationInfo()) {
        const [error] = await testerSetting.saveEvaluationIndicator(evaluationData.value);
        if (error) {
            return;
        }
        notification.success('评估指标权重保存成功');
        editable.value = false;
        await loadEvaluationData();
    }
};



onMounted(() => {
  loadEvaluationData();
  loadPurposes();
});
</script>

<template>
 <ExpandGrid title="评估指标权重">
    <template #button>
      <div class="text-3 flex items-center">
        <template v-if="editable">
          <span class="cursor-pointer" @click.stop="toggleEditMode">
            <Icon icon="icon-zhongzhi2" class="mr-1" />{{ t('actions.cancel') }}
          </span>
          <Button
            type="text"
            class="ml-2 text-3 py-0 h-5"
            @click.stop="saveEvaluationInfo">
            <Icon icon="icon-baocun" class="mr-1" />{{ t('actions.save') }}
          </Button>
        </template>
        <Button
          v-else
           v-show="appContext.isAdmin()"
          class="text-3 py-0 h-5"
          type="text"
          @click.stop="toggleEditMode">
          <Icon icon="icon-shuxie" class="mr-1" />{{ t('actions.edit') }}
        </Button>
      </div>
    </template>
    <template #default>
        <div class="grid grid-cols-1 md:grid-cols-5 lg:grid-cols-10 gap-3 px-3 pt-3">
            <div v-for="item in purposes" :key="item.value">
                <div class="text-3 leading-5 flex-1">
                    <div class="leading-5 flex-1 mb-3">
                        {{ item.message }}
                    </div>
                    <div class="text-3 leading-5 flex-1">
                        <InputNumber
                            v-show="editable"
                            v-model:value="evaluationData[item.value]"
                            size="small"
                            :parser="value => Math.round(+value)"
                            :step="1"
                            :min="0"
                            :max="100"/>
                        <div v-show="!editable" class="w-25 bg-gray-light rounded h-7 leading-7 px-2">{{ evaluationDataShow[item.value] }}%</div>
                    </div>
                </div>
            </div>
        </div>
    </template>
 </ExpandGrid>
</template>