package test.dark;

import com.dark.util.DarkConfigUtils;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * 描述: 测试配置文件读取
 *
 * @author : <a href="mailto:xueteng@yinli56.com ">teng.xue</a>
 * @version : Ver 1.0
 * @date : 2018/2/26
 */

public class ConfigUtilTest extends BaseJunit4Test{

    @Test
    public void test(){

        Properties prop = DarkConfigUtils.getProperties();
        String version=prop.getProperty("server.tablet.version");
        System.out.println(version);
    }

    /**
     * 加载classPath下所有的配置文件
     */
    @Test
    public void testLoadClassPathResource() throws IOException {
        System.out.println("测试加载classPath下所有的配置文件");
        //配置扫描路径
        List<String> rootScan = new ArrayList<>();
        rootScan.add("classpath*:**/*.properties");// 扫描二级目录
        rootScan.add("classpath*:*.properties");// 扫描一级目录
        //1.从classPath中加载所有的resource
        PathMatchingResourcePatternResolver resolver=new PathMatchingResourcePatternResolver();
        List<Resource> resourceList=new ArrayList<>();
        for (String pattern : rootScan) {
            Resource[] res=resolver.getResources(pattern);
            //使用spring CollectionUtils工具类转化
            resourceList.addAll(CollectionUtils.arrayToList(res));
        }
        //2.过滤出开头是: jar:file/file:  的Resource
        String url;
        List<Resource> resourcesFilter=new ArrayList<>();
        for (Resource resource:resourceList){
            if (!StringUtils.isEmpty(resource)){
                url=resource.getURL().toString();
                //这里使用了spring的工具类StringUtils
                if (StringUtils.startsWithIgnoreCase(url, "jar:file")|| StringUtils.startsWithIgnoreCase(url, "file:")) {
                    resourcesFilter.add(resource);
                }

            }
        }

        //3.从Resource 中读取数据到Properties 中
        Properties properties=new Properties();
        for(Resource resource:resourcesFilter){
            properties.load(resource.getInputStream());
        }

        System.out.println("classPath下的所有配置文件信息如下:"+properties.toString());


    }

}
