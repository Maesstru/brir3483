package drinkshop.reports;

import drinkshop.domain.Order;
import drinkshop.repository.Repository;

public class DailyReportService {
    private final Repository<Integer, Order> repo;

    public DailyReportService(Repository<Integer, Order> repo) {
        this.repo = repo;
    }

    public double getTotalRevenue() {
        return repo.findAll().stream().mapToDouble(Order::getTotalPrice).sum();
    }

    public int getTotalOrders() {

        return repo.findAll().size();
    }
}