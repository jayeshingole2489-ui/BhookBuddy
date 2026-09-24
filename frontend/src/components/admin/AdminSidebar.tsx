import React from 'react';
import { NavLink } from 'react-router-dom';

const links = [
  { to: '/admin', label: '📊 Dashboard', exact: true },
  { to: '/admin/users', label: '👥 Users' },
  { to: '/admin/restaurants', label: '🏪 Restaurants' },
  { to: '/admin/orders', label: '📦 Orders' },
  { to: '/admin/payments', label: '💳 Payments' },
  { to: '/admin/deliveries', label: '🛵 Deliveries' },
];

const AdminSidebar: React.FC = () => {
  return (
    <div style={{
      width: '220px',
      flexShrink: 0,
      background: '#1A1A2E',
      borderRadius: '12px',
      padding: '16px 0',
      height: 'fit-content',
      position: 'sticky',
      top: '90px',
    }}>
      <div style={{ padding: '0 16px 16px', borderBottom: '1px solid #2a2a3e', marginBottom: '8px' }}>
        <span style={{ fontSize: '13px', fontWeight: 600, color: '#888', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Admin Panel
        </span>
      </div>
      {links.map(({ to, label, exact }) => (
        <NavLink
          key={to}
          to={to}
          end={exact}
          style={({ isActive }) => ({
            display: 'block',
            padding: '11px 20px',
            fontSize: '14px',
            fontWeight: 500,
            color: isActive ? '#FF6B35' : '#ccc',
            background: isActive ? 'rgba(255, 107, 53, 0.1)' : 'transparent',
            textDecoration: 'none',
            borderLeft: isActive ? '3px solid #FF6B35' : '3px solid transparent',
            transition: 'all 0.15s',
          })}
        >
          {label}
        </NavLink>
      ))}
    </div>
  );
};

export default AdminSidebar;
