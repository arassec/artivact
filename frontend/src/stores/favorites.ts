import {defineStore} from 'pinia';
import {FavoriteItemData, TranslatableString} from '../components/artivact-models';
import {api} from '../boot/axios';

export const useFavoritesStore = defineStore('favorites', {
  state: () => ({
    favorites: [] as FavoriteItemData[],
    loaded: false,
    copiedProperties: null as Record<string, TranslatableString> | null
  }),

  getters: {
    favoritesList(state) {
      return state.favorites;
    },
    isFavorite: (state) => (itemId: string) => {
      return state.favorites.some(f => f.itemId === itemId);
    },
    getCopiedProperties(state) {
      return state.copiedProperties;
    }
  },

  actions: {
    async loadFavorites() {
      try {
        const response = await api.get('/api/favorites');
        this.favorites = response.data;
        this.loaded = true;
      } catch (error) {
        console.error('Failed to load favorites:', error);
      }
    },

    async markAsFavorite(itemId: string, title: string, thumbnailUrl: string | null) {
      const wasAlreadyFavorite = this.isFavorite(itemId);
      try {
        // Optimistically add to list
        if (!wasAlreadyFavorite) {
          this.favorites.push({
            itemId,
            title,
            thumbnailUrl: thumbnailUrl || ''
          });
        }
        await api.post(`/api/favorites/${itemId}`);
      } catch (error) {
        console.error('Failed to mark item as favorite:', error);
        // Revert optimistic update only if we added it
        if (!wasAlreadyFavorite) {
          this.favorites = this.favorites.filter(f => f.itemId !== itemId);
        }
        throw error;
      }
    },

    async unmarkAsFavorite(itemId: string) {
      const previousFavorites = [...this.favorites];
      try {
        // Optimistically remove from list
        this.favorites = this.favorites.filter(f => f.itemId !== itemId);
        await api.delete(`/api/favorites/${itemId}`);
      } catch (error) {
        console.error('Failed to unmark item as favorite:', error);
        // Revert optimistic update
        this.favorites = previousFavorites;
        throw error;
      }
    },

    async checkFavoriteStatus(itemId: string): Promise<boolean> {
      try {
        const response = await api.get(`/api/favorites/${itemId}`);
        return response.data;
      } catch (error) {
        console.error('Failed to check favorite status:', error);
        return false;
      }
    },

    copyProperties(properties: Record<string, TranslatableString>) {
      this.copiedProperties = JSON.parse(JSON.stringify(properties));
    }
  }
});
