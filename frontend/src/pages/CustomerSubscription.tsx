import { useEffect, useState } from "react";
import {useNavigate, useParams} from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import {
    FaBolt,
    FaTachometerAlt,
    FaDollarSign,
    FaCalendarAlt,
    FaUser,
    FaPhone,
    FaMapMarkerAlt,
    FaInfoCircle
} from "react-icons/fa";
import { SubscriptionDetailsDTO, CustomerStatus, SubscriptionStatus } from "../types.ts";
import { Button } from "../shared/Button.tsx";

export const CustomerSubscription = () => {
    const { customerId } = useParams<{ customerId: string }>();
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [subscription, setSubscription] = useState<SubscriptionDetailsDTO | null>(null);
    const navigate = useNavigate();

    const fetchSubscription = async () => {
        try {
            const { data } = await axios.get<SubscriptionDetailsDTO>(`/api/subscriptions/${customerId}`);
            setSubscription(data);
        } catch (err) {
            console.error("Error fetching subscription:", err);
            toast.error("Failed to load subscription details");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchSubscription();
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
            <div className="p-4 bg-blue-50 rounded-lg border border-blue-200 text-blue-600">
                No subscription found for this customer.
            </div>
        );
    }

    const getStatusBadgeClass = (status: SubscriptionStatus) => {
        switch (status) {
            case 'ACTIVE':
                return 'bg-green-100 text-green-800';
            case 'EXPIRING':
                return 'bg-yellow-100 text-yellow-800';
            case 'EXPIRED':
                return 'bg-red-100 text-red-800';
            case 'CANCELLED':
                return 'bg-gray-100 text-gray-800';
            default:
                return 'bg-blue-100 text-blue-800';
        }
    };

    const getCustomerStatusBadgeClass = (status: CustomerStatus) => {
        switch (status) {
            case 'ACTIVE':
                return 'bg-green-100 text-green-800';
            case 'EXPIRING':
                return 'bg-yellow-100 text-yellow-800';
            case 'SUSPENDED':
                return 'bg-orange-100 text-orange-800';
            case 'EXPIRED':
                return 'bg-red-100 text-red-800';
            case 'PENDING_ACTIVATION':
                return 'bg-blue-100 text-blue-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

    return (
        <div className="space-y-8">
            <div className="bg-white rounded-lg shadow-md overflow-hidden">
                <div className="p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-semibold text-gray-800">Customer Information</h2>
                </div>

                <div className="p-6 space-y-4">
                    <div className="flex items-center">
                        <FaUser className="text-blue-500 mr-3 w-5" />
                        <div>
                            <p className="text-sm text-gray-500">Full Name</p>
                            <p className="font-medium">{subscription.customer.fullName}</p>
                        </div>
                    </div>

                    <div className="flex items-center">
                        <FaPhone className="text-blue-500 mr-3 w-5" />
                        <div>
                            <p className="text-sm text-gray-500">Phone</p>
                            <p className="font-medium">{subscription.customer.phone}</p>
                        </div>
                    </div>

                    <div className="flex items-center">
                        <FaMapMarkerAlt className="text-blue-500 mr-3 w-5" />
                        <div>
                            <p className="text-sm text-gray-500">Address</p>
                            <p className="font-medium">
                                {subscription.customer.address.street}, {subscription.customer.address.city}
                                {subscription.customer.address.country && `, ${subscription.customer.address.country}`}
                                {subscription.customer.address.postalCode && `, ${subscription.customer.address.postalCode}`}
                            </p>
                        </div>
                    </div>

                    <div className="flex items-center">
                        <FaInfoCircle className="text-blue-500 mr-3 w-5" />
                        <div>
                            <p className="text-sm text-gray-500">Status</p>
                            <span className={`px-2 py-1 text-xs rounded-full ${getCustomerStatusBadgeClass(subscription.customer.status)}`}>
                                {subscription.customer.status.replace('_', ' ')}
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <div className="bg-white rounded-lg shadow-md overflow-hidden">
                <div className="p-6 border-b border-gray-200">
                    <h2 className="text-2xl font-semibold text-gray-800">Subscription Details</h2>
                </div>

                <div className="p-6 space-y-4">
                    <div className="flex justify-between items-center">
                        <h3 className="text-lg font-medium text-gray-800">{subscription.internetPlan.name}</h3>
                        <span className={`px-3 py-1 text-sm rounded-full ${getStatusBadgeClass(subscription.status)}`}>
                            {subscription.status}
                        </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4">
                        <div className="flex items-center space-x-3 p-4 bg-gray-50 rounded-lg">
                            <FaBolt className="text-blue-500 text-xl" />
                            <div>
                                <p className="text-sm text-gray-500">Speed</p>
                                <p className="font-medium">{subscription.internetPlan.speed}</p>
                            </div>
                        </div>

                        <div className="flex items-center space-x-3 p-4 bg-gray-50 rounded-lg">
                            <FaTachometerAlt className="text-blue-500 text-xl" />
                            <div>
                                <p className="text-sm text-gray-500">Bandwidth</p>
                                <p className="font-medium">{subscription.internetPlan.bandwidth}</p>
                            </div>
                        </div>

                        <div className="flex items-center space-x-3 p-4 bg-gray-50 rounded-lg">
                            <FaDollarSign className="text-blue-500 text-xl" />
                            <div>
                                <p className="text-sm text-gray-500">Price</p>
                                <p className="font-medium">${subscription.internetPlan.price.toFixed(2)}/mo</p>
                            </div>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                        <div className="flex items-center space-x-3 p-4 bg-gray-50 rounded-lg">
                            <FaCalendarAlt className="text-blue-500 text-xl" />
                            <div>
                                <p className="text-sm text-gray-500">End Date</p>
                                <p className="font-medium">{new Date(subscription.endDate).toLocaleDateString()}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {subscription.customer.notes && (
                <div className="bg-white rounded-lg shadow-md overflow-hidden">
                    <div className="p-6 border-b border-gray-200">
                        <h2 className="text-2xl font-semibold text-gray-800">Customer Notes</h2>
                    </div>
                    <div className="p-6">
                        <p className="text-gray-700">{subscription.customer.notes}</p>
                    </div>
                </div>
            )}

            <div className="flex justify-between space-x-3">
                <Button onClick={() => navigate("/")}>Back</Button>
                <Button onClick={() => navigate(`/customer/subscription/${customerId}/edit`)}>Manage Subscription</Button>
            </div>
        </div>
    );
};