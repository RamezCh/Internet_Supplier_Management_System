import { InputHTMLAttributes, forwardRef, ChangeEvent } from 'react';

type InputProps = {
    label?: string;
    onChange?: (event: ChangeEvent<HTMLInputElement>) => void;
    placeholder?: string;
    error?: string;
    containerClassName?: string;
    labelClassName?: string;
    inputClassName?: string;
    value?: string | number | null;  // Added null here
} & Omit<InputHTMLAttributes<HTMLInputElement>, 'className' | 'onChange' | 'value'>;

export const Input = forwardRef<HTMLInputElement, InputProps>(
    ({
         label,
         onChange,
         placeholder,
         error,
         containerClassName = '',
         labelClassName = '',
         inputClassName = '',
         id,
         required,
         value = '',  // Default empty string for null/undefined
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
                    onChange={onChange}
                    placeholder={placeholder}
                    aria-invalid={!!error}
                    aria-describedby={error ? `${id}-error` : undefined}
                    required={required}
                    value={value ?? ''}  // Convert null/undefined to empty string
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