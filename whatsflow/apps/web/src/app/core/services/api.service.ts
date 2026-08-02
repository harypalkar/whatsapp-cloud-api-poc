import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  get<T>(path: string, params?: Record<string, string | number | boolean>) {
    let httpParams = new HttpParams();
    if (params) Object.entries(params).forEach(([k, v]) => httpParams = httpParams.set(k, String(v)));
    return this.http.get<ApiResponse<T>>(`${environment.apiBaseUrl}${path}`, { params: httpParams });
  }

  post<T>(path: string, body?: unknown) {
    return this.http.post<ApiResponse<T>>(`${environment.apiBaseUrl}${path}`, body ?? {});
  }

  put<T>(path: string, body?: unknown) {
    return this.http.put<ApiResponse<T>>(`${environment.apiBaseUrl}${path}`, body ?? {});
  }

  delete<T>(path: string) {
    return this.http.delete<ApiResponse<T>>(`${environment.apiBaseUrl}${path}`);
  }
}
