package baji.lab1.service;

import baji.lab1.entity.Order;
import baji.lab1.entity.Product;
import baji.lab1.entity.User;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public byte[] generateExcelReport() throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Стили
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle moneyStyle = createMoneyStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // 1. ЛИСТ "ОБЩАЯ СТАТИСТИКА"
        Sheet summarySheet = workbook.createSheet("Общая статистика");
        createSummarySheet(summarySheet, headerStyle, titleStyle, moneyStyle);

        // 2. ЛИСТ "ЗАКАЗЫ"
        Sheet ordersSheet = workbook.createSheet("Заказы");
        createOrdersSheet(ordersSheet, headerStyle, dateStyle, moneyStyle);

        // 3. ЛИСТ "ТОВАРЫ"
        Sheet productsSheet = workbook.createSheet("Товары");
        createProductsSheet(productsSheet, headerStyle, moneyStyle);

        // 4. ЛИСТ "КЛИЕНТЫ"
        Sheet clientsSheet = workbook.createSheet("Клиенты");
        createClientsSheet(clientsSheet, headerStyle);

        // Авторазмер колонок
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
                sheet.autoSizeColumn(j);
            }
        }

        // Запись в массив байтов
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    private void createSummarySheet(Sheet sheet, CellStyle headerStyle, CellStyle titleStyle, CellStyle moneyStyle) {
        // Заголовок
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ОТЧЕТ ПО МАГАЗИНУ VINYL");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        // Дата
        Row dateRow = sheet.createRow(1);
        dateRow.createCell(0).setCellValue("Дата формирования:");
        dateRow.createCell(1).setCellValue(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        // Пустая строка
        sheet.createRow(2);

        // Заголовки показателей
        Row headerRow = sheet.createRow(3);
        String[] headers = {"Показатель", "Значение", "Ед.изм", "Примечание"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Данные
        List<Order> allOrders = orderRepository.findAll();
        List<Product> allProducts = productRepository.findAll();
        List<User> allUsers = userRepository.findAll();

        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrder = allOrders.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(allOrders.size()), 2, BigDecimal.ROUND_HALF_UP);

        Object[][] data = {
                {"Всего товаров", allProducts.size(), "шт.", "В каталоге"},
                {"Всего клиентов", allUsers.size(), "чел.", "Зарегистрировано"},
                {"Всего заказов", allOrders.size(), "шт.", "За все время"},
                {"Общая выручка", totalRevenue.doubleValue(), "руб.", "Сумма всех заказов"},
                {"Средний чек", avgOrder.doubleValue(), "руб.", "Выручка / Кол-во заказов"},
                {"Товаров на складе", allProducts.stream().mapToInt(Product::getStock).sum(), "шт.", "Общий остаток"}
        };

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(4 + i);
            row.createCell(0).setCellValue(data[i][0].toString());

            Cell valueCell = row.createCell(1);
            if (data[i][0].equals("Общая выручка") || data[i][0].equals("Средний чек")) {
                valueCell.setCellValue((Double) data[i][1]);
                valueCell.setCellStyle(moneyStyle);
            } else {
                valueCell.setCellValue(data[i][1].toString());
            }

            row.createCell(2).setCellValue(data[i][2].toString());
            row.createCell(3).setCellValue(data[i][3].toString());
        }
    }

    private void createOrdersSheet(Sheet sheet, CellStyle headerStyle, CellStyle dateStyle, CellStyle moneyStyle) {
        // Заголовок
        Row headerRow = sheet.createRow(0);
        String[] headers = {"№", "Дата заказа", "Клиент", "Сумма", "Статус", "Товаров", "Адрес доставки"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Данные (последние 50 заказов)
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        int limit = Math.min(50, orders.size());

        for (int i = 0; i < limit; i++) {
            Order order = orders.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(order.getId());

            Cell dateCell = row.createCell(1);
            if (order.getOrderDate() != null) {
                dateCell.setCellValue(order.getOrderDate());
                dateCell.setCellStyle(dateStyle);
            }

            row.createCell(2).setCellValue(order.getUser() != null ?
                    order.getUser().getUsername() : "Гость");

            Cell amountCell = row.createCell(3);
            if (order.getTotalAmount() != null) {
                amountCell.setCellValue(order.getTotalAmount().doubleValue());
                amountCell.setCellStyle(moneyStyle);
            }

            row.createCell(4).setCellValue(order.getStatus() != null ?
                    order.getStatus() : "НОВЫЙ");

            row.createCell(5).setCellValue(order.getProducts() != null ?
                    order.getProducts().size() : 0);

            row.createCell(6).setCellValue(order.getDeliveryAddress() != null ?
                    order.getDeliveryAddress() : "");
        }
    }

    private void createProductsSheet(Sheet sheet, CellStyle headerStyle, CellStyle moneyStyle) {
        // Заголовок
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Название", "Цена", "На складе", "Бренд", "Категория", "Рейтинг"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Данные (все товары)
        List<Product> products = productRepository.findAll();

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(product.getId());
            row.createCell(1).setCellValue(product.getName());

            Cell priceCell = row.createCell(2);
            priceCell.setCellValue(product.getPrice());
            priceCell.setCellStyle(moneyStyle);

            Cell stockCell = row.createCell(3);
            stockCell.setCellValue(product.getStock());
            // Подсветка если мало на складе
            if (product.getStock() == 0) {
                stockCell.getRow().getSheet().getWorkbook()
                        .createCellStyle().setFillForegroundColor(IndexedColors.RED.getIndex());
            } else if (product.getStock() < 10) {
                stockCell.getRow().getSheet().getWorkbook()
                        .createCellStyle().setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            }

            row.createCell(4).setCellValue(product.getBrand() != null ?
                    product.getBrand().getName() : "");
            row.createCell(5).setCellValue(product.getCategory() != null ?
                    product.getCategory().getName() : "");
            row.createCell(6).setCellValue(product.getAverageRating() != null ?
                    product.getAverageRating().doubleValue() : 0);
        }
    }

    private void createClientsSheet(Sheet sheet, CellStyle headerStyle) {
        // Заголовок
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Имя", "Email", "Заказов", "Последний заказ"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Данные (все пользователи с количеством заказов)
        List<User> users = userRepository.findAll();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getEmail());

            // Количество заказов пользователя
            long orderCount = orderRepository.countByUser(user);
            row.createCell(3).setCellValue(orderCount);

            // Дата последнего заказа
            Order lastOrder = orderRepository.findFirstByUserOrderByOrderDateDesc(user);
            if (lastOrder != null && lastOrder.getOrderDate() != null) {
                row.createCell(4).setCellValue(lastOrder.getOrderDate()
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            }
        }
    }

    // Создание стилей
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createMoneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00\" руб.\""));
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd.MM.yyyy HH:mm"));
        return style;
    }
}