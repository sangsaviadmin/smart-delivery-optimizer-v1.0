import { useEffect, useMemo, useState } from "react";
import { api } from "../services/api";

function formatNumber(value, decimals = 0) {
  return Number(value || 0).toLocaleString("en-IN", {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

function formatPercent(value, decimals = 2) {
  return `${Number(value || 0).toFixed(decimals)}%`;
}

function formatCurrency(value) {
  return `₹${Number(value || 0).toLocaleString("en-IN", {
    maximumFractionDigits: 0,
  })}`;
}

function calculateReduction(baseline, optimized) {
  if (!baseline) {
    return 0;
  }

  return ((baseline - optimized) / baseline) * 100;
}

function calculateIncrease(baseline, optimized) {
  if (!baseline) {
    return 0;
  }

  return ((optimized - baseline) / baseline) * 100;
}

function getRagStatus(change) {
  if (change > 0.001) {
    return "green";
  }

  if (change < -0.001) {
    return "red";
  }

  return "amber";
}

function RagIndicator({ status }) {
  const title =
    status === "green"
      ? "Improved"
      : status === "red"
        ? "Deteriorated"
        : "Trade-off";

  return (
    <span
      className={`rag-indicator ${status}`}
      title={title}
    />
  );
}

function HealthBadge({ status }) {
  return (
    <span
      className={`health-badge ${status.toLowerCase()}`}
    >
      <span className="health-dot" />
      {status}
    </span>
  );
}

function SummaryBox({
  label,
  value,
  meta,
  status,
}) {
  return (
    <div className="summary-box">
      <div className="summary-box-label">
        {label}
      </div>

      <div className="summary-box-value">
        {value}
      </div>

      <div className="summary-box-meta">
        {status && (
          <RagIndicator status={status} />
        )}

        {meta}
      </div>
    </div>
  );
}

function KpiTooltip({
  meaning,
  baseline,
  optimized,
}) {
  return (
    <span className="kpi-tooltip-wrapper">
      <span
        className="kpi-tooltip-icon"
        aria-label="Show KPI definition and calculation details"
      >
        ?
      </span>

      <span className="kpi-tooltip">
        <span className="kpi-tooltip-line">
          <strong>What it means:</strong>{" "}
          {meaning}
        </span>

        <span className="kpi-tooltip-line">
          <strong>Baseline:</strong>{" "}
          {baseline}
        </span>

        <span className="kpi-tooltip-line">
          <strong>Optimized:</strong>{" "}
          {optimized}
        </span>
      </span>
    </span>
  );
}

function KpiComparison() {
  const [summary, setSummary] = useState(null);
  const [orders, setOrders] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;

    async function loadDashboardData() {
      try {
        setLoading(true);
        setError("");

        const [
          summaryData,
          ordersData,
          vehiclesData,
        ] = await Promise.all([
          api.getOptimizationSummary(),
          api.getOrders(),
          api.getVehicles(),
        ]);

        if (mounted) {
          setSummary(summaryData);
          setOrders(ordersData);
          setVehicles(vehiclesData);
        }
      } catch (err) {
        if (mounted) {
          setError(
            err.message ||
              "Unable to load optimization summary."
          );
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    loadDashboardData();

    return () => {
      mounted = false;
    };
  }, []);

  const resourceFacts = useMemo(() => {
    const totalPackageWeight = orders.reduce(
      (sum, order) =>
        sum + Number(order.packageWeightKg || 0),
      0
    );

    const totalVehicleCapacity = vehicles.reduce(
      (sum, vehicle) =>
        sum + Number(vehicle.capacityKg || 0),
      0
    );

    const totalFleet = vehicles.length;

    const availableVehicles = vehicles.filter(
      (vehicle) =>
        String(vehicle.availability || "")
          .toLowerCase() === "available"
    ).length;

    const unavailableVehicles =
      totalFleet - availableVehicles;

    const maximumWorkingMinutes =
      vehicles.length > 0
        ? Math.max(
            ...vehicles.map((vehicle) =>
              Number(
                vehicle.maxWorkingMinutes || 0
              )
            )
          )
        : 0;

    return {
      totalOrders: orders.length,
      totalFleet,
      availableVehicles,
      unavailableVehicles,
      maximumWorkingMinutes,
      totalPackageWeight,
      totalVehicleCapacity,
    };
  }, [orders, vehicles]);

  const metrics = useMemo(() => {
    if (!summary) {
      return null;
    }

    const distanceChange =
      calculateReduction(
        summary.baselineDistanceKm,
        summary.totalDistanceKm
      );

    const timeChange =
      calculateReduction(
        summary.baselineTimeMin,
        summary.totalTimeMin
      );

    const planningDaysChange =
      calculateReduction(
        summary.baselinePlanningDays,
        summary.planningDays
      );

    const ordersPerDayChange =
      calculateIncrease(
        summary.baselineOrdersPerDay,
        summary.averageOrdersPerDay
      );

    const allocationRate =
      summary.baselineOrders === 0
        ? 0
        : (summary.ordersAllocated /
            summary.baselineOrders) *
          100;

    const baselineAverageTimePerDay =
      summary.baselinePlanningDays === 0
        ? 0
        : summary.baselineTimeMin /
          summary.baselinePlanningDays;

    const baselineAverageDistancePerDay =
      summary.baselinePlanningDays === 0
        ? 0
        : summary.baselineDistanceKm /
          summary.baselinePlanningDays;

    const baselinePlannedTimePerOrder =
      summary.baselineOrders === 0
        ? 0
        : summary.baselineTimeMin /
          summary.baselineOrders;

    const optimizedPlannedTimePerOrder =
      summary.ordersAllocated === 0
        ? 0
        : summary.totalTimeMin /
          summary.ordersAllocated;

    const plannedTimePerOrderChange =
      calculateReduction(
        baselinePlannedTimePerOrder,
        optimizedPlannedTimePerOrder
      );

    let healthStatus = "Green";

    if (allocationRate < 100) {
      healthStatus = "Red";
    } else if (
      distanceChange < 0 &&
      timeChange < 0
    ) {
      healthStatus = "Red";
    } else if (
      distanceChange < 0 ||
      timeChange < 0
    ) {
      healthStatus = "Amber";
    }

    return {
      distanceChange,
      timeChange,
      planningDaysChange,
      ordersPerDayChange,
      allocationRate,
      baselineAverageTimePerDay,
      baselineAverageDistancePerDay,
      baselinePlannedTimePerOrder,
      optimizedPlannedTimePerOrder,
      plannedTimePerOrderChange,
      healthStatus,
    };
  }, [summary]);

  if (loading) {
    return (
      <div className="page">
        <div className="page-header">
          <div>
            <h1>Dashboard</h1>
            <p>
              Delivery route and resource
              optimization performance
            </p>
          </div>
        </div>

        <div className="loading-state">
          Loading optimization results...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page">
        <div className="page-header">
          <div>
            <h1>Dashboard</h1>
            <p>
              Delivery route and resource
              optimization performance
            </p>
          </div>
        </div>

        <div className="error-state">
          {error}
        </div>
      </div>
    );
  }

  if (!summary || !metrics) {
    return (
      <div className="page">
        <div className="empty-state">
          No optimization data available.
        </div>
      </div>
    );
  }

  const planningDaysRag =
    metrics.planningDaysChange > 0
      ? "green"
      : metrics.planningDaysChange < 0
        ? "red"
        : "amber";

  const distanceRag =
    getRagStatus(metrics.distanceChange);

  const timeRag =
    getRagStatus(metrics.timeChange);

  const ordersPerDayRag =
    getRagStatus(
      metrics.ordersPerDayChange
    );

  const plannedTimePerOrderRag =
    getRagStatus(
      metrics.plannedTimePerOrderChange
    );

  const utilization =
    summary.averageVehicleUtilizationPercent;

  const utilizationStatus = "amber";

  const utilizationStatusText =
    "Trade-off";

  const costChange =
    calculateReduction(
      summary.baselineTotalCostInr,
      summary.totalCostInr
    );

  return (
    <div className="page dashboard-page">

      {/* =====================================================
          SECTION 1 — PLANNING & RESOURCE SNAPSHOT
          ===================================================== */}

      <div className="page-header">

        <div>
          <h1>Dashboard</h1>

          <p>
            Delivery route and resource
            optimization performance
          </p>
        </div>

        <HealthBadge
          status={metrics.healthStatus}
        />

      </div>

      <div className="section-heading">
        <h2>
          Inventory &amp; Resources
        </h2>

        <p>
          Fixed facts and resource constraints from the delivery dataset.
        </p>
      </div>

      <div className="summary-box-grid">

        <SummaryBox
          label="Delivery Orders"
          value={formatNumber(
            resourceFacts.totalOrders
          )}
          meta="orders in scope"
        />

        <SummaryBox
          label="Total Fleet"
          value={formatNumber(
            resourceFacts.totalFleet
          )}
          meta="vehicles in the master fleet"
        />

        <SummaryBox
          label="Available Vehicles"
          value={formatNumber(
            resourceFacts.availableVehicles
          )}
          meta={
            `${formatNumber(
              resourceFacts.unavailableVehicles
            )} vehicles unavailable`
          }
        />

        <SummaryBox
          label="Maximum Working Time"
          value={formatNumber(
            resourceFacts.maximumWorkingMinutes
          )}
          meta="per vehicle per day"
        />

        <SummaryBox
          label="Total Package Weight"
          value={`${formatNumber(
            resourceFacts.totalPackageWeight,
            2
          )} kg`}
          meta="total weight of all delivery orders"
        />

        <SummaryBox
          label="Total Vehicle Capacity"
          value={`${formatNumber(
            resourceFacts.totalVehicleCapacity,
            2
          )} kg`}
          meta="combined capacity of the master fleet"
        />

      </div>

      {/* =====================================================
          SECTION 2 — KPI TABLE
          ===================================================== */}

      <section className="dashboard-section">

        <div className="section-heading">

          <h2>
            KPI Comparison
          </h2>

          <p>
            Current delivery plan versus
            improved delivery plan.
          </p>

        </div>

        <div className="kpi-table-container">

          <table className="kpi-comparison-table">

            <thead>

              <tr>
                <th>KPI</th>
                <th>Direction</th>
                <th>Baseline</th>
                <th>Optimized</th>
                <th>Change</th>
                <th>Status</th>
                <th>Interpretation</th>
              </tr>

            </thead>

            <tbody>

              {/* TOTAL DISTANCE */}

              <tr>

                <td className="kpi-name">

                  <span className="kpi-name-content">

                    Total Travel Distance

                    <KpiTooltip
                      meaning={
                        "Total kilometres travelled by all delivery vehicles. " +
                        "It represents the overall travel required to complete the deliveries."
                      }
                      baseline={
                        "Sum of Existing Planned Distance Km for all delivery orders " +
                        "in the supplied dataset."
                      }
                      optimized={
                        "Sum of route distances generated by the optimization algorithm, " +
                        "including shortest-path travel between locations and return to the hub."
                      }
                    />

                  </span>

                </td>

                <td className="direction-cell">
                  Lower is better
                </td>

                <td>
                  {formatNumber(
                    summary.baselineDistanceKm,
                    2
                  )}{" "}
                  km
                </td>

                <td>
                  {formatNumber(
                    summary.totalDistanceKm,
                    2
                  )}{" "}
                  km
                </td>

                <td className="change-cell positive">
                  −
                  {Math.abs(
                    metrics.distanceChange
                  ).toFixed(2)}
                  %
                </td>

                <td className="status-cell">

                  <RagIndicator
                    status={distanceRag}
                  />

                  Improved

                </td>

                <td>
                  Less travel required
                </td>

              </tr>

              {/* TOTAL TIME */}

              <tr>

                <td className="kpi-name">

                  <span className="kpi-name-content">

                    Total Delivery Time

                    <KpiTooltip
                      meaning={
                        "Total time required to complete all deliveries, including " +
                        "travel and delivery service time."
                      }
                      baseline={
                        "Sum of Existing Estimated Time Min from the supplied order dataset."
                      }
                      optimized={
                        "Total optimized route time, calculated from optimized route distance " +
                        "using the model's 40 km/h average-speed assumption, plus delivery service time."
                      }
                    />

                  </span>

                </td>

                <td className="direction-cell">
                  Lower is better
                </td>

                <td>
                  {formatNumber(
                    summary.baselineTimeMin,
                    2
                  )}{" "}
                  min
                </td>

                <td>
                  {formatNumber(
                    summary.totalTimeMin,
                    2
                  )}{" "}
                  min
                </td>

                <td className="change-cell positive">
                  −
                  {Math.abs(
                    metrics.timeChange
                  ).toFixed(2)}
                  %
                </td>

                <td className="status-cell">

                  <RagIndicator
                    status={timeRag}
                  />

                  Improved

                </td>

                <td>
                  Lower total delivery time
                </td>

              </tr>

              {/* DAYS NEEDED */}

              <tr>

                <td className="kpi-name">

                  <span className="kpi-name-content">

                    Days Needed to Deliver All Orders

                    <KpiTooltip
                      meaning={
                        "Estimated number of days required to deliver all orders."
                      }
                      baseline={
                        "Number of days required by the current delivery planning approach " +
                        "to assign all orders to available compatible vehicles while respecting " +
                        "vehicle capacity and working-time limits."
                      }
                      optimized={
                        "Number of days required by the improved delivery plan generated by " +
                        "the optimization algorithm."
                      }
                    />

                  </span>

                </td>

                <td className="direction-cell">
                  Lower is better
                </td>

                <td>
                  {formatNumber(
                    summary.baselinePlanningDays
                  )}{" "}
                  days
                </td>

                <td>
                  {formatNumber(
                    summary.planningDays
                  )}{" "}
                  days
                </td>

                <td className="change-cell positive">
                  −
                  {Math.abs(
                    metrics.planningDaysChange
                  ).toFixed(2)}
                  %
                </td>

                <td className="status-cell">

                  <RagIndicator
                    status={planningDaysRag}
                  />

                  Improved

                </td>

                <td>
                  All orders can be delivered in fewer days
                </td>

              </tr>

              {/* ORDERS PER DAY */}

              <tr>

                <td className="kpi-name">

                  <span className="kpi-name-content">

                    Orders Delivered per Day

                    <KpiTooltip
                      meaning={
                        "Average number of delivery orders that can be handled each day. " +
                        "It indicates daily delivery throughput."
                      }
                      baseline={
                        "Total orders divided by the number of days required by the current " +
                        "delivery planning approach."
                      }
                      optimized={
                        "Orders successfully assigned by the improved delivery plan divided " +
                        "by the number of days required by that plan."
                      }
                    />

                  </span>

                </td>

                <td className="direction-cell">
                  Higher is better
                </td>

                <td>
                  {formatNumber(
                    summary.baselineOrdersPerDay,
                    2
                  )}
                </td>

                <td>
                  {formatNumber(
                    summary.averageOrdersPerDay,
                    2
                  )}
                </td>

                <td className="change-cell positive">
                  +
                  {Math.abs(
                    metrics.ordersPerDayChange
                  ).toFixed(2)}
                  %
                </td>

                <td className="status-cell">

                  <RagIndicator
                    status={ordersPerDayRag}
                  />

                  Improved

                </td>

                <td>
                  Higher daily delivery throughput
                </td>

              </tr>

              {/* TOTAL COST */}

              <tr>

                <td className="kpi-name">

                  <span className="kpi-name-content">

                    Total Cost

                    <KpiTooltip
                      meaning={
                        "Estimated transportation cost of the delivery plan, based on " +
                        "travel distance and vehicle operating cost per kilometre."
                      }
                      baseline={
                        "Constructed baseline cost. Each order's Existing Planned Distance Km " +
                        "is multiplied by the cost per kilometre of the compatible vehicle " +
                        "assigned by the current delivery planning approach."
                      }
                      optimized={
                        "Each improved route distance is multiplied by the cost per kilometre " +
                        "of its assigned vehicle."
                      }
                    />

                  </span>

                </td>

                <td className="direction-cell">
                  Lower is better
                </td>

                <td>
                  {summary.baselineTotalCostInr != null
                    ? formatCurrency(
                        summary.baselineTotalCostInr
                      )
                    : "—"}
                </td>

                <td>
                  {formatCurrency(
                    summary.totalCostInr
                  )}
                </td>

                <td
                  className={
                    summary.baselineTotalCostInr != null
                      ? "change-cell positive"
                      : "not-available"
                  }
                >
                  {summary.baselineTotalCostInr != null
                    ? `−${Math.abs(
                        costChange
                      ).toFixed(2)}%`
                    : "—"}
                </td>

                <td className="status-cell">

                  <RagIndicator
                    status="green"
                  />

                  Improved

                </td>

                <td>
                  Lower transportation cost
                </td>

              </tr>

              {/* AVERAGE TIME / DAY */}

              <tr>

                <td className="kpi-name">

                  <span className="kpi-name-content">

                    Average Delivery Time per Day

                    <KpiTooltip
                      meaning={
                        "Average delivery time required each day across the complete delivery schedule."
                      }
                      baseline={
                        "Current plan's total delivery time divided by the number of days " +
                        "required to deliver all orders."
                      }
                      optimized={
                        "Total improved delivery time divided by the number of days required " +
                        "by the improved delivery plan."
                      }
                    />

                  </span>

                </td>

                <td className="direction-cell">
                  Lower is better
                </td>

                <td>
                  {formatNumber(
                    metrics.baselineAverageTimePerDay,
                    1
                  )}{" "}
                  min
                </td>

                <td>
                  {formatNumber(
                    summary.averageTimePerDayMin,
                    1
                  )}{" "}
                  min
                </td>

                <td className="change-cell positive">
                  −
                  {Math.abs(
                    calculateReduction(
                      metrics.baselineAverageTimePerDay,
                      summary.averageTimePerDayMin
                    )
                  ).toFixed(2)}
                  %
                </td>

                <td className="status-cell">

                  <RagIndicator
                    status="green"
                  />

                  Improved

                </td>

                <td>
                  Lower average daily delivery time
                </td>

              </tr>

              {/* AVERAGE DISTANCE / DAY */}

              <tr>

                <td className="kpi-name">

                  <span className="kpi-name-content">

                    Average Travel Distance per Day

                    <KpiTooltip
                      meaning={
                        "Average kilometres travelled during one day. " +
                        "It shows the typical daily travel required by the delivery fleet."
                      }
                      baseline={
                        "Current plan's total travel distance divided by the number of days " +
                        "required to deliver all orders."
                      }
                      optimized={
                        "Total improved travel distance divided by the number of days required " +
                        "by the improved delivery plan."
                      }
                    />

                  </span>

                </td>

                <td className="direction-cell">
                  Lower is better
                </td>

                <td>
                  {formatNumber(
                    metrics.baselineAverageDistancePerDay,
                    1
                  )}{" "}
                  km
                </td>

                <td>
                  {formatNumber(
                    summary.averageDistancePerDayKm,
                    1
                  )}{" "}
                  km
                </td>

                <td className="change-cell positive">
                  −
                  {Math.abs(
                    calculateReduction(
                      metrics.baselineAverageDistancePerDay,
                      summary.averageDistancePerDayKm
                    )
                  ).toFixed(2)}
                  %
                </td>

                <td className="status-cell">

                  <RagIndicator
                    status="green"
                  />

                  Improved

                </td>

                <td>
                  Lower average daily travel
                </td>

              </tr>

              {/* AVERAGE VEHICLE USAGE */}

              <tr>

                <td className="kpi-name">

                  <span className="kpi-name-content">

                    Average Vehicle Usage

                    <KpiTooltip
                      meaning={
                        "Average percentage of available vehicle working capacity used " +
                        "by the delivery plan."
                      }
                      baseline={
                        "Average workload across used vehicle-days divided by each vehicle's " +
                        "maximum working minutes. Baseline workload uses Existing Estimated " +
                        "Time Min plus service time."
                      }
                      optimized={
                        "Average improved route time divided by the assigned vehicle's " +
                        "maximum working minutes."
                      }
                    />

                  </span>

                </td>

                <td className="direction-cell">
                  Higher is better
                </td>

                <td>
                  {summary.baselineAverageVehicleUtilizationPercent != null
                    ? formatPercent(
                        summary.baselineAverageVehicleUtilizationPercent
                      )
                    : "—"}
                </td>

                <td>
                  {formatPercent(
                    utilization
                  )}
                </td>

                <td className="change-cell negative">
                  {summary.baselineAverageVehicleUtilizationPercent != null
                    ? `−${Math.abs(
                        calculateReduction(
                          summary.baselineAverageVehicleUtilizationPercent,
                          utilization
                        )
                      ).toFixed(2)}%`
                    : "—"}
                </td>

                <td className="status-cell">

                  <RagIndicator
                    status={utilizationStatus}
                  />

                  {utilizationStatusText}

                </td>

                <td>
                  Lower usage reflects the trade-off from
                  reducing travel, delivery time and cost
                  while maintaining full order allocation.
                </td>

              </tr>

              {/* AVERAGE TIME / ORDER */}

              <tr>

                <td className="kpi-name">

                  <span className="kpi-name-content">

                    Average Delivery Time per Order

                    <KpiTooltip
                      meaning={
                        "Average time associated with completing one delivery order. " +
                        "It shows the average time required per order."
                      }
                      baseline={
                        "Current plan's total delivery time divided by the total number of orders."
                      }
                      optimized={
                        "Total improved delivery time divided by the number of orders successfully " +
                        "loaded for delivery."
                      }
                    />

                  </span>

                </td>

                <td className="direction-cell">
                  Lower is better
                </td>

                <td>
                  {formatNumber(
                    metrics.baselinePlannedTimePerOrder,
                    2
                  )}{" "}
                  min
                </td>

                <td>
                  {formatNumber(
                    metrics.optimizedPlannedTimePerOrder,
                    2
                  )}{" "}
                  min
                </td>

                <td className="change-cell positive">
                  −
                  {Math.abs(
                    metrics.plannedTimePerOrderChange
                  ).toFixed(2)}
                  %
                </td>

                <td className="status-cell">

                  <RagIndicator
                    status={plannedTimePerOrderRag}
                  />

                  Improved

                </td>

                <td>
                  Lower time required per order
                </td>

              </tr>

            </tbody>

          </table>

        </div>

      </section>

      {/* =====================================================
          SECTION 3 — FINAL INTERPRETATION
          ===================================================== */}

      <section className="dashboard-section">

        <div className="section-heading">

          <h2>
            Final Interpretation
          </h2>

        </div>

        <div className="interpretation-panel">

          <div className="interpretation-row">

            <RagIndicator
              status="green"
            />

            <span>
              The improved delivery plan
              successfully loaded{" "}
              <strong>
                {formatNumber(
                  summary.ordersAllocated
                )}
              </strong>{" "}
              of{" "}
              <strong>
                {formatNumber(
                  summary.baselineOrders
                )}
              </strong>{" "}
              orders for delivery, achieving a{" "}
              <strong>
                {formatPercent(
                  metrics.allocationRate
                )}{" "}
                success rate
              </strong>.
            </span>

          </div>

          <div className="interpretation-row">

            <RagIndicator
              status="green"
            />

            <span>
              Total travel distance decreased
              by{" "}
              <strong>
                {metrics.distanceChange.toFixed(
                  2
                )}
                %
              </strong>{" "}
              and total delivery time
              decreased by{" "}
              <strong>
                {metrics.timeChange.toFixed(
                  2
                )}
                %
              </strong>.
            </span>

          </div>

          <div className="interpretation-row">

            <RagIndicator
              status="green"
            />

            <span>
              The time needed to deliver all
              orders reduced from{" "}
              <strong>
                {summary.baselinePlanningDays}
              </strong>{" "}
              days to{" "}
              <strong>
                {summary.planningDays}
              </strong>{" "}
              days, while the number of orders
              handled per day increased by{" "}
              <strong>
                {metrics.ordersPerDayChange.toFixed(
                  2
                )}
                %
              </strong>.
            </span>

          </div>

          <div className="interpretation-row">

            <RagIndicator
              status="green"
            />

            <span>
              Total improved delivery cost is{" "}
              <strong>
                {formatCurrency(
                  summary.totalCostInr
                )}
              </strong>
              {summary.baselineTotalCostInr != null && (
                <>
                  , representing a{" "}
                  <strong>
                    {Math.abs(
                      costChange
                    ).toFixed(2)}
                    %
                  </strong>{" "}
                  reduction against the
                  constructed current-plan cost.
                </>
              )}
            </span>

          </div>

          <div className="interpretation-row">

            <RagIndicator
              status="amber"
            />

            <span>
              Average vehicle usage changed
              from{" "}
              <strong>
                {summary.baselineAverageVehicleUtilizationPercent != null
                  ? formatPercent(
                      summary.baselineAverageVehicleUtilizationPercent
                    )
                  : "—"}
              </strong>{" "}
              in the current plan to{" "}
              <strong>
                {formatPercent(
                  utilization
                )}
              </strong>{" "}
              in the improved plan. This is
              treated as a{" "}
              <strong>
                trade-off
              </strong>{" "}
              because the improved plan reduces
              travel, delivery time and cost while
              maintaining full order allocation.
            </span>

          </div>

          <div className="interpretation-note">

            Current-plan cost and vehicle usage
            are calculated using the constructed
            current delivery planning approach.
            The source dataset does not contain
            historical vehicle-to-order allocation
            records.

          </div>

        </div>

      </section>

    </div>
  );
}

export default KpiComparison;