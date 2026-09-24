import React, { useState, useEffect, useMemo } from 'react';
import { Restaurant } from '../types';
import { restaurantService } from '../services/restaurantService';
import RestaurantCard from '../components/restaurant/RestaurantCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import CartSidebar from '../components/cart/CartSidebar';
import Footer from '../components/common/Footer';

const CUISINE_FILTERS = [
  'All',
  'Indian',
  'Chinese',
  'Italian',
  'Pizza',
  'Burgers',
  'Biryani',
  'Mexican',
  'Thai',
];

const Restaurants: React.FC = () => {
  const [restaurants, setRestaurants] = useState<Restaurant[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeCuisine, setActiveCuisine] = useState('All');

  useEffect(() => {
    restaurantService
      .getActive()
      .then(setRestaurants)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    return restaurants.filter((r) => {
      const matchesSearch =
        r.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        r.cuisineType.toLowerCase().includes(searchQuery.toLowerCase()) ||
        r.city.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesCuisine =
        activeCuisine === 'All' ||
        r.cuisineType.toLowerCase() === activeCuisine.toLowerCase();
      return matchesSearch && matchesCuisine;
    });
  }, [restaurants, searchQuery, activeCuisine]);

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <CartSidebar />

      <div style={{ flex: 1 }}>
        {/* Header */}
        <div
          style={{
            background: 'white',
            borderBottom: '1px solid #E5E5E5',
            padding: '24px',
          }}
        >
          <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
            <h1
              style={{
                fontSize: '28px',
                fontWeight: 800,
                color: '#1A1A2E',
                marginBottom: '16px',
              }}
            >
              All Restaurants
            </h1>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search restaurants by name, cuisine or city..."
              style={{
                width: '100%',
                maxWidth: '480px',
                padding: '12px 20px',
                border: '1.5px solid #E5E5E5',
                borderRadius: '24px',
                fontSize: '15px',
                outline: 'none',
                marginBottom: '20px',
                boxSizing: 'border-box',
              }}
              onFocus={(e) => (e.target.style.borderColor = '#FF6B35')}
              onBlur={(e) => (e.target.style.borderColor = '#E5E5E5')}
            />
            {/* Cuisine filters */}
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              {CUISINE_FILTERS.map((cuisine) => (
                <button
                  key={cuisine}
                  onClick={() => setActiveCuisine(cuisine)}
                  style={{
                    padding: '7px 16px',
                    borderRadius: '20px',
                    border: '1.5px solid',
                    borderColor:
                      activeCuisine === cuisine ? '#FF6B35' : '#E5E5E5',
                    background:
                      activeCuisine === cuisine ? '#FF6B35' : 'white',
                    color: activeCuisine === cuisine ? 'white' : '#666',
                    fontSize: '13px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    transition: 'all 0.15s',
                  }}
                >
                  {cuisine}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Results */}
        <div
          style={{ maxWidth: '1200px', margin: '0 auto', padding: '32px 24px' }}
        >
          {loading ? (
            <LoadingSpinner />
          ) : filtered.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🔍</div>
              <h3>No restaurants found</h3>
              <p>Try adjusting your search or filter criteria.</p>
              <button
                onClick={() => {
                  setSearchQuery('');
                  setActiveCuisine('All');
                }}
                className="btn-primary"
                style={{ marginTop: '16px' }}
              >
                Clear Filters
              </button>
            </div>
          ) : (
            <>
              <p
                style={{
                  fontSize: '14px',
                  color: '#666',
                  marginBottom: '20px',
                }}
              >
                {filtered.length} restaurant
                {filtered.length !== 1 ? 's' : ''} found
              </p>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns:
                    'repeat(auto-fill, minmax(300px, 1fr))',
                  gap: '24px',
                }}
              >
                {filtered.map((r) => (
                  <RestaurantCard key={r.id} restaurant={r} />
                ))}
              </div>
            </>
          )}
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default Restaurants;
