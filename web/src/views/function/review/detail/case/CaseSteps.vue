<script lang="ts" setup>
import { ref, defineAsyncComponent } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { funcCase } from '@/api/tester';

interface Props {
  caseInfo: {[key: string]: any};
  readonly: boolean;
}
const props = withDefaults(defineProps<Props>(), {
    caseInfo: () => ({}),
    readonly: true
});

const emits = defineEmits<{(e: 'change')}>();

const { t } = useI18n();
const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/CaseSteps.vue'));

const editable = ref(false);
const stepsValue = ref();

const saveLoading = ref(false);

const toEdit = () => {
    editable.value = true;
}

const cancel = () => {
    editable.value = false;
    stepsValue.value = JSON.parse(JSON.stringify(props?.caseInfo?.steps || []));
}

const confirm = async () => {
    saveLoading.value = true;
    const [error] = await funcCase.updateCase([{
        id: props.caseInfo.id,
        steps: stepsValue.value
    }]);
    saveLoading.value = false;

    if (error) {
        return;
    }
    editable.value = false;
    emits('change');
}

</script>
<template>

<div>
    <template v-if="!props.readonly">
        <div v-show="!editable" class="flex justify-end">
            <Button size="small" type="link" @click="toEdit">
                <Icon icon="icon-xiugai" />
            </Button>
        </div>
        <div v-show="editable" class="flex justify-end">
            <Button size="small" type="link" @click="cancel">
                {{ t('actions.cancel') }}
            </Button>
            <Button
                :loading="saveLoading"
                size="small"
                type="link"
                @click="confirm">
                {{ t('actions.confirm') }}
            </Button>
        </div>
    </template>
    <CaseStep
        v-model:value="stepsValue"
        :defaultValue="editable ? stepsValue : props?.caseInfo?.steps"
        :stepView="props?.caseInfo?.stepView?.value"
        :readonly="!editable" />
</div>
</template>