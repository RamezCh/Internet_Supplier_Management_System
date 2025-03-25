import { useEffect, useState } from "react";
import axios from "axios";
import {Customer} from "../types.ts";
import {CustomerCard} from "../components/CustomerCard.tsx";

export const Customers = () => {
    const [customers, setCustomers] = useState<Customer[] | undefined>();

    const getCustomers = async () => {
        try {
            const response = await axios.get<{ content: Customer[] }>("/api/customers");
            setCustomers(response.data.content);
        } catch (error) {
            console.error("Error fetching customers:", error);
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
                    onEdit={() => {}}
                    onDelete={() => {}}
                />
            ))}
        </div>
    );
}