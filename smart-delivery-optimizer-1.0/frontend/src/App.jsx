import { useState } from "react";

import Orders from "./pages/Orders";
import Vehicles from "./pages/Vehicles";
import Trips from "./pages/Trips";
import KpiComparison from "./pages/KpiComparison";

function App() {
  const [activePage, setActivePage] =
    useState("Dashboard");

  const navigation = [
    "Dashboard",
    "Orders",
    "Vehicles",
    "Trips",
  ];

  const renderPage = () => {
    switch (activePage) {
      case "Orders":
        return <Orders />;

      case "Vehicles":
        return <Vehicles />;

      case "Trips":
        return <Trips />;

      case "Dashboard":
      default:
        return <KpiComparison />;
    }
  };

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <div className="brand-title">
            LogiSmart
          </div>

          <div className="brand-subtitle">
            Delivery Route & Resource Optimization
          </div>
        </div>
      </header>

      <div className="app-body">
        <aside className="sidebar">
          <nav>
            {navigation.map((item) => (
              <button
                key={item}
                className={`nav-item ${
                  activePage === item
                    ? "active"
                    : ""
                }`}
                onClick={() =>
                  setActivePage(item)
                }
              >
                {item}
              </button>
            ))}
          </nav>
        </aside>

        <main className="main-content">
          {renderPage()}
        </main>
      </div>
    </div>
  );
}

export default App;