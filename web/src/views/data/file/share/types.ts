/**
 * Interface for share list items
 */
export interface ShareListItem {
  expiredDuration: {
    value: string;
    unit: {
      value: string;
      message: string;
    }
  };
  password?: string;
  public0: boolean;
  url: string;
  remark?: string;
  id: string;
  editPassd?: boolean;
  tempPass?: string;
  expiredFlag?: boolean;
  objectIds?: string[];
}

/**
 * Interface for share list state
 */
export interface ShareListState {
  visible: boolean;
  list: ShareListItem[];
}

/**
 * Interface for share form data
 */
export interface ShareForm {
  expiredDuration: string;
  password: string | undefined;
  public0: boolean;
  url: string;
  expiredFlag: boolean;
  remark: string | undefined;
  id: string | undefined;
}


/**
 * Interface for share modal state
 */
export interface ShareModalState {
  enums: Array<{ label: string; value: boolean }>;
  unitEnum: any[];
  expiredEnums: Array<{ label: string; value: boolean }>;
}