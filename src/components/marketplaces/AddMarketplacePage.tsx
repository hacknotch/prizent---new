import React from "react";
import { useNavigate } from "react-router-dom";
import "./AddMarketplacePage.css";

const AddMarketplacePage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="add-marketplace-bg">
      <main className="add-marketplace-main">
        <header className="add-marketplace-header">
          <button className="back-btn" onClick={() => navigate("/marketplaces")}> 
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M15 18L9 12L15 6" stroke="#1E1E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
          <h2 className="breadcrumb">Add Marketplace</h2>

          <div className="header-actions">
            <button className="icon-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M10.5 18C14.6421 18 18 14.6421 18 10.5C18 6.35786 14.6421 3 10.5 3C6.35786 3 3 6.35786 3 10.5C3 14.6421 6.35786 18 10.5 18Z" stroke="#1E1E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M21 21L16.65 16.65" stroke="#1E1E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
            <button className="icon-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 8C18 6.4087 17.3679 4.88258 16.2426 3.75736C15.1174 2.63214 13.5913 2 12 2C10.4087 2 8.88258 2.63214 7.75736 3.75736C6.63214 4.88258 6 6.4087 6 8C6 15 3 17 3 17H21C21 17 18 15 18 8Z" stroke="#1E1E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M13.73 21C13.5542 21.3031 13.3019 21.5547 12.9982 21.7295C12.6946 21.9044 12.3504 21.9965 12 21.9965C11.6496 21.9965 11.3054 21.9044 11.0018 21.7295C10.6982 21.5547 10.4458 21.3031 10.27 21" stroke="#1E1E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
            <button className="icon-btn profile-btn">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="#1E1E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="#1E1E1E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </button>
          </div>
        </header>

        <h3 className="section-title">Marketplace Details</h3>

        <div className="details-card">
          <input className="text-input" placeholder="enter marketplace name" />
          <input className="text-input" placeholder="discription" />
          <div className="select-input">
            <span>Category mapping</span>
            <svg width="10" height="5" viewBox="0 0 10 5" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M1 1L5 4L9 1" stroke="#454545" strokeWidth="1.25" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </div>
          <label className="activate-row">
            <input type="checkbox" />
            <span>Activate marketplace</span>
          </label>
        </div>

        <div className="grid-3">
          <div className="panel">
            <div className="panel-header">
              <span>Product cost</span>
              <div className="panel-units">
                <span>%</span>
                <label className="switch">
                  <input type="checkbox" />
                  <span className="slider" />
                </label>
                <span>Rs</span>
              </div>
            </div>
            <div className="panel-divider" />
            <div className="panel-columns">
              <span>From cost</span>
              <span>To cost</span>
            </div>
            <div className="panel-form-grid">
              <input className="small-input" placeholder="0" />
              <input className="small-input" placeholder="500" />
              <input className="small-input" placeholder="500" />
              <input className="small-input" placeholder="1000" />
            </div>
            <button className="link-btn">+ Add slab</button>
          </div>

          <div className="panel">
            <div className="panel-header">
              <span>Commission Breakdown</span>
            </div>
            <div className="panel-divider" />
            <div className="commission-row">
              <div className="select-sm">Platform fee</div>
              <input className="num-input" placeholder="500" />
              <span className="unit">%</span>
              <label className="switch">
                <input type="checkbox" />
                <span className="slider" />
              </label>
              <span className="unit">Rs</span>
            </div>
            <div className="commission-row">
              <div className="select-sm">Logistics</div>
              <input className="num-input" placeholder="500" />
              <span className="unit">%</span>
              <label className="switch">
                <input type="checkbox" />
                <span className="slider" />
              </label>
              <span className="unit">Rs</span>
            </div>
            <div className="commission-row">
              <div className="select-sm">Platform fee</div>
              <input className="num-input" placeholder="500" />
              <span className="unit">%</span>
              <label className="switch">
                <input type="checkbox" />
                <span className="slider" />
              </label>
              <span className="unit">Rs</span>
            </div>
            <button className="link-btn">+ Add Commision</button>
          </div>

          <div className="panel">
            <div className="panel-header">
              <span>Shipping Cost</span>
            </div>
            <div className="panel-divider" />
            <div className="panel-columns">
              <span>Cost Slabs</span>
              <span>Shipping cost</span>
            </div>
            <div className="panel-form-grid">
              <div className="select-sm">1-500rs</div>
              <input className="small-input" placeholder="500" />
              <div className="select-sm">500-800rs</div>
              <input className="small-input" placeholder="500" />
            </div>
            <button className="link-btn">+ cost slab</button>
          </div>
        </div>

        <div className="footer-actions">
          <button className="cancel-btn" onClick={() => navigate("/marketplaces")}>Cancel</button>
          <button className="save-btn">SAVE</button>
        </div>
      </main>
    </div>
  );
};

export default AddMarketplacePage;
