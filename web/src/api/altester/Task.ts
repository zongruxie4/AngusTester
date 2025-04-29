import { http } from '@xcan-angus/tools';

let baseUrl: string;
export default class API {
  constructor (prefix: string) {
    baseUrl = prefix + '/task';
  }

  loadTaskList (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/search`, params);
  }

  addTag (names: string[]): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/tag`, names);
  }

  addTask (parmas): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}`, parmas);
  }

  editTask (id: string, parmas): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}`, parmas);
  }

  editTaskName (id: string, name: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/name?name=${name}`);
  }

  updateTask (taskId: string, parmas): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${taskId}`, parmas);
  }

  editTaskDescription (id: string, parmas: {description: string}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/description`, parmas);
  }

  editEvalWorkloadApi<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/evalWorkload`, params);
  }

  editActualWorkload<T> (id: string, params: T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/actualWorkload`, params);
  }

  editDeadlineDateApi (id: string, deadlineDate: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/deadline/${deadlineDate}`);
  }

  editTaskAssignees (id: string, parmas: { assigneeId: string }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/assignee`, parmas);
  }

  editConfirmors (id: string, parmas: { confirmorId: string|null }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/confirmor`, parmas);
  }

  editTaskPriority (id: string, priority: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/priority/${priority}`);
  }

  editTaskTaskType (id: string, taskType: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/type?type=${taskType}`);
  }

  updateAttachment (id: string, parmas: { attachments: { name: string, url: string }[] }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/attachment`, parmas);
  }

  editTagsApi (id: string, parmas: { tagIds: string[] | null }): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/tag`, parmas);
  }

  moveTask<T> (params: T) {
    return http.patch(`${baseUrl}/move`, params, { paramsType: false });
  }

  loadTaskInfo (id: string): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${id}`);
  }

  startProcessing (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/start`);
  }

  cancelTask (id: string, option = {}): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/cancel`, null, option);
  }

  completeTask (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/complete`);
  }

  deleteTask (ids: string[]): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}`, { ids });
  }

  processedTask (id: string): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/processed`);
  }

  reopenTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/reopen`);
  }

  restartTask (id: string): Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/${id}/restart`);
  }

  loadTaskRemark (params): Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/remark`, params);
  }

  addTaskRemark (params: { taskId: string, content: string }): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/remark`, params);
  }

  delTaskRemark (remarkId: string): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/remark/${remarkId}`);
  }

  confirmTask (id: string, status: 'SUCCESS' | 'FAIL'): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/result/${status}/confirm`);
  }

  updateResult (id, result: 'SUCCESS' | 'FAIL'): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${id}/process/result/${result}`);
  }

  favouriteTask (id:string, option?: {[key: string]: any}): Promise<[Error | null, any]> {
    if (!option) { option = {}; }
    return http.post(`${baseUrl}/${id}/favourite`, null, { ...option });
  }

  followTask (id:string, option = {}): Promise<[Error | null, any]> {
    return http.post(`${baseUrl}/${id}/follow`, null, option);
  }

  cancelFavouriteTask (id:string, option = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/favourite`, null, option);
  }

  cancelFollowTask (id:string, option = {}): Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/${id}/follow`, null, option);
  }

  deleteTrashTask (taskId: string) : Promise<[Error | null, any]> {
    return http.del(`${baseUrl}/trash/${taskId}`);
  }

  backTrashTask (taskId: string) : Promise<[Error | null, any]> {
    return http.patch(`${baseUrl}/trash/${taskId}/back`);
  }

  searchTrashTask <T> (params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/trash/search`, params);
  }

  getUserSprintAuth<T> (sprintId: string, userId: string, params: T) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/sprint/${sprintId}/user/${userId}/auth`, params);
  }

  getTaskResult (taskType: string, targetId: string, testType: string) : Promise<[Error | null, any]> {
    return http.get(`${baseUrl}/${taskType}/${targetId}/${testType}/result`);
  }

  setSubTask<T> (taskId: string, params:T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/subtask/set`, params, { paramsType: true });
  }

  cancelSubTask<T> (taskId: string, params:T): Promise<[Error | null, any]> {
    return http.put(`${baseUrl}/${taskId}/subtask/cancel`, params, { paramsType: true });
  }

}
