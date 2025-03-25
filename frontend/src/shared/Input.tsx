import { InputHTMLAttributes, forwardRef, ChangeEvent } from 'react';

type InputProps = {
    label?: string;
    handleOnChange?: (event: ChangeEvent<HTMLInputElement>) => void;
    placeholder?: string;
    error?: string;
    containerClassName?: string;
    labelClassName?: string;
    inputClassName?: string;
} & Omit<InputHTMLAttributes<HTMLInputElement>, 'className' | 'onChange'>;

export const Input = forwardRef<HTMLInputElement, InputProps>(
    ({
         label,
         handleOnChange,
         placeholder,
         error,
         containerClassName = '',
         labelClassName = '',
         inputClassName = '',
         id,
         required,
         ...props
     }, ref) => {
        return (
            <div className={`mb-4 ${containerClassName}`}>
                {label && (
                    <label
                        htmlFor={id}
                        className={`block text-sm font-medium ${error ? 'text-red-600' : 'text-gray-700'} mb-1 ${labelClassName}`}
                    >
                        {label}
                        {required && <span className="text-red-500"> *</span>}
                    </label>
                )}
                <input
                    ref={ref}
                    id={id}
                    className={`w-full px-3 py-2 border ${error ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-2 ${error ? 'focus:ring-red-500' : 'focus:ring-blue-500'} focus:border-blue-500 ${inputClassName}`}
                    onChange={handleOnChange}
                    placeholder={placeholder}
                    aria-invalid={!!error}
                    aria-describedby={error ? `${id}-error` : undefined}
                    required={required}
                    {...props}
                />
                {error && (
                    <p id={`${id}-error`} className="mt-1 text-sm text-red-600">
                        {error}
                    </p>
                )}
            </div>
        );
    }
);

Input.displayName = 'Input';