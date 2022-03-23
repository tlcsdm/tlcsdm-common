package com.tlcsdm.common.base;

import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.util.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 常用方法封装
 *
 * @author: TangLiang
 * @date: 2021/4/14 15:16
 * @since: 1.0
 */
public class BaseUtils {
    /**
     * 链路追踪key
     */
    public static String TRACE_ID = "traceId";
    /**
     * 数据集key
     */
    public static String DATA = "data";
    /**
     * 汉语中数字大写
     */
    private static final String[] CN_UPPER_NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    /**
     * 汉语中货币单位大写，这样的设计类似于占位符
     */
    private static final String[] CN_UPPER_MONETARY_UNIT = {"分", "角", "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟"};
    /**
     * 特殊字符：整
     */
    private static final String CN_FULL = "整";
    /**
     * 特殊字符：负
     */
    private static final String CN_NEGATIVE = "负";
    /**
     * 金额的精度，默认值为2
     */
    private static final int MONEY_PRECISION = 2;
    /**
     * 特殊字符：零元整
     */
    private static final String CN_ZEOR_FULL = "零元" + CN_FULL;

    /**
     * 类不能实例化
     */
    private BaseUtils() {
    }

    /**
     * 把输入的金额转换为汉语中人民币的大写
     *
     * @param numberOfMoney 输入的金额
     * @return 对应的汉语大写
     */
    public static String number2CNMonetaryUnit(BigDecimal numberOfMoney) {
        StringBuffer sb = new StringBuffer();
        // -1, 0, or 1 as the value of this BigDecimal is negative, zero, or
        // positive.
        int signum = numberOfMoney.signum();
        // 零元整的情况
        if (signum == 0) {
            return CN_ZEOR_FULL;
        }
        // 这里会进行金额的四舍五入
        long number = numberOfMoney.movePointRight(MONEY_PRECISION).setScale(0, 4).abs().longValue();
        // 得到小数点后两位值
        long scale = number % 100;
        int numUnit = 0;
        int numIndex = 0;
        boolean getZero = false;
        // 判断最后两位数，一共有四中情况：00 = 0, 01 = 1, 10, 11
        if (!(scale > 0)) {
            numIndex = 2;
            number = number / 100;
            getZero = true;
        }
        if ((scale > 0) && (!(scale % 10 > 0))) {
            numIndex = 1;
            number = number / 10;
            getZero = true;
        }
        int zeroSize = 0;
        while (true) {
            if (number <= 0) {
                break;
            }
            // 每次获取到最后一个数
            numUnit = (int) (number % 10);
            if (numUnit > 0) {
                if ((numIndex == 9) && (zeroSize >= 3)) {
                    sb.insert(0, CN_UPPER_MONETARY_UNIT[6]);
                }
                if ((numIndex == 13) && (zeroSize >= 3)) {
                    sb.insert(0, CN_UPPER_MONETARY_UNIT[10]);
                }
                sb.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                getZero = false;
                zeroSize = 0;
            } else {
                ++zeroSize;
                if (!(getZero)) {
                    sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                }
                if (numIndex == 2) {
                    if (number > 0) {
                        sb.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                    }
                } else if (((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
                    sb.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                }
                getZero = true;
            }
            // 让number每次都去掉最后一个数
            number = number / 10;
            ++numIndex;
        }
        // 如果signum == -1，则说明输入的数字为负数，就在最前面追加特殊字符：负
        if (signum == -1) {
            sb.insert(0, CN_NEGATIVE);
        }
        // 输入的数字小数点后两位为"00"的情况，则要在最后追加特殊字符：整
        if (!(scale > 0)) {
            sb.append(CN_FULL);
        }
        return sb.toString();
    }

    /**
     * 获得MD5加密结果
     *
     * @param string 加密内容
     */
    public static String getMd5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte hash[] = md.digest();
            StringBuffer sb = new StringBuffer();
            int i = 0;
            for (int offset = 0; offset < hash.length; offset++) {
                i = hash[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(i));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 不同浏览器将下载文件名处理为中文
     *
     * @param request HttpServletRequest对象
     * @param s       文件名
     * @return 文件名
     * @throws UnsupportedEncodingException 不支持的解码方式
     */
    public static String getFormatString(HttpServletRequest request, String s) throws UnsupportedEncodingException {
        String filename = s;
        String userAgent = request.getHeader("User-Agent").toUpperCase();
        if (userAgent.indexOf("FIREFOX") > 0) {
            filename = new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1); // firefox浏览器
        } else if (userAgent.indexOf("MSIE") > 0) {
            filename = URLEncoder.encode(s, "UTF-8");// IE浏览器
        } else if (userAgent.indexOf("CHROME") > 0) {
            filename = new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);// 谷歌
        }
        return filename;
    }

    /**
     * 下载文件
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @param request     request
     * @param response    response
     */
    public static void download(InputStream inputStream, String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        fileName = getFormatString(request, fileName);
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);// 下载模式
        @Cleanup ServletOutputStream out = response.getOutputStream();
        byte[] content = new byte[65535];
        int length = 0;
        while ((length = inputStream.read(content)) != -1) {
            out.write(content, 0, length);
            out.flush();
        }
    }

