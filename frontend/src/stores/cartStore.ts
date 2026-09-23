import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { getCartList, addToCart, updateCart, deleteCartItem } from '@/api/modules/cart';
import type { CartItem, AddToCartParams, UpdateCartParams } from '@/api/types';

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([]);
  const loading = ref(false);

  const totalCount = computed(() =>
    items.value.reduce((sum: number, item: CartItem) => sum + item.num, 0)
  );

  const totalPrice = computed(() =>
    items.value.reduce((sum: number, item: CartItem) => sum + (item.selected ? item.price * item.num : 0), 0)
  );

  const selectedItems = computed(() => items.value.filter((item: CartItem) => item.selected));

  const fetchCartList = async () => {
    loading.value = true;
    try {
      const res = await getCartList();
      items.value = res.data.map((item: CartItem) => ({ ...item, selected: true }));
    } finally {
      loading.value = false;
    }
  };

  const addToCartAction = async (params: AddToCartParams) => {
    await addToCart(params);
    await fetchCartList();
  };

  const updateCartItem = async (cartId: number, params: UpdateCartParams) => {
    await updateCart(cartId, params);
    await fetchCartList();
  };

  const removeCartItem = async (cartId: number) => {
    await deleteCartItem(cartId);
    await fetchCartList();
  };

  const toggleSelect = (cartId: number) => {
    const item = items.value.find((i: CartItem) => i.cartId === cartId);
    if (item) {
      item.selected = !item.selected;
    }
  };

  const selectAll = (selected: boolean) => {
    items.value.forEach((item: CartItem) => (item.selected = selected));
  };

  return {
    items,
    loading,
    totalCount,
    totalPrice,
    selectedItems,
    fetchCartList,
    addToCart: addToCartAction,
    updateCartItem,
    removeCartItem,
    toggleSelect,
    selectAll,
  };
}, {
  persist: {
    key: 'cart-store',
    storage: localStorage,
  },
});