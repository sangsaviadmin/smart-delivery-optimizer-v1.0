package com.logismart.repository;

import com.logismart.model.Vehicle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VehicleRepository {

    private final JdbcTemplate jdbcTemplate;

    public VehicleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Vehicle> findAll() {
        String sql = """
                SELECT vehicle_id, hub, vehicle_type, capacity_kg,
                       cost_per_km_inr, availability, max_working_minutes
                FROM vehicle_master
                ORDER BY vehicle_id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Vehicle vehicle = new Vehicle();

            vehicle.setVehicleId(rs.getString("vehicle_id"));
            vehicle.setHub(rs.getString("hub"));
            vehicle.setVehicleType(rs.getString("vehicle_type"));
            vehicle.setCapacityKg(rs.getDouble("capacity_kg"));
            vehicle.setCostPerKmInr(rs.getDouble("cost_per_km_inr"));
            vehicle.setAvailability(rs.getString("availability"));
            vehicle.setMaxWorkingMinutes(
                    rs.getInt("max_working_minutes"));

            return vehicle;
        });
    }
}