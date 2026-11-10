package com.hrms.portal.service;

import com.hrms.portal.entity.Payslip;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfPayslipGeneratorService {

    private static final NumberFormat CURRENCY_FMT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    // Corporate Color Palette
    private static final Color PRIMARY_COLOR = new Color(30, 41, 59);    // Slate 800
    private static final Color ACCENT_COLOR = new Color(79, 70, 229);    // Indigo 600
    private static final Color BG_LIGHT = new Color(248, 250, 252);      // Slate 50
    private static final Color BG_HEADER = new Color(241, 245, 249);     // Slate 100
    private static final Color BORDER_COLOR = new Color(226, 232, 240);  // Slate 200
    private static final Color TEXT_MUTED = new Color(100, 116, 139);    // Slate 500
    private static final Color TEXT_DARK = new Color(15, 23, 42);        // Slate 900
    private static final Color SUCCESS_BG = new Color(240, 253, 244);    // Green 50
    private static final Color SUCCESS_TEXT = new Color(22, 101, 52);    // Green 800

    public byte[] generatePayslipPdf(Payslip p) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, PRIMARY_COLOR);
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_MUTED);
            Font sectionFont = new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARY_COLOR);
            Font tableHeaderFont = new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_COLOR);
            Font boldFont = new Font(Font.HELVETICA, 9, Font.BOLD, TEXT_DARK);
            Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXT_DARK);
            Font smallMuted = new Font(Font.HELVETICA, 8, Font.ITALIC, TEXT_MUTED);
            Font netAmountFont = new Font(Font.HELVETICA, 15, Font.BOLD, ACCENT_COLOR);

            // 1. Header Banner
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{65, 35});
            headerTable.setSpacingAfter(15);

            PdfPCell companyCell = new PdfPCell();
            companyCell.setBorder(Rectangle.NO_BORDER);
            companyCell.addElement(new Paragraph("HRMS ENTERPRISE PORTAL", titleFont));
            companyCell.addElement(new Paragraph("Human Resources & Workforce Operations • Global HQ", subtitleFont));
            companyCell.addElement(new Paragraph("Confidential Salary Disbursement Statement", subtitleFont));
            headerTable.addCell(companyCell);

            PdfPCell periodCell = new PdfPCell();
            periodCell.setBorder(Rectangle.NO_BORDER);
            periodCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph pPeriod = new Paragraph("PAYSLIP", new Font(Font.HELVETICA, 14, Font.BOLD, ACCENT_COLOR));
            pPeriod.setAlignment(Element.ALIGN_RIGHT);
            periodCell.addElement(pPeriod);
            Paragraph pMonth = new Paragraph(p.getPayPeriod().toUpperCase(), new Font(Font.HELVETICA, 10, Font.BOLD, PRIMARY_COLOR));
            pMonth.setAlignment(Element.ALIGN_RIGHT);
            periodCell.addElement(pMonth);
            Paragraph pRef = new Paragraph("Doc Ref: PAY-" + p.getYear() + "-" + String.format("%02d", p.getMonth()) + "-" + p.getId(), smallMuted);
            pRef.setAlignment(Element.ALIGN_RIGHT);
            periodCell.addElement(pRef);
            headerTable.addCell(periodCell);

            document.add(headerTable);

            // Divider Line
            PdfPTable divider = new PdfPTable(1);
            divider.setWidthPercentage(100);
            PdfPCell lineCell = new PdfPCell();
            lineCell.setBackgroundColor(ACCENT_COLOR);
            lineCell.setFixedHeight(2f);
            lineCell.setBorder(Rectangle.NO_BORDER);
            divider.addCell(lineCell);
            divider.setSpacingAfter(15);
            document.add(divider);

            // 2. Employee & Payment Summary Table
            PdfPTable empTable = new PdfPTable(4);
            empTable.setWidthPercentage(100);
            empTable.setWidths(new float[]{22, 28, 22, 28});
            empTable.setSpacingAfter(18);

            addInfoRow(empTable, "Employee ID:", "EMP-" + (p.getEmployee() != null ? p.getEmployee().getId() : "N/A"),
                    "Pay Period:", p.getPayPeriod(), boldFont, normalFont);
            addInfoRow(empTable, "Employee Name:", p.getEmployeeName(),
                    "Disbursement Date:", p.getPaymentDate() != null ? p.getPaymentDate().format(DATE_FMT) : "Pending", boldFont, normalFont);
            addInfoRow(empTable, "Department:", p.getDepartment() != null ? p.getDepartment() : "General",
                    "Payment Method:", p.getPaymentMethod() != null ? p.getPaymentMethod() : "DIRECT_DEPOSIT", boldFont, normalFont);
            addInfoRow(empTable, "Designation:", p.getJobTitle() != null ? p.getJobTitle() : "Specialist",
                    "Payment Status:", p.getStatus() != null ? p.getStatus() : "GENERATED", boldFont, normalFont);

            document.add(empTable);

            // 3. Earnings & Deductions Table
            Paragraph breakdownTitle = new Paragraph("SALARY BREAKDOWN", sectionFont);
            breakdownTitle.setSpacingAfter(6);
            document.add(breakdownTitle);

            PdfPTable salaryTable = new PdfPTable(4);
            salaryTable.setWidthPercentage(100);
            salaryTable.setWidths(new float[]{32, 18, 32, 18});
            salaryTable.setSpacingAfter(15);

            // Header row
            addTableHeader(salaryTable, "EARNINGS", tableHeaderFont);
            addTableHeader(salaryTable, "AMOUNT (₹)", tableHeaderFont);
            addTableHeader(salaryTable, "DEDUCTIONS", tableHeaderFont);
            addTableHeader(salaryTable, "AMOUNT (₹)", tableHeaderFont);

            // Row 1: Basic & PF
            addTableRow(salaryTable, "Basic Salary", formatMoney(p.getBasicSalary()),
                    "Provident Fund (PF)", formatMoney(p.getPfDeduction()), normalFont);

            // Row 2: HRA & Professional Tax
            addTableRow(salaryTable, "House Rent Allowance (HRA)", formatMoney(p.getHra()),
                    "Professional Tax (PT)", formatMoney(p.getProfessionalTax()), normalFont);

            // Row 3: Special Allowance & Income Tax
            addTableRow(salaryTable, "Special Allowance", formatMoney(p.getSpecialAllowance()),
                    "Income Tax (TDS)", formatMoney(p.getIncomeTax()), normalFont);

            // Row 4: Performance Bonus & Other Deductions
            addTableRow(salaryTable, "Performance / Project Bonus", formatMoney(p.getBonus()),
                    "Other Statutory Deductions", formatMoney(p.getOtherDeductions()), normalFont);

            // Totals Row
            PdfPCell totalGrossLabel = new PdfPCell(new Phrase("Total Gross Earnings", boldFont));
            totalGrossLabel.setBackgroundColor(BG_HEADER);
            totalGrossLabel.setPadding(6);
            salaryTable.addCell(totalGrossLabel);

            PdfPCell totalGrossVal = new PdfPCell(new Phrase(formatMoney(p.getGrossEarnings()), boldFont));
            totalGrossVal.setBackgroundColor(BG_HEADER);
            totalGrossVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalGrossVal.setPadding(6);
            salaryTable.addCell(totalGrossVal);

            PdfPCell totalDedLabel = new PdfPCell(new Phrase("Total Deductions", boldFont));
            totalDedLabel.setBackgroundColor(BG_HEADER);
            totalDedLabel.setPadding(6);
            salaryTable.addCell(totalDedLabel);

            PdfPCell totalDedVal = new PdfPCell(new Phrase(formatMoney(p.getTotalDeductions()), boldFont));
            totalDedVal.setBackgroundColor(BG_HEADER);
            totalDedVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalDedVal.setPadding(6);
            salaryTable.addCell(totalDedVal);

            document.add(salaryTable);

            // 4. Net Pay Callout Box
            PdfPTable netTable = new PdfPTable(2);
            netTable.setWidthPercentage(100);
            netTable.setWidths(new float[]{60, 40});
            netTable.setSpacingAfter(20);

            PdfPCell wordsCell = new PdfPCell();
            wordsCell.setBackgroundColor(SUCCESS_BG);
            wordsCell.setPadding(12);
            wordsCell.setBorderColor(new Color(187, 247, 208));
            wordsCell.addElement(new Paragraph("Net Pay in Words:", boldFont));
            wordsCell.addElement(new Paragraph(numberToWords(p.getNetSalary().longValue()) + " Only", new Font(Font.HELVETICA, 10, Font.ITALIC, SUCCESS_TEXT)));
            netTable.addCell(wordsCell);

            PdfPCell netAmountCell = new PdfPCell();
            netAmountCell.setBackgroundColor(SUCCESS_BG);
            netAmountCell.setPadding(12);
            netAmountCell.setBorderColor(new Color(187, 247, 208));
            netAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph netLabel = new Paragraph("NET SALARY PAYABLE", new Font(Font.HELVETICA, 8, Font.BOLD, SUCCESS_TEXT));
            netLabel.setAlignment(Element.ALIGN_RIGHT);
            netAmountCell.addElement(netLabel);
            Paragraph netVal = new Paragraph("₹ " + formatMoney(p.getNetSalary()), netAmountFont);
            netVal.setAlignment(Element.ALIGN_RIGHT);
            netAmountCell.addElement(netVal);
            netTable.addCell(netAmountCell);

            document.add(netTable);

            // 5. Digital Verification & Footer Note
            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setWidthPercentage(100);
            PdfPCell footerCell = new PdfPCell();
            footerCell.setBackgroundColor(BG_LIGHT);
            footerCell.setBorderColor(BORDER_COLOR);
            footerCell.setPadding(10);
            footerCell.addElement(new Paragraph("• This payslip is electronically generated by the HRMS Portal system and does not require a physical signature.", smallMuted));
            footerCell.addElement(new Paragraph("• For any discrepancies or tax inquiries, please submit a ticket to the Payroll Helpdesk within 5 business days.", smallMuted));
            footerCell.addElement(new Paragraph("• Strictly confidential: Unauthorized distribution or disclosure is prohibited under corporate policy.", smallMuted));
            footerTable.addCell(footerCell);
            document.add(footerTable);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error rendering payslip PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String title, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setBackgroundColor(BG_HEADER);
        cell.setPadding(6);
        cell.setBorderColor(BORDER_COLOR);
        table.addCell(cell);
    }

    private void addTableRow(PdfPTable table, String col1, String col2, String col3, String col4, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(col1, font));
        c1.setPadding(5);
        c1.setBorderColor(BORDER_COLOR);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(col2, font));
        c2.setPadding(5);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBorderColor(BORDER_COLOR);
        table.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase(col3, font));
        c3.setPadding(5);
        c3.setBorderColor(BORDER_COLOR);
        table.addCell(c3);

        PdfPCell c4 = new PdfPCell(new Phrase(col4, font));
        c4.setPadding(5);
        c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c4.setBorderColor(BORDER_COLOR);
        table.addCell(c4);
    }

    private void addInfoRow(PdfPTable table, String l1, String v1, String l2, String v2, Font boldFont, Font normalFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(l1, boldFont));
        c1.setBackgroundColor(BG_LIGHT);
        c1.setPadding(5);
        c1.setBorderColor(BORDER_COLOR);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(v1, normalFont));
        c2.setPadding(5);
        c2.setBorderColor(BORDER_COLOR);
        table.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase(l2, boldFont));
        c3.setBackgroundColor(BG_LIGHT);
        c3.setPadding(5);
        c3.setBorderColor(BORDER_COLOR);
        table.addCell(c3);

        PdfPCell c4 = new PdfPCell(new Phrase(v2, normalFont));
        c4.setPadding(5);
        c4.setBorderColor(BORDER_COLOR);
        table.addCell(c4);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }

    private String numberToWords(long n) {
        if (n == 0) return "Zero Rupees";
        return convertToIndianCurrency(n);
    }

    private static final String[] UNITS = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private String convertToIndianCurrency(long n) {
        if (n < 0) return "Minus " + convertToIndianCurrency(-n);
        StringBuilder sb = new StringBuilder();

        if (n / 10000000 > 0) {
            sb.append(convertLessThanThousand((int) (n / 10000000))).append(" Crore ");
            n %= 10000000;
        }
        if (n / 100000 > 0) {
            sb.append(convertLessThanThousand((int) (n / 100000))).append(" Lakh ");
            n %= 100000;
        }
        if (n / 1000 > 0) {
            sb.append(convertLessThanThousand((int) (n / 1000))).append(" Thousand ");
            n %= 1000;
        }
        if (n / 100 > 0) {
            sb.append(convertLessThanThousand((int) (n / 100))).append(" Hundred ");
            n %= 100;
        }
        if (n > 0) {
            sb.append(convertLessThanHundred((int) n)).append(" ");
        }

        sb.append("Rupees");
        return sb.toString().trim().replaceAll("\\s+", " ");
    }

    private String convertLessThanThousand(int n) {
        if (n == 0) return "";
        StringBuilder sb = new StringBuilder();
        if (n / 100 > 0) {
            sb.append(UNITS[n / 100]).append(" Hundred ");
            n %= 100;
        }
        if (n > 0) {
            sb.append(convertLessThanHundred(n));
        }
        return sb.toString().trim();
    }

    private String convertLessThanHundred(int n) {
        if (n < 20) return UNITS[n];
        return TENS[n / 10] + (n % 10 > 0 ? " " + UNITS[n % 10] : "");
    }
}
