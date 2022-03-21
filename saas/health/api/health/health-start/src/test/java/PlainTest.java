import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.health.common.constant.Constant;
import com.alibaba.sreworks.health.common.utils.CommonTools;
import com.alibaba.sreworks.health.domain.bo.DefinitionExConfig;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.util.DigestUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2021/10/20 10:32
 */
public class PlainTest {

    @Test
    public void test() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        list.add("e");
        list.add("f");
        list.add("g");
        System.out.println(list.subList(0,2));
        System.out.println(list.subList(2,4));
        System.out.println(list.subList(4,6));
        System.out.println(list.subList(6,7));
    }

    @Test
    public void test1() {
        List<CommonTools.IndexStepRange> results = CommonTools.generateStepIndex(7, 1);
        results.forEach(result -> {
            System.out.println(result.getStartIndex());
            System.out.println(result.getEndIndex());
            System.out.println("===========");
        });
    }

    @Test
    public void test2() {
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, -1);
        calendar.add(Calendar.DATE, -1);
        System.out.println(calendar.getTimeInMillis());
        System.out.println(now.getTime());
    }

    @Test
    public void test3() {
        DecimalFormat df =new DecimalFormat("0.00000");
        System.out.println(Double.valueOf(df.format(1 -(double)2/3)));
    }
}
