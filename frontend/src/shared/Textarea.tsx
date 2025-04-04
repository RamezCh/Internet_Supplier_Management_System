import { TextareaHTMLAttributes, forwardRef, ChangeEvent } from 'react';

type TextareaProps = {
    label?: string;
    onChange?: (event: ChangeEvent<HTMLTextAreaElement>) => void;
    placeholder?: string;
    error?: string;
    containerClassName?: string;
    labelClassName?: string;
    textareaClassName?: string;
    value?: string | number | null;  // Added null here
} & Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, 'className' | 'onChange' | 'value'>;

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
    ({
         label,
         onChange,
         placeholder,
         error,
         containerClassName = '',
         labelClassName = '',
         textareaClassName = '',
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
                <textarea
                    ref={ref}
                    id={id}
                    className={`w-full px-3 py-2 border ${error ? 'border-red-500' : 'border-gray-300'} rounded-md shadow-sm focus:outline-none focus:ring-2 ${error ? 'focus:ring-red-500' : 'focus:ring-blue-500'} focus:border-blue-500 min-h-[100px] ${textareaClassName}`}
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

Textarea.displayName = 'Textarea';