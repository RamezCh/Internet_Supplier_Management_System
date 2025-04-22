import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import { SubscriptionDTO, InternetPlanSmallDTO, SubscriptionDetailsDTO } from "../types.ts";
import { Button } from "../shared/Button.tsx";
import { Select } from "../shared/Select.tsx";

export const ManageSubscription = () => {
    const { customerId } = useParams<{ customerId: string }>();
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [isSaving, setIsSaving] = useState<boolean>(false);
    const [internetPlans, setInternetPlans] = useState<InternetPlanSmallDTO[]>([]);
    const [subscription, setSubscription] = useState<SubscriptionDetailsDTO | null>(null);
    const [subscriptionData, setSubscriptionData] = useState<SubscriptionDTO>({
        customerId: customerId ?? '',
        internetPlanId: '',
        startDate: '',
        endDate: '',
        status: 'ACTIVE'
    });
    const navigate = useNavigate();

    const statusOptions = [
        { value: 'ACTIVE', label: 'Active' },
        { value: 'EXPIRING', label: 'Expiring' },
        { value: 'EXPIRED', label: 'Expired' },
        { value: 'CANCELLED', label: 'Cancelled' }
    ];

    const fetchSubscription = async () => {
        try {
            const { data } = await axios.get<SubscriptionDetailsDTO>(`/api/subscriptions/${customerId}`);
            setSubscription(data);
            setSubscriptionData({
                customerId: customerId ?? '',
                internetPlanId: data.internetPlan.id,
                startDate: data.startDate,
                endDate: data.endDate,
                status: data.status
            });
        } catch (err) {
            console.error("Error fetching subscription:", err);
            toast.error("Failed to load subscription details");
        }
    };

    const fetchInternetPlans = async () => {
        try {
            const { data } = await axios.get<InternetPlanSmallDTO[]>('/api/internet_plans/small');
            setInternetPlans(data);
        } catch (err) {
            console.error("Error fetching internet plans:", err);
            toast.error("Failed to load internet plans");
        }
    };

    const fetchData = async () => {
        setIsLoading(true);
        try {
            await Promise.all([fetchSubscription(), fetchInternetPlans()]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            setIsSaving(true);
            await axios.put(`/api/subscriptions/${customerId}`, subscriptionData);
            toast.success("Subscription updated successfully");
            navigate(`/customer/subscription/${customerId}`);
        } catch (err) {
            console.error("Error updating subscription:", err);
            toast.error("Failed to update subscription");
        } finally {
            setIsSaving(false);
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const { name, value } = e.target;
        setSubscriptionData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    useEffect(() => {
        fetchData();
    }, [customerId]);

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
            </div>
        );
    }

    if (!subscription) {
        return (
            <div className="p-4 text-center text-red-600">
                No subscription found for this customer.
            </div>
        );
    }

    return (
        <div className="w-full p-4">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Manage Subscription</h1>

            {/* Customer Subscription Data */}
            <div className="mb-8 space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <p className="block text-sm font-medium text-gray-500">Customer Name</p>
                        <p className="mt-1 text-gray-900">{subscription.customer.fullName}</p>
                    </div>
                    <div>
                        <p className="block text-sm font-medium text-gray-500">Current Plan</p>
                        <p className="mt-1 text-gray-900">{subscription.internetPlan.name}</p>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <p className="block text-sm font-medium text-gray-500">End Date</p>
                        <p className="mt-1 text-gray-900">{new Date(subscription.endDate).toLocaleDateString()}</p>
                    </div>
                </div>

                <div>
                    <p className="block text-sm font-medium text-gray-500">Current Status</p>
                    <p className="mt-1 text-gray-900">{subscription.status}</p>
                </div>
            </div>

            {/* Edit Form */}
            <form onSubmit={handleSubmit} className="space-y-6">
                <Select
                    label="Change Internet Plan"
                    name="internetPlanId"
                    value={subscriptionData.internetPlanId}
                    onChange={handleChange}
                    options={internetPlans.map(plan => ({
                        value: plan.id,
                        label: plan.name
                    }))}
                />

                <Select
                    label="Change Subscription Status"
                    name="status"
                    value={subscriptionData.status}
                    onChange={handleChange}
                    options={statusOptions}
                />

                <div className="flex justify-between pt-4">
                    <Button
                        type="button"
                        onClick={() => navigate(`/customer/subscription/${customerId}`)}
                        variant="secondary"
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        isLoading={isSaving}
                    >
                        Save Changes
                    </Button>
                </div>
            </form>
        </div>
    );
};