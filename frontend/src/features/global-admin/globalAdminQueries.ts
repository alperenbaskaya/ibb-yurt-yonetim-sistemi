import axios from 'axios'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as api from '../../api/globalAdminApi'
import type { AdmissionStatus } from '../../types/student'
import type { CreateDocumentTypeRequest, CreateTermDocumentRequirementRequest, CreateUserRequest, DormitoryTermRequest, UpdateDocumentTypeRequest, UpdateDormitoryRequest, UpdateUserRequest } from '../../types/globalAdmin'

export const globalAdminKeys = {
  root: (userId: number) => ['global-admin', userId] as const,
  dashboard: (u:number) => [...globalAdminKeys.root(u),'dashboard'] as const,
  dormitories: (u:number) => [...globalAdminKeys.root(u),'dormitories'] as const,
  users: (u:number) => [...globalAdminKeys.root(u),'users'] as const,
  studentsRoot: (u:number) => [...globalAdminKeys.root(u),'students'] as const,
  students: (u:number,dormitoryId:number) => [...globalAdminKeys.studentsRoot(u),dormitoryId] as const,
  admissions: (u:number) => [...globalAdminKeys.root(u),'admissions'] as const,
  terms: (u:number) => [...globalAdminKeys.root(u),'terms'] as const,
  documentTypes: (u:number) => [...globalAdminKeys.root(u),'document-types'] as const,
  requirements: (u:number,termId:number) => [...globalAdminKeys.root(u),'requirements',termId] as const,
}
export const useGlobalDashboard=(u:number)=>useQuery({queryKey:globalAdminKeys.dashboard(u),queryFn:api.getGlobalDashboard})
export const useDormitories=(u:number)=>useQuery({queryKey:globalAdminKeys.dormitories(u),queryFn:api.getDormitories})
export const useGlobalUsers=(u:number)=>useQuery({queryKey:globalAdminKeys.users(u),queryFn:api.getGlobalUsers})
export const useGlobalStudents=(u:number,dormitoryId:number|null)=>useQuery({queryKey:globalAdminKeys.students(u,dormitoryId??0),queryFn:()=>api.getGlobalStudentsByDormitory(dormitoryId!),enabled:dormitoryId!==null})
export const useGlobalAdmissions=(u:number)=>useQuery({queryKey:globalAdminKeys.admissions(u),queryFn:()=>api.getGlobalAdmissions()})
export const useDormitoryTerms=(u:number)=>useQuery({queryKey:globalAdminKeys.terms(u),queryFn:api.getDormitoryTerms})
export const useDocumentTypes=(u:number)=>useQuery({queryKey:globalAdminKeys.documentTypes(u),queryFn:api.getDocumentTypes})
export const useRequirements=(u:number,termId:number|null)=>useQuery({queryKey:globalAdminKeys.requirements(u,termId??0),queryFn:()=>api.getRequirementsByTerm(termId!),enabled:termId!==null})

function useInvalidate(){const qc=useQueryClient();return (keys:readonly (readonly unknown[])[])=>Promise.all(keys.map(queryKey=>qc.invalidateQueries({queryKey}))) }
export function useDormitoryMutations(u:number){const inv=useInvalidate();return {create:useMutation({mutationFn:api.createDormitory,onSuccess:()=>inv([globalAdminKeys.dormitories(u),globalAdminKeys.dashboard(u)])}),update:useMutation({mutationFn:({id,request}:{id:number;request:UpdateDormitoryRequest})=>api.updateDormitory(id,request),onSuccess:()=>inv([globalAdminKeys.dormitories(u),globalAdminKeys.dashboard(u)])})}}
export function useUserMutations(u:number){const inv=useInvalidate();return {create:useMutation({mutationFn:(r:CreateUserRequest)=>api.createGlobalUser(r),onSuccess:()=>inv([globalAdminKeys.users(u),globalAdminKeys.dashboard(u)])}),update:useMutation({mutationFn:({id,request}:{id:number;request:UpdateUserRequest})=>api.updateGlobalUser(id,request),onSuccess:()=>inv([globalAdminKeys.users(u),globalAdminKeys.dashboard(u)])})}}
export function useAdmissionMutation(u:number){const inv=useInvalidate();const refresh=()=>inv([globalAdminKeys.admissions(u),globalAdminKeys.dashboard(u),globalAdminKeys.studentsRoot(u)]);return useMutation({mutationFn:({id,status}:{id:number;status:AdmissionStatus})=>api.updateGlobalAdmissionStatus(id,{status}),onSuccess:refresh,onError:async(e:unknown)=>{if(axios.isAxiosError(e)&&e.response?.status===409)await refresh()}})}
export function useTermMutations(u:number){const inv=useInvalidate();const refresh=()=>inv([globalAdminKeys.terms(u),globalAdminKeys.dashboard(u),[...globalAdminKeys.root(u),'requirements']]);return {create:useMutation({mutationFn:(r:DormitoryTermRequest)=>api.createDormitoryTerm(r),onSuccess:refresh}),update:useMutation({mutationFn:({id,request}:{id:number;request:DormitoryTermRequest})=>api.updateDormitoryTerm(id,request),onSuccess:refresh}),active:useMutation({mutationFn:({id,active}:{id:number;active:boolean})=>api.setDormitoryTermActive(id,active),onSuccess:refresh}),remove:useMutation({mutationFn:api.deleteDormitoryTerm,onSuccess:refresh})}}
export function useDocumentTypeMutations(u:number){const inv=useInvalidate();const refresh=()=>inv([globalAdminKeys.documentTypes(u),[...globalAdminKeys.root(u),'requirements']]);return {create:useMutation({mutationFn:(r:CreateDocumentTypeRequest)=>api.createDocumentType(r),onSuccess:refresh}),update:useMutation({mutationFn:({id,request}:{id:number;request:UpdateDocumentTypeRequest})=>api.updateDocumentType(id,request),onSuccess:refresh})}}
export function useRequirementMutations(u:number,termId:number){const qc=useQueryClient();const refresh=()=>qc.invalidateQueries({queryKey:globalAdminKeys.requirements(u,termId)});return {create:useMutation({mutationFn:(r:CreateTermDocumentRequirementRequest)=>api.createRequirement(r),onSuccess:refresh}),update:useMutation({mutationFn:({id,required}:{id:number;required:boolean})=>api.updateRequirement(id,required),onSuccess:refresh})}}
