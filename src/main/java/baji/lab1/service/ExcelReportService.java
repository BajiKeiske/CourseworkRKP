package baji.lab1.service;

import baji.lab1.entity.*;
import baji.lab1.repository.OrderRepository;
import baji.lab1.repository.ProductRepository;
import baji.lab1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public byte[] generateExcelReport() throws IOException {

        Workbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle moneyStyle = createMoneyStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        Sheet summarySheet = workbook.createSheet("Общая статистика");
        createSummarySheet(summarySheet, headerStyle, titleStyle, moneyStyle);

        Sheet ordersSheet = workbook.createSheet("Заказы");
        createOrdersSheet(ordersSheet, headerStyle, dateStyle, moneyStyle);

        Sheet productsSheet = workbook.createSheet("Товары");
        createProductsSheet(productsSheet, headerStyle, moneyStyle);

        Sheet clientsSheet = workbook.createSheet("Клиенты");
        createClientsSheet(clientsSheet, headerStyle);

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);

            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(0);
                if (row != null) {
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        sheet.autoSizeColumn(j);
                    }
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    // ================= SUMMARY =================

    private void createSummarySheet(Sheet sheet,
                                    CellStyle headerStyle,
                                    CellStyle titleStyle,
                                    CellStyle moneyStyle) {

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ОТЧЁТ VINYL SHOP");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        sheet.createRow(1).createCell(0)
                .setCellValue("Дата: " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        sheet.createRow(2);

        Row header = sheet.createRow(3);
        String[] h = {"Показатель", "Значение", "Ед", "Описание"};

        for (int i = 0; i < h.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(h[i]);
            c.setCellStyle(headerStyle);
        }

        List<Order> orders = orderRepository.findAll();
        List<Product> products = productRepository.findAll();
        List<User> users = userRepository.findAll();

        BigDecimal revenue = orders.stream()
                .filter(o -> o.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avg = orders.isEmpty()
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(orders.size()), 2, BigDecimal.ROUND_HALF_UP);

        Object[][] data = {
                {"Заказов", orders.size(), "шт", ""},
                {"Товаров", products.size(), "шт", ""},
                {"Клиентов", users.size(), "шт", ""},
                {"Выручка", revenue.doubleValue(), "руб", ""},
                {"Средний чек", avg.doubleValue(), "руб", ""},
                {"Остаток товаров", products.stream().mapToInt(Product::getStock).sum(), "шт", ""}
        };

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(4 + i);

            row.createCell(0).setCellValue(data[i][0].toString());

            Cell val = row.createCell(1);
            val.setCellValue(Double.parseDouble(data[i][1].toString()));
            val.setCellStyle(moneyStyle);

            row.createCell(2).setCellValue(data[i][2].toString());
            row.createCell(3).setCellValue(data[i][3].toString());
        }
    }

    // ================= ORDERS =================

    private void createOrdersSheet(Sheet sheet,
                                   CellStyle headerStyle,
                                   CellStyle dateStyle,
                                   CellStyle moneyStyle) {

        String[] headers = {"ID", "Дата", "Клиент", "Сумма", "Статус", "Товары"};

        Row header = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        int limit = Math.min(50, orders.size());

        for (int i = 0; i < limit; i++) {

            Order o = orders.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(o.getId());

            if (o.getOrderDate() != null) {
                Cell d = row.createCell(1);
                d.setCellValue(o.getOrderDate());
                d.setCellStyle(dateStyle);
            }

            row.createCell(2).setCellValue(
                    o.getUser() != null ? o.getUser().getUsername() : "—"
            );

            if (o.getTotalAmount() != null) {
                Cell m = row.createCell(3);
                m.setCellValue(o.getTotalAmount().doubleValue());
                m.setCellStyle(moneyStyle);
            }

            //  enum-safe
            row.createCell(4).setCellValue(
                    o.getStatus() != null ? o.getStatus().name() : "NEW"
            );

            row.createCell(5).setCellValue(
                    o.getOrderItems() != null ? o.getOrderItems().size() : 0
            );
        }
    }

    // ================= PRODUCTS =================

    private void createProductsSheet(Sheet sheet,
                                     CellStyle headerStyle,
                                     CellStyle moneyStyle) {

        String[] headers = {"ID", "Название", "Цена", "Склад"};

        Row header = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        List<Product> products = productRepository.findAll();

        for (int i = 0; i < products.size(); i++) {

            Product p = products.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getName());

            Cell price = row.createCell(2);
            price.setCellValue(p.getPrice());
            price.setCellStyle(moneyStyle);

            row.createCell(3).setCellValue(p.getStock());
        }
    }

    // ================= CLIENTS =================

    private void createClientsSheet(Sheet sheet,
                                    CellStyle headerStyle) {

        String[] headers = {"ID", "Имя", "Email"};

        Row header = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell c = header.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        List<User> users = userRepository.findAll();

        for (int i = 0; i < users.size(); i++) {

            User u = users.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(u.getId());
            row.createCell(1).setCellValue(u.getUsername());
            row.createCell(2).setCellValue(u.getEmail());
        }
    }

    // ================= STYLES =================

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        return s;
    }

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 16);
        s.setFont(f);
        return s;
    }

    private CellStyle createMoneyStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        return s;
    }

    private CellStyle createDateStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("dd.MM.yyyy HH:mm"));
        return s;
    }

    public byte[] generateReportByPeriod(LocalDate startDate, LocalDate endDate) throws IOException {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Order> orders = orderRepository.findByOrderDateBetween(start, end).stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .toList();

        long totalOrders = orders.size();
        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageCheck = totalOrders == 0 ? BigDecimal.ZERO :
                totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        // ТОП-5 товаров
        Map<String, Long> productSales = new HashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                productSales.merge(item.getProduct().getName(), (long) item.getQuantity(), Long::sum);
            }
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Отчёт " + startDate + " — " + endDate);

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("ОТЧЁТ О ПРОДАЖАХ");

        Row periodRow = sheet.createRow(1);
        periodRow.createCell(0).setCellValue("Период: " + startDate + " — " + endDate);

        Row statsRow = sheet.createRow(3);
        statsRow.createCell(0).setCellValue("Всего заказов: " + totalOrders);
        statsRow.createCell(1).setCellValue("Общая выручка: " + totalSales + " ₽");
        statsRow.createCell(2).setCellValue("Средний чек: " + averageCheck + " ₽");

        Row topHeader = sheet.createRow(5);
        topHeader.createCell(0).setCellValue("ТОВАР");
        topHeader.createCell(1).setCellValue("КОЛИЧЕСТВО");

        List<Map.Entry<String, Long>> sorted = productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .toList();

        int rowNum = 6;
        for (Map.Entry<String, Long> entry : sorted) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }
}