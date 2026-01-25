import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [userType, setUserType] = useState<'brand' | 'superadmin' | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Navigate based on selected user type
    if (userType === 'superadmin') {
      navigate('/superadmin');
    } else if (userType === 'brand') {
      navigate('/brands');
    }
  };

  return (
    <div className="login-bg">
      <div className="login-form-bg">
        <div className="login-content">
          <h1 className="login-title">Prizent</h1>
          <p className="login-subtitle">Where fashion meets intelligent pricing</p>
          
          {/* User Type Selection */}
          <div className="user-type-selection">
            <button
              type="button"
              className={`user-type-btn ${userType === 'brand' ? 'active' : ''}`}
              onClick={() => setUserType('brand')}
            >
              Brand User
            </button>
            <button
              type="button"
              className={`user-type-btn ${userType === 'superadmin' ? 'active' : ''}`}
              onClick={() => setUserType('superadmin')}
            >
              Super Admin
            </button>
          </div>

          <form className="login-form" onSubmit={handleSubmit}>
            <div className="login-form-group">
              <label htmlFor="email" className="login-label">Your Email</label>
              <input type="email" id="email" className="login-input" placeholder="enter your username" autoComplete="username" />
            </div>
            <div className="login-form-group">
              <label htmlFor="password" className="login-label">Your Password</label>
              <input type="password" id="password" className="login-input" placeholder="enter your password" autoComplete="current-password" />
            </div>
            <div className="login-actions">
              <button type="button" className="forgot-password-btn">Forgot password?</button>
            </div>
            <button type="submit" className="login-btn" disabled={!userType}>Access workspace</button>
          </form>
        </div>
      </div>
      <div className="login-image-bg"></div>
    </div>
  );
};

export default LoginPage;