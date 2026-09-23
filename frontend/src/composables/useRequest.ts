import { ref } from 'vue';

interface UseRequestOptions<T> {
  immediate?: boolean;
  onSuccess?: (data: T) => void;
  onError?: (error: Error) => void;
}

export function useRequest<T, P extends unknown[] = unknown[]>(
  apiFn: (...args: P) => Promise<T>,
  options: UseRequestOptions<T> = {}
) {
  const loading = ref(false);
  const data = ref<T | null>(null);
  const error = ref<Error | null>(null);

  const run = async (...args: P): Promise<T | null> => {
    loading.value = true;
    error.value = null;
    try {
      const result = await apiFn(...args);
      data.value = result;
      options.onSuccess?.(result);
      return result;
    } catch (err) {
      error.value = err as Error;
      options.onError?.(err as Error);
      return null;
    } finally {
      loading.value = false;
    }
  };

  if (options.immediate) {
    run(...([] as unknown as P));
  }

  return {
    loading,
    data,
    error,
    run,
  };
}