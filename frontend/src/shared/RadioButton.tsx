import { RadioGroupProps } from '../types';
import {ChangeEvent, FC} from "react";

export const RadioButton: FC<RadioGroupProps> = ({
                                                    options,
                                                    name,
                                                    selectedValue,
                                                    onChange,
                                                    orientation = 'vertical',
                                                    className = '',
                                                }) => {
    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        onChange(e.target.value);
    };

    const groupClasses = `flex gap-4 ${orientation === 'horizontal' ? 'flex-row' : 'flex-col'} ${className}`;

    return (
        <div className={groupClasses}>
            {options.map((option) => (
                <label
                    key={option.value}
                    className={`flex items-center space-x-2 cursor-pointer ${
                        option.disabled ? 'opacity-50 cursor-not-allowed' : ''
                    }`}
                >
                    <input
                        type="radio"
                        name={name}
                        value={option.value}
                        checked={selectedValue === option.value}
                        onChange={handleChange}
                        disabled={option.disabled}
                        className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                    />
                    <span className="text-gray-700">{option.label}</span>
                </label>
            ))}
        </div>
    );
};