export type TargetType = 'API' | 'SERVICE';

export type ShareObj = {
  id: string;
  name: string;
  targetType: {
      value: TargetType;
      message: string;
  },
  targetId: string;
  apiIds: string[];
  allFlag: boolean;
  url: string;
  expiredFlag: boolean;
  expiredDate?: string;
  public0: boolean;
  password?: string;
  createdBy: string;
  creator: string;
  createdDate: string;
  validFlag: boolean;
  isEdit?:boolean;
  isLoading?:boolean;
  apiList:{ id: string, summary: string; method: string }[];
}

export interface ListType{
  targetId:string,
  apiIds?: string[],
  password?:string,
  url?:string,
  id:string,
  name:string,
  expiredDate?: string,
  editPassd?: boolean,
  tempPass?: string,
  public0: boolean,
  expiredFlag?: boolean,
  targetType: 'PROJECT' | 'SERVICE' | 'API',
  remark?: string,
  seeUrl?: boolean,
  seePassword?: boolean,
}

export interface StateType{
  visible:boolean,
  list:Array<ListType>
}
