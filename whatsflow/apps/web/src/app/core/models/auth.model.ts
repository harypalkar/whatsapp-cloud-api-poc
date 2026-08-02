export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  tenantId: string;
  email: string;
  roles: string[];
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  code?: string;
}
