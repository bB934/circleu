import { useCartStore } from '@/stores/cartStore';

export function useCart() {
  const cartStore = useCartStore();

  return {
    items: cartStore.items,
    loading: cartStore.loading,
    totalCount: cartStore.totalCount,
    totalPrice: cartStore.totalPrice,
    selectedItems: cartStore.selectedItems,
    fetchCartList: cartStore.fetchCartList,
    addToCart: cartStore.addToCart,
    updateCartItem: cartStore.updateCartItem,
    removeCartItem: cartStore.removeCartItem,
    toggleSelect: cartStore.toggleSelect,
    selectAll: cartStore.selectAll,
  };
}