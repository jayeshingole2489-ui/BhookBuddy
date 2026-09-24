import React, { useState } from 'react';
import { MenuItem } from '../../types';
import { useCart } from '../../contexts/CartContext';
import { useToast } from '../../App';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

interface MenuItemCardProps {
  item: MenuItem;
}

const MenuItemCard: React.FC<MenuItemCardProps> = ({ item }) => {
  const { addToCart } = useCart();
  const { showToast } = useToast();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [adding, setAdding] = useState(false);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!item.available) return;
    try {
      setAdding(true);
      await addToCart(item.id, item.restaurantId);
      showToast(`${item.name} added to cart`, 'success');
    } catch (error: unknown) {
      const msg = error instanceof Error ? error.message : 'Failed to add to cart';
      showToast(msg, 'error');
    } finally {
      setAdding(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      gap: '16px',
      padding: '16px',
      background: 'white',
      borderRadius: '12px',
      boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
      opacity: item.available ? 1 : 0.6,
      transition: 'box-shadow 0.2s',
    }}
      onMouseEnter={(e) => { if (item.available) (e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)'); }}
      onMouseLeave={(e) => { (e.currentTarget.style.boxShadow = '0 1px 4px rgba(0,0,0,0.06)'); }}
    >
      {/* Image */}
      {item.imageUrl && (
        <img
          src={item.imageUrl}
          alt={item.name}
          style={{ width: '90px', height: '90px', borderRadius: '10px', objectFit: 'cover', flexShrink: 0 }}
        />
      )}

      {/* Content */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px', marginBottom: '6px' }}>
          <span className="veg-indicator">{item.vegetarian ? '🟢' : '🔴'}</span>
          <h4 style={{ fontSize: '15px', fontWeight: 600, color: '#1A1A2E', margin: 0 }}>{item.name}</h4>
        </div>

        {item.description && (
          <p style={{ fontSize: '13px', color: '#666', marginBottom: '8px', lineHeight: 1.4, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
            {item.description}
          </p>
        )}

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '8px' }}>
          <span className="price" style={{ fontSize: '16px' }}>₹{item.price.toFixed(2)}</span>
          <button
            onClick={handleAddToCart}
            disabled={!item.available || adding}
            style={{
              padding: '7px 16px',
              background: item.available ? '#FF6B35' : '#ccc',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              fontSize: '13px',
              fontWeight: 600,
              cursor: item.available ? 'pointer' : 'not-allowed',
              transition: 'background 0.2s',
              whiteSpace: 'nowrap',
            }}
            onMouseEnter={(e) => { if (item.available) (e.currentTarget.style.background = '#E55A28'); }}
            onMouseLeave={(e) => { if (item.available) (e.currentTarget.style.background = '#FF6B35'); }}
          >
            {adding ? '...' : item.available ? '+ Add' : 'Unavailable'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default MenuItemCard;
