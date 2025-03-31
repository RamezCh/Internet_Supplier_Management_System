import { SelectHTMLAttributes, forwardRef, ChangeEvent } from 'react';

type SelectProps = {
    label?: string;
    onChange?: (event: ChangeEvent<HTMLSelectElement>) => void;
    options: { value: string; label: string }[];
    error?: string;
    containerClassName?: string;
    labelClassName?: string;
    selectClassName?: string;
    value?: string | null;  // Added null here
} & Omit<SelectHTMLAttributes<HTMLSelectElement>, 'className' | 'onChange' | 'value'>;

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
    ({
         label,
         onChange,
         options,
         error,
         containerClassName = '',
         labelClassName = '',
         selectClassName = '',
         id,
         required,
         value = '',  // Default empty string
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
                <select
                    ref={ref}
                    id={id}
                    className={`w-full px-3 py-2 border ${error ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-2 ${error ? 'focus:ring-red-500' : 'focus:ring-blue-500'} focus:border-blue-500 ${selectClassName}`}
                    onChange={onChange}
                    aria-invalid={!!error}
                    aria-describedby={error ? `${id}-error` : undefined}
                    required={required}
                    value={value ?? ''}  // Convert null/undefined to empty string
                    {...props}
                >
                    {options.map((option) => (
                        <option key={option.value} value={option.value}>
                            {option.label}
                        </option>
                    ))}
                </select>
                {error && (
                    <p id={`${id}-error`} className="mt-1 text-sm text-red-600">
                        {error}
                    </p>
                )}
            </div>
        );
    }
);

Select.displayName = 'Select';