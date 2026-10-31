package com.example.calendarmemo.util;

import com.example.calendarmemo.model.Festival;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 本地节日 / 纪念日数据。
 *
 * <p>公历节日按 MM-dd 直接匹配；农历节日使用 2024–2035 年的预生成表，
 * 避免引入网络接口或额外的农历依赖。清明节使用节气日期表；
 * 母亲节、父亲节、感恩节按“第几个星期几”计算。</p>
 *
 * <p>数据与界面分离：日历格只问“这一天有没有节日”，日期详情条再展示
 * 节日名称与一句说明。</p>
 */
public final class FestivalUtils {

    private static final Map<String, Festival> SOLAR = new LinkedHashMap<>();
    private static final Map<String, Festival> LUNAR = new LinkedHashMap<>();
    private static final Map<String, Festival> QINGMING = new LinkedHashMap<>();

    /** 农历节日：yyyy-MM-dd|名称。由 2024–2035 年农历表生成。 */
    private static final String[] LUNAR_DATES = {
            "2024-02-10|春节",
            "2024-02-24|元宵节",
            "2024-03-11|龙抬头",
            "2024-06-10|端午节",
            "2024-08-10|七夕",
            "2024-09-17|中秋节",
            "2024-10-11|重阳节",
            "2025-01-07|腊八节",
            "2025-01-22|小年",
            "2025-01-28|除夕",
            "2025-01-29|春节",
            "2025-02-12|元宵节",
            "2025-03-01|龙抬头",
            "2025-05-31|端午节",
            "2025-08-29|七夕",
            "2025-10-06|中秋节",
            "2025-10-29|重阳节",
            "2026-01-26|腊八节",
            "2026-02-10|小年",
            "2026-02-16|除夕",
            "2026-02-17|春节",
            "2026-03-03|元宵节",
            "2026-03-20|龙抬头",
            "2026-06-19|端午节",
            "2026-08-19|七夕",
            "2026-09-25|中秋节",
            "2026-10-18|重阳节",
            "2027-01-15|腊八节",
            "2027-01-30|小年",
            "2027-02-05|除夕",
            "2027-02-06|春节",
            "2027-02-20|元宵节",
            "2027-03-09|龙抬头",
            "2027-06-09|端午节",
            "2027-08-08|七夕",
            "2027-09-15|中秋节",
            "2027-10-08|重阳节",
            "2028-01-04|腊八节",
            "2028-01-19|小年",
            "2028-01-25|除夕",
            "2028-01-26|春节",
            "2028-02-09|元宵节",
            "2028-02-26|龙抬头",
            "2028-05-28|端午节",
            "2028-08-26|七夕",
            "2028-10-03|中秋节",
            "2028-10-26|重阳节",
            "2029-01-22|腊八节",
            "2029-02-06|小年",
            "2029-02-12|除夕",
            "2029-02-13|春节",
            "2029-02-27|元宵节",
            "2029-03-16|龙抬头",
            "2029-06-16|端午节",
            "2029-08-16|七夕",
            "2029-09-22|中秋节",
            "2029-10-16|重阳节",
            "2030-01-11|腊八节",
            "2030-01-26|小年",
            "2030-02-02|除夕",
            "2030-02-03|春节",
            "2030-02-17|元宵节",
            "2030-03-05|龙抬头",
            "2030-06-05|端午节",
            "2030-08-05|七夕",
            "2030-09-12|中秋节",
            "2030-10-05|重阳节",
            "2031-01-01|腊八节",
            "2031-01-16|小年",
            "2031-01-22|除夕",
            "2031-01-23|春节",
            "2031-02-06|元宵节",
            "2031-02-22|龙抬头",
            "2031-06-24|端午节",
            "2031-08-24|七夕",
            "2031-10-01|中秋节",
            "2031-10-24|重阳节",
            "2032-01-20|腊八节",
            "2032-02-04|小年",
            "2032-02-10|除夕",
            "2032-02-11|春节",
            "2032-02-25|元宵节",
            "2032-03-13|龙抬头",
            "2032-06-12|端午节",
            "2032-08-12|七夕",
            "2032-09-19|中秋节",
            "2032-10-12|重阳节",
            "2033-01-08|腊八节",
            "2033-01-23|小年",
            "2033-01-30|除夕",
            "2033-01-31|春节",
            "2033-02-14|元宵节",
            "2033-03-02|龙抬头",
            "2033-06-01|端午节",
            "2033-08-01|七夕",
            "2033-09-08|中秋节",
            "2033-10-01|重阳节",
            "2034-01-27|腊八节",
            "2034-02-11|小年",
            "2034-02-18|除夕",
            "2034-02-19|春节",
            "2034-03-05|元宵节",
            "2034-03-21|龙抬头",
            "2034-06-20|端午节",
            "2034-08-20|七夕",
            "2034-09-27|中秋节",
            "2034-10-20|重阳节",
            "2035-01-16|腊八节",
            "2035-01-31|小年",
            "2035-02-07|除夕",
            "2035-02-08|春节",
            "2035-02-22|元宵节",
            "2035-03-11|龙抬头",
            "2035-06-10|端午节",
            "2035-08-10|七夕",
            "2035-09-16|中秋节",
            "2035-10-09|重阳节",
            "2036-01-05|腊八节",
            "2036-01-20|小年",
            "2036-01-27|除夕"
    };

