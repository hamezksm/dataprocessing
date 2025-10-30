package dev.hamez.dataprocessing.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Streaming Excel reader using SAX parsing for memory-efficient processing of
 * large XLSX files. This approach reads the Excel file row by row without
 * loading the entire file into memory.
 */
public class StreamingExcelReader {

    private static final Logger logger = LoggerFactory.getLogger(StreamingExcelReader.class);

    /**
     * Functional interface for processing each row
     */
    @FunctionalInterface
    public interface RowProcessor {

        void processRow(int rowNum, List<String> cells) throws Exception;
    }

    /**
     * Read Excel file using streaming SAX parser
     *
     * @param inputStream Excel file input stream
     * @param rowProcessor Callback to process each row
     * @throws Exception if processing fails
     */
    public static void readExcelStreaming(InputStream inputStream, RowProcessor rowProcessor)
            throws Exception {

        logger.info("Starting streaming Excel read...");
        long startTime = System.currentTimeMillis();

        try (OPCPackage opcPackage = OPCPackage.open(inputStream)) {
            XSSFReader reader = new XSSFReader(opcPackage);
            SharedStringsTable sst = (SharedStringsTable) reader.getSharedStringsTable();
            XMLReader parser = createXMLReader(sst, rowProcessor);

            // Process the first sheet
            Iterator<InputStream> sheets = reader.getSheetsData();
            if (sheets.hasNext()) {
                try (InputStream sheet = sheets.next()) {
                    InputSource sheetSource = new InputSource(sheet);
                    parser.parse(sheetSource);
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Completed streaming Excel read in {} ms", duration);
    }

    private static XMLReader createXMLReader(SharedStringsTable sst, RowProcessor rowProcessor)
            throws SAXException, ParserConfigurationException {

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        SheetHandler handler = new SheetHandler(sst, rowProcessor);
        xmlReader.setContentHandler(handler);

        return xmlReader;
    }

    /**
     * SAX handler for processing Excel sheet XML
     */
    private static class SheetHandler extends DefaultHandler {

        private final SharedStringsTable sst;
        private final RowProcessor rowProcessor;
        // private final DataFormatter formatter = new DataFormatter();

        private String lastContents;
        private boolean nextIsString;
        private int currentRow = 0;
        private int currentCol = 0;
        private final List<String> currentRowData = new ArrayList<>();
        private String cellReference;
        private int rowCount = 0;

        public SheetHandler(SharedStringsTable sst, RowProcessor rowProcessor) {
            this.sst = sst;
            this.rowProcessor = rowProcessor;
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
                throws SAXException {

            // Start of a row
            if ("row".equals(name)) {
                String rowNum = attributes.getValue("r");
                if (rowNum != null) {
                    currentRow = Integer.parseInt(rowNum);
                }
                currentRowData.clear();
                currentCol = 0;
            } // Start of a cell
            else if ("c".equals(name)) {
                cellReference = attributes.getValue("r");
                String cellType = attributes.getValue("t");
                nextIsString = "s".equals(cellType);
                lastContents = "";
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {

            // End of a cell value
            if ("v".equals(name) || "t".equals(name)) {
                if (nextIsString) {
                    try {
                        int idx = Integer.parseInt(lastContents);
                        lastContents = sst.getItemAt(idx).getString();
                    } catch (NumberFormatException e) {
                        // Keep the original value if parsing fails
                    }
                }

                // Pad with empty strings if cells are skipped
                int thisCol = getColumnIndex(cellReference);
                while (currentCol < thisCol) {
                    currentRowData.add("");
                    currentCol++;
                }

                currentRowData.add(lastContents);
                currentCol++;
            } // End of a row
            else if ("row".equals(name)) {
                try {
                    rowProcessor.processRow(currentRow, currentRowData);
                    rowCount++;

                    // Log progress every 100k rows
                    if (rowCount % 100000 == 0) {
                        logger.info("Processed {} rows...", rowCount);
                    }
                } catch (Exception e) {
                    throw new SAXException("Error processing row " + currentRow, e);
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            lastContents += new String(ch, start, length);
        }

        /**
         * Get column index from cell reference (e.g., "A1" -> 0, "B1" -> 1)
         */
        private int getColumnIndex(String cellRef) {
            if (cellRef == null || cellRef.isEmpty()) {
                return 0;
            }

            // Extract column letters
            String colRef = cellRef.replaceAll("[0-9]", "");
            int col = 0;
            for (int i = 0; i < colRef.length(); i++) {
                col = col * 26 + (colRef.charAt(i) - 'A' + 1);
            }
            return col - 1;
        }
    }
}
