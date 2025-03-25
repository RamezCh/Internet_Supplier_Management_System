import { useEffect, useState } from "react";
import axios from "axios";
import {Customer} from "../types.ts";
import {CustomerCard} from "../components/CustomerCard.tsx";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

export const Customers = () => {
    const [customers, setCustomers] = useState<Customer[] | undefined>();

    const getCustomers = async () => {
        try {
            const response = await axios.get<{ content: Customer[] }>("/api/customers");
            setCustomers(response.data.content);
        } catch (error) {
            console.error("Error fetching customers:", error);
            toast.error("Failed to load customers");
        }
    }

    const handleDelete = async (username: string) => {
        try {
            await axios.delete("/api/customers/" + username);
            toast.success("Customer deleted successfully");
            await getCustomers();
        } catch (error) {
            console.error("Error deleting customer:", error);
            toast.error("Failed to delete customer");
        }
    }

    useEffect(() => {
        getCustomers();
    }, []);

    return (
        <div className="customers-container">
            {customers?.map((customer) => (
                <CustomerCard
                    key={customer.username}
                    customer={customer}
                    onDelete={handleDelete}
                />
            ))}
        </div>
    );
}