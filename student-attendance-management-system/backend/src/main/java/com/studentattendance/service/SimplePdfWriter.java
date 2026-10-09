package com.studentattendance.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class SimplePdfWriter {

    private SimplePdfWriter() {
    }

    static byte[] write(List<String> lines) {
        // A fixed-width font permits deterministic wrapping without clipping rows.
        List<String> wrapped = new ArrayList<>();
        for (String line : lines) {
            String printable = (line == null ? "" : line).replaceAll("[^\\x20-\\x7E]", "?");
            if (printable.isEmpty()) wrapped.add("");
            for (int start = 0; start < printable.length(); start += 90) {
                wrapped.add(printable.substring(start, Math.min(start + 90, printable.length())));
            }
        }
        int pageCount = Math.max(1, (wrapped.size() + 47) / 48);
        List<byte[]> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.UTF_8));
        StringBuilder kids = new StringBuilder();
        for (int page = 0; page < pageCount; page++) kids.append(4 + page * 2).append(" 0 R ");
        objects.add(("<< /Type /Pages /Kids [" + kids + "] /Count " + pageCount + " >>").getBytes(StandardCharsets.UTF_8));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>".getBytes(StandardCharsets.UTF_8));
        for (int page = 0; page < pageCount; page++) {
            int contentId = 5 + page * 2;
            objects.add(("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 3 0 R >> >> /Contents "
                    + contentId + " 0 R >>").getBytes(StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder("BT\n/F1 9 Tf\n50 750 Td\n14 TL\n");
            for (int i = page * 48; i < Math.min((page + 1) * 48, wrapped.size()); i++) {
                content.append('(').append(escape(wrapped.get(i))).append(") Tj\nT*\n");
            }
            content.append("ET\n");
            int length = content.toString().getBytes(StandardCharsets.UTF_8).length;
            objects.add(("<< /Length " + length + " >>\nstream\n" + content + "endstream").getBytes(StandardCharsets.UTF_8));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, "%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            write(out, (i + 1) + " 0 obj\n");
            out.writeBytes(objects.get(i));
            write(out, "\nendobj\n");
        }

        int xref = out.size();
        write(out, "xref\n0 " + (objects.size() + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (Integer offset : offsets) {
            write(out, String.format("%010d 00000 n \n", offset));
        }
        write(out, "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        return out.toByteArray();
    }

    private static void write(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

}
