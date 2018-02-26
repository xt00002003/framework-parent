package com.dark.support.config;


import com.dark.exceptions.GeneralException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;

/**
 * TODO liudejian 类描述.
 *
 * @version : Ver 1.0
 * @author    : <a href="mailto:dejianliu@ebnew.com">liudejian</a>
 * @date    : 2015-2-12 下午2:36:26
 */
public class ResourceComparator implements Comparator<Resource>, Serializable {


    private static final long serialVersionUID = 6675499683503360919L;

    @Override
    public int compare(Resource o1, Resource o2) {
        try {
            if (o1 instanceof FileSystemResource
                    || (o1 instanceof UrlResource
                    && ((UrlResource) o1).getURL().getProtocol().equals("file"))) {
                return 1;
            }
        } catch (IOException exception) {
            throw new GeneralException("invalid url protocol : " + o1);
        }
        return -1;
    }


}