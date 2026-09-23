import { ref, reactive } from 'vue';
import type { PageParams, PageResult } from '@/api/types';

export function usePagination<T, Q extends Record<string, any> = {}>(
  fetchFn: (params: PageParams & Q) => Promise<PageResult<T>>
) {
  const loading = ref(false);
  const list = ref<T[]>([]);
  const total = ref(0);

  const pageParams = reactive<PageParams>({
    page: 1,
    size: 10,
    keyword: '',
  });

  const query = ref<Partial<Q>>({});

  const fetchData = async (resetPage = true) => {
    if (resetPage) {
      pageParams.page = 1;
    }
    loading.value = true;
    try {
      const result = await fetchFn({
        ...pageParams,
        ...query.value,
      } as PageParams & Q);
      list.value = result.list;
      total.value = result.total;
    } catch (error) {
      console.error('fetch error:', error);
    } finally {
      loading.value = false;
    }
  };

  const onPageChange = (page: number) => {
    pageParams.page = page;
    fetchData(false);
  };

  const onSizeChange = (size: number) => {
    pageParams.size = size;
    fetchData(true);
  };

  const resetQuery = () => {
    query.value = {} as Partial<Q>;
    fetchData(true);
  };

  return {
    loading,
    list,
    total,
    pageParams,
    query,
    fetchData,
    onPageChange,
    onSizeChange,
    resetQuery,
  };
}