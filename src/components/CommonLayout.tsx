import React from 'react';
import { Outlet } from 'react-router-dom';
import CommonNavbar from './CommonNavbar';
import './CommonLayout.css';

const CommonLayout: React.FC = () => {
  return (
    <div className="app-layout">
      <CommonNavbar />
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
};

export default CommonLayout;