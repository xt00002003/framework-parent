package test.dark;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 描述: 测试基类
 *
 * @author : <a href="mailto:xueteng@yinli56.com ">teng.xue</a>
 * @version : Ver 1.0
 * @date : 2018/2/26
 */
@RunWith(SpringJUnit4ClassRunner.class) //使用junit4进行测试
@ContextConfiguration(locations={"classpath:framework-core.xml"}) //加载配置文件
public class BaseJunit4Test {
}
