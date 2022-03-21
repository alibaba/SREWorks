import com.alibaba.sreworks.warehouse.common.constant.DwConstant;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2021/11/23 16:13
 */
public class PlainTest {

    @Test
    public void test1() {
        List<Integer> results = new ArrayList<>();
        List<Integer> validDocuments = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            validDocuments.add(i);
        }

        Integer[] ignores = validDocuments.parallelStream().filter(document -> document > 50).map(document -> {
            document += 1;
            try {
                Thread.sleep(1000);    //延时2秒
            } catch (Exception ex) {

            }
            return document;
        }).toArray(Integer[]::new);

        System.out.println(ignores.length);
    }

    @Test
    public void test2() {
        List<Integer> results = new ArrayList<>();
        List<Integer> validDocuments = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            validDocuments.add(i);
        }

        Integer[] ignores = validDocuments.stream().filter(document -> document > 50).map(document -> {
            document += 1;
            try {
                Thread.sleep(1000);    //延时2秒
            } catch (Exception ex) {

            }
            return document;
        }).toArray(Integer[]::new);

        System.out.println(ignores.length);
    }

    @Test
    public void test3() {
        System.out.println(DateTimeFormatter.ofPattern(DwConstant.FORMAT_HOUR).format(LocalDateTime.now()));
        System.out.println(DateTimeFormatter.ofPattern("yyyyMMddww").format(LocalDateTime.now()));
    }
}
