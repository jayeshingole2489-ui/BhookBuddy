import React, { useState, useEffect, useMemo } from 'react';
import { useParams } from 'react-router-dom';
import { Restaurant, MenuItem } from '../types';
import { restaurantService } from '../services/restaurantService';
import MenuItemCard from '../components/restaurant/MenuItemCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import CartSidebar from '../components/cart/CartSidebar';

const getCuisineEmoji = (cuisineType: string): string => {
  const map: Record<string, string> = {
    Indian: '🍛',
    Chinese: '🍜',
    Italian: '🍝',
    Pizza: '🍕',
    Burgers: '🍔',
    Mexican: '🌮',
    Thai: '🍜',
    Biryani: '🍚',
  };
  return map[cuisineType] ?? '🍽️';
};

const RestaurantDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [restaurant, setRestaurant] = useState<Restaurant | null>(null);
  const [menuItems, setMenuItems] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeCategory, setActiveCategory] = useState('All');

  useEffect(() => {
    if (!id) return;
    const fetchData = async () => {
      try {
        const [restaurantData, menuData] = await Promise.all([
          restaurantService.getById(Number(id)),
          restaurantService.getMenuItems(Number(id)),
        ]);
        setRestaurant(restaurantData);
        setMenuItems(menuData);
      } catch {
        setError('Restaurant not found');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  const categories = useMemo(() => {
    const cats = Array.from(new Set(menuItems.map((item) => item.category)));
    return ['All', ...cats];
  }, [menuItems]);

  const groupedItems = useMemo(() => {
    if (activeCategory !== 'All') {
      return {
        [activeCategory]: menuItems.filter(
          (item) => item.category === activeCategory
        ),
      };
    }
    return menuItems.reduce<Record<string, MenuItem[]>>((acc, item) => {
      if (!acc[item.category]) acc[item.category] = [];
      acc[item.category].push(item);
      return acc;
    }, {});
  }, [menuItems, activeCategory]);

  if (loading) return <LoadingSpinner fullPage />;

  if (error || !restaurant) {
    return (
      <div className="empty-state" style={{ paddingTop: '80px' }}>
        <div className="empty-icon">😕</div>
        <h3>{error || 'Restaurant not found'}</h3>
      </div>
    );
  }

  return (
    <div>
      <CartSidebar />

      {/* Restaurant Header */}
      <div
        style={{
          background: 'linear-gradient(135deg, #1A1A2E, #2d2d4e)',
          color: 'white',
          padding: '40px 24px',
        }}
      >
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <div
            style={{
              display: 'flex',
              alignItems: 'flex-start',
              gap: '24px',
              flexWrap: 'wrap',
            }}
          >
            {/* Cuisine icon */}
            <div
              style={{
                width: '100px',
                height: '100px',
                borderRadius: '16px',
                background: 'rgba(255,255,255,0.1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '48px',
                flexShrink: 0,
              }}
            >
              {getCuisineEmoji(restaurant.cuisineType)}
            </div>

            <div style={{ flex: 1 }}>
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                  marginBottom: '8px',
                  flexWrap: 'wrap',
                }}
              >
                <h1 style={{ fontSize: '28px', fontWeight: 800 }}>
                  {restaurant.name}
                </h1>
                {!restaurant.active && (
                  <span
                    style={{
                      background: '#e74c3c',
                      padding: '4px 10px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 600,
                    }}
                  >
                    Currently Closed
                  </span>
                )}
              </div>

              <p
                style={{
                  color: 'rgba(255,255,255,0.7)',
                  marginBottom: '16px',
                  fontSize: '15px',
                }}
              >
                {restaurant.description ||
                  `${restaurant.cuisineType} cuisine · ${restaurant.city}`}
              </p>

              <div
                style={{ display: 'flex', gap: '24px', flexWrap: 'wrap' }}
              >
                <span
                  style={{
                    background:
                      restaurant.rating >= 4 ? '#2ecc71' : '#f39c12',
                    padding: '3px 8px',
                    borderRadius: '8px',
                    fontWeight: 700,
                    fontSize: '14px',
                  }}
                >
                  ⭐ {restaurant.rating.toFixed(1)}
                </span>
                <span
                  style={{ color: 'rgba(255,255,255,0.7)', fontSize: '14px' }}
                >
                  🕐 {restaurant.estimatedDeliveryMinutes} min
                </span>
                <span
                  style={{ color: 'rgba(255,255,255,0.7)', fontSize: '14px' }}
                >
                  🛵 ₹{restaurant.deliveryFee.toFixed(2)} delivery
                </span>
                <span
                  style={{ color: 'rgba(255,255,255,0.7)', fontSize: '14px' }}
                >
                  📦 Min. ₹{restaurant.minOrderAmount.toFixed(2)}
                </span>
                {restaurant.phone && (
                  <span
                    style={{
                      color: 'rgba(255,255,255,0.7)',
                      fontSize: '14px',
                    }}
                  >
                    📞 {restaurant.phone}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Category Tabs */}
      <div
        style={{
          background: 'white',
          borderBottom: '1px solid #E5E5E5',
          position: 'sticky',
          top: '70px',
          zIndex: 10,
        }}
      >
        <div
          style={{ maxWidth: '1200px', margin: '0 auto', padding: '0 24px' }}
        >
          <div
            style={{
              display: 'flex',
              gap: '4px',
              overflowX: 'auto',
              padding: '0 0 1px',
            }}
          >
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setActiveCategory(cat)}
                style={{
                  padding: '14px 20px',
                  border: 'none',
                  background: 'none',
                  fontSize: '14px',
                  fontWeight: 600,
                  color: activeCategory === cat ? '#FF6B35' : '#666',
                  borderBottom:
                    activeCategory === cat
                      ? '2px solid #FF6B35'
                      : '2px solid transparent',
                  cursor: 'pointer',
                  whiteSpace: 'nowrap',
                  transition: 'color 0.15s',
                }}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Menu */}
      <div
        style={{ maxWidth: '1200px', margin: '0 auto', padding: '32px 24px' }}
      >
        {menuItems.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🍽️</div>
            <h3>Menu coming soon</h3>
            <p>This restaurant hasn't added their menu yet.</p>
          </div>
        ) : (
          Object.entries(groupedItems).map(([category, items]) => (
            <div key={category} style={{ marginBottom: '40px' }}>
              <h2
                style={{
                  fontSize: '20px',
                  fontWeight: 700,
                  color: '#1A1A2E',
                  marginBottom: '16px',
                  paddingBottom: '8px',
                  borderBottom: '2px solid #FFF3EE',
                }}
              >
                {category}
              </h2>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns:
                    'repeat(auto-fill, minmax(340px, 1fr))',
                  gap: '12px',
                }}
              >
                {items.map((item) => (
                  <MenuItemCard key={item.id} item={item} />
                ))}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default RestaurantDetail;
