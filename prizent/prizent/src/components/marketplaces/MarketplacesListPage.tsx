import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./MarketplacesListPage.css";
import marketplaceService, { Marketplace, getSlabsForCategory } from '../../services/marketplaceService';
import { getCustomFields, getCustomFieldValues, CustomFieldResponse, CustomFieldValueResponse } from '../../services/customFieldService';
import brandService, { Brand } from '../../services/brandService';

const MarketplacesListPage: React.FC = () => {
  const navigate = useNavigate();
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(8);
  const [marketplaces, setMarketplaces] = useState<Marketplace[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [filter, setFilter] = useState({ name: '', description: '', enabled: 'all' as 'all' | 'enabled' | 'disabled', brandId: '' });
  type Slab = { id: number; from: string; to: string; value: string; valueType: 'P' | 'A' };
  const createEmptySlab = (): Slab => ({ id: Date.now() + Math.floor(Math.random() * 1000000), from: '', to: '', value: '', valueType: 'A' });

  const [brandSections, setBrandSections] = useState<Array<{
    id: number;
    brandId: string;
    commissionSlabs: Slab[];
    marketingSlabs: Slab[];
    shippingSlabs: Slab[];
  }>>([{ id: Date.now(), brandId: '', commissionSlabs: [createEmptySlab()], marketingSlabs: [createEmptySlab()], shippingSlabs: [createEmptySlab()] }]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [customFields, setCustomFields] = useState<CustomFieldResponse[]>([]);
  const [marketplaceFieldValues, setMarketplaceFieldValues] = useState<Map<number, CustomFieldValueResponse[]>>(new Map());

  // Fetch custom fields on component mount
  useEffect(() => {
    const fetchCustomFieldsData = async () => {
      try {
        const fields = await getCustomFields('m'); // 'm' for marketplaces
        const enabledFields = fields.filter(f => f.enabled);
        setCustomFields(enabledFields);
        console.log('Loaded marketplace custom fields:', enabledFields);
      } catch (err) {
        console.error('Failed to fetch custom fields:', err);
      }
    };
    fetchCustomFieldsData();
    // fetch brands for filter
    const fetchBrands = async () => {
      try {
        const resp = await brandService.getAllBrands();
        if (resp.success && resp.brands) setBrands(resp.brands);
      } catch (err) {
        console.error('Failed to fetch brands for filters', err);
      }
    };
    fetchBrands();
  }, []);

  // Fetch marketplaces on component mount and page change
  useEffect(() => {
    fetchMarketplaces();
  }, [currentPage]);

  const fetchMarketplaces = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Convert 1-based page to 0-based for API
      const apiPage = currentPage - 1;
      const response = await marketplaceService.getAllMarketplaces(apiPage, itemsPerPage);
      
      if (response.success && response.marketplaces) {
        setMarketplaces(response.marketplaces.content);
        setTotalPages(response.marketplaces.totalPages);
        setTotalElements(response.marketplaces.totalElements);

        // Fetch custom field values for all marketplaces
        const valuesMap = new Map<number, CustomFieldValueResponse[]>();
        await Promise.all(
          response.marketplaces.content.map(async (marketplace: Marketplace) => {
            try {
              const values = await getCustomFieldValues('m', marketplace.id);
              if (values && values.length > 0) {
                valuesMap.set(marketplace.id, values);
              }
            } catch {
              // Skip marketplaces with no custom field values
            }
          })
        );
        setMarketplaceFieldValues(valuesMap);
      } else {
        setError(response.message || 'Failed to load marketplaces');
      }
    } catch (err: any) {
      console.error('Error fetching marketplaces:', err);
      setError('Failed to load marketplaces. Please try again.');
      
      // Fallback: Set empty data if API fails
      setMarketplaces([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  // Client-side filtering for quick layout preview (name/description/brand/enabled)
  const filteredMarketplaces = marketplaces.filter(m => {
    if (filter.name && !m.name.toLowerCase().includes(filter.name.toLowerCase())) return false;
    if (filter.description && !m.description.toLowerCase().includes(filter.description.toLowerCase())) return false;
    if (filter.brandId) {
      // Brand relationship isn't on marketplace object by default; skip if absent
      // Leaving placeholder for future backend-provided brand info
    }
    if (filter.enabled === 'enabled' && !m.enabled) return false;
    if (filter.enabled === 'disabled' && m.enabled) return false;
    return true;
  });

  // When selected marketplace changes, populate slabs into the first brand section
  useEffect(() => {
    const mp = filteredMarketplaces.length > 0 ? filteredMarketplaces[0] : null;
    if (!mp) {
      setBrandSections(prev => prev.map((s, i) => i === 0 ? ({ ...s, commissionSlabs: [createEmptySlab()], marketingSlabs: [createEmptySlab()], shippingSlabs: [createEmptySlab()] }) : s));
      return;
    }
    const comm: Slab[] = (mp.costs || []).filter(c => c.costCategory === 'COMMISSION').map((c, idx) => ({ id: Date.now() + idx, from: (c.costProductRange || '').split('-')[0] || '', to: (c.costProductRange || '').split('-')[1] || '', value: String(c.costValue), valueType: c.costValueType }));
    const mark: Slab[] = (mp.costs || []).filter(c => c.costCategory === 'MARKETING').map((c, idx) => ({ id: Date.now() + idx + 1000, from: (c.costProductRange || '').split('-')[0] || '', to: (c.costProductRange || '').split('-')[1] || '', value: String(c.costValue), valueType: c.costValueType }));
    const ship: Slab[] = (mp.costs || []).filter(c => c.costCategory === 'SHIPPING').map((c, idx) => ({ id: Date.now() + idx + 2000, from: (c.costProductRange || '').split('-')[0] || '', to: (c.costProductRange || '').split('-')[1] || '', value: String(c.costValue), valueType: c.costValueType }));

    setBrandSections(prev => {
      if (prev.length === 0) return [{ id: Date.now(), brandId: '', commissionSlabs: comm.length ? comm : [createEmptySlab()], marketingSlabs: mark.length ? mark : [createEmptySlab()], shippingSlabs: ship.length ? ship : [createEmptySlab()] }];
      return prev.map((s, i) => i === 0 ? ({ ...s, commissionSlabs: comm.length ? comm : [createEmptySlab()], marketingSlabs: mark.length ? mark : [createEmptySlab()], shippingSlabs: ship.length ? ship : [createEmptySlab()] }) : s);
    });
  }, [marketplaces, filteredMarketplaces]);

  const validateNumericInput = (value: string) => value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');

  const updateSlab = (sectionIndex: number, which: 'commission'|'marketing'|'shipping', index:number, field:string, value:string) => {
    const validated = field === 'value' ? validateNumericInput(value) : value;
    setBrandSections(prev => prev.map((sec, si) => {
      if (si !== sectionIndex) return sec;
      const update = (arr: Slab[]) => arr.map((s,i) => i===index ? ({ ...s, [field]: validated }) : s);
      if (which === 'commission') return { ...sec, commissionSlabs: update(sec.commissionSlabs) };
      if (which === 'marketing') return { ...sec, marketingSlabs: update(sec.marketingSlabs) };
      return { ...sec, shippingSlabs: update(sec.shippingSlabs) };
    }));
  };

  const addSlab = (sectionIndex:number, which:'commission'|'marketing'|'shipping') => {
    setBrandSections(prev => prev.map((sec, si) => si===sectionIndex ? (
      which === 'commission' ? { ...sec, commissionSlabs: [ ...sec.commissionSlabs, createEmptySlab() ] } : which === 'marketing' ? { ...sec, marketingSlabs: [ ...sec.marketingSlabs, createEmptySlab() ] } : { ...sec, shippingSlabs: [ ...sec.shippingSlabs, createEmptySlab() ] }
    ) : sec));
    // focus the newly added slab's first input
    setTimeout(() => {
      try {
        const box = document.querySelector(`.brand-box[data-section="${sectionIndex}"]`);
        if (!box) return;
        const rows = box.querySelectorAll('.panel-form-grid');
        const last = rows[rows.length - 1] as HTMLElement | undefined;
        if (last) {
          const input = last.querySelector('input.small-input') as HTMLInputElement | null;
          if (input) input.focus();
        }
      } catch (e) {
        // ignore focus errors
      }
    }, 50);
  };

  const removeSlab = (sectionIndex:number, which:'commission'|'marketing'|'shipping', index:number) => {
    setBrandSections(prev => prev.map((sec, si) => {
      if (si !== sectionIndex) return sec;
      if (which === 'commission') return { ...sec, commissionSlabs: sec.commissionSlabs.length>1 ? sec.commissionSlabs.filter((_,i)=>i!==index) : sec.commissionSlabs };
      if (which === 'marketing') return { ...sec, marketingSlabs: sec.marketingSlabs.length>1 ? sec.marketingSlabs.filter((_,i)=>i!==index) : sec.marketingSlabs };
      return { ...sec, shippingSlabs: sec.shippingSlabs.length>1 ? sec.shippingSlabs.filter((_,i)=>i!==index) : sec.shippingSlabs };
    }));
  };

  const addBrandSection = () => {
    setBrandSections(prev => [...prev, { id: Date.now(), brandId: '', commissionSlabs: [createEmptySlab()], marketingSlabs: [createEmptySlab()], shippingSlabs: [createEmptySlab()] }]);
  };

  

  const removeBrandSection = (index: number) => {
    setBrandSections(prev => {
      if (prev.length <= 1) return prev; // keep at least one section
      return prev.filter((_, i) => i !== index);
    });
  };

  const handleToggleStatus = async (marketplace: Marketplace) => {
    try {
      await marketplaceService.toggleMarketplace(marketplace.id, !marketplace.enabled);
      // Refresh the data after toggle
      await fetchMarketplaces();
    } catch (err: any) {
      console.error('Error toggling marketplace:', err);
      setError('Failed to update marketplace status');
    }
  };

  const handleDeleteMarketplace = async (marketplace: Marketplace) => {
    if (!window.confirm(`Are you sure you want to delete marketplace "${marketplace.name}"? This action cannot be undone.`)) {
      return;
    }
    
    try {
      setLoading(true);
      await marketplaceService.deleteMarketplace(marketplace.id);
      // Refresh the data after delete
      await fetchMarketplaces();
    } catch (err: any) {
      console.error('Error deleting marketplace:', err);
      setError('Failed to delete marketplace');
    } finally {
      setLoading(false);
    }
  };

  const handleEditMarketplace = (marketplace: Marketplace) => {
    // Navigate to edit page (implement when edit page is ready)
    navigate(`/marketplaces/edit/${marketplace.id}`);
  };

  // Pagination handlers
  const goToPage = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  const goToPrevious = () => {
    if (currentPage > 1) {
      setCurrentPage(currentPage - 1);
    }
  };

  const goToNext = () => {
    if (currentPage < totalPages) {
      setCurrentPage(currentPage + 1);
    }
  };

  // Generate page numbers to display
  const getPageNumbers = () => {
    const pages = [];
    if (totalPages <= 5) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage <= 3) {
        pages.push(1, 2, 3, 4, 5);
      } else if (currentPage >= totalPages - 2) {
        pages.push(totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages);
      } else {
        pages.push(currentPage - 2, currentPage - 1, currentPage, currentPage + 1, currentPage + 2);
      }
    }
    return pages;
  };

  return (
    <div className="marketplaces-bg">
      <main className="marketplaces-main">
        <header className="marketplaces-header">
          <span className="breadcrumb">Configuration &gt; Marketplaces</span>
          <div className="header-actions">
            <button className="icon-btn">
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M8.85713 0.000114168C3.97542 0.000114168 0 3.97554 0 8.85724C0 13.7389 3.97542 17.7144 8.85713 17.7144C10.9899 17.7144 12.9497 16.9555 14.4811 15.6932L18.5368 19.7489C18.8717 20.0837 19.4141 20.0837 19.7489 19.7489C20.0837 19.4141 20.0837 18.8705 19.7489 18.5368L15.6932 14.4811C16.9555 12.9499 17.7144 10.99 17.7144 8.85713C17.7144 3.97542 13.7388 0.000114168 8.85713 0.000114168ZM8.85713 1.7144C12.8125 1.7144 16 4.90182 16 8.85724C16 12.8127 12.8125 16.0001 8.85713 16.0001C4.90171 16.0001 1.71428 12.8127 1.71428 8.85724C1.71428 4.90182 4.90171 1.7144 8.85713 1.7144Z" fill="#1E1E1E"/>
              </svg>
            </button>
            <button className="icon-btn">
              <svg width="16" height="21" viewBox="0 0 16 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M7.74966 0.25C6.66274 0.25 5.77627 1.13519 5.77627 2.22038V3.15169C3.34058 3.96277 1.58929 6.23466 1.58929 8.9187V14.0271L0.283816 16.7429C0.25865 16.7955 0.247214 16.8535 0.250574 16.9116C0.253933 16.9697 0.271978 17.026 0.303028 17.0753C0.334077 17.1246 0.37712 17.1652 0.428145 17.1934C0.47917 17.2216 0.536515 17.2364 0.594837 17.2366H4.71029V17.2493C4.71029 18.9083 6.07492 20.25 7.74966 20.25C9.4244 20.25 10.787 18.9083 10.787 17.2493V17.2366H14.9025C14.961 17.2369 15.0187 17.2224 15.0701 17.1944C15.1215 17.1664 15.1649 17.1258 15.1963 17.0765C15.2276 17.0271 15.2459 16.9706 15.2494 16.9123C15.2529 16.8539 15.2414 16.7957 15.2162 16.7429L13.91 14.0271V8.9187C13.91 6.23391 12.1578 3.96151 9.72103 3.15102V2.22038C9.72103 1.13519 8.83658 0.25 7.74966 0.25ZM7.7166 0.940239C7.72771 0.939962 7.73847 0.940239 7.74966 0.940239C8.46558 0.940239 9.0295 1.50507 9.0295 2.22038V2.96515C8.61676 2.87928 8.18848 2.83384 7.74966 2.83384C7.30964 2.83384 6.8809 2.8795 6.46712 2.96583V2.22038C6.46712 1.51625 7.01656 0.957515 7.7166 0.940239ZM7.74966 3.5234C10.7881 3.5234 13.2192 5.92639 13.2192 8.9187V14.1032C13.2187 14.1551 13.23 14.2064 13.2522 14.2534L14.354 16.547H1.14266L2.24439 14.2534C2.26753 14.2067 2.27975 14.1553 2.28015 14.1032V8.9187C2.28015 5.92639 4.71119 3.52341 7.74966 3.5234ZM5.40115 17.2366H10.0955V17.2493C10.0955 18.534 9.0578 19.5604 7.74966 19.5604C6.44152 19.5604 5.40115 18.534 5.40115 17.2493V17.2366Z" fill="black" stroke="#1E1E1E" strokeWidth="0.5"/>
              </svg>
            </button>
            <button className="icon-btn profile-btn">
              <svg width="21" height="20" viewBox="0 0 21 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                <mask id="path-1-inside-1_166_508" fill="white">
                  <path d="M5.6703 11.1873H14.606C16.166 11.1873 17.5839 11.8254 18.6113 12.8528C19.6387 13.8802 20.2769 15.2981 20.2769 16.8582V19.5251C20.2769 19.7871 20.0639 20 19.802 20H0.474905C0.21231 20 0 19.7871 0 19.5251V16.8582C0 15.2981 0.638172 13.8809 1.66558 12.8528C2.69299 11.8248 4.11087 11.1873 5.67092 11.1873H5.6703ZM10.1381 0C11.5032 0 12.7386 0.553124 13.6338 1.44768C14.5284 2.34224 15.0815 3.57823 15.0815 4.94273C15.0815 6.30785 14.5284 7.54384 13.6338 8.4384C12.7392 9.33296 11.5032 9.88608 10.1381 9.88608C8.77301 9.88608 7.53763 9.33296 6.64246 8.4384C5.7479 7.54384 5.19477 6.30785 5.19477 4.94273C5.19477 3.57823 5.7479 2.34224 6.64246 1.44768C7.53701 0.553124 8.77301 0 10.1381 0ZM12.9615 2.12C12.2389 1.3974 11.2406 0.95043 10.1381 0.95043C9.0356 0.95043 8.03737 1.3974 7.31477 2.12C6.59217 2.8426 6.1452 3.84083 6.1452 4.94335C6.1452 6.04588 6.59217 7.04473 7.31477 7.76671C8.03737 8.48931 9.0356 8.93565 10.1381 8.93565C11.2406 8.93565 12.2389 8.48869 12.9615 7.76671C13.6841 7.04411 14.131 6.04588 14.131 4.94335C14.131 3.84083 13.6841 2.8426 12.9615 2.12ZM14.606 12.1377H5.6703C4.37223 12.1377 3.19272 12.6691 2.33665 13.5245C1.48121 14.38 0.949809 15.5601 0.949809 16.8576V19.0496H19.3264V16.8576C19.3264 15.5601 18.795 14.38 17.9396 13.5245C17.0841 12.6691 15.904 12.1377 14.606 12.1377Z"/>
                </mask>
                <path d="M5.6703 11.1873H14.606C16.166 11.1873 17.5839 11.8254 18.6113 12.8528C19.6387 13.8802 20.2769 15.2981 20.2769 16.8582V19.5251C20.2769 19.7871 20.0639 20 19.802 20H0.474905C0.21231 20 0 19.7871 0 19.5251V16.8582C0 15.2981 0.638172 13.8809 1.66558 12.8528C2.69299 11.8248 4.11087 11.1873 5.67092 11.1873H5.6703ZM10.1381 0C11.5032 0 12.7386 0.553124 13.6338 1.44768C14.5284 2.34224 15.0815 3.57823 15.0815 4.94273C15.0815 6.30785 14.5284 7.54384 13.6338 8.4384C12.7392 9.33296 11.5032 9.88608 10.1381 9.88608C8.77301 9.88608 7.53763 9.33296 6.64246 8.4384C5.7479 7.54384 5.19477 6.30785 5.19477 4.94273C5.19477 3.57823 5.7479 2.34224 6.64246 1.44768C7.53701 0.553124 8.77301 0 10.1381 0ZM12.9615 2.12C12.2389 1.3974 11.2406 0.95043 10.1381 0.95043C9.0356 0.95043 8.03737 1.3974 7.31477 2.12C6.59217 2.8426 6.1452 3.84083 6.1452 4.94335C6.1452 6.04588 6.59217 7.04473 7.31477 7.76671C8.03737 8.48931 9.0356 8.93565 10.1381 8.93565C11.2406 8.93565 12.2389 8.48869 12.9615 7.76671C13.6841 7.04411 14.131 6.04588 14.131 4.94335C14.131 3.84083 13.6841 2.8426 12.9615 2.12ZM14.606 12.1377H5.6703C4.37223 12.1377 3.19272 12.6691 2.33665 13.5245C1.48121 14.38 0.949809 15.5601 0.949809 16.8576V19.0496H19.3264V16.8576C19.3264 15.5601 18.795 14.38 17.9396 13.5245C17.0841 12.6691 15.904 12.1377 14.606 12.1377Z" stroke="#1E1E1E" strokeWidth="2" mask="url(#path-1-inside-1_166_508)"/>
              </svg>
            </button>
          </div>
        </header>

        <div className="marketplaces-divider" />

        <div className="marketplaces-toolbar">
          <div className="marketplaces-title-block">
            <h2 className="marketplace-list-title">Marketplace List</h2>
            <span className="marketplaces-list-count">{loading ? 'Loading...' : 'Add/Edit Marketplace'}</span>
          </div>
          
        </div>

        <div className="marketplaces-card">
          {error && (
            <div className="error-message" style={{padding: '16px', textAlign: 'center', color: '#C23939'}}>
              {error}
            </div>
          )}
          {/* Top filter / quick-edit area matching hand-drawn layout */}
          <div className="marketplace-top-form">
            <input className="mf-input" placeholder="Name" value={filter.name} onChange={(e) => setFilter({...filter, name: e.target.value})} />
            <input className="mf-input" placeholder="Description" value={filter.description} onChange={(e) => setFilter({...filter, description: e.target.value})} />
            <label className="mf-enable">
              <input type="checkbox" checked={filter.enabled !== 'all' && filter.enabled === 'enabled'} onChange={(e) => setFilter({...filter, enabled: e.target.checked ? 'enabled' : 'all'})} />
              <span>Enable</span>
            </label>
          </div>

          <div className="top-add-wrapper">
            <button className="add-marketplace-btn" onClick={addBrandSection}>
              <svg width="10" height="10" viewBox="0 0 10 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="4" width="2" height="10" fill="white" />
                <rect y="4" width="10" height="2" fill="white" />
              </svg>
              Add New Brand
            </button>
          </div>

          
          {brandSections.map((section, si) => (
            <div key={section.id} className="brand-box" data-section={si}>
              <div className="brand-title-row">
                <h3 className="brand-title">BRAND</h3>
                <button className="remove-section-btn" onClick={() => removeBrandSection(si)} type="button">✕</button>
              </div>
              <div className="brand-controls">
                <select className="mf-select" value={section.brandId} onChange={(e) => setBrandSections(prev => prev.map((s, i) => i===si?({...s, brandId: e.target.value}):s))}>
                  <option value="">Select Brand</option>
                  {brands.map(b => (<option key={b.id} value={String(b.id)}>{b.name}</option>))}
                </select>
              </div>

              <div className="brand-panels">
                <div className="mc-panel">
                  <div className="mc-panel-title">Commission</div>
                  <div className="mc-panel-body">
                    {section.commissionSlabs.map((s, i) => (
                      <div key={s.id} className="panel-form-grid" data-slab={i} style={{gridTemplateColumns: '1fr 1fr 1fr auto'}}>
                        <input className="small-input" placeholder="From cost" value={s.from} onChange={(e)=>updateSlab(si, 'commission', i, 'from', e.target.value)} />
                        <input className="small-input" placeholder="To cost" value={s.to} onChange={(e)=>updateSlab(si, 'commission', i, 'to', e.target.value)} />
                        <input className="small-input" placeholder="Value" value={s.value} onChange={(e)=>updateSlab(si, 'commission', i, 'value', e.target.value)} />
                        {section.commissionSlabs.length>1 && <button className="delete-btn" onClick={()=>removeSlab(si, 'commission', i)} type="button">✕</button>}
                      </div>
                    ))}
                    <button className="link-btn" onClick={()=>addSlab(si, 'commission')} type="button">+ Add slab</button>
                  </div>
                </div>

                <div className="mc-panel">
                  <div className="mc-panel-title">Marketing</div>
                  <div className="mc-panel-body">
                    {section.marketingSlabs.map((s, i) => (
                      <div key={s.id} className="panel-form-grid" data-slab={i} style={{gridTemplateColumns: '1fr 1fr 1fr auto'}}>
                        <input className="small-input" placeholder="From cost" value={s.from} onChange={(e)=>updateSlab(si, 'marketing', i, 'from', e.target.value)} />
                        <input className="small-input" placeholder="To cost" value={s.to} onChange={(e)=>updateSlab(si, 'marketing', i, 'to', e.target.value)} />
                        <input className="small-input" placeholder="Value" value={s.value} onChange={(e)=>updateSlab(si, 'marketing', i, 'value', e.target.value)} />
                        {section.marketingSlabs.length>1 && <button className="delete-btn" onClick={()=>removeSlab(si, 'marketing', i)} type="button">✕</button>}
                      </div>
                    ))}
                    <button className="link-btn" onClick={()=>addSlab(si, 'marketing')} type="button">+ Add slab</button>
                  </div>
                </div>

                <div className="mc-panel">
                  <div className="mc-panel-title">Shipping</div>
                  <div className="mc-panel-body">
                    {section.shippingSlabs.map((s, i) => (
                      <div key={s.id} className="panel-form-grid" data-slab={i} style={{gridTemplateColumns: '1fr 1fr 1fr auto'}}>
                        <input className="small-input" placeholder="From cost" value={s.from} onChange={(e)=>updateSlab(si, 'shipping', i, 'from', e.target.value)} />
                        <input className="small-input" placeholder="To cost" value={s.to} onChange={(e)=>updateSlab(si, 'shipping', i, 'to', e.target.value)} />
                        <input className="small-input" placeholder="Value" value={s.value} onChange={(e)=>updateSlab(si, 'shipping', i, 'value', e.target.value)} />
                        {section.shippingSlabs.length>1 && <button className="delete-btn" onClick={()=>removeSlab(si, 'shipping', i)} type="button">✕</button>}
                      </div>
                    ))}
                    <button className="link-btn" onClick={()=>addSlab(si, 'shipping')} type="button">+ Add slab</button>
                  </div>
                </div>
              </div>
            </div>
          ))}
          {/* History listing removed per design request */}
          
          
            <div className="marketplaces-table">
              {loading ? (
                <div style={{padding: '40px', textAlign: 'center'}}>
                  Loading marketplaces...
                </div>
              ) : (
                filteredMarketplaces.map((marketplace) => {
                const fieldValues = marketplaceFieldValues.get(marketplace.id) || [];
                const getFieldValue = (fieldId: number) => {
                  const value = fieldValues.find(v => v.customFieldId === fieldId);
                  return value ? value.value : '-';
                };
                
                return (
                  <div className="marketplace-card" key={marketplace.id}>
                    <div className="mc-header">
                      <div className="mc-name">{marketplace.name}</div>
                      <div className={`status-badge ${marketplace.enabled ? 'active' : 'inactive'}`}>{marketplace.enabled ? 'Active' : 'Inactive'}</div>
                    </div>
                    <div className="mc-description">{marketplace.description}</div>
                    <div className="mc-brand">Brand: {(marketplace as any).brandName || '-'}</div>
                    <div className="mc-panels">
                      <div className="mc-panel">
                        <div className="mc-panel-title">Commission</div>
                        <div className="mc-panel-body">
                          {getSlabsForCategory(marketplace.costs || [], 'COMMISSION').map((s, i) => <div key={i} className="mc-slab">{s}</div>)}
                        </div>
                      </div>
                      <div className="mc-panel">
                        <div className="mc-panel-title">Marketing</div>
                        <div className="mc-panel-body">
                          {getSlabsForCategory(marketplace.costs || [], 'MARKETING').map((s, i) => <div key={i} className="mc-slab">{s}</div>)}
                        </div>
                      </div>
                      <div className="mc-panel">
                        <div className="mc-panel-title">Shipping</div>
                        <div className="mc-panel-body">
                          {getSlabsForCategory(marketplace.costs || [], 'SHIPPING').map((s, i) => <div key={i} className="mc-slab">{s}</div>)}
                        </div>
                      </div>
                    </div>
                    <div className="mc-actions">
                      <button className="action-btn edit-btn" title="Edit" onClick={() => handleEditMarketplace(marketplace)}>Edit</button>
                      <button className="action-btn delete-btn" title="Delete" onClick={() => handleDeleteMarketplace(marketplace)}>Delete</button>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default MarketplacesListPage;