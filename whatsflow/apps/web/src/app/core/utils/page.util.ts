/** Normalize Spring Page or WhatsFlow PageResponse into a consistent shape. */
export function unwrapPage<T = any>(data: any): {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
} {
  if (!data) {
    return { content: [], page: 0, size: 0, totalElements: 0, totalPages: 0 };
  }
  if (Array.isArray(data)) {
    return { content: data, page: 0, size: data.length, totalElements: data.length, totalPages: 1 };
  }
  const content: T[] = data.content ?? data.items ?? data.data ?? [];
  const totalElements = Number(data.totalElements ?? data.total ?? content.length) || 0;
  const size = Number(data.size ?? data.pageSize ?? content.length) || content.length || 20;
  const page = Number(data.page ?? data.number ?? 0) || 0;
  const totalPages = Number(data.totalPages ?? (size ? Math.ceil(totalElements / size) : 1)) || 1;
  return { content, page, size, totalElements, totalPages };
}

export function parseAttrs(json?: string | null): Record<string, string> {
  if (!json) return {};
  try {
    const o = JSON.parse(json);
    return typeof o === 'object' && o ? o : {};
  } catch {
    return {};
  }
}

export function formatInr(n: number | string | undefined | null): string {
  const v = Number(n) || 0;
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v);
}

export function formatNum(n: number | string | undefined | null): string {
  return new Intl.NumberFormat('en-IN').format(Number(n) || 0);
}
