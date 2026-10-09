package com.studentattendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;
import com.studentattendance.dto.report.AttendanceReportResponse;
import com.studentattendance.dto.report.AttendanceReportRow;
import org.junit.jupiter.api.Test;

class ExportTests {
    @Test
    void csvEscapesQuotesAndNeutralizesSpreadsheetFormulas() {
        AttendanceReportService service = new AttendanceReportService(null, null, null, null, null, null);
        var row = new AttendanceReportRow(1L, "=1+1", "Name, \"quoted\"", 1L, "@SUM(1)", "+formula",
                3, 2, 1, new BigDecimal("66.67"));
        var report = new AttendanceReportResponse("Report", null, null, null, null, null, 1, List.of(row));
        String csv = new String(service.exportCsv(report), StandardCharsets.UTF_8);
        assertThat(csv).contains("\"'=1+1\"", "\"Name, \"\"quoted\"\"\"", "\"'@SUM(1)\"", "3,2,1,66.67");
    }

    @Test
    void pdfPaginatesAllRowsAndHasCorrectObjectOffsets() {
        List<String> lines = IntStream.range(0, 120).mapToObj(i -> "Student row " + i).toList();
        String pdf = new String(SimplePdfWriter.write(lines), StandardCharsets.UTF_8);
        assertThat(pdf).startsWith("%PDF-1.4").contains("/Count 3", "Student row 119").endsWith("%%EOF\n");
        int xref = Integer.parseInt(pdf.substring(pdf.lastIndexOf("startxref\n") + 10).split("\n")[0]);
        assertThat(pdf.substring(xref)).startsWith("xref\n");
        String[] entries = pdf.substring(xref).split("\n");
        int count = Integer.parseInt(entries[1].split(" ")[1]);
        for (int i = 1; i < count; i++) {
            int offset = Integer.parseInt(entries[i + 2].substring(0, 10));
            assertThat(pdf.substring(offset)).startsWith(i + " 0 obj\n");
        }
    }

    @Test
    void pdfWrapsLongLinesWithoutDroppingTheirEnding() {
        String pdf = new String(SimplePdfWriter.write(List.of("x".repeat(250) + "THE-END")), StandardCharsets.UTF_8);
        assertThat(pdf).contains("THE-END");
    }
}
