import apiClient from './api';

export interface User {
  id: number;
  username: string;
  name: string;
  emailId?: string;
  phoneNumber?: string;
  employeeDesignation?: string;
  role: string;
  enabled: boolean;
  clientId: number;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  name: string;
  emailId?: string;
  phoneNumber?: string;
  employeeDesignation?: string;
  role: string;
  enabled?: boolean;
}

export interface UpdateUserRequest {
  name?: string;
  username?: string;
  emailId?: string;
  phoneNumber?: string;
  employeeDesignation?: string;
  role?: string;
  password?: string;
  enabled?: boolean;
}

export interface UsersResponse {
  success: boolean;
  message: string;
  users?: User[];
  user?: User;
  count?: number;
}

const userService = {
  // Get all users for a client
  getAllUsers: async (clientId: number): Promise<UsersResponse> => {
    console.log('=== USER SERVICE GET ALL USERS ===');
    console.log('Client ID:', clientId);
    
    try {
      const response = await apiClient.get(`admin/users?clientId=${clientId}`);
      console.log('✓ Users retrieved:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error fetching users:', error);
      throw error;
    }
  },

  // Get user by ID
  getUserById: async (userId: number): Promise<UsersResponse> => {
    console.log('=== USER SERVICE GET USER BY ID ===');
    console.log('User ID:', userId);
    
    try {
      const response = await apiClient.get(`admin/users/${userId}`);
      console.log('✓ User retrieved:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error fetching user:', error);
      throw error;
    }
  },

  // Create new user
  createUser: async (request: CreateUserRequest, clientId: number): Promise<UsersResponse> => {
    console.log('=== USER SERVICE CREATE USER ===');
    console.log('Request:', request);
    console.log('Client ID:', clientId);
    
    try {
      const response = await apiClient.post(`admin/users?clientId=${clientId}`, request);
      console.log('✓ User created:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error creating user:', error);
      throw error;
    }
  },

  // Update user
  updateUser: async (userId: number, request: UpdateUserRequest): Promise<UsersResponse> => {
    console.log('=== USER SERVICE UPDATE USER ===');
    console.log('User ID:', userId);
    console.log('Request:', request);
    
    try {
      const response = await apiClient.put(`admin/users/${userId}`, request);
      console.log('✓ User updated:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error updating user:', error);
      throw error;
    }
  },

  // Enable user
  enableUser: async (userId: number): Promise<UsersResponse> => {
    console.log('=== USER SERVICE ENABLE USER ===');
    console.log('User ID:', userId);
    
    try {
      const response = await apiClient.patch(`admin/users/${userId}/enable`);
      console.log('✓ User enabled:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error enabling user:', error);
      throw error;
    }
  },

  // Disable user
  disableUser: async (userId: number): Promise<UsersResponse> => {
    console.log('=== USER SERVICE DISABLE USER ===');
    console.log('User ID:', userId);
    
    try {
      const response = await apiClient.patch(`admin/users/${userId}/disable`);
      console.log('✓ User disabled:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error disabling user:', error);
      throw error;
    }
  },

  // Delete user
  deleteUser: async (userId: number): Promise<UsersResponse> => {
    console.log('=== USER SERVICE DELETE USER ===');
    console.log('User ID:', userId);
    
    try {
      const response = await apiClient.delete(`admin/users/${userId}`);
      console.log('✓ User deleted:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error deleting user:', error);
      throw error;
    }
  },
};

export default userService;
