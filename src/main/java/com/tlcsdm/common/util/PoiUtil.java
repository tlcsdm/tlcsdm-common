package com.tlcsdm.common.util;

import com.tlcsdm.common.base.BaseUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.converter.core.BasicURIResolver;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.Color;
import java.awt.Font;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.poi.ss.usermodel.IndexedColors.AUTOMATIC;

/**
 * @author: TangLiang
 * @date: 2022/1/24 10:37
 * @since: 1.0
 */
public class PoiUtil {

    /**
     * Word 转为　HTML doc
     * <p>
     * filePath:  D:/images
     * reqUrl:  ".." + "/Document"
     *
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static String wordToHtml(InputStream inputStream, String filePath, String reqUrl) throws IOException, ParserConfigurationException, TransformerException {
        HWPFDocument wordDoc = new HWPFDocument(inputStream);
        WordToHtmlConverter wthc = new WordToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        wthc.setPicturesManager((bytes, pt, string, f, f1) -> reqUrl + File.separator + string);
        wthc.processDocument(wordDoc);
        List<org.apache.poi.hwpf.usermodel.Picture> pics = wordDoc.getPicturesTable().getAllPictures();
        if (null != pics && pics.size() > 0) {
            for (org.apache.poi.hwpf.usermodel.Picture pic : pics) {
                pic.writeImageContent(new FileOutputStream(filePath + File.separator + pic.suggestFullFileName()));
            }
        }
        Document htmlDocument = wthc.getDocument();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DOMSource domSource = new DOMSource(htmlDocument);
        StreamResult streamResult = new StreamResult(out);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        serializer.transform(domSource, streamResult);

        out.close();
        inputStream.close();

        return out.toString();
    }

    /**
     * Word 转为　HTML docx
     * <p>
     * filePath:  D:/images
     * reqUrl:  ".." + "/Document"
     *
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static String wordDocxToHtml(InputStream inputStream, String filePath, String reqUrl) throws IOException {
        XWPFDocument document = new XWPFDocument(inputStream);
        // 2) 解析 XHTML配置 (这里设置IURIResolver来设置图片存放的目录)
        File imageFolderFile = new File(filePath);
        XHTMLOptions options = XHTMLOptions.create().URIResolver(new BasicURIResolver(reqUrl));
        options.setExtractor(new FileImageExtractor(imageFolderFile));
        options.setIgnoreStylesIfUnused(false);
        options.setFragment(true);
        //使用字符数组流获取解析的内容
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XHTMLConverter.getInstance().convert(document, baos, options);
        String content = baos.toString();
        baos.close();
        inputStream.close();

        return content;
    }

    /**
     * 转换EXCEL为HTML
     *
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static String convertExcelToHtml(InputStream inputStream) throws Exception {
        String excelHtml = null;
        Workbook wb = WorkbookFactory.create(inputStream);// 此WorkbookFactory在POI-3.10版本中使用需要添加dom4j
        if (wb instanceof XSSFWorkbook) {
            XSSFWorkbook xWb = (XSSFWorkbook) wb;
            excelHtml = getExcelHtml(xWb, true);
        }
        if (wb instanceof HSSFWorkbook) {
            HSSFWorkbook hWb = (HSSFWorkbook) wb;
            excelHtml = getExcelHtml(hWb, true);
        }

        //wb.close();
        inputStream.close();

        return excelHtml;
    }

    public static String convertExcelToHtmlByWb(Workbook workbook) {
        return getExcelHtml(workbook, true);
    }

    /**
     * @功能描述 POI 读取 Excel 转 HTML 支持 2003xls 和 2007xlsx 版本 包含样式
     * @author Devil
     * @创建时间 2015/4/19 21:34
     */
    public static String getExcelHtml(Workbook wb, boolean isWithStyle) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);// 获取第一个Sheet的内容
            Map<String, List<Picture>> sheetPictureMap = getSheetPictrues(sheet, wb);// 获取excel中的图片

            int lastRowNum = sheet.getLastRowNum();
            Map<String, String> map[] = getRowSpanColSpanMap(sheet);
            sb.append("<table style='border-collapse:collapse;' width='100%'>");
            Row row = null; // 兼容
            Cell cell = null; // 兼容

            for (int rowNum = sheet.getFirstRowNum(); rowNum <= lastRowNum; rowNum++) {
                row = sheet.getRow(rowNum);
                if (row == null) {
                    sb.append("<tr><td > &nbsp;</td></tr>");
                    continue;
                }
                sb.append("<tr>");
                int lastColNum = row.getLastCellNum();
                for (int colNum = 0; colNum < lastColNum; colNum++) {
                    cell = row.getCell(colNum);
                    if (cell == null) { // 特殊情况 空白的单元格会返回null
                        sb.append("<td align='left' valign='center' style='border: 1px solid rgb(0, 0, 0); width: 2304px; font-size: 110%; font-weight: 400;'>&nbsp;</td>");
                        continue;
                    }

                    String pictureKey = rowNum + "," + colNum;
                    String pictureHtml = "";
                    boolean hasPicture = false;// 判断该行是否存在图片
                    if (sheetPictureMap.containsKey(pictureKey)) {
                        List<Picture> pictureList = sheetPictureMap.get(pictureKey);
                        for (Picture picture : pictureList) {
                            //pictureHtml += "<img src=data:image/jpeg;base64," + new String(Base64.encodeBase64(picture.getPictureData().getData())) + " oncontextmenu=\"return false;\" ondragstart=\"return false;\"style=\"height:" + picture.getImageDimension().getHeight() + "px;width:" + picture.getImageDimension().getHeight() + "px;position:absolute;top:" + picture.getClientAnchor().getDy1() / 12700 + "px;left:" + picture.getClientAnchor().getDx1() / 12700 + "px\">";
                        }
                        hasPicture = true;
                    }

                    String stringValue = BaseUtils.getCellValue(cell);
                    if (map[0].containsKey(rowNum + "," + colNum)) {
                        String pointString = map[0].get(rowNum + "," + colNum);
                        map[0].remove(rowNum + "," + colNum);
                        int bottomeRow = Integer.parseInt(pointString.split(",")[0]);
                        int bottomeCol = Integer.parseInt(pointString.split(",")[1]);
                        int rowSpan = bottomeRow - rowNum + 1;
                        int colSpan = bottomeCol - colNum + 1;
                        sb.append("<td rowspan= '").append(rowSpan).append("' colspan= '").append(colSpan).append("' ");
                    } else if (map[1].containsKey(rowNum + "," + colNum)) {
                        map[1].remove(rowNum + "," + colNum);
                        continue;
                    } else {
                        sb.append("<td ");
                    }

                    // 判断是否需要样式
                    if (isWithStyle) {
                        dealExcelStyle(wb, sheet, cell, sb, hasPicture);// 处理单元格样式
                    }

                    sb.append(">");
                    if (sheetPictureMap.containsKey(pictureKey)) {
                        sb.append(pictureHtml);
                    }
                    if ((stringValue == null || "".equals(stringValue.trim())) && !row.getZeroHeight()) {
                        sb.append(" &nbsp; ");
                    } else {
                        // 将ascii码为160的空格转换为html下的空格（&nbsp;）
                        sb.append(stringValue.replace(String.valueOf((char) 160), "&nbsp;"));
                    }
                    sb.append("</td>");
                }
                sb.append("</tr>");
            }

            sb.append("</table>");
            sb.append("<br /><br />");
        }

        return sb.toString();
    }

    @SuppressWarnings({"unchecked"})
    private static Map<String, String>[] getRowSpanColSpanMap(Sheet sheet) {
        Map<String, String> map0 = new HashMap<String, String>();
        Map<String, String> map1 = new HashMap<String, String>();
        int mergedNum = sheet.getNumMergedRegions();
        CellRangeAddress range = null;
        for (int i = 0; i < mergedNum; i++) {
            range = sheet.getMergedRegion(i);
            int topRow = range.getFirstRow();
            int topCol = range.getFirstColumn();
            int bottomRow = range.getLastRow();
            int bottomCol = range.getLastColumn();
            map0.put(topRow + "," + topCol, bottomRow + "," + bottomCol);
            int tempRow = topRow;
            while (tempRow <= bottomRow) {
                int tempCol = topCol;
                while (tempCol <= bottomCol) {
                    map1.put(tempRow + "," + tempCol, "");
                    tempCol++;
                }
                tempRow++;
            }
            map1.remove(topRow + "," + topCol);
        }
        return new Map[]{map0, map1};
    }

    /**
     * 处理表格样式
     *
     * @param wb
     * @param sheet
     * @param cell
     * @param sb
     */
    private static void dealExcelStyle(Workbook wb, Sheet sheet, Cell cell, StringBuffer sb, Boolean hasPicture) {
        boolean rowInvisible = sheet.getRow(cell.getRowIndex()).getZeroHeight();

        int columnWidth = sheet.getColumnWidth(cell.getColumnIndex());
        int columnHeight = (int) (sheet.getRow(cell.getRowIndex()).getHeight() / 15.625);
        if (rowInvisible) {
            columnHeight = 0;
        }

        CellStyle cellStyle = cell.getCellStyle();
        if (cellStyle != null) {
            short alignment = cellStyle.getAlignment().getCode();
            sb.append("align='" + convertAlignToHtml(alignment) + "' ");// 单元格内容的水平对齐方式
            short verticalAlignment = cellStyle.getVerticalAlignment().getCode();
            sb.append("valign='" + convertVerticalAlignToHtml(verticalAlignment) + "' ");// 单元格中内容的垂直排列方式

            if (wb instanceof XSSFWorkbook) {
                XSSFFont xf = ((XSSFCellStyle) cellStyle).getFont();
                short boldWeight = getBoldweight(xf.getBold());
                sb.append("style='");
                sb.append("font-weight:").append(boldWeight).append(";"); // 字体加粗
                sb.append("font-size: " + xf.getFontHeight() / 1.5 + "%;"); // 字体大小
                sb.append("width:" + columnWidth + "px;");
                sb.append("height:" + columnHeight + "px;");
                if (hasPicture) {
                    sb.append("height:" + columnHeight + "px;position:relative;");
                }

                XSSFColor xc = xf.getXSSFColor();
                if (xc != null && !"".equals(xc)) {
                    sb.append("color:#" + xc.getARGBHex().substring(2) + ";"); // 字体颜色
                }

                XSSFColor bgColor = (XSSFColor) cellStyle.getFillForegroundColorColor();
                if (bgColor != null) {
                    sb.append("background-color:#" + bgColor.getARGBHex().substring(2) + ";"); // 背景颜色
                }
                if (!rowInvisible) {
                    sb.append(getBorderStyle(0, cellStyle.getBorderTop().getCode(), ((XSSFCellStyle) cellStyle).getTopBorderXSSFColor()));
                    sb.append(getBorderStyle(1, cellStyle.getBorderRight().getCode(), ((XSSFCellStyle) cellStyle).getRightBorderXSSFColor()));
                    sb.append(getBorderStyle(2, cellStyle.getBorderBottom().getCode(), ((XSSFCellStyle) cellStyle).getBottomBorderXSSFColor()));
                    sb.append(getBorderStyle(3, cellStyle.getBorderLeft().getCode(), ((XSSFCellStyle) cellStyle).getLeftBorderXSSFColor()));
                }
            } else if (wb instanceof HSSFWorkbook) {
                HSSFFont hf = ((HSSFCellStyle) cellStyle).getFont(wb);
                short boldWeight = getBoldweight(hf.getBold());
                short fontColor = hf.getColor();
                sb.append("style='");
                HSSFPalette palette = ((HSSFWorkbook) wb).getCustomPalette(); // 类HSSFPalette用于求的颜色的国际标准形式
                HSSFColor hc = palette.getColor(fontColor);
                sb.append("font-weight:").append(boldWeight).append(";"); // 字体加粗
                sb.append("font-size: " + hf.getFontHeight() / 1.5 + "%;"); // 字体大小
                String fontColorStr = convertToStardColor(hc);
                if (fontColorStr != null && !"".equals(fontColorStr.trim())) {
                    sb.append("color:" + fontColorStr + ";"); // 字体颜色
                }
                sb.append("width:" + columnWidth + "px;");
                sb.append("height:" + columnHeight + "px;");
                if (hasPicture) {
                    sb.append("height:" + columnHeight + "px;position:relative;");
                }
                short bgColor = cellStyle.getFillForegroundColor();
                hc = palette.getColor(bgColor);
                String bgColorStr = convertToStardColor(hc);
                if (bgColorStr != null && !"".equals(bgColorStr.trim())) {
                    sb.append("background-color:" + bgColorStr + ";"); // 背景颜色
                }
                if (!rowInvisible) {
                    sb.append(getBorderStyle(palette, 0, cellStyle.getBorderTop().getCode(), cellStyle.getTopBorderColor()));
                    sb.append(getBorderStyle(palette, 1, cellStyle.getBorderRight().getCode(), cellStyle.getRightBorderColor()));
                    sb.append(getBorderStyle(palette, 3, cellStyle.getBorderLeft().getCode(), cellStyle.getLeftBorderColor()));
                    sb.append(getBorderStyle(palette, 2, cellStyle.getBorderBottom().getCode(), cellStyle.getBottomBorderColor()));
                }
            }

            sb.append("' ");
        }
    }

    /**
     * 单元格内容的水平对齐方式
     */
    private static String convertAlignToHtml(short alignment) {
        String align = "left";
        switch (HorizontalAlignment.forInt(alignment)) {
            case LEFT:
                align = "left";
                break;
            case CENTER:
                align = "center";
                break;
            case RIGHT:
                align = "right";
                break;
            default:
                break;
        }
        return align;
    }

    /**
     * 单元格中内容的垂直排列方式
     *
     * @param verticalAlignment
     * @return
     */
    private static String convertVerticalAlignToHtml(short verticalAlignment) {
        String valign = "middle";
        switch (VerticalAlignment.forInt(verticalAlignment)) {
            case BOTTOM:
                valign = "bottom";
                break;
            case CENTER:
                valign = "center";
                break;
            case TOP:
                valign = "top";
                break;
            default:
                break;
        }
        return valign;
    }

    private static String convertToStardColor(HSSFColor hc) {
        StringBuffer sb = new StringBuffer();
        if (hc != null) {
            if (AUTOMATIC.index == hc.getIndex()) {
                return null;
            }
            sb.append("#");
            for (int i = 0; i < hc.getTriplet().length; i++) {
                sb.append(BaseUtils.fillWithZero(Integer.toHexString(hc.getTriplet()[i])));
            }
        }

        return sb.toString();
    }

    private static final String[] bordesr = {"border-top:", "border-right:", "border-bottom:", "border-left:"};
    private static final String[] borderStyles = {"solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid ", "solid", "solid", "solid", "solid", "solid"};

    private static String getBorderStyle(HSSFPalette palette, int b, short s, short t) {
        if (s == 0) {
            return bordesr[b] + borderStyles[s] + "#d0d7e5 0px;";
        }

        String borderColorStr = convertToStardColor(palette.getColor(t));
        borderColorStr = borderColorStr == null || borderColorStr.length() < 1 ? "#000000" : borderColorStr;
        return bordesr[b] + borderStyles[s] + borderColorStr + " 1px;";
    }

    private static String getBorderStyle(int b, short s, XSSFColor xc) {
        if (s == 0) {
            return bordesr[b] + borderStyles[s] + "#d0d7e5 0px;";
        }

        if (xc != null) {
            String borderColorStr = xc.getARGBHex();// t.getARGBHex();
            borderColorStr = borderColorStr == null || borderColorStr.length() < 1 ? "#000000" : borderColorStr.substring(2);
            return bordesr[b] + borderStyles[s] + borderColorStr + " 1px;";
        }

        return "";
    }

    /**
     * 获取Excel图片公共方法
     *
     * @param sheet    当前sheet对象
     * @param workbook 工作簿对象
     * @return Map key:图片单元格索引（1,1）String，value:图片流Picture
     */
    public static Map<String, List<Picture>> getSheetPictrues(Sheet sheet, Workbook workbook) {
        if (workbook instanceof XSSFWorkbook) {
            return getSheetPictureMap2007((XSSFSheet) sheet);
        } else if (workbook instanceof HSSFWorkbook) {
            return getSheetPictrues2003((HSSFSheet) sheet);
        } else {
            return null;
        }
    }

    /**
     * 获取Excel2007图片
     *
     * @param sheet 当前sheet对象
     * @return Map key:图片单元格索引（1,1）String，value:图片流Picture
     */
    private static Map<String, List<Picture>> getSheetPictureMap2007(XSSFSheet sheet) {
        Map<String, List<Picture>> sheetPictureMap = new HashMap<>();

        for (POIXMLDocumentPart documentPart : sheet.getRelations()) {
            if (documentPart instanceof XSSFDrawing) {
                XSSFDrawing drawing = (XSSFDrawing) documentPart;
                List<XSSFShape> shapeList = drawing.getShapes();
                for (XSSFShape shape : shapeList) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    XSSFClientAnchor anchor = picture.getPreferredSize();
                    CTMarker ctMarker = anchor.getFrom();
                    String pictureKey = ctMarker.getRow() + "," + ctMarker.getCol();
                    List<Picture> pictureList = sheetPictureMap.computeIfAbsent(pictureKey, k -> new ArrayList<>());
                    pictureList.add(picture);
                }
            }
        }

        return sheetPictureMap;
    }

    /**
     * 获取Excel2003图片
     *
     * @param sheet 当前sheet对象
     * @return Map key:图片单元格索引（1,1）String，value:图片流Picture
     */
    private static Map<String, List<Picture>> getSheetPictrues2003(HSSFSheet sheet) {
        Map<String, List<Picture>> sheetPictureMap = new HashMap<>();

        // 处理sheet中的图形
        HSSFPatriarch hssfPatriarch = sheet.getDrawingPatriarch();
        if (hssfPatriarch != null) {
            // 获取所有的形状图
            List<HSSFShape> shapes = hssfPatriarch.getChildren();
            for (HSSFShape sp : shapes) {
                if (sp instanceof HSSFPicture) {
                    // 转换
                    HSSFPicture picture = (HSSFPicture) sp;
                    // 图形定位
                    if (picture.getAnchor() instanceof HSSFClientAnchor) {
                        HSSFClientAnchor anchor = (HSSFClientAnchor) picture.getAnchor();
                        String pictureKey = anchor.getRow1() + "," + String.valueOf(anchor.getCol1());
                        List<Picture> pictureList = sheetPictureMap.computeIfAbsent(pictureKey, k -> new ArrayList<>());
                        pictureList.add(picture);
                    }
                }
            }
        }
        return sheetPictureMap;
    }

    public static byte[] bufferedImageToBytes(BufferedImage bufferedImage, String imageFormat) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, imageFormat, byteArrayOutputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 单元格内写字，居中，自动换行
     *
     * @param g2d
     * @param text
     * @param x      单元格坐标
     * @param y      单元格坐标
     * @param width  单元格宽度
     * @param height 单元格高度
     * @param font   字体
     */
    public static void drawStringInCell(Graphics2D g2d, String text, int x, int y, int width, int height, String valign, Font font) {
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics(font); // 计算文字长度
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getAscent() + fontMetrics.getDescent();

        String[] texts;// 分割字符串，分行
        int lineNum = 1;
        if (textWidth > width * 0.98) {
            lineNum = (int) Math.ceil(textWidth / (width * 0.98));
            int length = (int) (width * 0.98 / textWidth * text.length());
            texts = new String[lineNum];
            for (int i = 0; i < lineNum; i++) {
                if (i < lineNum - 1) {
                    texts[i] = text.substring(length * i, length * (i + 1));
                } else {
                    texts[i] = text.substring(length * i);
                }
            }
        } else {
            texts = new String[1];
            texts[0] = text;
        }

        int textX;// 写字
        int textY;
        for (int i = 0; i < lineNum; i++) {
            textX = (width - fontMetrics.stringWidth(texts[i])) / 2 + x;// 横向居中
            if ("top".equals(valign)) {
                textY = textHeight * i + y + fontMetrics.getAscent();// 纵向居中
            } else if ("bottom".equals(valign)) {
                textY = (height - textHeight * lineNum) + textHeight * i + y + fontMetrics.getAscent();// 纵向居中
            } else {
                textY = (height - textHeight * lineNum) / 2 + textHeight * i + y + fontMetrics.getAscent();// 纵向居中
            }
            g2d.drawString(texts[i], textX, textY);
        }
    }

    /**
     * 为图片添加阴影
     *
     * @param bufferedImage
     * @param size
     * @param color
     * @param alpha
     * @return
     */
    public static BufferedImage applyShadow(BufferedImage bufferedImage, int size, Color color, float alpha) {
        BufferedImage result = createCompatibleImage(bufferedImage, bufferedImage.getWidth() + (size * 2), bufferedImage.getHeight() + (size * 2));
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(generateShadow(bufferedImage, size, color, alpha), size, size, null);
        g2d.drawImage(bufferedImage, 0, 0, null);
        g2d.dispose();

        return result;
    }

    private static BufferedImage createCompatibleImage(BufferedImage image, int width, int height) {
        return getGraphicsConfiguration().createCompatibleImage(width, height, image.getTransparency());
    }

    private static BufferedImage createCompatibleImage(int width, int height) {
        return createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    }

    private static BufferedImage createCompatibleImage(int width, int height, int transparency) {
        BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width, height, transparency);
        image.coerceData(true);
        return image;
    }

    private static GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    private static BufferedImage generateShadow(BufferedImage imgSource, int size, Color color, float alpha) {
        int imgWidth = imgSource.getWidth() + (size * 2);
        int imgHeight = imgSource.getHeight() + (size * 2);

        BufferedImage imgMask = createCompatibleImage(imgWidth, imgHeight);
        Graphics2D g2d = imgMask.createGraphics();
        applyQualityRenderingHints(g2d);

        int x = Math.round((imgWidth - imgSource.getWidth()) / 2f);
        int y = Math.round((imgHeight - imgSource.getHeight()) / 2f);
        g2d.drawImage(imgSource, x, y, null);
        g2d.dispose();

        return generateBlur(imgMask, (size * 2), color, alpha);
    }

    private static void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static BufferedImage generateBlur(BufferedImage imgSource, int size, Color color, float alpha) {
        //GaussianFilter filter = new GaussianFilter(size);

        int imgWidth = imgSource.getWidth();
        int imgHeight = imgSource.getHeight();

        BufferedImage imgBlur = createCompatibleImage(imgWidth, imgHeight);
        Graphics2D g2 = imgBlur.createGraphics();
        applyQualityRenderingHints(g2);

        g2.drawImage(imgSource, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
        g2.setColor(color);

        g2.fillRect(0, 0, imgSource.getWidth(), imgSource.getHeight());
        g2.dispose();

        //imgBlur = filter.filter(imgBlur, null);

        return imgBlur;
    }

    private static short getBoldweight(boolean hasBold) {
        return (short) (hasBold ? 700 : 400);
    }

}
