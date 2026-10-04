from pathlib import Path

import pandas as pd
import psycopg2


PROJECT_ROOT = Path(__file__).resolve().parents[2]

DATA_FILE = (
    PROJECT_ROOT
    / "database"
    / "data"
    / "Competitor Beta Dataset for Students.xlsx"
)

DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "database": "smart_delivery_optimizer",
    "user": "postgres",
    "password": "sangsaviadmin@1983",
}


def main():
    print(f"Reading: {DATA_FILE}")

    workbook = pd.ExcelFile(DATA_FILE)

    connection = psycopg2.connect(**DB_CONFIG)
    cursor = connection.cursor()

    # ---------------------------------------------------------
    # 1. Vehicle Master
    # ---------------------------------------------------------

    vehicles = pd.read_excel(
        workbook,
        sheet_name="Vehicle Master"
    )

    vehicle_sql = """
        INSERT INTO vehicle_master (
            vehicle_id,
            hub,
            vehicle_type,
            capacity_kg,
            cost_per_km_inr,
            availability,
            max_working_minutes
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        ON CONFLICT (vehicle_id) DO NOTHING
    """

    for _, row in vehicles.iterrows():
        cursor.execute(
            vehicle_sql,
            (
                row["Vehicle_ID"],
                row["Hub"],
                row["Vehicle_Type"],
                row["Capacity_Kg"],
                row["Cost_per_Km_INR"],
                row["Availability"],
                row["Max_Working_Minutes"],
            ),
        )

    print(f"Processed {len(vehicles)} vehicles.")


    # ---------------------------------------------------------
    # 2. Delivery Orders
    # ---------------------------------------------------------

    orders = pd.read_excel(
        workbook,
        sheet_name="Delivery Orders"
    )

    order_sql = """
        INSERT INTO delivery_orders (
            order_id,
            origin_hub,
            destination_zone,
            distance_km,
            package_weight_kg,
            priority,
            delivery_window,
            required_vehicle_type,
            service_time_min,
            existing_planned_distance_km,
            existing_estimated_time_min,
            delay_min,
            delivery_status
        )
        VALUES (
            %s, %s, %s, %s, %s, %s, %s,
            %s, %s, %s, %s, %s, %s
        )
        ON CONFLICT (order_id) DO NOTHING
    """

    for _, row in orders.iterrows():
        cursor.execute(
            order_sql,
            (
                row["Order_ID"],
                row["Origin_Hub"],
                row["Destination_Zone"],
                row["Distance_Km"],
                row["Package_Weight_Kg"],
                row["Priority"],
                row["Delivery_Window"],
                row["Required_Vehicle_Type"],
                row["Service_Time_Min"],
                row["Existing_Planned_Distance_Km"],
                row["Existing_Estimated_Time_Min"],
                row["Delay_Min"],
                row["Delivery_Status"],
            ),
        )

    print(f"Processed {len(orders)} delivery orders.")


    # ---------------------------------------------------------
    # 3. Zone Distance Matrix
    # ---------------------------------------------------------

    matrix = pd.read_excel(
        workbook,
        sheet_name="Zone Distance Matrix"
    )

    matrix_sql = """
        INSERT INTO zone_distance_matrix (
            from_location,
            to_location,
            distance_km
        )
        VALUES (%s, %s, %s)
        ON CONFLICT (from_location, to_location)
        DO UPDATE SET distance_km = EXCLUDED.distance_km
    """

    for _, row in matrix.iterrows():

        from_location = row["From / To"]

        for to_location in matrix.columns:
            if to_location == "From / To":
                continue

            distance = row[to_location]

            if pd.isna(distance):
                continue

            cursor.execute(
                matrix_sql,
                (
                    from_location,
                    to_location,
                    float(distance),
                ),
            )

    print("Processed zone distance matrix.")


    # ---------------------------------------------------------
    # Commit
    # ---------------------------------------------------------

    connection.commit()

    cursor.close()
    connection.close()

    print("\nData import completed successfully.")


if __name__ == "__main__":
    main()