    static {
        // 公历固定节日
        putSolar(1, 1, "元旦", "公历新年的第一天，回顾过去，也许下新的愿望。", "公历节日");
        putSolar(2, 14, "情人节", "表达爱意与陪伴的日子。", "公历节日");
        putSolar(3, 8, "妇女节", "致敬每一位女性的付出与力量。", "国际节日");
        putSolar(3, 12, "植树节", "植树造林，亲近自然。", "公历节日");
        putSolar(4, 1, "愚人节", "轻松玩笑的西方节日，记得把握分寸。", "公历节日");
        putSolar(5, 1, "劳动节", "致敬每一位劳动者，也给自己放个假。", "国际节日");
        putSolar(5, 4, "青年节", "纪念五四运动，青春与责任同行。", "纪念日");
        putSolar(5, 12, "防灾减灾日", "学习防灾知识，守护生命安全。", "纪念日");
        putSolar(6, 1, "儿童节", "属于孩子的节日，也提醒大人保留一点童心。", "国际节日");
        putSolar(7, 1, "建党节", "中国共产党成立纪念日。", "纪念日");
        putSolar(8, 1, "建军节", "中国人民解放军建军纪念日。", "纪念日");
        putSolar(9, 10, "教师节", "感谢每一位传道授业解惑的老师。", "公历节日");
        putSolar(9, 18, "九一八纪念日", "铭记历史，吾辈自强。", "纪念日");
        putSolar(10, 1, "国庆节", "庆祝中华人民共和国成立，祝福祖国繁荣昌盛。", "公历节日");
        putSolar(12, 13, "国家公祭日", "铭记历史，珍爱和平。", "纪念日");
        putSolar(12, 24, "平安夜", "圣诞前夜，愿平安喜乐。", "公历节日");
        putSolar(12, 25, "圣诞节", "分享礼物与祝福的节日。", "公历节日");
        putSolar(12, 31, "跨年夜", "告别旧岁，迎接新年。", "公历节日");

        // 清明节（节气日期表，2024–2035）
        String[] qingming = {
                "2024-04-04", "2025-04-04", "2026-04-05", "2027-04-05",
                "2028-04-04", "2029-04-04", "2030-04-05", "2031-04-05",
                "2032-04-04", "2033-04-04", "2034-04-05", "2035-04-05"
        };
        for (String date : qingming) {
            QINGMING.put(date, new Festival("清明节",
                    "扫墓祭祖、踏青郊游，慎终追远。", "传统节日"));
        }

        // 农历节日
        for (String row : LUNAR_DATES) {
            String[] parts = row.split("\\|");
            if (parts.length == 2) {
                LUNAR.put(parts[0], new Festival(parts[1],
                        lunarDescription(parts[1]), "传统节日"));
            }
        }
    }

    private FestivalUtils() {
    }

    private static void putSolar(int month, int day, String name,
                                 String description, String type) {
        SOLAR.put(String.format(Locale.CHINA, "%02d-%02d", month, day),
                new Festival(name, description, type));
    }

    private static String lunarDescription(String name) {
        switch (name) {
            case "春节":
                return "农历正月初一，辞旧迎新、阖家团圆的传统节日。";
            case "元宵节":
                return "农历正月十五，赏花灯、吃元宵，春节年俗的尾声。";
            case "龙抬头":
                return "农历二月初二，春耕开始，民间有理发、吃龙食的习俗。";
            case "端午节":
                return "农历五月初五，纪念屈原，吃粽子、赛龙舟。";
            case "七夕":
                return "农历七月初七，乞巧与爱情主题的传统节日。";
            case "中秋节":
                return "农历八月十五，赏月、吃月饼，象征团圆。";
            case "重阳节":
                return "农历九月初九，登高、敬老、赏菊。";
            case "腊八节":
                return "农历十二月初八，喝腊八粥，年味渐浓。";
            case "小年":
                return "农历十二月二十三前后，祭灶、扫尘，准备过年。";
            case "除夕":
                return "农历一年的最后一天，守岁、吃年夜饭。";
            default:
                return "传统节日。";
        }
    }

    /**
     * 返回某一天的全部节日；同一天可能同时命中公历与农历节日。
     */
    public static List<Festival> findAll(Calendar date) {
        Map<String, Festival> result = new LinkedHashMap<>();
        String monthDay = String.format(Locale.CHINA, "%02d-%02d",
                date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
        String full = String.format(Locale.CHINA, "%04d-%02d-%02d",
                date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH));

        add(result, SOLAR.get(monthDay));
        add(result, LUNAR.get(full));
        add(result, QINGMING.get(full));

        if (isNthWeekday(date, Calendar.SUNDAY, 2) && date.get(Calendar.MONTH) == Calendar.MAY) {
            add(result, new Festival("母亲节",
                    "五月第二个星期日，记得向妈妈说一声谢谢。", "国际节日"));
        }
        if (isNthWeekday(date, Calendar.SUNDAY, 3) && date.get(Calendar.MONTH) == Calendar.JUNE) {
            add(result, new Festival("父亲节",
                    "六月第三个星期日，记得向爸爸说一声谢谢。", "国际节日"));
        }
        if (isNthWeekday(date, Calendar.THURSDAY, 4) && date.get(Calendar.MONTH) == Calendar.NOVEMBER) {
            add(result, new Festival("感恩节",
                    "十一月第四个星期四，表达感谢与分享。", "国际节日"));
        }
        return new ArrayList<>(result.values());
    }

    /** 返回某一天的首个节日；没有则返回 null。 */
    public static Festival find(Calendar date) {
        List<Festival> all = findAll(date);
        return all.isEmpty() ? null : all.get(0);
    }

    public static boolean hasFestival(Calendar date) {
        return find(date) != null;
    }

    private static void add(Map<String, Festival> map, Festival festival) {
        if (festival != null) {
            map.put(festival.name, festival);
        }
    }

    private static boolean isNthWeekday(Calendar date, int dayOfWeek, int nth) {
        if (date.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            return false;
        }
        int day = date.get(Calendar.DAY_OF_MONTH);
        return ((day - 1) / 7) + 1 == nth;
    }
}