    /**
     * 流转字节数组
     *
     * @param inputStream 流
     * @return 流转字节数组
     * @throws Exception IOException
     */
    public static byte[] inputStreamToBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] content = new byte[65535];
        int length = 0;
        while ((length = inputStream.read(content)) != -1) {
            baos.write(content, 0, length);
        }

        return baos.toByteArray();
    }

    /**
     * 流转字符串
     *
     * @param inputStream 流
     * @return 流转字符串
     * @throws Exception IOException
     */
    public static String inputStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] content = new byte[65535];
        int length = 0;
        while ((length = inputStream.read(content)) != -1) {
            baos.write(content, 0, length);
        }

        return baos.toString();
    }

    /**
     * 个位数填充0
     *
     * @param str 需要填充的字符串
     * @return 填充后结果
     */
    public static String fillWithZero(String str) {
        if (str != null && str.length() < 2) {
            return "0" + str;
        }
        return str;
    }

    /**
     * 获得UUID
     *
     * @return UUID
     */
    public static String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 返回成功信息(用于加载对象)
     *
     * @param result 加载对象数据
     * @return java.util.Map
     */
    public static Map<String, Object> loadSuccess(Map<String, Object> result) {
        Map<String, Object> data = new HashMap<>(4);
        data.put(DATA, result);
        return success(data);
    }

    /**
     * 返回成功信息(用于保存返回id信息)
     *
     * @param id id
     * @return java.util.Map
     */
    public static Map<String, Object> saveSuccess(String id) {
        Map<String, Object> result = new HashMap<>(4);
        result.put("success", true);
        result.put(DATA, id);
        return result;
    }

    /**
     * 返回成功信息(用于存储过程方式的结果返回)
     *
     * @param result 数据集
     * @return java.util.Map
     */
    public static Map<String, Object> success(Map<String, Object> result) {
        result.put("success", true);
        return result;
    }

    /**
     * 返回成功信息(用于JdbcTemplate的结果返回)
     *
     * @param list 数据集
     * @return java.util.Map
     */
    public static <T> Map<String, Object> success(List<T> list) {
        Map<String, Object> result = new HashMap<>(4);
        result.put("success", true);
        result.put(DATA, list);
        return result;
    }

    /**
     * 返回成功信息(用于JdbcTemplate的结果返回)
     *
     * @param list  数据集
     * @param total 数据集总数
     * @return java.util.Map
     */
    public static <T> Map<String, Object> success(List<T> list, int total) {
        Map<String, Object> result = success(list);
        result.put("total", total);
        return result;
    }

    /**
     * 返回成功信息(用于JdbcTemplate的结果返回)
     *
     * @param list  数据集
     * @param total 数据集总数
     * @return java.util.Map
     */
    public static <T> Map<String, Object> success(List<T> list, long total) {
        Map<String, Object> result = success(list);
        result.put("total", total);
        return result;
    }

    /**
     * 返回成功信息(用于存储过程方式的结果返回)
     *
     * @return java.util.Map
     */
    public static Map<String, Object> success() {
        return Collections.singletonMap("success", true);
    }

    /**
     * 返回失败信息(用于存储过程方式的结果返回)
     *
     * @param message 错误信息
     * @return java.util.Map
     */
    public static Map<String, Object> failed(String message) {
        Map<String, Object> result = new HashMap<>(4);
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    /**
     * 返回失败信息(用于存储过程方式的结果返回)
     *
     * @param result  数据集
     * @param message 错误信息
     * @return java.util.Map
     */
    public static Map<String, Object> failed(Map<String, Object> result, String message) {
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    /**
     * 获取请求客户端ip地址
     *
     * @param request request
     * @return 客户端ip地址
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }
        //解决经过nginx转发, 配置了proxy_set_header x-forwarded-for $proxy_add_x_forwarded_for;带来的多ip的情况
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }

        return ip;
    }

    /**
     * 获取请求url路径
     *
     * @param request request
     * @return url路径
     */
    public static String getUrl(HttpServletRequest request) {
        String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getServletPath();
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }

        return url;
    }

    /**
     * 主机名
     *
     * @return 主机名
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "未知";
    }

    /**
     * 获取cookie值
     *
     * @param key     cookie键
     * @param request request
     * @return cookie值
     */
    public static String getCookieValue(String key, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (key.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    /**
     * 分页
     *
     * @param list  需要分页的list
     * @param page  当前页数
     * @param limit 每次展示页数
     * @return 分页后的list
     */
    public static <T> List<T> page(List<T> list, Integer page, Integer limit) {
        return page != null && limit != null && page > 0 && limit > 0 ?
                list.stream()
                        .skip(limit * (page - 1))
                        .limit(limit)
                        .collect(Collectors.toList())
                : list;
    }

    /**
     * 设置cookie
     *
     * @param response HttpServletResponse
     * @param name     cookie名字
     * @param value    cookie值
     * @param maxAge   cookie生命周期  以秒为单位
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        if (maxAge > 0) {
            cookie.setMaxAge(maxAge);
        }
        response.addCookie(cookie);
    }

    /**
     * 首字母小写
     *
     * @param string
     * @return
     */
    public static String toLowerCase4Index(String string) {
        if (Character.isLowerCase(string.charAt(0))) {
            return string;
        }

        char[] chars = string.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 首字母大写
     *
     * @param string
     * @return
     */
    public static String toUpperCase4Index(String string) {
        char[] chars = string.toCharArray();
        chars[0] = toUpperCase(chars[0]);
        return String.valueOf(chars);
    }

    /**
     * 字符转成大写
     *
     * @param chars
     * @return
     */
    public static char toUpperCase(char chars) {
        if (97 <= chars && chars <= 122) {
            chars ^= 32;
        }
        return chars;
    }

    /**
     * 根据容量获取map初始大小
     * 参考JDK8中putAll方法中的实现以及
     * guava的newHashMapWithExpectedSize方法
     *
     * @param expectedSize 容量大小
     */
    public static int newHashMapWithExpectedSize(int expectedSize) {
        if (expectedSize < 3) {
            return 4;
        } else {
            return expectedSize < 1073741824 ? (int) ((float) expectedSize / 0.75F + 1.0F) : 2147483647;
        }
    }

    /**
     * 深度克隆
     */
    public static Object deepClone(Object object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            if (object != null) {
                objectOutputStream.writeObject(object);
            }
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            return objectInputStream.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 通用excel导出
     *
     * @param wb    HSSFWorkbook对象
     * @param sheet HSSFSheet
     * @param list  导出数据 如果有数据需要格式化或者其它操作，可将数据处理后再传入
     * @param map   key 表头 value 字段名
     */
    public static void dealCommonExcel(HSSFWorkbook wb, HSSFSheet sheet, List<Map<String, Object>> list, LinkedHashMap<String, String> map) {
        int length = map.size() + 1;
        List<String> keyList = new ArrayList<>(length);
        List<String> valueList = new ArrayList<>(length);
        map.forEach((key, value) -> {
            keyList.add(key);
            valueList.add(value);
        });
        for (int i = 0; i < length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 2);
        }

        HSSFRow row = sheet.createRow(0);
        row.setHeightInPoints(30);

        //标题栏样式
        HSSFCellStyle style = wb.createCellStyle();
        HSSFFont font = wb.createFont();
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);//垂直
        font.setFontHeightInPoints((short) 12);//设置字体大小
        style.setFont(font);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框

        HSSFCell cell0 = row.createCell(0);
        cell0.setCellValue("序号");
        cell0.setCellStyle(style);
        for (int i = 1; i < length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(keyList.get(i - 1));
            cell.setCellStyle(style);
        }

        //添加边框
        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setWrapText(true);//自动换行
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中

        for (int i = 0; i < list.size(); i++) {
            row = sheet.createRow(i + 1);
            row.setHeightInPoints(25);

            HSSFCell cellContent = row.createCell(0);
            cellContent.setCellValue(i + 1);
            cellContent.setCellStyle(cellStyle);

            for (int j = 1; j < length; j++) {
                cellContent = row.createCell(j);
                cellContent.setCellValue(valueOf(list.get(i).get(valueList.get(j - 1))));
                cellContent.setCellStyle(cellStyle);
            }
        }
    }

    /**
     * 重写String.valueOf方法
     *
     * @return 为null返回空字符串
     */
    public static String valueOf(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    /**
     * 下载文件
     *
     * @param wb       excel对象
     * @param fileName 文件名
     * @param request  request
     * @param response response
     */
    public static void download(Workbook wb, String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        fileName = getFormatString(request, fileName);
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        OutputStream os = response.getOutputStream();
        wb.write(os);
        os.flush();
        os.close();
    }

    /**
     * 新增一天
     *
     * @param date 需要新增一天的日期
     */
    public static Date addOneDay(Date date) {
        if (date == null) {
            return date;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTime();
    }

    /**
     * 将String字符串转换为java.sql.Timestamp格式日期,用于数据库保存
     *
     * @param strDate    表示日期的字符串
     * @param dateFormat 传入字符串的日期表示格式（如："yyyy-MM-dd HH:mm:ss"）
     * @return java.sql.Timestamp类型日期对象（如果转换失败则返回null）
     */
    public static Timestamp strToSqlDate(String strDate, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = null;
        try {
            date = simpleDateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timestamp timestamp = new Timestamp(date.getTime());
        return timestamp;
    }

    /**
     * 将java.util.Date对象转化为java.sql.Timestamp对象
     *
     * @param date 要转化的java.util.Date对象
     * @return 转化后的java.sql.Timestamp对象
     */
    public static Timestamp dateToTime(Date date) {
        String strDate = dateToStr(date, "yyyy-MM-dd HH:mm:ss SSS");
        return strToSqlDate(strDate, "yyyy-MM-dd HH:mm:ss SSS");
    }

    /**
     * 将java.util.Date对象转化为String字符串
     *
     * @param date      要格式的java.util.Date对象
     * @param strFormat 输出的String字符串格式的限定（如："yyyy-MM-dd HH:mm:ss"）
     * @return 表示日期的字符串
     */
    public static String dateToStr(Date date, String strFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strFormat);
        return simpleDateFormat.format(date);
    }

    /**
     * 获取表格单元格Cell内容
     *
     * @param cell
     * @return
     */
    public static String getCellValue(Cell cell) {
        String result;
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:// 数字类型
                if (HSSFDateUtil.isCellDateFormatted(cell)) {// 处理日期格式、时间格式
                    SimpleDateFormat sdf = null;
                    if (cell.getCellStyle().getDataFormat() == HSSFDataFormat.getBuiltinFormat("h:mm")) {
                        sdf = new SimpleDateFormat("HH:mm");
                    } else {// 日期
                        sdf = new SimpleDateFormat("yyyy-MM-dd");
                    }
                    Date date = cell.getDateCellValue();
                    result = sdf.format(date);
                } else if (cell.getCellStyle().getDataFormat() == 58) {
                    // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    double value = cell.getNumericCellValue();
                    Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
                    result = sdf.format(date);
                } else {
                    double value = cell.getNumericCellValue();
                    CellStyle style = cell.getCellStyle();
                    DecimalFormat format = new DecimalFormat();
                    String temp = style.getDataFormatString();
                    // 单元格设置成常规
                    if (temp.equals("General")) {
                        format.applyPattern("#.####");
                    }
                    result = format.format(value);
                }
                break;
            case Cell.CELL_TYPE_STRING:// String类型
                result = cell.getRichStringCellValue().toString();
                break;
            case Cell.CELL_TYPE_BLANK:
                result = "";
                break;
            case Cell.CELL_TYPE_FORMULA:
                result = String.valueOf(cell.getNumericCellValue());
                break;
            default:
                result = "";
                break;
        }
        return result;
    }

}
