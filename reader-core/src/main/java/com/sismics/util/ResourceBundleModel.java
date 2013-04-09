package com.sismics.util;

import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Overloads {@link freemarker.ext.beans.ResourceBundleModel} so that single quotes are processed uniformly.
 * 
 * @author bgamard
 */
public class ResourceBundleModel extends freemarker.ext.beans.ResourceBundleModel {

    /**
     * Constructor.
     * 
     * @param bundle Resource bundle
     * @param wrapper Wrapper
     */
    public ResourceBundleModel(ResourceBundle bundle, BeansWrapper wrapper) {
        super(bundle, wrapper);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        // Must have at least one argument - the key
        if (arguments.size() < 1)
            throw new TemplateModelException("No message key was specified");
        // Read it
        Iterator it = arguments.iterator();
        String key = unwrap((TemplateModel) it.next()).toString();
        try {
            // Let's use MessageFormat whether there are parameters or not
//            if (!it.hasNext()) {
//                return wrap(((ResourceBundle) object).getObject(key));
//            }

            // Copy remaining arguments into an Object[]
            int args = arguments.size() - 1;
            Object[] params = new Object[args];
            for (int i = 0; i < args; ++i)
                params[i] = unwrap((TemplateModel) it.next());

            // Invoke format
            return new StringModel(format(key, params), wrapper);
        } catch (MissingResourceException e) {
            throw new TemplateModelException("No such key: " + key);
        } catch (Exception e) {
            throw new TemplateModelException(e.getMessage());
        }
    }
}
