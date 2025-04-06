import { ChangeEvent, FormEvent, useState, useEffect } from "react";
import { Input } from "../shared/Input";
import { RadioButton } from "../shared/RadioButton";
import { InternetPlanDTO } from "../types.ts";
import {
    FaPlus,
    FaSave,
    FaUndo,
    FaTimes,
    FaSpinner
} from "react-icons/fa";

interface InternetPlanFormProps {
    onSubmit: (plan: InternetPlanDTO) => Promise<void>;
    isSubmitting: boolean;
    submitButtonText: string;
    resetButtonText: string;
    mode: "add" | "edit";
    submissionError?: Partial<Record<keyof InternetPlanDTO, string>>;
    initialData?: InternetPlanDTO;
    onCancel?: () => void;
}

export const InternetPlanForm = ({
                                     onSubmit,
                                     isSubmitting,
                                     submitButtonText,
                                     resetButtonText,
                                     mode,
                                     submissionError,
                                     initialData,
                                     onCancel
                                 }: InternetPlanFormProps) => {
    const [formData, setFormData] = useState<InternetPlanDTO>({
        name: "",
        speed: "",
        price: 0,
        bandwidth: "",
        isActive: true,
    });

    const [clientErrors, setClientErrors] = useState<
        Partial<Record<keyof InternetPlanDTO, string>>
    >({});

    useEffect(() => {
        if (initialData) {
            setFormData(initialData);
        }
    }, [initialData]);

    const validateForm = (): boolean => {
        const errors: Partial<Record<keyof InternetPlanDTO, string>> = {};

        if (!formData.name.trim()) errors.name = "Plan name is required";
        if (!formData.speed.trim()) errors.speed = "Speed is required";
        else if (formData.speed.length > 10) errors.speed = "Max 10 characters";
        if (formData.price < 0) errors.price = "Must be non-negative";
        if (!formData.bandwidth.trim()) errors.bandwidth = "Bandwidth is required";

        setClientErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const parseFormValue = (value: string, type: string): string | number => {
        if (type !== "number") return value;
        return value === "" ? "" : Number(value);
    };

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
        const { name, value, type } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: parseFormValue(value, type)
        }));

        if (clientErrors[name as keyof InternetPlanDTO]) {
            setClientErrors(prev => ({ ...prev, [name]: undefined }));
        }
    };

    const handleStatusChange = (value: string) => {
        setFormData(prev => ({ ...prev, isActive: value === "active" }));
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;
        await onSubmit({ ...formData, price: Number(formData.price) });
    };

    const handleReset = () => {
        setFormData(initialData || {
            name: "",
            speed: "",
            price: 0,
            bandwidth: "",
            isActive: true,
        });
        setClientErrors({});
    };

    const getError = (field: keyof InternetPlanDTO) =>
        submissionError?.[field] ?? clientErrors[field];

    return (
        <div className="mx-auto p-6">
            <div className="mb-6">
                <h2 className="text-xl font-semibold text-gray-800 flex items-center gap-2">
                    {mode === "add" ? (
                        <><FaPlus className="text-blue-500" /> Add Plan</>
                    ) : (
                        <><FaSave className="text-blue-500" /> Edit Plan</>
                    )}
                </h2>
            </div>

            <form onSubmit={handleSubmit} noValidate className="space-y-4">
                <Input
                    label="Plan Name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="Premium Fiber"
                    required
                    error={getError('name')}
                />

                <div className="grid grid-cols-2 gap-4">
                    <Input
                        label="Speed"
                        name="speed"
                        value={formData.speed}
                        onChange={handleChange}
                        placeholder="100 Mbps"
                        required
                        maxLength={10}
                        error={getError('speed')}
                    />
                    <Input
                        label="Price"
                        name="price"
                        type="number"
                        value={formData.price}
                        onChange={handleChange}
                        placeholder="49.99"
                        min="0"
                        step="0.01"
                        required
                        error={getError('price')}
                    />
                </div>

                <Input
                    label="Bandwidth"
                    name="bandwidth"
                    value={formData.bandwidth}
                    onChange={handleChange}
                    placeholder="Unlimited"
                    required
                    error={getError('bandwidth')}
                />

                <div className="mb-4">
                    <p className="block text-sm font-medium text-gray-700 mb-2">
                        Status
                    </p>
                    <RadioButton
                        options={[
                            { value: "active", label: "Active" },
                            { value: "inactive", label: "Inactive" },
                        ]}
                        name="status"
                        selectedValue={formData.isActive ? "active" : "inactive"}
                        onChange={handleStatusChange}
                    />
                    {getError('isActive') && (
                        <p className="mt-1 text-sm text-red-600">{getError('isActive')}</p>
                    )}
                </div>

                <div className="pt-4 flex justify-between border-t border-gray-100">
                    <button
                        type="button"
                        onClick={onCancel || handleReset}
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded hover:bg-gray-50 flex items-center gap-2"
                        disabled={isSubmitting}
                    >
                        {onCancel ? (
                            <><FaTimes /> Cancel</>
                        ) : (
                            <><FaUndo /> {resetButtonText}</>
                        )}
                    </button>
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
                    >
                        {isSubmitting ? (
                            <><FaSpinner className="animate-spin" /> Processing...</>
                        ) : (
                            <>
                                {mode === "add" ? <FaPlus /> : <FaSave />}
                                {submitButtonText}
                            </>
                        )}
                    </button>
                </div>
            </form>
        </div>
    );
};