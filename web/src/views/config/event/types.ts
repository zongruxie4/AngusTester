/**
 * Table column configuration
 */
export interface TableColumn {
  title: string;
  dataIndex: string;
  key?: string;
  width?: string;
  ellipsis?: boolean;
}

/**
 * Push configuration for event notifications
 * @interface Push
 */
export interface Push {
  /**
   * Primary key with value and display message
   */
  pkey: {
    /**
     * Key value
     */
    value: string;

    /**
     * Display message
     */
    message: string
  };

  /**
   * Array of receive addresses (currently null)
   */
  receiveAddresses: null;

  /**
   * Array of receive IDs
   */
  receiveIds: string[];
}

/**
 * Push setting configuration for events
 * @interface PushSetting
 */
export interface PushSetting {
  /**
   * Event key
   */
  ekey: string;

  /**
   * Event code identifier
   */
  eventCode: string;

  /**
   * Display name of the event
   */
  eventName: string;

  /**
   * Event type with value and display message
   */
  eventType: {
    /**
     * Type value
     */
    value: string;

    /**
     * Display message
     */
    message: string
  };

  /**
   * Unique identifier for the push setting
   */
  id: string;

  /**
   * Array of notice types with value and display message
   */
  noticeTypes: {
    /**
     * Type value
     */
    value: string;

    /**
     * Display message
     */
    message: string
  }[];

  /**
   * Array of push configurations
   */
  pushSetting: Push[];

  /**
   * Array of push types with value and display message
   */
  pushType: {
    /**
     * Type value
     */
    value: string;

    /**
     * Display message
     */
    message: string
  }[];

  /**
   * Receive setting configuration
   */
  receiveSetting: {
    /**
     * Receiver configuration
     */
    receivers: {
      /**
       * Array of receiver types
       */
      receiverTypes: {
        /**
         * Type value
         */
        value: string;

        /**
         * Display message
         */
        message: string
      }[];

      /**
       * Array of notice types
       */
      noticeTypes: {
        /**
         * Type value
         */
        value: string;

        /**
         * Display message
         */
        message: string
      }[];

      /**
       * Array of receivers
       */
      receivers: {
        /**
         * Receiver full name
         */
        fullName: string;

        /**
         * Receiver ID
         */
        id: string;
      }[];
    };

    /**
     * Array of channels
     */
    channels: {
      /**
       * Channel type
       */
      channelType: {
        /**
         * Type value
         */
        value: string;

        /**
         * Display message
         */
        message: string
      };

      /**
       * Channel ID
       */
      id: string;
    }[];
  };

  /**
   * Sanitized push message
   */
  pushMsg?: string;

  /**
   * Target type
   */
  targetType?: string;
}

/**
 * Push record for event notifications
 * @interface PushRecord
 */
export interface PushRecord {
  /**
   * Description of the event
   */
  description: string;

  /**
   * Event key
   */
  ekey: string;

  /**
   * Error message (if any)
   */
  errMsg: string;

  /**
   * Event code identifier
   */
  eventCode: string;

  /**
   * Event content details
   */
  eventContent: string;

  /**
   * Execution number
   */
  execNo: string;

  /**
   * Unique identifier for the push record
   */
  id: string;

  /**
   * Push status with value and display message
   */
  pushStatus: {
    /**
     * Status value
     */
    value: string;

    /**
     * Display message
     */
    message: string
  };

  /**
   * Tenant ID
   */
  tenantId: string;

  /**
   * Tenant name
   */
  tenantName: string;

  /**
   * Trigger time of the event
   */
  triggerTime: string;

  /**
   * Event type
   */
  type: string;

  /**
   * Push message content
   */
  pushMsg?: string;

  /**
   * Event name
   */
  name?: string;

  /**
   * Full name
   */
  fullName?: string;

  /**
   * Created date
   */
  createdDate?: string;
}
