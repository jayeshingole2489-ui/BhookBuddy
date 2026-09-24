import React from 'react';
import { Link } from 'react-router-dom';

const Footer: React.FC = () => {
  return (
    <footer style={{
      background: '#1A1A2E',
      color: 'white',
      padding: '48px 24px 24px',
      marginTop: 'auto',
    }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '40px',
          marginBottom: '40px',
        }}>
          <div>
            <div style={{ marginBottom: '12px' }}>
              <span style={{ fontSize: '20px', fontWeight: 800 }}>Swift</span>
              <span style={{ fontSize: '20px', fontWeight: 800, color: '#FF6B35' }}>Bite</span>
              <span style={{
                width: '6px', height: '6px', borderRadius: '50%',
                background: '#FF6B35', display: 'inline-block', marginLeft: '2px', marginBottom: '6px',
              }} />
            </div>
            <p style={{ color: '#aaa', fontSize: '14px', lineHeight: 1.6 }}>
              Delicious food, delivered swift.<br />
              Your favorite meals at your doorstep.
            </p>
          </div>
          <div>
            <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '16px', color: '#ccc', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Quick Links</h4>
            {[['/', 'Home'], ['/restaurants', 'Restaurants'], ['/orders', 'My Orders'], ['/profile', 'Profile']].map(([to, label]) => (
              <Link key={to} to={to} style={{ display: 'block', color: '#aaa', textDecoration: 'none', marginBottom: '10px', fontSize: '14px' }}
                onMouseEnter={(e) => (e.currentTarget.style.color = '#FF6B35')}
                onMouseLeave={(e) => (e.currentTarget.style.color = '#aaa')}>
                {label}
              </Link>
            ))}
          </div>
          <div>
            <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '16px', color: '#ccc', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Contact</h4>
            <p style={{ color: '#aaa', fontSize: '14px', marginBottom: '8px' }}>📧 support@swiftbite.com</p>
            <p style={{ color: '#aaa', fontSize: '14px', marginBottom: '8px' }}>📞 1800-SWIFT-BITE</p>
            <p style={{ color: '#aaa', fontSize: '14px' }}>⏰ 8 AM – 11 PM, 7 days</p>
          </div>
        </div>
        <div style={{ borderTop: '1px solid #2a2a3e', paddingTop: '24px', textAlign: 'center', color: '#666', fontSize: '13px' }}>
          © {new Date().getFullYear()} SwiftBite. All rights reserved.
        </div>
      </div>
    </footer>
  );
};

export default Footer;
