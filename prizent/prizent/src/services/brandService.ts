import apiClient from './api';

export interface Brand {
  id: number;
  clientId: number;
  name: string;
  description?: string;
  logoUrl?: string;
  enabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateBrandRequest {
  name: string;
  description?: string;
  logoUrl?: string;
  enabled?: boolean;
}

export interface UpdateBrandRequest {
  name?: string;
  description?: string;
  logoUrl?: string;
  enabled?: boolean;
}

export interface BrandsResponse {
  success: boolean;
  message: string;
  brands?: Brand[];
  brand?: Brand;
  count?: number;
}

const brandService = {
  // Get all brands
  getAllBrands: async (): Promise<BrandsResponse> => {
    console.log('=== BRAND SERVICE GET ALL BRANDS ===');
    
    try {
      const response = await apiClient.get('admin/brands');
      console.log('✓ Brands retrieved:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error fetching brands:', error);
      throw error;
    }
  },

  // Get brand by ID
  getBrandById: async (brandId: number): Promise<BrandsResponse> => {
    console.log('=== BRAND SERVICE GET BRAND BY ID ===');
    console.log('Brand ID:', brandId);
    
    try {
      const response = await apiClient.get(`admin/brands/${brandId}`);
      console.log('✓ Brand retrieved:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error fetching brand:', error);
      throw error;
    }
  },

  // Create new brand
  createBrand: async (request: CreateBrandRequest): Promise<BrandsResponse> => {
    console.log('=== BRAND SERVICE CREATE BRAND ===');
    console.log('Request:', request);
    
    try {
      const response = await apiClient.post('admin/brands', request);
      console.log('✓ Brand created:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error creating brand:', error);
      throw error;
    }
  },

  // Update brand
  updateBrand: async (brandId: number, request: UpdateBrandRequest): Promise<BrandsResponse> => {
    console.log('=== BRAND SERVICE UPDATE BRAND ===');
    console.log('Brand ID:', brandId);
    console.log('Request:', request);
    
    try {
      const response = await apiClient.put(`admin/brands/${brandId}`, request);
      console.log('✓ Brand updated:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error updating brand:', error);
      throw error;
    }
  },

  // Delete brand
  deleteBrand: async (brandId: number): Promise<BrandsResponse> => {
    console.log('=== BRAND SERVICE DELETE BRAND ===');
    console.log('Brand ID:', brandId);
    
    try {
      const response = await apiClient.delete(`admin/brands/${brandId}`);
      console.log('✓ Brand deleted:', response.data);
      return response.data;
    } catch (error: any) {
      console.error('Error deleting brand:', error);
      throw error;
    }
  },
};

export default brandService;
