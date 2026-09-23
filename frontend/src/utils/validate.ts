export const required = (message: string = '必填项') => ({
  required: true,
  message,
  trigger: 'blur' as const,
});

export const minLength = (min: number, message?: string) => ({
  min,
  message: message || `最少${min}个字符`,
  trigger: 'blur' as const,
});

export const maxLength = (max: number, message?: string) => ({
  max,
  message: message || `最多${max}个字符`,
  trigger: 'blur' as const,
});

export const email = (message: string = '邮箱格式不正确') => ({
  type: 'email' as const,
  message,
  trigger: 'blur' as const,
});

export const phone = (message: string = '手机号格式不正确') => ({
  pattern: /^1[3-9]\d{9}$/,
  message,
  trigger: 'blur' as const,
});

export const number = (message: string = '请输入数字') => ({
  type: 'number' as const,
  message,
  trigger: 'blur' as const,
});

export const price = (message: string = '请输入有效价格') => ({
  pattern: /^\d+(\.\d{1,2})?$/,
  message,
  trigger: 'blur' as const,
});

export const confirmPassword = (password: string, message: string = '两次输入密码不一致') => ({
  validator: (_rule: any, value: string) => {
    if (value !== password) {
      return Promise.reject(new Error(message));
    }
    return Promise.resolve();
  },
  trigger: 'blur' as const,
});

export const url = (message: string = '链接格式不正确') => ({
  type: 'url' as const,
  message,
  trigger: 'blur' as const,
});