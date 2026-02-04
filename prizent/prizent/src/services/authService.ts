import apiClient from './api';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  token?: string;
  user?: {
    id: string;      // UUID
    username: string;
    clientId: string; // Changed from number to string
    roles: string[];
  };
}

const authService = {
  // Login
  login: async (username: string, password: string): Promise<LoginResponse> => {
    console.log('=== AUTH SERVICE LOGIN START ===');
    console.log('authService.login called with username:', username);
    console.log('Password length:', password.length);
    
    const requestBody = {
      username,
      password
    };
    console.log('Request body:', requestBody);
    
    try {
      console.log('Making POST request to /auth/login...');
      const response = await apiClient.post('/auth/login', requestBody);
      
      console.log('✓ Response received!');
      console.log('Response status:', response.status);
      console.log('Response headers:', response.headers);
      console.log('Response data:', response.data);
      console.log('Response data type:', typeof response.data);
      
      // Store token in localStorage if login successful
      if (response.data.success && response.data.token) {
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('user', JSON.stringify(response.data.user));
        console.log('✓ Token and user stored in localStorage');
      }
      
      console.log('=== AUTH SERVICE LOGIN END (SUCCESS) ===');
      return response.data;
    } catch (error: any) {
      console.error('=== AUTH SERVICE LOGIN ERROR ===');
      console.error('Error object:', error);
      console.error('Error message:', error.message);
      console.error('Error response:', error.response);
      console.error('Error request:', error.request);
      console.error('Error config:', error.config);
      console.error('=== AUTH SERVICE LOGIN END (ERROR) ===');
      throw error;
    }
  },

  // Logout
  logout: async () => {
    console.log('=== AUTH SERVICE LOGOUT START ===');
    try {
      const token = localStorage.getItem('token');
      console.log('Token exists:', !!token);
      if (token) {
        console.log('Token preview:', token.substring(0, 20) + '...');
        console.log('Making POST request to /auth/logout...');
        // Call backend logout endpoint - Authorization header will be added by interceptor
        const response = await apiClient.post('/auth/logout', {});
        console.log('✓ Backend logout successful');
        console.log('Response:', response.data);
      } else {
        console.log('No token found in localStorage');
      }
    } catch (error) {
      console.error('❌ Error calling backend logout:', error);
      // Continue with local logout even if backend call fails
    } finally {
      // Always clear local storage
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      console.log('✓ Local storage cleared');
      console.log('=== AUTH SERVICE LOGOUT END ===');
    }
  },

  // Check if user is authenticated
  isAuthenticated: (): boolean => {
    const token = localStorage.getItem('token');
    if (!token) {
      return false;
    }
    
    // Basic check - you can add JWT expiry validation here
    try {
      // Simple validation - check if token exists and user data is valid
      const userStr = localStorage.getItem('user');
      if (!userStr) {
        // Token exists but no user data - invalid state
        localStorage.removeItem('token');
        return false;
      }
      const user = JSON.parse(userStr);
      if (!user || !user.username) {
        // Invalid user data
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        return false;
      }
      return true;
    } catch (error) {
      // Invalid JSON or other error
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      return false;
    }
  },

  // Get current user
  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  // Get token
  getToken: (): string | null => {
    return localStorage.getItem('token');
  }
};

export default authService;
