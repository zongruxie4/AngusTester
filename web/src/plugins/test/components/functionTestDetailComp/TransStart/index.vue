<script setup lang="ts">
import { computed, ref } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Arrow, Icon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import StatusTag from '@/plugins/test/components/StatusTag/index.vue';
import { ExecContent } from '@/plugins/test/types';

/**
 * Protocol target types supported within a transaction
 */
type ProtocolTarget = 'JDBC' | 'TRANS_END' | 'RENDEZVOUS' | 'WAITING_TIME' | 
  'THROUGHPUT' | 'HTTP' | 'FTP' | 'LDAP' | 'MAIL' | 'SMTP' | 'TCP' | 'WEBSOCKET' | 'JMS';

/**
 * Child step configuration interface
 * Represents a test step within the transaction
 */
interface TransactionChild {
  target: ProtocolTarget;      // Protocol or component type
  name: string;                // Step name
  linkName: string;            // Linked name for content matching
  description: string;         // Optional description
  enabled: boolean;            // Whether this step is enabled
  beforeName: string;          // Name of previous step
  transactionName: string;     // Parent transaction name
}

/**
 * Transaction start configuration interface
 */
interface TransStartValue {
  target: 'TRANS_START';           // Target type identifier
  name: string;                    // Transaction name
  description: string;             // Optional description
  enabled: boolean;                // Whether transaction is enabled
  beforeName: string;              // Name of step before transaction
  transactionName: string;         // Transaction identifier
  children?: TransactionChild[];   // Child steps within transaction
}

/**
 * Component props interface
 */
interface Props {
  value: TransStartValue;      // Transaction start configuration
  content: ExecContent[];      // Execution results for all steps
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  content: undefined
});

/**
 * Constant: Unique identifier for collapse panel
 * Used to manage panel open/close state
 */
const UUID = utils.uuid();

/**
 * Ref: Active collapse panel key
 * Controls which panel is expanded (undefined = collapsed)
 */
const collapseActiveKey = ref<string>();

/**
 * Ref: Arrow open state
 * Tracks whether the collapse arrow is in open position
 */
const arrowOpen = ref(collapseActiveKey.value === UUID);

/**
 * Toggle collapse panel open/close state
 * Updates both arrow state and active panel key
 * 
 * @param open - Whether to open (true) or close (false) the panel
 */
const arrowChange = (open: boolean): void => {
  arrowOpen.value = open;
  
  if (open) {
    // Open panel by setting active key to UUID
    collapseActiveKey.value = UUID;
    return;
  }

  // Close panel by clearing active key
  collapseActiveKey.value = undefined;
};

/**
 * Computed: HTTP/JDBC request names within transaction
 * Extracts linkNames of all JDBC-type children for content matching
 * Note: Despite the variable name, this currently filters for JDBC targets
 * 
 * @returns Array of linkNames for JDBC children, or empty array
 */
const httpNames = computed<string[]>(() => {
  if (!props.value?.children?.length) {
    return [];
  }

  // Filter children by JDBC target type and extract linkNames
  return props.value?.children?.filter(item => item.target === 'JDBC')?.map(item => item.linkName);
});

/**
 * Computed: Execution content for HTTP/JDBC requests
 * Filters execution results to only include those matching transaction's children
 * Used for status calculation and response analysis
 * 
 * @returns Array of ExecContent matching child request names
 */
const httpContents = computed<ExecContent[]>(() => {
  const names = httpNames.value;
  if (names.length === 0) {
    return [];
  }

  // Match execution content by name
  return props.content?.filter(item => names.includes(item.name));
});

// Commented out: Body size calculation
// Can be enabled to display total response body size for all requests in transaction
// const bodySize = computed(() => {
//   const size = httpContents.value.reduce((prev, cur) => {
//     prev = prev + (+cur.content.response.bodySize);
//     return prev;
//   }, 0);
//
//   return utils.formatBytes(size);
// });

/**
 * Status type matching StatusTag component
 */
type StatusType = 'success' | 'fail' | 'ignore' | 'block';

/**
 * Computed: Transaction execution status
 * Calculates overall status based on enabled child requests' success/failure
 * 
 * Status logic:
 * - 'block': No execution content available (not yet executed)
 * - 'success': All enabled child requests succeeded
 * - 'fail': At least one enabled child request failed
 * 
 * @returns Status value: 'block' | 'success' | 'fail'
 */
const status = computed<StatusType>(() => {
  // No execution content means transaction hasn't been executed
  if (!props.content?.length) {
    return 'block';
  }

  // Get all enabled JDBC-type children
  const enabledApis = props.value?.children?.filter(item => item.target === 'JDBC' && item.enabled) || [];
  const totalNum = enabledApis.length;
  
  // Count successful requests from execution content
  const successNum = httpContents.value.filter(item => item.content?.success)?.length;
  
  // Success only if all enabled requests succeeded
  if (totalNum === successNum) {
    return 'success';
  }

  // Any failure means transaction failed
  return 'fail';
});
</script>

<template>
  <!--
    Transaction start collapsible container
    - White background with 12px font
    - Controls expand/collapse of transaction content
  -->
  <Collapse
    :activeKey="collapseActiveKey"
    style="background-color: #fff;font-size: 12px;"
    class="timeline-collapse">
    
    <!--
      Collapse panel for transaction
      - Uses UUID as unique key
      - Custom arrow in header (showArrow=false)
      - Disabled collapsible to use custom click handler
    -->
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      collapsible="disabled">
      
      <!-- Custom header with transaction info and status -->
      <template #header>
        <!--
          Header container (clickable to expand/collapse)
          - Full width horizontal layout
          - Contains icon, name, status, and arrow
        -->
        <div 
          class="w-full flex items-center px-3 py-2.5 cursor-pointer" 
          @click="arrowChange(!arrowOpen)">
          
          <!-- Transaction icon -->
          <Icon 
            class="flex-shrink-0 text-4 mr-3" 
            icon="icon-shiwu" />
          
          <!-- Transaction name (truncated with tooltip) -->
          <div 
            :title="props.value?.name" 
            class="truncate min-w-55 max-w-100 mr-5 name">
            {{ props.value?.name }}
          </div>
          
          <!-- Right section: Status and body size (commented out) -->
          <div class="flex-1 justify-end flex items-center mr-3">
            <!-- Commented out: Total body size display -->
            <!-- <div class="mr-5">{{ bodySize }}</div> -->
            
            <!-- Status tag based on transaction enabled state -->
            <template v-if="!props.value?.enabled">
              <!-- Disabled status (default) -->
              <StatusTag />
            </template>
            <template v-else>
              <!-- Status based on execution results (success/fail/block) -->
              <StatusTag :value="status" />
            </template>
          </div>
          
          <!-- Expand/collapse arrow indicator -->
          <Arrow 
            :open="arrowOpen" 
            @change="arrowChange" />
        </div>
      </template>
      
      <!--
        Slot for transaction content
        Parent component inserts child steps here (requests, markers, etc.)
      -->
      <slot name="default"></slot>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header {
  padding: 0;
  border-color: var(--border-divider);
  line-height: 20px;
}

.ant-collapse> :deep(.ant-collapse-item) .ant-collapse-content-box {
  padding: 0 12px;
  line-height: 20px;
}
</style>
