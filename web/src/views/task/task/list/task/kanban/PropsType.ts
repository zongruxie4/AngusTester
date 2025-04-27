export type SprintPermissionKey = 'MODIFY_SPRINT' | 'DELETE_SPRINT' | 'ADD_TASK' | 'MODIFY_TASK' | 'DELETE_TASK' | 'EXPORT_TASK' | 'RESTART_TASK' | 'REOPEN_TASK' | 'GRANT'

export type ActionMenuItem = {
    name: string;
    key: 'delete' | 'edit' | 'start' | 'processed' | 'uncompleted' | 'completed' | 'reopen' | 'restart' | 'cancel' | 'move' | 'cancelFavourite' | 'favourite' | 'cancelFollow' | 'follow' | 'copyLink';
    icon: string;
    disabled: boolean;
    hide: boolean;
    tip?: string;
}
