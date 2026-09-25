declare namespace JSX {
  type ChangeEvent = {
    target: {
      value: string;
    };
  };

  type IntrinsicProps = {
    [propName: string]: any;
    children?: any;
    className?: string;
    id?: string;
    title?: string;
  };

  interface IntrinsicElements {
    input: IntrinsicProps & { onChange?: (event: ChangeEvent) => void };
    select: IntrinsicProps & { onChange?: (event: ChangeEvent) => void };
    textarea: IntrinsicProps & { onChange?: (event: ChangeEvent) => void };
    [elementName: string]: any;
  }
}

declare module "react" {
  export type FormEvent<T = Element> = {
    preventDefault(): void;
    currentTarget: T;
  };

  export type Dispatch<T> = (value: T) => void;
  export type SetStateAction<T> = T | ((previous: T) => T);

  export function useState<T>(initialState: T | (() => T)): [T, Dispatch<SetStateAction<T>>];
  export function useEffect(effect: () => void | (() => void), dependencies?: unknown[]): void;
  export function useMemo<T>(factory: () => T, dependencies?: unknown[]): T;

  export const StrictMode: (props: { children?: unknown }) => any;
}

declare module "react/jsx-runtime" {
  export const jsx: any;
  export const jsxs: any;
  export const Fragment: any;
}

declare module "react/jsx-dev-runtime" {
  export const jsxDEV: any;
  export const Fragment: any;
}

declare module "react-dom/client" {
  export function createRoot(container: Element): {
    render(children: unknown): void;
  };
}
